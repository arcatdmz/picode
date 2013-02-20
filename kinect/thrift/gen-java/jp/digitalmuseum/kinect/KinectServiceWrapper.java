package jp.digitalmuseum.kinect;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashSet;
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
		public void frameUpdated(Frame frame, BufferedImage image);
	}
	
	private class FrameGrabber implements Runnable {
		private DataBufferInt dataBuffer;
		private IntBuffer intBuffer;
		private ByteBuffer byteBuffer;

		public FrameGrabber() {
			dataBuffer = (DataBufferInt) image.getRaster().getDataBuffer();
			intBuffer = IntBuffer.wrap(dataBuffer.getData());
			byteBuffer = ByteBuffer.allocate(640*480*4);
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
			byteBuffer.put((byte)0);
			byteBuffer.put(imageData, 0, imageData.length - 1);
			byteBuffer.rewind();
			synchronized (image) {
				intBuffer.put(byteBuffer.asIntBuffer());
				intBuffer.rewind();
			}
			synchronized (KinectServiceWrapper.this) {
				for (FrameListener listener : listeners) {
					listener.frameUpdated(frame, image);
				}
			}
		}
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
	public synchronized Set<String> getKeywords() throws TException {
		return client.getKeywords();
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
