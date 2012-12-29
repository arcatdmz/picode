package com.phybots.picode.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import processing.app.RobokoSketch;

import com.phybots.picode.ui.PicodeMain;

public class NewSketchAction extends AbstractAction {
	private static final long serialVersionUID = 2044354384275000188L;
	private PicodeMain robokoMain;

	public NewSketchAction(PicodeMain robokoMain) {
		this.robokoMain = robokoMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		RobokoSketch robokoSketch = RobokoSketch.newInstance(robokoMain);
		if (robokoSketch != null) {
			robokoMain.setSketch(robokoSketch);
		}
	}
}
