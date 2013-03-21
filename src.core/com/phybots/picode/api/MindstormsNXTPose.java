package com.phybots.picode.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class MindstormsNXTPose extends Pose {
	private int[] rotationCounts;

	public MindstormsNXTPose() {
		super();
		rotationCounts = new int[3];
	}

	public boolean importData(int[] rotationCounts) {
		if (rotationCounts == null
				|| rotationCounts.length != this.rotationCounts.length)
			return false;
		for (int i = 0; i < rotationCounts.length; i ++) {
			if (rotationCounts[i] == Integer.MAX_VALUE) {
				return false;
			}
			this.rotationCounts[i] = rotationCounts[i];
		}
		return true;
	}

	public int[] getData() {
		return rotationCounts.clone();
	}

	@Override
	public void load(BufferedReader reader) throws IOException {
		for (int i = 0; i < rotationCounts.length; i ++) {
			rotationCounts[i] = Integer.valueOf(reader.readLine().trim());
		}
	}

	@Override
	public void save(BufferedWriter writer) throws IOException {
		for (int i = 0; i < rotationCounts.length; i ++) {
			writer.write(String.valueOf(rotationCounts[i]));
			writer.newLine();
		}
	}

	@Override
	public Pose interpolate(Pose pose, float proportion) {
		MindstormsNXTPose newPose = (MindstormsNXTPose) this.clone();
		MindstormsNXTPose nxtPose = (MindstormsNXTPose) pose;
		for (int i = 0; i < rotationCounts.length; i ++) {
			newPose.rotationCounts[i] = (int) (newPose.rotationCounts[i] * proportion + nxtPose.rotationCounts[i] * (1.0f - proportion));
		}
		return newPose;
	}

	@Override
	public boolean eq(Pose pose) {
		return eq(pose, .3f);
	}

	@Override
	public boolean eq(Pose pose, float maxDifference) {
		if (!(pose instanceof MindstormsNXTPose)) {
			return false;
		}
		MindstormsNXTPose p = (MindstormsNXTPose) pose;
		double distance = 0;
		for (int i = 0; i < rotationCounts.length; i ++) {
			double diff = rotationCounts[i] - p.rotationCounts[i]; // 0 ~
			distance += diff*diff;
		}
		distance = Math.sqrt(distance/(360*360*rotationCounts.length));
		// System.out.println("distance: " + distance);
		return distance == Double.NaN ? false : distance < maxDifference;
	}
}
