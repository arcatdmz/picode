package jp.digitalmuseum.kinect;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.phybots.picode.core.internal.HumanPose;

public class KinectClient extends Thread {
	private Set<KinectListener> listeners =
			Collections.synchronizedSet(
					new HashSet<KinectListener>());
	private Socket socket;
	private DataInputStream inputStream;

	public static void main(String[] args) {

		// Initialize GUI.
		final KinectPanel panel = new KinectPanel();
		final Timer timer = new Timer();
		final KinectClient client = new KinectClient();
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				JFrame frame = new JFrame();
				frame.add(panel);
				frame.pack();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						timer.cancel();
						client.close();
					}
				});
				frame.setVisible(true);
			}
		});
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						panel.repaint();
					}
				});
			}
		}, 1000/30, 1000/30);

		client.addKinectListener(panel);
		client.start();
	}

	public void addKinectListener(KinectListener listener) {
		listeners.add(listener);
	}

	public void removeListener(KinectListener listener) {
		listeners.remove(listener);
	}

	public void run() {
		try {
			connect("localhost", 9000);
			String line;
			while ((line = readLine()) != null) {
				String[] data = line.split(" ");
				if (data.length <= 3) {
					if (data.length == 2) {
						if (data[0].equals("Phrase")) {
							for (KinectListener listener : listeners) {
								listener.speechRecognized(data[1]);
							}
						} else if (data[0].equals("Image")) {
							int length = Integer.parseInt(data[1]);
							BufferedImage image = readImage(this, length);
							if (image != null) {
								for (KinectListener listener : listeners) {
									listener.imageUpdated(image);
								}
							}
						}
					}
					continue;
				}
				if (data[2].equals("Basic")) {
					for (KinectListener listener : listeners) {
						listener.bodyUpdated(parsePosition(data, 3));
					}
					continue;
				}
				if (data[2].equals("Joints")) {
					int j = (data.length - 3) / 3;
					float[][] joints = new float[j][];
					for (int i = 0; i < j; i ++) {
						joints[i] = parsePosition(data, (i+1)*3);
					}
					for (KinectListener listener : listeners) {
						listener.jointsUpdated(joints);
					}
				}
			}
			close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	private static BufferedImage readImage(KinectClient client, int length) {
		int off = 0;
		byte[] buf = new byte[length];
		try {
			while (off < length) {
				int read = client.read(buf, off, length - off);
				off += read;
			}
			return ImageIO.read(new ByteArrayInputStream(buf));
		} catch (IOException e) {
			return null;
		}
	}

	private static float[] parsePosition(String[] data, int start) {
		return new float[] {
				Float.valueOf(data[start  ]),	
				Float.valueOf(data[start+1]),	
				Float.valueOf(data[start+2])
		};
	}

	private void connect(String host, int port) throws IOException {
		socket = new Socket(host, port);
		inputStream = new DataInputStream(
						socket.getInputStream());
	}

	@SuppressWarnings("deprecation")
	private String readLine() throws IOException {
		return inputStream == null ? null : inputStream.readLine();
	}

	private int read(byte[] buf, int off, int len) throws IOException {
		return inputStream.read(buf, off, len);
	}

	private void close() {
		try {
			if (inputStream != null) {
				inputStream.close();
			}
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		inputStream = null;
		socket = null;
	}

	public static interface KinectListener {
		public void speechRecognized(String word);
		public void imageUpdated(BufferedImage image);
		public void bodyUpdated(float[] body);
		public void bodyUpdated(int[] body);
		public void jointsUpdated(float[][] joints);
		public void jointsUpdated(int[][] joints);
	}

	public static class KinectPanel extends JPanel implements KinectListener {
		private static final long serialVersionUID = 8935515227172394238L;
		private BasicStroke basicStroke = new BasicStroke(3);
		private float[] body;
		private float[][] joints;
		private HumanPose pose = new HumanPose();
		private int makeGreen = 0;

		public void speechRecognized(String word) {
			System.out.print("Speech recognized: ");
			System.out.println(word);
			makeGreen = 15;
		}

		public void imageUpdated(BufferedImage image) {
			try {
				ImageIO.write(
						image,
						"JPEG",
						new File("Test.jpg"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void bodyUpdated(int[] body) {
			// this.body = body;
		}

		public void jointsUpdated(int[][] joints) {
			// this.joints = joints;
		}

		public void bodyUpdated(float[] body) {
			this.body = body;
		}

		public void jointsUpdated(float[][] joints) {
			this.joints = joints;
			pose.importData(joints);
			/*
			double[] wea = pose.getWaistEulerAngle();
			System.out.println(
					String.format(
							"Eular angle: a%.2f b%.2f c%.2f",
							wea[0] * 180 / Math.PI,
							wea[1] * 180 / Math.PI,
							wea[2] * 180 / Math.PI));
			System.out.println(
					String.format(
							"Elbow angle: L%.2f R%.2f",
							pose.getLeftElbowAngle() * 180 / Math.PI,
							pose.getRightElbowAngle() * 180 / Math.PI));
			*/
			System.out.println(
					String.format(
							"Knee angle: L%.2f R%.2f",
							pose.getLeftKneeAngle() * 180 / Math.PI,
							pose.getRightKneeAngle() * 180 / Math.PI));
		}

		@Override
		public void paintComponent(Graphics g) {
			Stroke defaultStroke = ((Graphics2D) g).getStroke();
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			int w = getWidth(), h = getHeight();

			// Clear background.
			g.setColor(Color.white);
			g.fillRect(0, 0, w-1, h-1);

			// Draw grids.
			g.setColor(Color.lightGray);
			int i = w/2, j = w/10*10;
			while (i < w*3/2) {
				i += 10;
				g.drawLine(i%j, 0, i%j, h-1);
			}
			i = h/2; j = h/10*10;
			while (i < h*3/2) {
				i += 10;
				g.drawLine(0, i%j, w-1, i%j);
			}

			// Draw skeletons.
			g.setColor(Color.black);
			g.drawLine(w/2, 0, w/2, h-1);
			g.drawLine(0, h/2, w-1, h/2);
			if (joints != null) {
				if (makeGreen > 0) {
					makeGreen --;
					g.setColor(Color.green);
				} else {
					g.setColor(Color.blue);
				}
				for (i = 0; i < joints.length; i ++) {
					float[] joint = joints[i];
					drawPoint(g, joint, i);
				}
				((Graphics2D) g).setStroke(basicStroke);
				drawLine(g, joints, 0, 1, 2, 3);
				drawLine(g, joints, 2, 4, 5, 6, 7);
				drawLine(g, joints, 2, 8, 9, 10, 11);
				drawLine(g, joints, 0, 12, 13, 14, 15);
				drawLine(g, joints, 0, 16, 17, 18, 19);
				((Graphics2D) g).setStroke(defaultStroke);
			}
			if (body != null) {
				g.setColor(Color.red);
				drawPoint(g, body);
			}
		}

		private void drawPoint(Graphics g, float[] point) {
			drawPoint(g, point, -1);
		}

		private void drawPoint(Graphics g, float[] point, int index) {
			float x = point[0], y = point[1], z = point[2];
			int w = getWidth(), h = getHeight();
			int sx = (int) (x/z*1000 + w/2 - 4), sy = (int) (-y/z*1000 + h/2 - 4);
			g.fillOval(sx, sy, 9, 9);
			g.drawString(String.format("%s(%.2f, %.2f, %.2f [cm])",
					index >= 0 ? String.valueOf(index) : "", x*100, y*100, z*100),
					sx+10, sy);
		}

		private void drawLine(Graphics g, float[][] points, int... indices) {
			float x, y, z;
			x = points[indices[0]][0];
			y = points[indices[0]][1];
			z = points[indices[0]][2];
			int w = getWidth(), h = getHeight();
			int sx = (int) (x/z*1000 + w/2), sy = (int) (-y/z*1000 + h/2);
			for (int i = 1; i < indices.length; i ++) {
				x = points[indices[i]][0];
				y = points[indices[i]][1];
				z = points[indices[i]][2];
				int ex = (int) (x/z*1000 + w/2), ey = (int) (-y/z*1000 + h/2);
				g.drawLine(sx, sy, ex, ey);
				sx = ex;
				sy = ey;
			}
		}
	}
}
