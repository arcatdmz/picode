package com.phybots.picode.api;

import com.phybots.picode.camera.Camera;
import com.phybots.picode.camera.KinectCamera;

public class Human extends Poser {

	public Human() {
		// Do nothing.
	}

	public Human(String name) {
		this();
		setName(name);
	}

	@Override
	protected void initialize() {
		motorManager = new HumanMotorManager(this);
	}

	public static Class<? extends Pose> getPoseClass() {
		return HumanPose.class;
	}

	public static Class<? extends Camera> getCameraClass() {
		return KinectCamera.class;
	}

	public static Class<? extends Camera> getSecondaryCameraClass() {
		return null;
	}

}
