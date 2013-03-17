package com.phybots.picode.api;

import java.lang.reflect.Constructor;

import com.phybots.picode.camera.Camera;

public class PoserTypeInfo {
	public String typeName;
	public boolean supportsConnector;
	public Constructor<? extends Poser> constructor;
	public Class<? extends Pose> poseClass;
	public Constructor<? extends Pose> poseConstructor;
	public Class<? extends Camera> defaultCameraClass;
	public Class<? extends Camera> secondaryCameraClass;
	public Constructor<? extends Camera> defaultCameraConstructor;
	public Constructor<? extends Camera> secondaryCameraConstructor;

	@Override
	public String toString() {
		return typeName;
	}

}