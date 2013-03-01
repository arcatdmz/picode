package com.phybots.picode.camera;

import java.awt.image.BufferedImage;

public class NormalCamera implements Camera {
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
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public void stop() {
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
	public void showFrame(boolean isVisible) {
		// TODO Implement this.
	}
	
	@Override
	public boolean isFrameVisible() {
		return false;
	}

}
