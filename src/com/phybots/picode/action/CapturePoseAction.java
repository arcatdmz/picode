package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.ui.pose.PoseLibrary;

public class CapturePoseAction extends AbstractAction {
	private static final long serialVersionUID = -4081128637015341817L;
	private PoseLibrary poseManager;

	public CapturePoseAction(PoseLibrary poseManager) {
		this.poseManager = poseManager;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		poseManager.capture();
	}
}
