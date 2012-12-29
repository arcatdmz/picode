package com.phybots.picode.core.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import com.phybots.Phybots;
import com.phybots.service.Service;
import com.phybots.task.ManageMindstormsNXTMotorState;
import com.phybots.picode.Pose;
import com.phybots.picode.RobotType;

public class MindstormsNXTPose extends Pose {
	private int[] rotationCounts;

	public MindstormsNXTPose() {
		super(RobotType.MindstormsNXT);
		rotationCounts = new int[3];
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
	public boolean applyTo(MotorManager motorManager) {
		Phybots.getInstance().getOutStream().println(
				"---Setting pose of the robot.");
		int i = 0;
		for (Service service : ((MindstormsNXTMotorManager) motorManager).getServices()) {
			if (!(service instanceof ManageMindstormsNXTMotorState)) {
				return false;
			}
			ManageMindstormsNXTMotorState manager = (ManageMindstormsNXTMotorState) service;
			manager.setRotationCount(rotationCounts[i ++]);
			Phybots.getInstance().getOutStream().println(
					String.format("Port %d: %d", i, rotationCounts[i - 1]));
		}
		return true;
	}

	@Override
	public void retrieveFrom(MotorManager motorManager) {
	  Phybots.getInstance().getOutStream().println(
				"---Retrieving pose of the robot.");
		int i = 0;
		for (Service service : ((MindstormsNXTMotorManager) motorManager).getServices()) {
			ManageMindstormsNXTMotorState manager = (ManageMindstormsNXTMotorState) service;
			rotationCounts[i ++] = manager.getRotationCount();
			Phybots.getInstance().getOutStream().println(
					String.format("Port %d: %d", i, rotationCounts[i - 1]));
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
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public boolean eq(Pose pose, float maxDifference) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}
}
