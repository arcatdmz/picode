package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.builder.Launcher;
import com.phybots.picode.ui.PicodeMain;

public class StopAction extends AbstractAction {
	private static final long serialVersionUID = 7556364882375393913L;
	private PicodeMain robokoMain;

	public StopAction(PicodeMain robokoMain) {
		this.robokoMain = robokoMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Launcher launcher = robokoMain.getLauncher();
		if (launcher != null) {
			launcher.close();
		}
	}
}
