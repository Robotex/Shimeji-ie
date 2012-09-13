/**Shimeji-ie*/

package com.group_finity.mascot.config;

import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.action.Action;
import com.group_finity.mascot.behavior.Behavior;
import com.group_finity.mascot.config.Settings;
import com.group_finity.mascot.exception.ActionInstantiationException;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.ConfigurationException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

public class Configuration {

	private static final Logger log = Logger.getLogger(Configuration.class.getName());

	private final Map<String, String> constants = new LinkedHashMap<String, String>();

	private final Map<String, ActionBuilder> actionBuilders = new LinkedHashMap<String, ActionBuilder>();

	private final Map<String, BehaviorBuilder> behaviorBuilders = new LinkedHashMap<String, BehaviorBuilder>();

	public void load(final Entry configurationNode, final String packageName) throws IOException, ConfigurationException {

		log.log(Level.INFO, "Starting reading");

		for (final Entry constant : configurationNode.selectChildren(Settings.getString("shimeji.mapper.constant","定数"))) {

			log.log(Level.INFO, "Constant...");

			constants.put( constant.getAttribute(Settings.getString("shimeji.mapper.name","名前")), constant.getAttribute(Settings.getString("shimeji.mapper.value","値")) );
		}

		for (final Entry list : configurationNode.selectChildren(Settings.getString("shimeji.mapper.action_list","動作リスト"))) {

			log.log(Level.INFO, "ActionList...");

			for (final Entry node : list.selectChildren(Settings.getString("shimeji.mapper.action","動作"))) {

				final ActionBuilder action = new ActionBuilder(this, node, packageName);

				if ( this.getActionBuilders().containsKey(action.getName())) {
					throw new ConfigurationException("Duplicated action name:"+action.getName());
				}

				this.getActionBuilders().put(action.getName(), action);
			}
		}

		for (final Entry list : configurationNode.selectChildren(Settings.getString("shimeji.mapper.behavior_list","行動リスト"))) {

			log.log(Level.INFO, "Behavior list...");

			loadBehaviors(list, new ArrayList<String>());
		}

		log.log(Level.INFO, "Finished loading configuration");
	}

	private void loadBehaviors(final Entry list, final List<String> conditions) {
		for (final Entry node : list.getChildren()) {

			if (node.getName().equals(Settings.getString("shimeji.mapper.condition","条件"))) {

				final List<String> newConditions = new ArrayList<String>(conditions);
				newConditions.add(node.getAttribute(Settings.getString("shimeji.mapper.condition","条件")));

				loadBehaviors(node, newConditions);

			} else if (node.getName().equals(Settings.getString("shimeji.mapper.behavior","行動"))) {
				final BehaviorBuilder behavior = new BehaviorBuilder(this, node, conditions);
				this.getBehaviorBuilders().put(behavior.getName(), behavior);
			}
		}
	}

	public Action buildAction(final String name, final Map<String, String> params) throws ActionInstantiationException {

		final ActionBuilder factory = this.actionBuilders.get(name);
		if (factory == null) {
			throw new ActionInstantiationException("Cannot find the corresponding operation: " + name);
		}

		return factory.buildAction(params);
	}

	public void validate() throws ConfigurationException{

		for(final ActionBuilder builder : getActionBuilders().values()) {
			builder.validate();
		}
		for(final BehaviorBuilder builder : getBehaviorBuilders().values()) {
			builder.validate();
		}
	}

	public Behavior buildBehavior(final String previousName, final Mascot mascot) throws BehaviorInstantiationException {

		final VariableMap context = new VariableMap();
		context.put("mascot", mascot);

		// TODO ここ以外で必要な場合は？根本的につくりを見直すべき
		for( Map.Entry<String, String> e : constants.entrySet() ) {
			context.put(e.getKey(), e.getValue());
		}

		final List<BehaviorBuilder> candidates = new ArrayList<BehaviorBuilder>();
		long totalFrequency = 0;
		for (final BehaviorBuilder behaviorFactory : this.getBehaviorBuilders().values()) {
			try {
				if (behaviorFactory.isEffective(context)) {
					candidates.add(behaviorFactory);
					totalFrequency += behaviorFactory.getFrequency();
				}
			} catch (final VariableException e) {
				log.log(Level.WARNING, "An error occurred during the evaluation of behavioral frequency", e);
			}
		}

		if (previousName != null) {
			final BehaviorBuilder previousBehaviorFactory = this.getBehaviorBuilders().get(previousName);
			if (!previousBehaviorFactory.isNextAdditive()) {
				totalFrequency = 0;
				candidates.clear();
			}
			for (final BehaviorBuilder behaviorFactory : previousBehaviorFactory.getNextBehaviorBuilders()) {
				try {
					if (behaviorFactory.isEffective(context)) {
						candidates.add(behaviorFactory);
						totalFrequency += behaviorFactory.getFrequency();
					}
				} catch (final VariableException e) {
					log.log(Level.WARNING, "An error occurred during the evaluation of behavioral frequency", e);
				}
			}
		}

		if (totalFrequency == 0) {
			mascot.setAnchor(new Point(
					(int) (Math.random() * (mascot.getEnvironment().getScreen().getRight()
							- mascot.getEnvironment()
					.getScreen().getLeft()))
					+ mascot.getEnvironment().getScreen().getLeft(), mascot.getEnvironment().getScreen().getTop() - 256));
			return buildBehavior(Settings.getString("shimeji.mapper.fall","落下する"));
		}

		double random = Math.random() * totalFrequency;

		for (final BehaviorBuilder behaviorFactory : candidates) {
			random -= behaviorFactory.getFrequency();
			if (random < 0) {
				return behaviorFactory.buildBehavior();
			}
		}

		return null;
	}

	public Behavior buildBehavior(final String name) throws BehaviorInstantiationException {
		return this.getBehaviorBuilders().get(name).buildBehavior();
	}

	public Map<String, String> getConstants() {
		return constants;
	}

	public Map<String, ActionBuilder> getActionBuilders() {
		return this.actionBuilders;
	}

	public Map<String, BehaviorBuilder> getBehaviorBuilders() {
		return this.behaviorBuilders;
	}


}
