/**Shimeji-ie*/

package com.group_finity.mascot.config;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.animation.Pose;
import com.group_finity.mascot.exception.AnimationInstantiationException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.image.ImagePair;
import com.group_finity.mascot.image.ImagePairLoader;
import com.group_finity.mascot.script.Variable;
import com.group_finity.mascot.config.Settings;

public class AnimationBuilder {

	private static final Logger log = Logger.getLogger(AnimationBuilder.class.getName());

	private final String condition;

	private final List<Pose> poses = new ArrayList<Pose>();
	
	private String packageName = null;
	
	public AnimationBuilder(final Entry animationNode) throws IOException {
		this(animationNode, Settings.getString("shimeji.cfg.default_mascot","default"));
	}

	public AnimationBuilder(final Entry animationNode, final String packageName) throws IOException {
		this.packageName = packageName;
			
		this.condition = animationNode.getAttribute(Settings.getString("shimeji.mapper.condition","èåè")) == null ? "true" : animationNode.getAttribute(Settings.getString("shimeji.mapper.condition","èåè"));

		log.log(Level.INFO, "Starting reading animation");

		for (final Entry frameNode : animationNode.getChildren()) {

			this.getPoses().add(loadPose(frameNode));
		}

		log.log(Level.INFO, "Finished loading animation");
	}

	private Pose loadPose(final Entry frameNode) throws IOException {

		final String imageText = "/img"+frameNode.getAttribute(Settings.getString("shimeji.mapper.image","âÊëú"));
		final String anchorText = frameNode.getAttribute(Settings.getString("shimeji.mapper.image_anchor","äÓèÄç¿ïW"));
		final String moveText = frameNode.getAttribute(Settings.getString("shimeji.mapper.velocity","à⁄ìÆë¨ìx"));
		final String durationText = frameNode.getAttribute(Settings.getString("shimeji.mapper.duration","í∑Ç≥"));

		final String[] anchorCoordinates = anchorText.split(",");
		final Point anchor = new Point(Integer.parseInt(anchorCoordinates[0]), Integer.parseInt(anchorCoordinates[1]));

		final ImagePair image = ImagePairLoader.load(imageText, anchor, packageName);
		log.log(Level.INFO, "Pose image loaded");

		final String[] moveCoordinates = moveText.split(",");
		final Point move = new Point(Integer.parseInt(moveCoordinates[0]), Integer.parseInt(moveCoordinates[1]));

		final int duration = Integer.parseInt(durationText);

		final Pose pose = new Pose(image, move.x, move.y, duration);

		log.log(Level.INFO, "Read pose({0})", pose);

		return pose;

	}

	public Animation buildAnimation() throws AnimationInstantiationException {
		try {
			return new Animation(Variable.parse(this.getCondition()), this.getPoses().toArray(new Pose[0]));
		} catch (final VariableException e) {
			throw new AnimationInstantiationException("Failed to evaluate the condition", e);
		}
	}

	private List<Pose> getPoses() {
		return this.poses;
	}

	private String getCondition() {
		return this.condition;
	}
}
