package jp.digitalmuseum.kinect.app;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import javax.swing.Action;

import org.apache.thrift.TException;

import jp.digitalmuseum.kinect.Frame;
import jp.digitalmuseum.kinect.KinectServiceWrapper;
import jp.digitalmuseum.kinect.KinectServiceWrapper.FrameListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class KinectClientFrame extends JFrame implements FrameListener {

	private static final long serialVersionUID = -1065767804512646130L;

	private JPanel contentPane;
	private final Action startAction = new StartAction();
	private final Action stopAction = new StopAction();
	
	private transient KinectServiceWrapper kinect =
			new KinectServiceWrapper("localhost", 9090);
	private transient BufferedImage image;
	private JPanel panel;
	private JSlider slider;

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

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (image != null) {
					g.drawImage(image,
							(getWidth() - image.getWidth())/2,
							(getHeight() - image.getHeight())/2,
							null);
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
