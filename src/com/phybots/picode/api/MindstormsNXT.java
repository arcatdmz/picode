package com.phybots.picode.api;

import com.phybots.entity.MindstormsNXT.Port;
import com.phybots.picode.PicodeMain;

public class MindstormsNXT extends Poser {
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
	}
	
	@Override
	public void dispose() {
	}

}
