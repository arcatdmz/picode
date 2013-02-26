package com.phybots.picode.camera;

import com.phybots.picode.api.Human;
import com.phybots.picode.api.MindstormsNXT;
import com.phybots.picode.api.Poser;

public class CameraManager {
	
	private KinectCamera kinectCamera;
	
	private NormalCamera normalCamera;
	
	public CameraManager() {
		kinectCamera = new KinectCamera();
		normalCamera = new NormalCamera();
	}

	public Camera getCamera(Poser poser) {
		if (poser instanceof Human) {
			return kinectCamera;
		} else if (poser instanceof MindstormsNXT) {
			return normalCamera;
		}
		return null;
	}
}
