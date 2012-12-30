package com.phybots.picode.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import com.phybots.picode.ui.PicodeMain;

public class RenameFileAction extends AbstractAction {
	private static final long serialVersionUID = -301527980746520731L;
	private PicodeMain picodeMain;

	public RenameFileAction(PicodeMain picodeMain) {
		this.picodeMain = picodeMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		picodeMain.getSketch().handleRenameCode();
	}
}
