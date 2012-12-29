package com.phybots.picode.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import com.phybots.picode.ui.PicodeMain;

public class RenameFileAction extends AbstractAction {
	private static final long serialVersionUID = -301527980746520731L;
	private PicodeMain robokoMain;

	public RenameFileAction(PicodeMain robokoMain) {
		this.robokoMain = robokoMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		robokoMain.getSketch().handleRenameCode();
	}
}
