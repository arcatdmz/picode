package com.phybots.picode.api;

import jp.digitalmuseum.connector.ConnectorFactory;
import jp.digitalmuseum.connector.FantomConnector;

import com.phybots.entity.MindstormsNXT.Port;
import com.phybots.entity.PhysicalRobotAbstractImpl;
import com.phybots.picode.api.remote.MindstormsNXTServer;
import com.phybots.picode.camera.Camera;
import com.phybots.picode.camera.KinectCamera;
import com.phybots.picode.camera.NormalCamera;

public class MindstormsNXT extends PoserWithConnector {
	com.phybots.entity.MindstormsNXT raw;

	@Override
	protected void initialize() {
		PhysicalRobotAbstractImpl.isAutoConnectEnabled = false;
		raw = new com.phybots.entity.MindstormsNXT();
		raw.removeDifferentialWheels();
		raw.addExtension("MindstormsNXTExtension", Port.A);
		raw.addExtension("MindstormsNXTExtension", Port.B);
		raw.addExtension("MindstormsNXTExtension", Port.C);
		if (PoserLibrary.getInstance().isWithIDE()) {
			motorManager = new MindstormsNXTMotorManager(this);
			MindstormsNXTServer.getInstance().register(this);
		} else {
			motorManager = new MindstormsNXTRemoteMotorManager(this);
		}
	}

	@Override
	public void setConnector(String connector) {
		try {
			raw.setConnector(ConnectorFactory.makeConnector(connector));
		} catch (InstantiationError e) {
			// This happens when there's no compatible jfantom native library.
			System.err.println(String.format("failed to set connector: %s", e.getMessage()));
		} catch (Exception e) {
			// Do nothing.
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

		// Do nothing if this brick is already connected.
		if (raw.isConnected()) {
			return true;
		}

		// If no connector is specified, use the first USB connection by default.
		if (raw.getConnector() == null) {
			try {
				String[] ids = FantomConnector.queryIdentifiers();
				if (ids != null) {
					setConnector(ids[0]);
				}
			} catch (InstantiationError e) {
				// This happens when there's no compatible jfantom native library.
				System.err.println(String.format("failed to set connector: %s", e.getMessage()));
			} catch (Exception e) {
				// Do nothing.
			}
		}

		// Try to connect to the remote NXT brick.
		if (motorManager instanceof MindstormsNXTRemoteMotorManager) {
			if (((MindstormsNXTRemoteMotorManager) motorManager).connect()) {
				motorManager.start();
				return true;
			}

			// Give up remote connection.
			motorManager = new MindstormsNXTMotorManager(this);
		}

		// Try to connect to the local NXT brick.
		boolean connected = raw.connect();
		if (connected) {
			motorManager.start();
		}
		return connected;
	}

	@Override
	public void disconnect() {
		motorManager.stop();
		if (motorManager instanceof MindstormsNXTMotorManager) {
			raw.disconnect();
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		if (PoserLibrary.getInstance().isWithIDE()) {
			MindstormsNXTServer.getInstance().unregister(this);
		}
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
