package com.phybots.picode.api;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.camera.Camera;
import com.phybots.picode.ui.pose.PoseLibrary;

public abstract class Poser {
	
	private PicodeMain picodeMain;
	protected MotorManager motorManager;
	protected Camera camera;

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
	
	public void setConnector(String connectionString) {
		throw new UnsupportedOperationException(
				String.format(
						"setConnector is not allowed for %s class",
						this.getClass().getSimpleName()));
	}

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
		// TODO Auto-generated method stub
		return null;
	}
}
