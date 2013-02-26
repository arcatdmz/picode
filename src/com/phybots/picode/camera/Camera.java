package com.phybots.picode.camera;

import java.awt.image.BufferedImage;

public interface Camera {
	public boolean start();
	public void stop();
	public BufferedImage getImage();
}
