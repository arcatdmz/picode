package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.ui.PicodeFrame;

public class EditSelectedPoseNameAction extends AbstractAction {
	private static final long serialVersionUID = -2067117053119392794L;
	private transient PicodeFrame robokoFrame;

	public EditSelectedPoseNameAction(PicodeFrame robokoFrame) {
		this.robokoFrame = robokoFrame;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		robokoFrame.editSelectedPoseName();
	}
}
