package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.api.Poser;
import com.phybots.picode.api.PoserLibrary;

public class CapturePoseAction extends AbstractAction {
	private static final long serialVersionUID = -4081128637015341817L;

	public CapturePoseAction() {
		putValue(NAME, "Capture");
		putValue(SHORT_DESCRIPTION, "Capture the current pose.");
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Poser poser = PoserLibrary.getInstance().getCurrentPoser();
		if (poser != null) {
			poser.capture();
		}
	}
}
