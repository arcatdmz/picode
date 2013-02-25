package com.phybots.picode.api;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.PoseLibrary;
import com.phybots.picode.camera.Camera;

public abstract class Poser {

	protected PicodeMain picodeMain;
	protected MotorManager motorManager;
	protected Camera camera;
	
	private String name;

	public Poser() {
		this(null);
	}

	public Poser(PicodeMain picodeMain) {
		this.picodeMain = picodeMain;
	}

	public MotorManager getMotorManager() {
		return motorManager;
	}
	
	public Camera getCamera() {
		return camera;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public abstract Pose newPoseInstance();

	public abstract void dispose();

	public boolean setPose(Pose pose) {
		return getMotorManager().setPose(pose);
	}

	public Pose getPose() {
		return getMotorManager().getPose();
	}

	public boolean isActing() {
		return getMotorManager().isActing();
	}

	public Action action() {
		Action action = new Action(this);
		return action;
	}

	public PoseLibrary getPoseLibrary() {
		return null;
	}
	
	public String getIdentifier() {
		return String.format("%s/%s", getClass().getSimpleName(), name);
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", name, getClass().getSimpleName());
	}

}
