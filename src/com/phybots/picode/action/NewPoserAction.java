package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.api.PoserInfo;
import com.phybots.picode.api.PoserLibrary;
import com.phybots.picode.ui.dialog.NewPoserDialog;

public class NewPoserAction extends AbstractAction {
	private static final long serialVersionUID = 4418251293922946830L;
	private PicodeMain picodeMain;

	public NewPoserAction(PicodeMain picodeMain) {
		this.picodeMain = picodeMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		NewPoserDialog dialog = new NewPoserDialog() {
			private static final long serialVersionUID = -5061323386607834944L;

			@Override
			public void actionPerformed(ActionEvent e) {
				String command = e.getActionCommand();
				if (command.equals("OK")) {
					PoserInfo poserInfo = contentPanel.getPoserInfo();
					PoserLibrary.getInstance().newPoserInstance(poserInfo);
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
