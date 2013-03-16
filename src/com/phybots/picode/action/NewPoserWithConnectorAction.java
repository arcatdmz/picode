package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.api.PoserInfo;
import com.phybots.picode.api.PoserLibrary;
import com.phybots.picode.ui.dialog.PoserEditDialog;

public class NewPoserWithConnectorAction extends AbstractAction {
	private static final long serialVersionUID = -3802099629457402372L;
	private PicodeMain picodeMain;

	public NewPoserWithConnectorAction(PicodeMain picodeMain) {
		this.picodeMain = picodeMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		PoserEditDialog dialog = new PoserEditDialog() {
			private static final long serialVersionUID = -3431634414171881257L;

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
