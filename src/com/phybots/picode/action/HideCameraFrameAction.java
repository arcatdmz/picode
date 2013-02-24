package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.PicodeMain;

public class HideCameraFrameAction extends AbstractAction {
	private static final long serialVersionUID = -2297070480522312162L;
	private transient PicodeMain picodeMain;

	public HideCameraFrameAction(PicodeMain picodeMain) {
		this.picodeMain = picodeMain;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		//picodeMain.showCaptureFrame(false);
		//TODO
	}
}
