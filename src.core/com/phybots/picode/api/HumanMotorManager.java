package com.phybots.picode.api;

import com.phybots.picode.camera.Camera;
import com.phybots.picode.camera.KinectCamera;

public class HumanMotorManager extends MotorManager {

	public HumanMotorManager(Poser poser) {
		super(poser);
	}

	public void start() {
	}

	public void stop() {
	}

	/**
	 * returns new pose instance w/ pose data w/o photo and name
	 * @see Poser#getPose()
	 * @see Poser#capture()
	 */
	@Override
	public Pose getPose() {
		Poser poser = getPoser();
		Camera camera = PoserLibrary.getInstance().getCameraManager().getCamera(poser);
		KinectCamera kinectCamera = (KinectCamera) camera;
		HumanPose pose = new HumanPose();
		pose.setPoserIdentifier(poser.getIdentifier());
		pose.setPoserType(poser.getPoserType());
		if (!pose.importData(kinectCamera.getLatestJoints())) {
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
