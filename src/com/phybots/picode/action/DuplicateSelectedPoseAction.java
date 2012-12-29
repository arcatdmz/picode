package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.ui.PicodeFrame;

public class DuplicateSelectedPoseAction extends AbstractAction {
	private static final long serialVersionUID = -5297099138353745191L;
	private transient PicodeFrame robokoFrame;

	public DuplicateSelectedPoseAction(PicodeFrame robokoFrame) {
		this.robokoFrame = robokoFrame;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		robokoFrame.duplicateSelectedPose();
	}
}
