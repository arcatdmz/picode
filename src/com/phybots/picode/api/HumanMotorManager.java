package com.phybots.picode.api;

public class HumanMotorManager extends MotorManager {

	public HumanMotorManager(Poser poser) {
		super(poser);
	}

	public void start() {
	}

	public void stop() {
	}

	@Override
	public Pose getPose() {
		return null;
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
