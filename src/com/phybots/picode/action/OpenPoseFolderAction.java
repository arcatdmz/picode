package com.phybots.picode.action;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;

import com.phybots.picode.PicodeSettings;
import com.phybots.picode.ProcessingIntegration;

public class OpenPoseFolderAction extends AbstractAction {
	private static final long serialVersionUID = 2092347018925625257L;

	@Override
	public void actionPerformed(ActionEvent arg0) {
		try {
			ProcessingIntegration.getPlatform().openFolder(
					new File(PicodeSettings.getPoseFolderPath()));
		} catch (Exception e) {
			// Do nothing.
		}
	}
}
