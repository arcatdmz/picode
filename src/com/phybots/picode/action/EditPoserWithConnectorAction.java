package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.api.Poser;
import com.phybots.picode.api.PoserInfo;
import com.phybots.picode.api.PoserLibrary;
import com.phybots.picode.api.PoserWithConnector;
import com.phybots.picode.ui.dialog.PoserEditDialog;

public class EditPoserWithConnectorAction extends AbstractAction {
	private static final long serialVersionUID = 4418251293922946830L;
	private PicodeMain picodeMain;

	public EditPoserWithConnectorAction(PicodeMain picodeMain) {
		this.picodeMain = picodeMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Poser poser = PoserLibrary.getInstance().getCurrentPoser();
		if (!(poser instanceof PoserWithConnector)) {
			return;
		}

		final PoserWithConnector p = (PoserWithConnector) poser;
		PoserEditDialog dialog = new PoserEditDialog() {
			private static final long serialVersionUID = -5061323386607834944L;

			@Override
			protected PoserInfo getOriginalPoserInfo() {
				return p.getInfo();
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				String command = e.getActionCommand();
				if (command.equals("OK")) {
					PoserInfo poserInfo = contentPanel.getPoserInfo();
					p.setName(poserInfo.name);
					p.setConnector(poserInfo.connector);
				} else if (command.equals("Remove")) {
					p.dispose();
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
