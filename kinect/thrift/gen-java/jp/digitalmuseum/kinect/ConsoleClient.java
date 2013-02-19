package jp.digitalmuseum.kinect;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class ConsoleClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TTransport transport = new TSocket("localhost", 9090);
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
		} catch (TException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		transport.close();
	}

}
