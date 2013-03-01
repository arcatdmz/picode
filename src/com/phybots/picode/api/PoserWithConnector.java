package com.phybots.picode.api;

public abstract class PoserWithConnector extends Poser {
	public abstract void setConnector(String connector);
	public abstract String getConnector();

	@Override
	public String getIdentifier() {
		return String.format("%s%s%s", super.getIdentifier(), Poser.identifierSeparator, getConnector());
	}

}
