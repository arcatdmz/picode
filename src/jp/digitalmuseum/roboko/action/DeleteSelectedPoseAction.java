package jp.digitalmuseum.roboko.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jp.digitalmuseum.roboko.ui.RobokoFrame;

public class DeleteSelectedPoseAction extends AbstractAction {
	private static final long serialVersionUID = -6903324217701209464L;
	private transient RobokoFrame robokoFrame;

	public DeleteSelectedPoseAction(RobokoFrame robokoFrame) {
		this.robokoFrame = robokoFrame;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		robokoFrame.removeSelectedPose();
	}
}
