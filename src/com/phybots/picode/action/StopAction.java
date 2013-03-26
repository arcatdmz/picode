package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.ui.PicodeFrame;

public class StopAction extends AbstractAction {
	private static final long serialVersionUID = 7556364882375393913L;
	private PicodeMain picodeMain;

	public StopAction(PicodeMain picodeMain) {
		this.picodeMain = picodeMain;
		putValue(NAME, "Stop");
		putValue(SHORT_DESCRIPTION, "Stop the running sketch.");
		putValue(SMALL_ICON, new ImageIcon(PicodeFrame.class.getResource("/stop.png")));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		picodeMain.stopSketch();
	}
}
