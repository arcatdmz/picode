package com.phybots.picode.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import com.phybots.picode.ui.PicodeMain;

public class DeleteFileAction extends AbstractAction {
	private static final long serialVersionUID = 8648208348407144085L;
	private PicodeMain picodeMain;

	public DeleteFileAction(PicodeMain picodeMain) {
		this.picodeMain = picodeMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		picodeMain.getSketch().handleDeleteCode();
	}
}
