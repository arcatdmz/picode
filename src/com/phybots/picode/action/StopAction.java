package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.builder.Launcher;

public class StopAction extends AbstractAction {
	private static final long serialVersionUID = 7556364882375393913L;
	private PicodeMain picodeMain;

	public StopAction(PicodeMain picodeMain) {
		this.picodeMain = picodeMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Launcher launcher = picodeMain.getLauncher();
		if (launcher != null) {
			launcher.close();
		}
	}
}
