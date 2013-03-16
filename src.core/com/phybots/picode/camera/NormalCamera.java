package com.phybots.picode.camera;

import java.awt.image.BufferedImage;

import com.phybots.service.ImageProvider.ImageListener;

public class NormalCamera extends CameraAbstractImpl implements ImageListener {
	private com.phybots.service.Camera camera;

	public NormalCamera(String identifier) {
		camera = new com.phybots.service.Camera(identifier);
	}

	public NormalCamera() {
		camera = new com.phybots.service.Camera();
	}

	@Override
	public boolean start() {
		try {
			camera.start();
			camera.addImageListener(this);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public void stop() {
		camera.removeImageListener(this);
		camera.stop();
	}

	@Override
	public void dispose() {
	}

	@Override
	public BufferedImage getImage() {
		return camera.getImage();
	}

	@Override
	public void imageUpdated(BufferedImage image) {
		for (CameraImageListener listener : listeners) {
			listener.imageUpdated(image);
		}
	}

}
