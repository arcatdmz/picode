package com.phybots.picode.camera;

import java.util.HashMap;
import java.util.Map;

import com.phybots.picode.api.Poser;
import com.phybots.picode.api.PoserLibrary;
import com.phybots.picode.api.PoserTypeInfo;

public class CameraManager {
	
	private Map<PoserTypeInfo, Camera> cameras;
	
	public CameraManager() {
		cameras = new HashMap<PoserTypeInfo, Camera>();
	}

	public Camera getCamera(Poser poser) {
		if (poser == null) {
			return null;
		}
		PoserTypeInfo poserType = PoserLibrary.getTypeInfo(poser);
		Camera camera = cameras.get(poserType);
		if (camera == null) {
			try {
				camera = poserType.cameraConstructor.newInstance();
				cameras.put(poserType, camera);
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
