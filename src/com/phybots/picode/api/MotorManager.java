package com.phybots.picode.api;

import com.phybots.picode.PicodeMain;

public abstract class MotorManager {
	private PicodeMain picodeMain;
	private Poser robot;

	public MotorManager(PicodeMain picodeMain, Poser robot) throws InstantiationException {
		this.picodeMain = picodeMain;
		this.robot = robot;
	}

	protected PicodeMain getPicodeMain() {
		return picodeMain;
	}

	protected Poser getRobot() {
		return robot;
	}

	public abstract void start();
	public abstract void stop();
	
	public abstract Pose getPose();
	public abstract boolean setPose(Pose pose);

	public abstract boolean isActing();
	public abstract void reset();
}
