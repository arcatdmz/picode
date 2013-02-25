package com.phybots.picode.api;

import com.phybots.picode.PicodeMain;

public abstract class PoserWithConnector extends Poser {

	public PoserWithConnector() {
		super();
	}

	public PoserWithConnector(PicodeMain picodeMain) {
		super(picodeMain);
	}

	public abstract void setConnector(String connector);
	public abstract String getConnector();
}
