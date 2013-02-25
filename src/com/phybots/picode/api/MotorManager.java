package com.phybots.picode.api;

import com.phybots.picode.PicodeMain;

public abstract class MotorManager {
	private PicodeMain picodeMain;
	private Poser poser;

	public MotorManager(PicodeMain picodeMain, Poser poser) {
		this.picodeMain = picodeMain;
		this.poser = poser;
	}

	protected PicodeMain getPicodeMain() {
		return picodeMain;
	}

	protected Poser getPoser() {
		return poser;
	}

	public abstract void start();
	public abstract void stop();
	
	public abstract Pose getPose();
	public abstract boolean setPose(Pose pose);

	public abstract boolean isActing();
	public abstract void reset();
}
