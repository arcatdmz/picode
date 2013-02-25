package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.api.Poser;
import com.phybots.picode.api.PoserManager;
import com.phybots.picode.api.PoserManager.PoserInfo;
import com.phybots.picode.ui.dialog.NewRobotDialog;

public class NewRobotAction extends AbstractAction {
	private static final long serialVersionUID = 4418251293922946830L;
	private PicodeMain picodeMain;

	public NewRobotAction(PicodeMain picodeMain) {
		this.picodeMain = picodeMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		NewRobotDialog dialog = new NewRobotDialog() {
			private static final long serialVersionUID = -5061323386607834944L;

			@Override
			public void actionPerformed(ActionEvent e) {
				String command = e.getActionCommand();
				if (command.equals("OK")) {
					PoserInfo poserInfo = contentPanel.getPoserInfo();
					Poser poser = picodeMain.getPoserManager().newPoserInstance(poserInfo);
					if (poser != null) {
						picodeMain.getPoserManager().addPoser(poser);
					}
				}
				picodeMain.getFrame().setEnabled(true);
				setVisible(false);
				dispose();
			}
		};
		picodeMain.getFrame().setEnabled(false);
		dialog.setVisible(true);
	}
}
