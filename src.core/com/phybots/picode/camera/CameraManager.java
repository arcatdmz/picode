package com.phybots.picode.camera;

import java.util.HashMap;
import java.util.Map;

import com.phybots.picode.api.Poser;
import com.phybots.picode.api.PoserLibrary;
import com.phybots.picode.api.PoserTypeInfo;

public class CameraManager {
	
	private Map<Poser, Camera> cameras;
	
	public CameraManager() {
		cameras = new HashMap<Poser, Camera>();
	}

	public void putCamera(Poser poser, Camera camera) {
		cameras.put(poser, camera);
	}

	public Camera getCamera(Poser poser) {
		if (poser == null) {
			return null;
		}
		Camera camera = cameras.get(poser);
		if (camera == null) {
			// Instantiate the default camera.
			PoserTypeInfo poserType = PoserLibrary.getTypeInfo(poser);
			try {
				camera = poserType.defaultCameraConstructor.newInstance();
				cameras.put(poser, camera);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return camera;
	}

	public void dispose() {
		for (Camera camera : cameras.values()) {
			camera.dispose();
		}
	}

}
