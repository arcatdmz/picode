package com.phybots.picode.api;

import java.io.IOException;

import com.phybots.picode.camera.Camera;

public abstract class Poser {

	public final static String identifierSeparator = "/";

	protected MotorManager motorManager;

	private String name;

	private PoserTypeInfo poserType;

	public Poser() {
		setPoserType(PoserLibrary.getTypeInfo(this));
		initialize();
		PoserLibrary.getInstance().addPoser(this);
	}
	
	protected abstract void initialize();

	public MotorManager getMotorManager() {
		return motorManager;
	}

	public Camera getCamera() {
		return PoserLibrary.getInstance().getCameraManager().getCamera(this);
	}

	private void setPoserType(PoserTypeInfo poserType) {
		this.poserType = poserType;
	}

	public PoserTypeInfo getPoserType() {
		return poserType;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void dispose() {
		PoserLibrary.getInstance().removePoser(this);
	}

	public boolean setPose(Pose pose) {
		return getMotorManager().setPose(pose);
	}

	public Pose getPose() {
		return getMotorManager().getPose();
	}

	public Pose capture() {
		PoseLibrary poseLibrary = PoseLibrary.getInstance();
		Pose pose = getPose();
		if (pose == null) {
			return pose;
		}
		pose.setName(poseLibrary.newName());
		pose.setPhoto(getCamera().getImage());
		try {
			pose.save();
		} catch (IOException e) {
			return null;
		}
		poseLibrary.addPose(pose);
		return pose;
	}

	public boolean isActing() {
		return getMotorManager().isActing();
	}

	public Action action() {
		Action action = new Action(this);
		return action;
	}

	public TypeBasedPoseLibrary getPoseLibrary() {
		return PoseLibrary.getInstance().getTypeBasedLibrary(poserType);
	}

	public void showCaptureFrame(boolean isVisible) {
		getCamera().showFrame(isVisible);
	}

	public String getIdentifier() {
		return getClass().getSimpleName();
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", name, getClass().getSimpleName());
	}

}
