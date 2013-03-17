package com.phybots.picode.camera;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.thrift.TException;

import com.phybots.picode.api.HumanPose;
import com.phybots.picode.ui.camera.CameraPanelAbstractImpl;
import com.phybots.picode.ui.camera.KinectCameraPanel;

import jp.digitalmuseum.kinect.Frame;
import jp.digitalmuseum.kinect.Joint;
import jp.digitalmuseum.kinect.JointType;
import jp.digitalmuseum.kinect.KinectServiceConstants;
import jp.digitalmuseum.kinect.KinectServiceWrapper;
import jp.digitalmuseum.kinect.KinectServiceWrapper.FrameListener;

public class KinectCamera extends CameraAbstractImpl implements FrameListener {

	public static final String KINECT_PATH = "./kinect/csharp/ConsoleKinectServer.exe";
	public static final int SKELETON_LIFE = 7;
	private static Process kinectProcess;

	private KinectServiceWrapper wrapper = new KinectServiceWrapper(
			"localhost", KinectServiceConstants.SERVER_DEFAULT_PORT);

	private Frame frame;
	private Joint[] joints;
	private BufferedImage colorImage;
	private int skeletonLife;

	public KinectCamera() {
		joints = new Joint[20];
	}
	
	public static boolean startServer() {
		if (kinectProcess != null) {
			try {
				kinectProcess.exitValue();
			} catch (Exception e) {
				// Previously executed process is still alive!
				return true;
			}
		}
		try {
			kinectProcess = Runtime.getRuntime().exec(new String[] {
					System.getProperty("user.dir") + KINECT_PATH
			});
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static void stopServer() {
		if (kinectProcess != null) {
			kinectProcess.destroy();
			kinectProcess = null;
		}
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
		if (wrapper.isStarted()) {
			wrapper.stop();
		}
	}

	@Override
	public void dispose() {
		stopServer();
	}

	@Override
	public BufferedImage getImage() {
		return colorImage;
	}

	public KinectServiceWrapper getKinect() {
		return wrapper;
	}

	public Frame getFrame() {
		return frame;
	}
	
	public Joint[] getLatestJoints() {
		return joints;
	}

	@Override
	public void frameUpdated(Frame frame, BufferedImage colorImage, short[] depthImageData) {
		this.frame = frame;
		if (frame.getJointsSize() == 20) {
			Arrays.fill(this.joints, null);
			setLatestJoints(frame.getJoints());
			skeletonLife = SKELETON_LIFE;
		} else if (skeletonLife > 0) {
			skeletonLife --;
			if (skeletonLife == 0) {
				Arrays.fill(this.joints, null);
			}
		}
		this.colorImage = colorImage;
		for (CameraImageListener listener : listeners) {
			listener.imageUpdated(colorImage);
		}
	}

	private void setLatestJoints(Map<JointType, Joint> joints) {
		this.joints[HumanPose.HIP_CENTER] = joints.get(JointType.HIP_CENTER);
		this.joints[HumanPose.SPINE] = joints.get(JointType.SPINE);
		this.joints[HumanPose.SHOULDER_CENTER] = joints.get(JointType.SHOULDER_CENTER);
		this.joints[HumanPose.HEAD] = joints.get(JointType.HEAD);
		this.joints[HumanPose.SHOULDER_RIGHT] = joints.get(JointType.SHOULDER_RIGHT);
		this.joints[HumanPose.ELBOW_RIGHT] = joints.get(JointType.ELBOW_RIGHT);
		this.joints[HumanPose.WRIST_RIGHT] = joints.get(JointType.WRIST_RIGHT);
		this.joints[HumanPose.HAND_RIGHT] = joints.get(JointType.HAND_RIGHT);
		this.joints[HumanPose.SHOULDER_LEFT] = joints.get(JointType.SHOULDER_LEFT);
		this.joints[HumanPose.ELBOW_LEFT] = joints.get(JointType.ELBOW_LEFT);
		this.joints[HumanPose.WRIST_LEFT] = joints.get(JointType.WRIST_LEFT);
		this.joints[HumanPose.HAND_LEFT] = joints.get(JointType.HAND_LEFT);
		this.joints[HumanPose.HIP_RIGHT] = joints.get(JointType.HIP_RIGHT);
		this.joints[HumanPose.KNEE_RIGHT] = joints.get(JointType.KNEE_RIGHT);
		this.joints[HumanPose.ANKLE_RIGHT] = joints.get(JointType.ANKLE_RIGHT);
		this.joints[HumanPose.FOOT_RIGHT] = joints.get(JointType.FOOT_RIGHT);
		this.joints[HumanPose.HIP_LEFT] = joints.get(JointType.HIP_LEFT);
		this.joints[HumanPose.KNEE_LEFT] = joints.get(JointType.KNEE_LEFT);
		this.joints[HumanPose.ANKLE_LEFT] = joints.get(JointType.ANKLE_LEFT);
		this.joints[HumanPose.FOOT_LEFT] = joints.get(JointType.FOOT_LEFT);
	}

	@Override
	public CameraPanelAbstractImpl newPanelInstance() {
		return new KinectCameraPanel(this);
	}
}
