package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.PicodeMain;

public class EditSelectedPoseNameAction extends AbstractAction {
	private static final long serialVersionUID = -2067117053119392794L;
	private transient PicodeMain picodeMain;

	public EditSelectedPoseNameAction(PicodeMain picodeMain) {
		this.picodeMain = picodeMain;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		picodeMain.getFrame().editSelectedPoseName();
	}

}
