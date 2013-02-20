package jp.digitalmuseum.kinect.app;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.apache.thrift.TException;

import jp.digitalmuseum.kinect.Frame;
import jp.digitalmuseum.kinect.Joint;
import jp.digitalmuseum.kinect.JointType;
import jp.digitalmuseum.kinect.KinectServiceConstants;
import jp.digitalmuseum.kinect.KinectServiceWrapper;
import jp.digitalmuseum.kinect.KinectServiceWrapper.FrameListener;

public class KinectClientFrame extends JFrame implements FrameListener {

	private static final long serialVersionUID = -1065767804512646130L;

	private JPanel contentPane;
	private JPanel panel;
	private JSlider slider;

	private final Action startAction = new StartAction();
	private final Action stopAction = new StopAction();
	
	private transient KinectServiceWrapper kinect =
			new KinectServiceWrapper("localhost", KinectServiceConstants.SERVER_DEFAULT_PORT);
	private transient Frame frame;
	private transient BufferedImage image;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					KinectClientFrame frame = new KinectClientFrame();
					frame.pack();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public KinectClientFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		panel = new JPanel() {
			private static final long serialVersionUID = -8183654090486553528L;
			private BasicStroke stroke = new BasicStroke(3);

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				int x = 0, y = 0;
				if (image != null) {
					g.drawImage(image, x, y, null);
					x = (getWidth() - image.getWidth()) / 2;
					y = (getHeight() - image.getHeight()) / 2;
				}
				if (frame != null
						&& frame.joints != null
						&& frame.joints.size() == 20) {
					g.setColor(Color.green);
					((Graphics2D) g).setStroke(stroke);
					drawLine(g, x, y, frame.joints,
							JointType.HIP_CENTER,
							JointType.SPINE,
							JointType.SHOULDER_CENTER,
							JointType.HEAD);
					drawLine(g, x, y, frame.joints,
							JointType.SHOULDER_CENTER,
							JointType.SHOULDER_RIGHT,
							JointType.ELBOW_RIGHT,
							JointType.WRIST_RIGHT,
							JointType.HAND_RIGHT);
					drawLine(g, x, y, frame.joints,
							JointType.SHOULDER_CENTER,
							JointType.SHOULDER_LEFT,
							JointType.ELBOW_LEFT,
							JointType.WRIST_LEFT,
							JointType.HAND_LEFT);
					drawLine(g, x, y, frame.joints,
							JointType.HIP_CENTER,
							JointType.HIP_RIGHT,
							JointType.KNEE_RIGHT,
							JointType.ANKLE_RIGHT,
							JointType.FOOT_RIGHT);
					drawLine(g, x, y, frame.joints,
							JointType.HIP_CENTER,
							JointType.HIP_LEFT,
							JointType.KNEE_LEFT,
							JointType.ANKLE_LEFT,
							JointType.FOOT_LEFT);
				}
			}

			private void drawLine(Graphics g, int x, int y, Map<JointType, Joint> joints, JointType... keys) {
				Joint sj = joints.get(keys[0]);
				double sx = 0, sy = 0;
				if (sj != null) {
					sx = sj.screenPosition.x + x;
					sy = sj.screenPosition.y + y;
				}
				for (int i = 1; i < keys.length; i ++) {
					Joint ej = joints.get(keys[i]);
					double ex = 0, ey = 0;
					if (ej != null) {
						ex = ej.screenPosition.x + x;
						ey = ej.screenPosition.y + y;
					}
					if (sj != null && ej != null) {
						g.drawLine((int)sx, (int)sy, (int)ex, (int)ey);
					}
					sj = ej;
					sx = ex;
					sy = ey;
				}
			}
		};
		panel.setPreferredSize(new Dimension(640, 480));
		contentPane.add(panel, BorderLayout.CENTER);
		
		slider = new JSlider();
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (kinect.isStarted()) {
					try {
						kinect.setAngle(slider.getValue());
					} catch (TException te) {
						te.printStackTrace();
					}
					setTitle(String.format("Kinect (Elevation angle: %d)", slider.getValue()));
				}
			}
		});
		slider.setToolTipText("Elevation angle");
		slider.setPaintTicks(true);
		slider.setMajorTickSpacing(10);
		slider.setMaximum(27);
		slider.setMinimum(-27);
		slider.setOrientation(SwingConstants.VERTICAL);
		contentPane.add(slider, BorderLayout.EAST);
		
		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton btnStart = new JButton("Start");
		btnStart.setAction(startAction);
		panel_1.add(btnStart);
		
		JButton btnStop = new JButton("Stop");
		btnStop.setAction(stopAction);
		panel_1.add(btnStop);
	}

	private class StartAction extends AbstractAction {
		private static final long serialVersionUID = 4134353985601569086L;
		public StartAction() {
			putValue(NAME, "Start");
			putValue(SHORT_DESCRIPTION, "Start retrieving data from the Kinect camera.");
		}
		public void actionPerformed(ActionEvent e) {
			if (!kinect.start()) {
				System.err.println("Server is not ready.");
				return;
			}
			try {
				slider.setValue(kinect.getAngle());
				kinect.addFrameListener(KinectClientFrame.this);
			} catch (TException te) {
				te.printStackTrace();
			}
		}
	}
	private class StopAction extends AbstractAction {
		private static final long serialVersionUID = 3031247155212739623L;
		public StopAction() {
			putValue(NAME, "Stop");
			putValue(SHORT_DESCRIPTION, "Stop retrieving data from the Kinect camera.");
		}
		public void actionPerformed(ActionEvent e) {
			kinect.removeFrameListener(KinectClientFrame.this);
			kinect.stop();
		}
	}
	@Override
	public void frameUpdated(Frame frame, BufferedImage image) {
		this.frame = frame;
		this.image = image;
		panel.repaint();
	}
	@Override
	public void dispose() {
		super.dispose();
		if (kinect.isStarted()) {
			kinect.stop();
		}
	}
}
