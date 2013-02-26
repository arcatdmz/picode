package com.phybots.picode.camera;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.apache.thrift.TException;

import jp.digitalmuseum.kinect.Frame;
import jp.digitalmuseum.kinect.KinectServiceConstants;
import jp.digitalmuseum.kinect.KinectServiceWrapper;
import jp.digitalmuseum.kinect.KinectServiceWrapper.FrameListener;

public class KinectCamera implements Camera, FrameListener {

	private static final String KINECT_PATH = "./kinect/csharp/ConsoleKinectServer.exe";
	private static Process kinectProcess;

	private KinectServiceWrapper wrapper = new KinectServiceWrapper(
			"localhost", KinectServiceConstants.SERVER_DEFAULT_PORT);

	private Frame frame;
	private BufferedImage colorImage;
	
	public static boolean startServer() {
		if (kinectProcess != null) {
			try {
				kinectProcess.exitValue();
			} catch (IllegalStateException ise) {
				// Previously executed process is still alive!
				return true;
			}
		}
		try {
			kinectProcess = Runtime.getRuntime().exec(new String[] {
					KINECT_PATH.replaceAll("/", File.pathSeparator)
			});
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean start() {
		if (!wrapper.start()) {
			if (!KinectCamera.startServer()) {
				return false;
			}
			for (int i = 0; i < 10; i ++) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// Do nothing.
				}
				if (wrapper.start()) {
					break;
				}
			}
			if (!wrapper.isStarted()) {
				return false;
			}
		}
		try {
			wrapper.setColorEnabled(true);
			wrapper.setDepthEnabled(false);
			wrapper.addKeyword("Capture");
			wrapper.setVoiceEnabled(true);
		} catch (TException e) {
			throw new IllegalStateException(e);
		}
		wrapper.addFrameListener(this);
		return true;
	}

	@Override
	public void stop() {
		wrapper.stop();
	}

	@Override
	public BufferedImage getImage() {
		return colorImage;
	}

	@Override
	public void frameUpdated(Frame frame, BufferedImage colorImage, short[] depthImageData) {
		this.frame = frame;
		this.colorImage = colorImage;
	}

}
