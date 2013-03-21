package com.phybots.picode.api;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.phybots.picode.api.remote.MindstormsNXTPoseData;
import com.phybots.picode.api.remote.MindstormsNXTService;
import com.phybots.picode.api.remote.MindstormsNXTServiceConstants;

public class MindstormsNXTRemoteMotorManager extends MotorManager {
	private TTransport transport;
	private MindstormsNXTService.Client client;
	private int id;

	public MindstormsNXTRemoteMotorManager(Poser poser) {
		this(poser, "127.0.0.1", MindstormsNXTServiceConstants.SERVER_DEFAULT_PORT);
	}

	public MindstormsNXTRemoteMotorManager(Poser poser, String host, int port) {
		this(poser, host, port, 300);
	}

	public MindstormsNXTRemoteMotorManager(Poser poser, String host, int port, int timeout) {
		super(poser);
		transport = new TSocket(host, port, timeout);
		TProtocol protocol = new TBinaryProtocol(transport);
		client = new MindstormsNXTService.Client(protocol);
	}

	public boolean connect() {
		try {
			transport.open();
		} catch (TTransportException e) {
			return false;
		}
		try {
			System.out.println("client: query " + getPoser().getIdentifier());
			id = client.connect(getPoser().getIdentifier());
		} catch (TException e) {
			return false;
		}
		System.out.println("client: connected " + id);
		return id >= 0;
	}

	@Override
	public boolean start() {
		if (!transport.isOpen()) {
			try {
				transport.open();
			} catch (TTransportException e) {
				return false;
			}
		}
		return id >= 0;
	}

	@Override
	public void stop() {
		transport.close();
	}

	@Override
	public Pose getPose() {
		Poser poser = getPoser();
		MindstormsNXTPose pose = new MindstormsNXTPose();
		pose.setPoserIdentifier(poser.getIdentifier());
		pose.setPoserType(poser.getPoserType());
		try {
			if (start()) {
				MindstormsNXTPoseData data = client.getPose(id);
				pose.importData(new int[] {data.a, data.b, data.c});
			}
		} catch (TException e) {
		}
		return pose;
	}

	@Override
	public boolean setPose(Pose pose) {
		if (!(pose instanceof MindstormsNXTPose)) {
			return false;
		}
		MindstormsNXTPoseData data = new MindstormsNXTPoseData();
		int[] rawData = ((MindstormsNXTPose) pose).getData();
		data.a = rawData[0];
		data.b = rawData[1];
		data.c = rawData[2];
		try {
			return start() && client.setPose(id, data);
		} catch (TException e) {
			return false;
		}
	}

	@Override
	public boolean isActing() {
		try {
			return start() && client.isActing(id);
		} catch (TException e) {
			return false;
		}
	}

	@Override
	public void reset() {
		try {
			if (start()) {
				client.reset(id);
			}
		} catch (TException e) {
			// Do nothing.
		}
	}

}
