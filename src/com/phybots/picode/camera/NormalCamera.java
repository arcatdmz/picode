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
	public void start() {
		camera.start();
	}

	@Override
	public void stop() {
		camera.stop();
	}

	@Override
	public BufferedImage getImage() {
		// TODO Auto-generated method stub
		return null;
	}
}
