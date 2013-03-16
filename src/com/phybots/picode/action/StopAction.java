package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.PicodeMain;

public class StopAction extends AbstractAction {
	private static final long serialVersionUID = 7556364882375393913L;
	private PicodeMain picodeMain;

	public StopAction(PicodeMain picodeMain) {
		this.picodeMain = picodeMain;
		putValue(NAME, "Stop");
		putValue(SHORT_DESCRIPTION, "Stop the running sketch.");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		picodeMain.stopSketch();
	}
}
