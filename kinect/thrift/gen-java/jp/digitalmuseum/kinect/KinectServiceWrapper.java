package jp.digitalmuseum.kinect;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class KinectServiceWrapper implements KinectService.Iface {
	private TTransport transport;
	private KinectService.Client client;
	private Frame frame;
	private BufferedImage image;
	private Future<?> future;
	private Set<FrameListener> listeners;

	public KinectServiceWrapper(String host, int port) {
		this(host, port, 300);
	}

	public KinectServiceWrapper(String host, int port, int timeout) {
		transport = new TSocket(host, port, timeout);
		TProtocol protocol = new TBinaryProtocol(transport);
		client = new KinectService.Client(protocol);
		image = new BufferedImage(
				640, 480, BufferedImage.TYPE_INT_BGR);
		listeners = new HashSet<FrameListener>();
	}
	
	public static final int playerIndexBitmask = 7;
	public static final int playerIndexBitmaskWidth = 3;
	
	// @see http://msdn.microsoft.com/en-us/library/hh973078.aspx#Depth_Ranges
	public static final short tooNearDepth = 0x0000;
	public static final short tooFarDepth = 0x0fff;
	public static final short unknownDepth = 0x1fff;
	
	public static void drawSkeleton(Graphics g, Frame frame, int x, int y) {
		if (frame != null
				&& frame.joints != null
				&& frame.joints.size() == 20) {
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
	
	private static void drawLine(Graphics g, int x, int y, Map<JointType, Joint> joints, JointType... keys) {
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
	
	public static int getPlayerIndex(short depthValue) {
		return depthValue & playerIndexBitmask;
	}
	
	public static int getRealDepth(short depthValue) {
		return depthValue >> playerIndexBitmaskWidth;
	}
	
	public synchronized boolean start() {
		try {
			transport.open();
		} catch (TTransportException e) {
			return false;
		}
		ScheduledExecutorService ses =
				Executors.newSingleThreadScheduledExecutor();
		future = ses.scheduleAtFixedRate(
				new FrameGrabber(), 0, 33, TimeUnit.MILLISECONDS);
		return true;
	}
	
	public synchronized void stop() {
		if (future != null) {
			future.cancel(true);
			transport.close();
			future = null;
		}
	}
	
	public synchronized boolean isStarted() {
		return future != null;
	}
	
	public synchronized void addFrameListener(FrameListener listener) {
		this.listeners.add(listener);
	}
	
	public synchronized boolean removeFrameListener(FrameListener listener) {
		return this.listeners.remove(listener);
	}
	
	public interface FrameListener {
		public void frameUpdated(Frame frame, BufferedImage image, short[] depthImageData);
	}
	
	private class FrameGrabber implements Runnable {
		private DataBufferInt colorImageBuffer;
		private ByteBuffer colorByteBuffer;
		private IntBuffer colorIntBuffer;
		private ByteBuffer depthByteBuffer;
		private ShortBuffer depthShortBuffer;

		public FrameGrabber() {
			colorImageBuffer = (DataBufferInt) image.getRaster().getDataBuffer();
			colorByteBuffer = ByteBuffer.allocate(640 * 480 * 4);
			colorIntBuffer = IntBuffer.wrap(colorImageBuffer.getData());
			depthByteBuffer = ByteBuffer.allocate(320 * 240 * 2);

			// C# server running on Windows converts short[] to byte[] with little-endian.
			// Therefore, we need to specify the endian-ness here to reconstruct it correctly.
			depthByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
			
			depthShortBuffer = ShortBuffer.allocate(320 * 240);
		}

		public void run() {
			try {
				synchronized (KinectServiceWrapper.this) {
					frame = client.getFrame();
				}
			} catch (TException e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						stop();
					}
				});
			}
			byte[] imageData = frame.getImage();
			colorByteBuffer.put((byte)0);
			colorByteBuffer.put(imageData, 0, imageData.length - 1);
			colorByteBuffer.rewind();
			synchronized (KinectServiceWrapper.this) {
				colorIntBuffer.put(colorByteBuffer.asIntBuffer());
				colorIntBuffer.rewind();
				if (frame.getDepthImage() != null) {
					depthByteBuffer.put(frame.getDepthImage());
					depthByteBuffer.rewind();
					depthShortBuffer.put(depthByteBuffer.asShortBuffer());
					depthShortBuffer.rewind();
					for (FrameListener listener : listeners) {
						listener.frameUpdated(frame, image, depthShortBuffer.array());
					}
				} else {
					for (FrameListener listener : listeners) {
						listener.frameUpdated(frame, image, null);
					}
				}
			}
		}
	}

	@Override
	public synchronized void setVoiceEnabled(boolean isEnabled) throws TException {
		client.setVoiceEnabled(isEnabled);
	}

	@Override
	public synchronized boolean isVoiceEnabled() throws TException {
		return client.isVoiceEnabled();
	}

	@Override
	public synchronized void addKeyword(String text) throws TException {
		client.addKeyword(text);
	}

	@Override
	public synchronized void removeKeyword(String text) throws TException {
		client.removeKeyword(text);
	}

	@Override
	public synchronized void setDepthEnabled(boolean isEnabled) throws TException {
		client.setDepthEnabled(isEnabled);
	}

	@Override
	public synchronized boolean isDepthEnabled() throws TException {
		return client.isDepthEnabled();
	}

	@Override
	public synchronized void setAngle(int angle) throws TException {
		client.setAngle(angle);
	}

	@Override
	public synchronized int getAngle() throws TException {
		return client.getAngle();
	}

	@Override
	public Frame getFrame() {
		return frame;
	}

	@Override
	public synchronized void shutdown() throws TException {
		client.shutdown();
	}
}
