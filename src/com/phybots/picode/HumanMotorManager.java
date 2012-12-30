package com.phybots.picode;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.Socket;

import jp.digitalmuseum.kinect.KinectClient;
import jp.digitalmuseum.kinect.KinectClient.KinectListener;

import com.phybots.picode.ui.CaptureFrameAbstractImpl;
import com.phybots.picode.ui.PicodeMain;

public class HumanMotorManager extends MotorManager {
	Process kinect;
	private BufferedImage image;
	private float[][] joints;

	public HumanMotorManager(PicodeMain picodeMain, Robot robot) throws InstantiationException {
		super(picodeMain, robot);
	}

	public void start() {

		// Check if there's already a running instance of the server.
		if (getpicodeMain() == null) {
			try {
				Socket socket = new Socket("localhost", 9000);
				socket.close();
			} catch (Exception e) {

				// If not, run the server.
				showCaptureFrame(true);
			}
		}
	}

	public void stop() {
		showCaptureFrame(false);
	}

	public void setEditable(boolean isEditable) {
	}

	public boolean isActing() {
		return false;
	}

	public void reset() {
	}

	@Override
	public void showCaptureFrame(boolean show) {
		if (show) {
			showCaptureFrame();
		} else {
			hideCaptureFrame();
		}
	}

	private void showCaptureFrame() {
		final PicodeMain picodeMain = getpicodeMain();
		if (kinect != null) {
			hideCaptureFrame();
		}
		try {
			final Process kinect = Runtime.getRuntime().exec(new String[] {
					"./kinect/bin/SkeletonServer.exe", "/AutomatedServer"
			});
			this.kinect = kinect;
			if (picodeMain != null) {
				picodeMain.setKinect(kinect);
			}
			KinectClient client = new KinectClient() {
				@Override
				public void run() {
					super.run();
					try {
						kinect.waitFor();
					} catch (InterruptedException e) {
						// Do nothing.
					}
					HumanMotorManager.this.kinect = null;
					if (picodeMain != null) {
						picodeMain.setKinect(null);
					}
				}
			};
			client.addKinectListener(new KinectListener() {
				public void speechRecognized(String word) {
				}
				public void jointsUpdated(float[][] joints) {
					HumanMotorManager.this.joints = joints;
				}
				public void jointsUpdated(int[][] joints) {
				}
				public void imageUpdated(BufferedImage image) {
					HumanMotorManager.this.image = image;
					if (picodeMain != null) {
						picodeMain.getPoseManager().capture();
					}
				}
				public void bodyUpdated(float[] body) {
				}
				public void bodyUpdated(int[] body) {
				}
			});
			Thread.sleep(1000);
			client.start();
		} catch (IOException e1) {
			// Do nothing.
		} catch (InterruptedException e2) {
			// Do nothing.
		}		
	}

	private void hideCaptureFrame() {
		if (kinect != null) {
			kinect.destroy();
		}
	}

	public float[][] getJoints() {
		return joints;
	}

	public BufferedImage getImage() {
		return image;
	}

	@Override
	protected CaptureFrameAbstractImpl newCaptureFrameInstance(PicodeMain picodeMain) {
		throw new UnsupportedOperationException();
	}
}
