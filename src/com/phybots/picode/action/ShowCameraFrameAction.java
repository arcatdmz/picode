package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.api.Poser;
import com.phybots.picode.api.PoserManager;

public class ShowCameraFrameAction extends AbstractAction {
	private static final long serialVersionUID = -2297070480522312162L;

	@Override
	public void actionPerformed(ActionEvent arg0) {
		PoserManager poserManager = PoserManager.getInstance();
		Poser currentPoser = poserManager.getCurrentPoser();
		if (currentPoser != null) {
			currentPoser.showCaptureFrame();
		}
	}
}
