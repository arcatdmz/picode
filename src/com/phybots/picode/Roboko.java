package com.phybots.picode;

import java.io.IOException;

public class Roboko {
	private static Roboko instance = null;

	private Roboko() {
	}

	public static Roboko getInstance() {
		if (instance == null) {
			instance = new Roboko();
		}
		return instance;
	}

	public static Pose pose(String poseName) {
		try {
			return Pose.load(poseName);
		} catch (IOException e) {
			return null;
		}
	}

	public static PoseSet poseSet(String poseSetName) {
		return PoseSet.load(poseSetName);
	}
}
