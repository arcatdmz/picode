package com.phybots.picode.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import com.phybots.picode.PicodeMain;

public class NewFileAction extends AbstractAction {
	private static final long serialVersionUID = -5886426071833985789L;
	private PicodeMain picodeMain;

	public NewFileAction(PicodeMain picodeMain) {
		this.picodeMain = picodeMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		picodeMain.getSketch().handleNewCode();
	}
}
