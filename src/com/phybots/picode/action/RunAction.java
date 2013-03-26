package com.phybots.picode.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.ui.PicodeFrame;

public class RunAction extends AbstractAction {
	private static final long serialVersionUID = -8531811907562726754L;
	private PicodeMain picodeMain;

	public RunAction(PicodeMain picodeMain) {
		this.picodeMain = picodeMain;
		putValue(NAME, "Run");
		putValue(SHORT_DESCRIPTION, "Run the current sketch.");
		putValue(SMALL_ICON, new ImageIcon(PicodeFrame.class.getResource("/run.png")));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		picodeMain.runSketch();
	}
}
