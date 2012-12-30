package com.phybots.picode.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import processing.app.SketchException;

import com.phybots.picode.builder.Builder;
import com.phybots.picode.ui.PicodeMain;

public class RunAction extends AbstractAction {
	private static final long serialVersionUID = -8531811907562726754L;
	private PicodeMain picodeMain;

	public RunAction(PicodeMain picodeMain) {
		this.picodeMain = picodeMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (picodeMain.getLauncher() != null) {
			new StopAction(picodeMain).actionPerformed(e);
		}
		Builder builder = new Builder(picodeMain, picodeMain.getSketch());
		try {
			picodeMain.getRobot().disconnect();
			picodeMain.setLauncher(builder.run());
		} catch (SketchException se) {
			picodeMain.getPintegration().statusError(se);
			picodeMain.getRobot().connect();
		}
	}
}
