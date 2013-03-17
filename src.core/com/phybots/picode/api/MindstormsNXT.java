package com.phybots.picode.api;

import jp.digitalmuseum.connector.ConnectorFactory;

import com.phybots.entity.MindstormsNXT.Port;
import com.phybots.picode.camera.Camera;
import com.phybots.picode.camera.KinectCamera;
import com.phybots.picode.camera.NormalCamera;

public class MindstormsNXT extends PoserWithConnector {
	com.phybots.entity.MindstormsNXT raw;

	@Override
	protected void initialize() {
		raw = new com.phybots.entity.MindstormsNXT();
		raw.removeDifferentialWheels();
		raw.addExtension("MindstormsNXTExtension", Port.A);
		raw.addExtension("MindstormsNXTExtension", Port.B);
		raw.addExtension("MindstormsNXTExtension", Port.C);
		motorManager = new MindstormsNXTMotorManager(this);
	}

	@Override
	public void setConnector(String connector) {
		try {
			raw.setConnector(ConnectorFactory.makeConnector(connector));
		} catch (Exception e) {
			// Do nothing.
			e.printStackTrace();
		}
	}

	@Override
	public String getConnector() {
		if (raw.getConnector() == null) {
			return "";
		}
		return raw.getConnector().getConnectionString();
	}

	@Override
	public boolean connect() {
		boolean connected = raw.connect();
		if (connected) {
			getMotorManager().start();
		}
		return connected;
	}

	@Override
	public void disconnect() {
		getMotorManager().stop();
		raw.disconnect();
	}

	public static Class<? extends Pose> getPoseClass() {
		return MindstormsNXTPose.class;
	}

	public static Class<? extends Camera> getCameraClass() {
		return NormalCamera.class;
	}

	public static Class<? extends Camera> getSecondaryCameraClass() {
		return KinectCamera.class;
	}

}
