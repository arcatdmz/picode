package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import com.phybots.picode.api.Poser;
import com.phybots.picode.api.PoserLibrary;
import com.phybots.picode.ui.PoseLibraryPanel;

public class ShowCameraFrameAction extends AbstractAction {
	private static final long serialVersionUID = -2297070480522312162L;

	public ShowCameraFrameAction() {
		putValue(NAME, "Capture");
		putValue(SHORT_DESCRIPTION, "Capture a new pose.");
		putValue(SMALL_ICON, new ImageIcon(PoseLibraryPanel.class.getResource("/camera.png")));
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Poser currentPoser = PoserLibrary.getInstance().getCurrentPoser();
		if (currentPoser != null) {
			currentPoser.showCaptureFrame(true);
		}
	}
}
