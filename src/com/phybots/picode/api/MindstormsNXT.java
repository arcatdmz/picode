package com.phybots.picode.api;

import jp.digitalmuseum.connector.ConnectorFactory;

import com.phybots.entity.MindstormsNXT.Port;
import com.phybots.picode.PicodeMain;
import com.phybots.picode.camera.NormalCamera;

public class MindstormsNXT extends PoserWithConnector {
	private com.phybots.entity.MindstormsNXT nxt;

	public MindstormsNXT() {
		super();
		initialize();
	}
	
	public MindstormsNXT(PicodeMain picodeMain) {
		super(picodeMain);
		initialize();
	}
	
	private void initialize() {
		nxt = new com.phybots.entity.MindstormsNXT();
		nxt.removeDifferentialWheels();
		nxt.addExtension("MindstormsNXTExtension", Port.A);
		nxt.addExtension("MindstormsNXTExtension", Port.B);
		nxt.addExtension("MindstormsNXTExtension", Port.C);
		camera = new NormalCamera();
		motorManager = new MindstormsNXTMotorManager(picodeMain, this);
	}
	
	@Override
	public void dispose() {
	}

	@Override
	public Pose newPoseInstance() {
		return new MindstormsNXTPose();
	}

	@Override
	public void setConnector(String connector) {
		nxt.setConnector(ConnectorFactory.makeConnector(connector));
	}

	@Override
	public String getConnector() {
		// TODO Auto-generated method stub
		return null;
	}

}
