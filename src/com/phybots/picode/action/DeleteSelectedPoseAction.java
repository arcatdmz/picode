package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.api.Pose;

public class DeleteSelectedPoseAction extends AbstractAction {
	private static final long serialVersionUID = -6903324217701209464L;
	private transient PicodeMain picodeMain;

	public DeleteSelectedPoseAction(PicodeMain picodeMain) {
		this.picodeMain = picodeMain;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Pose pose = picodeMain.getFrame().getSelectedPose();
		picodeMain.getGlobalPoseLibrary().removePose(pose);
	}
}
