package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.api.Pose;
import com.phybots.picode.ui.PoseLibraryPanel;

public class DeleteSelectedPoseAction extends AbstractAction {
	private static final long serialVersionUID = -6903324217701209464L;
	private transient PicodeMain picodeMain;

	public DeleteSelectedPoseAction(PicodeMain picodeMain) {
		this.picodeMain = picodeMain;
		putValue(NAME, "Delete");
		putValue(SHORT_DESCRIPTION, "Delete the selected pose.");
		putValue(SMALL_ICON, new ImageIcon(PoseLibraryPanel.class.getResource("/trashcan.png")));
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Pose pose = picodeMain.getFrame().getSelectedPose();
		if (pose != null) {
			pose.delete();
		}
	}

}
