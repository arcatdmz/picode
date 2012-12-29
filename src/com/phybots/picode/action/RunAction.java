package com.phybots.picode.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import processing.app.SketchException;

import com.phybots.picode.builder.Builder;
import com.phybots.picode.ui.PicodeMain;

public class RunAction extends AbstractAction {
	private static final long serialVersionUID = -8531811907562726754L;
	private PicodeMain robokoMain;

	public RunAction(PicodeMain robokoMain) {
		this.robokoMain = robokoMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (robokoMain.getLauncher() != null) {
			new StopAction(robokoMain).actionPerformed(e);
		}
		Builder builder = new Builder(robokoMain, robokoMain.getSketch());
		try {
			robokoMain.getRobot().disconnect();
			robokoMain.setLauncher(builder.run());
		} catch (SketchException se) {
			robokoMain.handleSketchException(se);
			robokoMain.getRobot().connect();
		}
	}
}
