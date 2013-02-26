package com.phybots.picode.api;

import com.phybots.picode.camera.Camera;

public abstract class Poser {

	protected MotorManager motorManager;
	
	private String name;

	public Poser() {
		initialize();
		PoserManager.getInstance().addPoser(this);
	}
	
	protected abstract void initialize();

	public MotorManager getMotorManager() {
		return motorManager;
	}

	public Camera getCamera() {
		return PoserManager.getInstance().getCameraManager().getCamera(this);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public abstract Pose newPoseInstance();

	public void dispose() {
		PoserManager.getInstance().removePoser(this);
	}

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
	
	public void showCaptureFrame() {
		//
	}
	
	public String getIdentifier() {
		return String.format("%s/%s", getClass().getSimpleName(), name);
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", name, getClass().getSimpleName());
	}

}
