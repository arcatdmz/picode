package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.ui.PicodeFrame;

public class DuplicateSelectedPoseAction extends AbstractAction {
	private static final long serialVersionUID = -5297099138353745191L;
	private transient PicodeFrame picodeFrame;

	public DuplicateSelectedPoseAction(PicodeFrame picodeFrame) {
		this.picodeFrame = picodeFrame;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		picodeFrame.duplicateSelectedPose();
	}
}
