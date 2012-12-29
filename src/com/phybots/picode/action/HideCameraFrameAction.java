package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.ui.PicodeMain;

public class HideCameraFrameAction extends AbstractAction {
	private static final long serialVersionUID = -2297070480522312162L;
	private transient PicodeMain robokoMain;

	public HideCameraFrameAction(PicodeMain robokoMain) {
		this.robokoMain = robokoMain;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		robokoMain.showCaptureFrame(false);
	}
}
