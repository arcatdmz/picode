package com.phybots.picode.api;

public abstract class MotorManager {
	private Poser poser;

	public MotorManager(Poser poser) {
		this.poser = poser;
	}

	protected Poser getPoser() {
		return poser;
	}

	public abstract void start();

	public abstract void stop();

	/**
	 * returns new pose instance w/ pose data w/o photo and name
	 * @see Poser#getPose()
	 * @see Poser#capture()
	 */
	public abstract Pose getPose();

	public abstract boolean setPose(Pose pose);

	public abstract boolean isActing();
	public abstract void reset();
}
