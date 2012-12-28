package jp.digitalmuseum.roboko.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jp.digitalmuseum.roboko.ui.RobokoFrame;

public class DuplicateSelectedPoseAction extends AbstractAction {
	private static final long serialVersionUID = -5297099138353745191L;
	private transient RobokoFrame robokoFrame;

	public DuplicateSelectedPoseAction(RobokoFrame robokoFrame) {
		this.robokoFrame = robokoFrame;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		robokoFrame.duplicateSelectedPose();
	}
}
