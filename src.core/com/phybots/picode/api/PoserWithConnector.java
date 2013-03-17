package com.phybots.picode.api;

public abstract class PoserWithConnector extends Poser {
	public abstract void setConnector(String connector);
	public abstract String getConnector();
	public abstract boolean connect();
	public abstract void disconnect();

	@Override
	public void dispose() {
		disconnect();
		super.dispose();
	}

	@Override
	public String getIdentifier() {
		return String.format("%s%s%s", super.getIdentifier(), Poser.identifierSeparator, getConnector());
	}

	@Override
	public PoserInfo getInfo() {
		PoserInfo poserInfo = super.getInfo();
		poserInfo.connector = getConnector();
		return poserInfo;
	}

}
