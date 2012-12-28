package jp.digitalmuseum.roboko.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jp.digitalmuseum.roboko.ui.RobokoFrame;

public class ApplySelectedPoseAction extends AbstractAction {
	private static final long serialVersionUID = -5534990587199854319L;
	private transient RobokoFrame robokoFrame;

	public ApplySelectedPoseAction(RobokoFrame robokoFrame) {
		this.robokoFrame = robokoFrame;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		robokoFrame.applySelectedPose();
	}
}
