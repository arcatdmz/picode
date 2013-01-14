package com.phybots.picode;

import java.awt.image.BufferedImage;

import com.phybots.picode.ui.CaptureFrameAbstractImpl;
import com.phybots.picode.ui.PicodeMain;

public abstract class MotorManager {
	private PicodeMain picodeMain;
	private Robot robot;
	private CaptureFrameAbstractImpl captureFrame;

	public MotorManager(PicodeMain picodeMain, Robot robot) throws InstantiationException {
		this.picodeMain = picodeMain;
		this.robot = robot;
	}

	protected PicodeMain getPicodeMain() {
		return picodeMain;
	}

	protected Robot getRobot() {
		return robot;
	}

	public abstract void start();
	public abstract void stop();

	public abstract void setEditable(boolean isEditable);
	public abstract boolean isActing();
	public abstract void reset();

	public void showCaptureFrame(boolean show) {
		if (captureFrame == null) {
			captureFrame = newCaptureFrameInstance(picodeMain);
			captureFrame.setSize(800, 580);
		}
		captureFrame.setVisible(show);
	}

	public BufferedImage getImage() {
		return picodeMain.getCamera().getImage();
	}

	protected abstract CaptureFrameAbstractImpl
			newCaptureFrameInstance(PicodeMain picodeMain);
}
