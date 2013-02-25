package com.phybots.picode.api;

import com.phybots.picode.PicodeMain;

public class MindstormsNXTMotorManager extends MotorManager {

	public MindstormsNXTMotorManager(PicodeMain picodeMain, Poser robot) {
		super(picodeMain, robot);
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

	@Override
	public Pose getPose() {
//		int i = 0;
//		for (Service service : ((MindstormsNXTMotorManager) motorManager).getServices()) {
//			ManageMindstormsNXTMotorState manager = (ManageMindstormsNXTMotorState) service;
//			rotationCounts[i ++] = manager.getRotationCount();
//			Phybots.getInstance().getOutStream().println(
//					String.format("Port %d: %d", i, rotationCounts[i - 1]));
//		}
		return null;
	}

	@Override
	public boolean setPose(Pose pose) {
		if (!(pose instanceof MindstormsNXTPose)) {
			return false;
		}
		MindstormsNXTPose myPose = (MindstormsNXTPose) pose;
		int i = 0;
// TODO
//		for (Service service : getServices()) {
//		ManageMindstormsNXTMotorState manager = (ManageMindstormsNXTMotorState) service;
//		manager.setRotationCount(mindstormsNXTPose.rotationCounts[i ++]);
//		Phybots.getInstance().getOutStream().println(
//				String.format("Port %d: %d", i, mindstormsNXTPose.rotationCounts[i - 1]));
//		}
		return true;
	}

	@Override
	public boolean isActing() {
		boolean isActing = false;
		// TODO
		return isActing;
	}

	@Override
	public void reset() {
	}
}
