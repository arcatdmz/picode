package com.phybots.picode.api;

import jp.digitalmuseum.connector.ConnectorFactory;

import com.phybots.entity.MindstormsNXT.Port;
import com.phybots.picode.camera.Camera;
import com.phybots.picode.camera.KinectCamera;
import com.phybots.picode.camera.NormalCamera;

public class MindstormsNXT extends PoserWithConnector {
	private com.phybots.entity.MindstormsNXT nxt;

	@Override
	protected void initialize() {
		nxt = new com.phybots.entity.MindstormsNXT();
		nxt.removeDifferentialWheels();
		nxt.addExtension("MindstormsNXTExtension", Port.A);
		nxt.addExtension("MindstormsNXTExtension", Port.B);
		nxt.addExtension("MindstormsNXTExtension", Port.C);
		motorManager = new MindstormsNXTMotorManager(this);
	}

	@Override
	public void setConnector(String connector) {
		try {
			nxt.setConnector(ConnectorFactory.makeConnector(connector));
		} catch (Exception e) {
			// Do nothing.
		}
	}

	@Override
	public String getConnector() {
		if (nxt.getConnector() == null) {
			return "";
		}
		return nxt.getConnector().getConnectionString();
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
