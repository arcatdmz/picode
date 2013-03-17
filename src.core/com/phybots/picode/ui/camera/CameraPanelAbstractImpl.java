package com.phybots.picode.ui.camera;

import javax.swing.JPanel;

import com.phybots.picode.camera.Camera;

public abstract class CameraPanelAbstractImpl extends JPanel {
	private static final long serialVersionUID = -5047012073027626427L;
	public abstract boolean start();
	public abstract void stop();
	public abstract Camera getCamera();
}
