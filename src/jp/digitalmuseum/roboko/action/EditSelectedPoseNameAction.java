package jp.digitalmuseum.roboko.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jp.digitalmuseum.roboko.ui.RobokoFrame;

public class EditSelectedPoseNameAction extends AbstractAction {
	private static final long serialVersionUID = -2067117053119392794L;
	private transient RobokoFrame robokoFrame;

	public EditSelectedPoseNameAction(RobokoFrame robokoFrame) {
		this.robokoFrame = robokoFrame;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		robokoFrame.editSelectedPoseName();
	}
}
