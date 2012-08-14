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

	public void load(final Entry configurationNode) throws IOException, ConfigurationException {

		log.log(Level.INFO, "�ݒ�ǂݍ��݊J�n");

		for (final Entry constant : configurationNode.selectChildren("�萔")) {

			log.log(Level.INFO, "�萔...");

			constants.put( constant.getAttribute("���O"), constant.getAttribute("�l") );
		}

		for (final Entry list : configurationNode.selectChildren("���샊�X�g")) {

			log.log(Level.INFO, "���샊�X�g...");

			for (final Entry node : list.selectChildren("����")) {

				final ActionBuilder action = new ActionBuilder(this, node);

				if ( this.getActionBuilders().containsKey(action.getName())) {
					throw new ConfigurationException("����̖��O���d�����Ă��܂�:"+action.getName());
				}

				this.getActionBuilders().put(action.getName(), action);
			}
		}

		for (final Entry list : configurationNode.selectChildren("�s�����X�g")) {

			log.log(Level.INFO, "�s�����X�g...");

			loadBehaviors(list, new ArrayList<String>());
		}

		log.log(Level.INFO, "�ݒ�ǂݍ��݊���");
	}

	private void loadBehaviors(final Entry list, final List<String> conditions) {
		for (final Entry node : list.getChildren()) {

			if (node.getName().equals("����")) {

				final List<String> newConditions = new ArrayList<String>(conditions);
				newConditions.add(node.getAttribute("����"));

				loadBehaviors(node, newConditions);

			} else if (node.getName().equals("�s��")) {
				final BehaviorBuilder behavior = new BehaviorBuilder(this, node, conditions);
				this.getBehaviorBuilders().put(behavior.getName(), behavior);
			}
		}
	}

	public Action buildAction(final String name, final Map<String, String> params) throws ActionInstantiationException {

		final ActionBuilder factory = this.actionBuilders.get(name);
		if (factory == null) {
			throw new ActionInstantiationException("�Ή����铮�삪������܂���: " + name);
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

		// TODO �����ȊO�ŕK�v�ȏꍇ�́H���{�I�ɂ�����������ׂ�
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
				log.log(Level.WARNING, "�s���p�x�̕]�����ɃG���[���������܂���", e);
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
					log.log(Level.WARNING, "�s���p�x�̕]�����ɃG���[���������܂���", e);
				}
			}
		}

		if (totalFrequency == 0) {
			mascot.setAnchor(new Point(
					(int) (Math.random() * (mascot.getEnvironment().getScreen().getRight()
							- mascot.getEnvironment()
					.getScreen().getLeft()))
					+ mascot.getEnvironment().getScreen().getLeft(), mascot.getEnvironment().getScreen().getTop() - 256));
			return buildBehavior("��������");
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