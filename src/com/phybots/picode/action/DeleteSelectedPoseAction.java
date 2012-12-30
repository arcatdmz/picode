package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.ui.PicodeFrame;

public class DeleteSelectedPoseAction extends AbstractAction {
	private static final long serialVersionUID = -6903324217701209464L;
	private transient PicodeFrame picodeFrame;

	public DeleteSelectedPoseAction(PicodeFrame picodeFrame) {
		this.picodeFrame = picodeFrame;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		picodeFrame.removeSelectedPose();
	}
}
