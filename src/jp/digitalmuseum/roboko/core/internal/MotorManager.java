package jp.digitalmuseum.roboko.core.internal;

import java.awt.image.BufferedImage;

import jp.digitalmuseum.roboko.RobokoMain;
import jp.digitalmuseum.roboko.core.Robot;
import jp.digitalmuseum.roboko.ui.CaptureFrameAbstractImpl;

public abstract class MotorManager {
	private RobokoMain robokoMain;
	private Robot robot;
	private CaptureFrameAbstractImpl captureFrame;

	public MotorManager(RobokoMain robokoMain, Robot robot) throws InstantiationException {
		this.robokoMain = robokoMain;
		this.robot = robot;
	}

	protected RobokoMain getRobokoMain() {
		return robokoMain;
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
			captureFrame = newCaptureFrameInstance(robokoMain);
			captureFrame.setSize(800, 580);
		}
		captureFrame.setVisible(show);
	}

	public BufferedImage getImage() {
		return robokoMain.getCamera().getImage();
	}

	protected abstract CaptureFrameAbstractImpl
			newCaptureFrameInstance(RobokoMain robokoMain);
}
