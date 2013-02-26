package com.phybots.picode.api;

import com.phybots.picode.camera.Camera;
import com.phybots.picode.camera.KinectCamera;

public class Human extends Poser {

	@Override
	protected void initialize() {
		motorManager = new HumanMotorManager(this);
	}

	@Override
	public Pose newPoseInstance() {
		return new HumanPose();
	}

	public static Class<? extends Camera> getCameraClass() {
		return KinectCamera.class;
	}

}
