package com.phybots.picode.action;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;

import com.phybots.picode.ui.PicodeMain;

public class SaveSketchAction extends AbstractAction {
	private static final long serialVersionUID = 3777538819909312326L;
	private PicodeMain robokoMain;

	public SaveSketchAction(PicodeMain robokoMain) {
		this.robokoMain = robokoMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			robokoMain.getSketch().save();
		} catch (IOException e1) {
			// 
		}
	}
}
