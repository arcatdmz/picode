package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.ui.PicodeFrame;

public class ApplySelectedPoseAction extends AbstractAction {
	private static final long serialVersionUID = -5534990587199854319L;
	private transient PicodeFrame picodeFrame;

	public ApplySelectedPoseAction(PicodeFrame picodeFrame) {
		this.picodeFrame = picodeFrame;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		picodeFrame.applySelectedPose();
	}
}
