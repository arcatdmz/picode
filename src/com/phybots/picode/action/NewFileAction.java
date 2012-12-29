package com.phybots.picode.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import com.phybots.picode.ui.PicodeMain;

public class NewFileAction extends AbstractAction {
	private static final long serialVersionUID = -5886426071833985789L;
	private PicodeMain robokoMain;

	public NewFileAction(PicodeMain robokoMain) {
		this.robokoMain = robokoMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		robokoMain.getSketch().handleNewCode();
	}
}
