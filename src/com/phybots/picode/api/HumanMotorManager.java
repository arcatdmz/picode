package com.phybots.picode.api;

import com.phybots.picode.PicodeMain;

public class HumanMotorManager extends MotorManager {

	public HumanMotorManager(PicodeMain picodeMain, Poser poser) {
		super(picodeMain, poser);
	}

	public void start() {
	}

	public void stop() {
	}

	@Override
	public Pose getPose() {
		// TODO Auto-generated method stub
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
