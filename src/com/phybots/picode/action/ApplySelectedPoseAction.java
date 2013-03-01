package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.api.Pose;
import com.phybots.picode.api.Poser;
import com.phybots.picode.api.PoserLibrary;

public class ApplySelectedPoseAction extends AbstractAction {
	private static final long serialVersionUID = -5534990587199854319L;
	private transient PicodeMain picodeMain;

	public ApplySelectedPoseAction(PicodeMain picodeMain) {
		this.picodeMain = picodeMain;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Pose pose = picodeMain.getFrame().getSelectedPose();
		Poser poser = PoserLibrary.getInstance().getCurrentPoser();
		poser.getMotorManager().setPose(pose);
	}

}
