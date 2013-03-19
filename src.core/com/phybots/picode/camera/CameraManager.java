package com.phybots.picode.camera;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.phybots.picode.api.Poser;
import com.phybots.picode.api.PoserLibrary;
import com.phybots.picode.api.PoserTypeInfo;

public class CameraManager {
	public static final String packageName = CameraAbstractImpl.class.getName().substring(0, 
			CameraAbstractImpl.class.getName().lastIndexOf("."));
	private Set<Camera> cameras;
	private Map<Poser, Camera> camerasMap;
	
	public CameraManager() {
		cameras = new HashSet<Camera>();
		camerasMap = new HashMap<Poser, Camera>();
	}

	public void putCamera(Poser poser, Camera camera) {
		cameras.add(camera);
		camerasMap.put(poser, camera);
	}

	public Camera getCamera(Poser poser) {
		if (poser == null) {
			return null;
		}
		Camera camera = camerasMap.get(poser);
		if (camera == null) {
			PoserTypeInfo poserType = PoserLibrary.getTypeInfo(poser);
			camera = getCamera(poserType.defaultCameraClass);
			if (camera == null) {
				camera = getCamera(poserType.secondaryCameraClass);
				if (camera == null) {
					return null;
				}
			}
			camerasMap.put(poser, camera);
		}
		return camera;
	}

	@SuppressWarnings("unchecked")
	public <C extends Camera> C getCamera(Class<C> cameraClass) {
		for (Camera camera : cameras) {
			if (cameraClass.isInstance(camera)) {
				return (C) camera;
			}
		}
		for (PoserTypeInfo poserType : PoserLibrary.getTypeInfos()) {
			if (cameraClass == poserType.defaultCameraClass) {
				try {
					Camera camera = poserType.defaultCameraConstructor.newInstance();
					cameras.add(camera);
					return (C) camera;
				} catch (Exception e) {
					// Do nothing.
				}
			} else if (cameraClass == poserType.secondaryCameraClass) {
				try {
					Camera camera = poserType.secondaryCameraConstructor.newInstance();
					cameras.add(camera);
					return (C) camera;
				} catch (Exception e) {
					// Do nothing.
				}
			}
		}
		return null;
	}

	public void dispose() {
		for (Camera camera : cameras) {
			camera.dispose();
		}
		cameras.clear();
		camerasMap.clear();
	}

}
