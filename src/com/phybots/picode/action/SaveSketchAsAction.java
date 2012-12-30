package com.phybots.picode.action;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;

import com.phybots.picode.ui.PicodeMain;

public class SaveSketchAsAction extends AbstractAction {
	private static final long serialVersionUID = -6036135232665670857L;
	private PicodeMain picodeMain;

	public SaveSketchAsAction(PicodeMain picodeMain) {
		this.picodeMain = picodeMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			picodeMain.getSketch().saveAs();
		} catch (IOException e1) {
			// 
		}
	}
}
