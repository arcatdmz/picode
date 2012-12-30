package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.ui.PicodeFrame;

public class EditSelectedPoseNameAction extends AbstractAction {
	private static final long serialVersionUID = -2067117053119392794L;
	private transient PicodeFrame picodeFrame;

	public EditSelectedPoseNameAction(PicodeFrame picodeFrame) {
		this.picodeFrame = picodeFrame;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		picodeFrame.editSelectedPoseName();
	}
}
