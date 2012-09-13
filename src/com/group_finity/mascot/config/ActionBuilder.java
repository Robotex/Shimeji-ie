/**Shimeji-ie*/

package com.group_finity.mascot.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.group_finity.mascot.action.Action;
import com.group_finity.mascot.action.Animate;
import com.group_finity.mascot.action.Move;
import com.group_finity.mascot.action.Select;
import com.group_finity.mascot.action.Sequence;
import com.group_finity.mascot.action.Stay;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.ActionInstantiationException;
import com.group_finity.mascot.exception.AnimationInstantiationException;
import com.group_finity.mascot.exception.ConfigurationException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.Variable;
import com.group_finity.mascot.script.VariableMap;
import com.group_finity.mascot.config.Settings;

public class ActionBuilder implements IActionBuilder {

	private static final Logger log = Logger.getLogger(ActionBuilder.class.getName());

	private final String type;

	private final String name;

	private final String className;

	private final Map<String, String> params = new LinkedHashMap<String, String>();

	private final List<AnimationBuilder> animationBuilders = new ArrayList<AnimationBuilder>();

	private final List<IActionBuilder> actionRefs = new ArrayList<IActionBuilder>();

	public ActionBuilder(final Configuration configuration, final Entry actionNode, final String packageName) throws IOException {
		this.name = actionNode.getAttribute(Settings.getString("shimeji.mapper.name","名前"));
		this.type = actionNode.getAttribute(Settings.getString("shimeji.mapper.type","種類"));
		this.className = actionNode.getAttribute(Settings.getString("shimeji.mapper.class","クラス"));

		log.log(Level.INFO, "Starting reading behavior({0})", this);

		this.getParams().putAll(actionNode.getAttributes());
		for (final Entry node : actionNode.selectChildren(Settings.getString("shimeji.mapper.animation","アニメーション"))) {
			this.getAnimationBuilders().add(new AnimationBuilder(node, packageName));
		}

		for (final Entry node : actionNode.getChildren()) {
			if (node.getName().equals(Settings.getString("shimeji.mapper.action_reference","動作参照"))) {
				this.getActionRefs().add(new ActionRef(configuration, node));
			} else if (node.getName().equals(Settings.getString("shimeji.mapper.action","動作"))) {
				this.getActionRefs().add(new ActionBuilder(configuration, node, packageName));
			}
		}

		log.log(Level.INFO, "Read operation complete");
	}

	@Override
	public String toString() {
		return "動作(" + getName() + "," + getType() + "," + getClassName() + ")";
	}

	@SuppressWarnings("unchecked")
	public Action buildAction(final Map<String, String> params) throws ActionInstantiationException {

		try {
			// Generates a variables map
			final VariableMap variables = createVariables(params);

			// Generates an animation
			final List<Animation> animations = createAnimations();

			// Generates a child action
			final List<Action> actions = createActions();

			if (this.type.equals(Settings.getString("shimeji.mapper.embedded","組み込み"))) {
				try {
					final Class<? extends Action> cls = (Class<? extends Action>) Class.forName(this.getClassName());
					try {

						try {
							return cls.getConstructor(List.class, VariableMap.class).newInstance(animations, variables);
						} catch (final Exception e) {
							// NOTE コンストラクタが無かったと思われるので次へ
						}

						return cls.getConstructor(VariableMap.class).newInstance(variables);
					} catch (final Exception e) {
						// NOTE コンストラクタが無かったと思われるので次へ
					}

					return cls.newInstance();
				} catch (final InstantiationException e) {
					throw new ActionInstantiationException("Failed to initialize the action class(" + this + ")", e);
				} catch (final IllegalAccessException e) {
					throw new ActionInstantiationException("Illegal Access Exception in action class(" + this + ")", e);
				} catch (final ClassNotFoundException e) {
					throw new ActionInstantiationException("Cannot find the action class(" + this + ")", e);
				}

			} else if (this.type.equals(Settings.getString("shimeji.mapper.move","移動"))) {
				return new Move(animations, variables);
			} else if (this.type.equals(Settings.getString("shimeji.mapper.stay","静止"))) {
				return new Stay(animations, variables);
			} else if (this.type.equals(Settings.getString("shimeji.mapper.animate","固定"))) {
				return new Animate(animations, variables);
			} else if (this.type.equals(Settings.getString("shimeji.mapper.sequence","複合"))) {
				return new Sequence(variables, actions.toArray(new Action[0]));
			} else if (this.type.equals(Settings.getString("shimeji.mapper.select","選択"))) {
				return new Select(variables, actions.toArray(new Action[0]));
			} else {
				throw new ActionInstantiationException("Unknown action type(" + this + ")");
			}

		} catch (final AnimationInstantiationException e) {
			throw new ActionInstantiationException("Failed to create animation(" + this + ")", e);
		} catch (final VariableException e) {
			throw new ActionInstantiationException("Failed the evaluation of parameters(" + this + ")", e);
		}
	}

	public void validate() throws ConfigurationException {

		for (final IActionBuilder ref : this.getActionRefs()) {
			ref.validate();
		}
	}

	private List<Action> createActions() throws ActionInstantiationException {
		final List<Action> actions = new ArrayList<Action>();
		for (final IActionBuilder ref : this.getActionRefs()) {
			actions.add(ref.buildAction(new HashMap<String, String>()));
		}
		return actions;
	}

	private List<Animation> createAnimations() throws AnimationInstantiationException {
		final List<Animation> animations = new ArrayList<Animation>();
		for (final AnimationBuilder animationFactory : this.getAnimationBuilders()) {
			animations.add(animationFactory.buildAnimation());
		}
		return animations;
	}

	private VariableMap createVariables(final Map<String, String> params) throws VariableException {
		final VariableMap variables = new VariableMap();
		for (final Map.Entry<String, String> param : this.getParams().entrySet()) {
			variables.put(param.getKey(), Variable.parse(param.getValue()));
		}
		for (final Map.Entry<String, String> param : params.entrySet()) {
			variables.put(param.getKey(), Variable.parse(param.getValue()));
		}
		return variables;
	}

	String getName() {
		return this.name;
	}

	private String getType() {
		return this.type;
	}

	private String getClassName() {
		return this.className;
	}

	private Map<String, String> getParams() {
		return this.params;
	}

	private List<AnimationBuilder> getAnimationBuilders() {
		return this.animationBuilders;
	}

	private List<IActionBuilder> getActionRefs() {
		return this.actionRefs;
	}


}
