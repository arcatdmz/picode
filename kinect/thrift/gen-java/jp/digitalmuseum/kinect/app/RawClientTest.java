package jp.digitalmuseum.kinect.app;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import jp.digitalmuseum.kinect.Frame;
import jp.digitalmuseum.kinect.KinectService;
import jp.digitalmuseum.kinect.KinectServiceConstants;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class RawClientTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final BufferedImage image = new BufferedImage(640, 480, BufferedImage.TYPE_INT_BGR);
		DataBufferInt dataBuffer = (DataBufferInt) image.getRaster().getDataBuffer();
		IntBuffer intBuffer = IntBuffer.wrap(dataBuffer.getData());
		final ByteBuffer byteBuffer = ByteBuffer.allocate(640*480*4);

		final JPanel panel = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(image,
						(getWidth() - image.getWidth())/2,
						(getHeight() - image.getHeight())/2,
						null);
			}
		};
		panel.setPreferredSize(new Dimension(640, 480));
		
		final JFrame window = new JFrame();
		window.add(panel);
		window.pack();
		
		TTransport transport = new TSocket("localhost", KinectServiceConstants.SERVER_DEFAULT_PORT);
		TProtocol protocol = new TBinaryProtocol(transport);
		KinectService.Client client = new KinectService.Client(protocol);

		try {
			transport.open();
		} catch (TTransportException e) {
			e.printStackTrace();
		}

		try {
			int angle = client.getAngle();
			client.setAngle(-10);
			for (int i = 0; i < 100; i ++) {
					System.out.println(String.format("angle: %d", client.getAngle()));
				if (i == 50) {
					client.setAngle(10);
				}
				Thread.sleep(100);
			}
			client.setAngle(angle);

			window.setVisible(true);
			for (int i = 0; i < 300; i ++) {
				Frame frame = client.getFrame();
				byte[] imageData = frame.getImage();
				byteBuffer.put((byte)0);
				byteBuffer.put(imageData, 0, imageData.length - 1);
				byteBuffer.rewind();
				intBuffer.put(byteBuffer.asIntBuffer());
				intBuffer.rewind();
				panel.repaint();
				Thread.sleep(33);
			}
			
		} catch (TException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		transport.close();
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				window.dispose();
			}
		});
	}

}
