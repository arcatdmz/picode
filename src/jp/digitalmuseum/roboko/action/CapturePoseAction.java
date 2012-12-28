package jp.digitalmuseum.roboko.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jp.digitalmuseum.roboko.ui.library.PoseManager;

public class CapturePoseAction extends AbstractAction {
	private static final long serialVersionUID = -4081128637015341817L;
	private PoseManager poseManager;

	public CapturePoseAction(PoseManager poseManager) {
		this.poseManager = poseManager;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		poseManager.capture();
	}
}
