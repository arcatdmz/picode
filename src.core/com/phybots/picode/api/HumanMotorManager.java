package com.phybots.picode.api;

import com.phybots.picode.camera.KinectCamera;

public class HumanMotorManager extends MotorManager {

	public HumanMotorManager(Poser poser) {
		super(poser);
	}

	public boolean start() {
		KinectCamera camera = PoserLibrary.getInstance().getCameraManager().getCamera(
				KinectCamera.class);
		return camera != null && camera.start();
	}

	public void stop() {
		KinectCamera camera = PoserLibrary.getInstance().getCameraManager().getCamera(
				KinectCamera.class);
		if (camera != null) {
			camera.stop();
		}
	}

	@Override
	public Pose getPose() {
		if (!start()) {
			return null;
		}
		HumanPose pose = new HumanPose();
		Poser poser = getPoser();
		pose.setPoserIdentifier(poser.getIdentifier());
		pose.setPoserType(poser.getPoserType());
		KinectCamera camera = PoserLibrary.getInstance().getCameraManager().getCamera(
				KinectCamera.class);
		if (!pose.importData(camera.getLatestJoints())) {
			return null;
		}
		return pose;
	}

	@Override
	public boolean setPose(Pose pose) {
		return false;
	}

	public boolean isActing() {
		return false;
	}

	public void reset() {
	}

}
