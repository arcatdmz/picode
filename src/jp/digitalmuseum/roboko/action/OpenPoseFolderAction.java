package jp.digitalmuseum.roboko.action;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;

import jp.digitalmuseum.roboko.ProcessingIntegration;
import jp.digitalmuseum.roboko.RobokoSettings;

public class OpenPoseFolderAction extends AbstractAction {
	private static final long serialVersionUID = 2092347018925625257L;

	@Override
	public void actionPerformed(ActionEvent arg0) {
		try {
			ProcessingIntegration.getPlatform().openFolder(
					new File(RobokoSettings.getPoseFolderPath()));
		} catch (Exception e) {
			// Do nothing.
		}
	}
}
