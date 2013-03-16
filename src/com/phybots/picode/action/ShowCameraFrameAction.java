package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.api.Poser;
import com.phybots.picode.api.PoserLibrary;

public class ShowCameraFrameAction extends AbstractAction {
	private static final long serialVersionUID = -2297070480522312162L;

	public ShowCameraFrameAction() {
		putValue(NAME, "Capture");
		putValue(SHORT_DESCRIPTION, "Capture a new pose.");
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Poser currentPoser = PoserLibrary.getInstance().getCurrentPoser();
		System.out.println(currentPoser);
		if (currentPoser != null) {
			// TODO To be implemented.
			// currentPoser.showCaptureFrame(true);
		}
	}
}
