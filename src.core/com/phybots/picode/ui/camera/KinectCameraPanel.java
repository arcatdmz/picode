package com.phybots.picode.ui.camera;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.thrift.TException;

import com.phybots.picode.action.CapturePoseAction;
import com.phybots.picode.camera.Camera;
import com.phybots.picode.camera.KinectCamera;

import jp.digitalmuseum.kinect.Frame;
import jp.digitalmuseum.kinect.Joint;
import jp.digitalmuseum.kinect.JointType;
import jp.digitalmuseum.kinect.KinectServiceWrapper;
import jp.digitalmuseum.kinect.KinectServiceWrapper.FrameListener;

public class KinectCameraPanel extends CameraPanelAbstractImpl implements FrameListener {
	private static final long serialVersionUID = -2181637179453387943L;

	private JPanel panel;
	private JSlider slider;

	private transient KinectCamera camera;
	private transient KinectServiceWrapper kinect;
	private transient Frame frame;
	private transient BufferedImage image;
	private transient short[] depthImageData;
	private transient BufferedImage depthImage;
	private transient int skeletonLife = 0;
	private transient Map<JointType, Joint> joints;

	public KinectCameraPanel(KinectCamera kinectCamera) {
		initialize();
		this.camera = kinectCamera;
		this.kinect = kinectCamera.getKinect();
	}

	private void initialize() {
		setLayout(new BorderLayout());

		panel = new KinectImagePanel();
		panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!kinect.isStarted()) {
					return;
				}
				try {
					boolean isDepthEnabled = kinect.isDepthEnabled();
					kinect.setDepthEnabled(!isDepthEnabled);
					System.out.print("Depth stream is ");
					System.out.println(
							isDepthEnabled ? "disabled." : "enabled");
				} catch (TException te) {
					te.printStackTrace();
				}
			}
		});
		panel.setPreferredSize(new Dimension(640, 480));
		add(panel, BorderLayout.CENTER);

		slider = new JSlider();
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (kinect != null && kinect.isStarted()) {
					try {
						kinect.setAngle(slider.getValue());
					} catch (TException te) {
						te.printStackTrace();
					}
				}
			}
		});
		slider.setToolTipText("Elevation angle");
		slider.setPaintTicks(true);
		slider.setMajorTickSpacing(10);
		slider.setMaximum(27);
		slider.setMinimum(-27);
		slider.setOrientation(SwingConstants.VERTICAL);
		add(slider, BorderLayout.EAST);
	}

	public boolean start() {
		if (camera.start()) {
			kinect.addFrameListener(this);
			try {
				slider.setValue(kinect.getAngle());
			} catch (TException e1) {
				e1.printStackTrace();
			}
			return true;
		}
		return false;
	}

	public void stop() {
		camera.stop();
		kinect.removeFrameListener(this);
	}

	public Camera getCamera() {
		return camera;
	}

	@Override
	public void frameUpdated(Frame frame, BufferedImage image, short[] depthImageData) {
		this.frame = frame;
		this.image = image;
		this.depthImageData = depthImageData;
		if (frame.isSetWords()) {
			boolean save = false;
			System.out.print("Word detected: ");
			for (String word : frame.getWords()) {
				System.out.print(word);
				save |= "capture".equalsIgnoreCase(word);
			}
			System.out.println();
			if (save) {
				new CapturePoseAction().actionPerformed(null);
			}
		}
		panel.repaint();
	}

	private class KinectImagePanel extends JPanel {
		private static final long serialVersionUID = 3843830856536157432L;

		private BasicStroke stroke = new BasicStroke(3);

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (frame == null) {
				return;
			}
			if (frame.isSetDepthImage()) {
				if (depthImage == null) {
					depthImage = new BufferedImage(
							320, 240, BufferedImage.TYPE_INT_RGB);
				}
				DataBufferInt dbi = (DataBufferInt)depthImage.getRaster().getDataBuffer();
				int[] pixels = dbi.getData();
				for (int i = 0; i < depthImageData.length; i ++) {
					int realDepth = KinectServiceWrapper.getRealDepth(depthImageData[i]);
					if (realDepth == KinectServiceWrapper.tooNearDepth) {
						pixels[i] = 0xff0000;
					} else if (realDepth == KinectServiceWrapper.tooFarDepth) {
						pixels[i] = 0x00ff00;
					} else if (realDepth == KinectServiceWrapper.unknownDepth) {
						pixels[i] = 0x0000ff;
					} else {
						int intensity = 0xff * Math.abs(realDepth) / KinectServiceWrapper.tooFarDepth;
						int index = KinectServiceWrapper.getPlayerIndex(depthImageData[i]);
						switch (index) {
						case 1:
						case 4:
						case 7:
							pixels[i] = (intensity << 16) + (intensity << 8);
							break;
						case 2:
						case 5:
							pixels[i] = (intensity << 16) + intensity;
							break;
						case 3:
						case 6:
							pixels[i] = (intensity << 8) + intensity;
							break;
						default:
							pixels[i] = (intensity << 16) + (intensity << 8) + intensity;
							break;
						}
					}
				}
				int x = (getWidth() - depthImage.getWidth() * 2) / 2;
				int y = (getHeight() - depthImage.getHeight() * 2) / 2;

				g.drawImage(depthImage, x, y, depthImage.getWidth() * 2, depthImage.getHeight() * 2, null);
			} else if (frame.isSetImage()) {
				int x = (getWidth() - image.getWidth()) / 2;
				int y = (getHeight() - image.getHeight()) / 2;

				g.drawImage(image, x, y, null);

				if (frame.joints.size() == 20) {
					joints = frame.joints;
					skeletonLife = KinectCamera.SKELETON_LIFE;
				} else if (skeletonLife > 0) {
					skeletonLife --;
				}
				if (skeletonLife > 0) {
					g.setColor(Color.green);
					((Graphics2D) g).setStroke(stroke);
					KinectServiceWrapper.drawSkeleton(g, joints, x, y);
				}
			}
		}
	}

}
