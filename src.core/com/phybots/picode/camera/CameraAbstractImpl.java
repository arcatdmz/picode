package com.phybots.picode.camera;

import java.util.HashSet;
import java.util.Set;

public abstract class CameraAbstractImpl implements Camera {
	protected Set<CameraImageListener> listeners;

	public CameraAbstractImpl() {
		listeners = new HashSet<CameraImageListener>();
	}

	@Override
	public void addImageListener(CameraImageListener listener) {
		listeners.add(listener);
	}

	@Override
	public boolean removeImageListener(CameraImageListener listener) {
		return listeners.remove(listener);
	}

}
