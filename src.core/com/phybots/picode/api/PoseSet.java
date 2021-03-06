package com.phybots.picode.api;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO [Enhancement] Machine learning support. To be implemented.
 */
public class PoseSet implements Serializable {
	private static final long serialVersionUID = 4193868468972287510L;
	private Set<Pose> positiveSamples;
	private Set<Pose> negativeSamples;

	public PoseSet() {
		positiveSamples = new HashSet<Pose>();
		negativeSamples = new HashSet<Pose>();
	}

	public static PoseSet load(String poseSetName) {
		return new PoseSet();
	}

	public void add(Pose pose, boolean isPositive) {
		if (pose == null) {
			return;
		}
		if (isPositive) {
			if (negativeSamples.contains(pose)) {
				negativeSamples.remove(pose);
			}
			positiveSamples.add(pose);
		} else {
			if (positiveSamples.contains(pose)) {
				positiveSamples.remove(pose);
			}
			negativeSamples.add(pose);
		}
	}

	public boolean remove(Pose pose) {
		return positiveSamples.remove(pose) ||
				negativeSamples.remove(pose);
	}
}
