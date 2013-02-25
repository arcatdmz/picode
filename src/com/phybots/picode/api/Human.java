package com.phybots.picode.api;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.camera.KinectCamera;

public class Human extends Poser {

	public Human() {
		super();
		initialize();
	}

	public Human(PicodeMain picodeMain) {
		super(picodeMain);
		initialize();
	}
	
	private void initialize() {
		camera = new KinectCamera();
		motorManager = new HumanMotorManager(picodeMain, this);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Pose newPoseInstance() {
		return new HumanPose();
	}

}
