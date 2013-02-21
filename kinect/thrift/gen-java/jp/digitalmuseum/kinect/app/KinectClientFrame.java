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
import java.awt.image.DataBufferInt;

import javax.imageio.ImageIO;
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
import jp.digitalmuseum.kinect.KinectServiceConstants;
import jp.digitalmuseum.kinect.KinectServiceWrapper;
import jp.digitalmuseum.kinect.KinectServiceWrapper.FrameListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	private transient short[] depthImageData;
	private transient BufferedImage depthImage;

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

					g.setColor(Color.green);
					((Graphics2D) g).setStroke(stroke);
					KinectServiceWrapper.drawSkeleton(g, frame, x, y);
				}
			}
		};
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
				kinect.addKeyword("Capture");
				kinect.setVoiceEnabled(true);
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
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss");
				try {
					File file = new File(sdf.format(new Date()) + ".jpg");
					ImageIO.write(image, "JPEG",
							new FileOutputStream(file));
					System.out.print("Current image is saved as: ");
					System.out.println(file.getAbsolutePath());
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
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
