
package com.phybots.picode.ui.camera;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import com.phybots.picode.camera.Camera;
import com.phybots.picode.camera.CameraImageListener;
import com.phybots.picode.camera.NormalCamera;

public class NormalCameraPanel extends CameraPanelAbstractImpl implements CameraImageListener {
	private static final long serialVersionUID = -2321182339166269822L;

	private transient NormalCamera normalCamera;
	private transient BufferedImage image;

	public NormalCameraPanel(NormalCamera normalCamera) {
		this.normalCamera = normalCamera;
	}

	public boolean start() {
		if (normalCamera.start()) {
			normalCamera.addImageListener(this);
			return true;
		}
		return false;
	}

	public void stop() {
		normalCamera.stop();
		normalCamera.removeImageListener(this);
	}

	public Camera getCamera() {
		return normalCamera;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image != null) {
			int x = (getWidth() - image.getWidth()) / 2;
			int y = (getHeight() - image.getHeight()) / 2;
			g.drawImage(image, x, y, null);
		}
	}

	@Override
	public void imageUpdated(BufferedImage image) {
		this.image = image;
		repaint();
	}
}
