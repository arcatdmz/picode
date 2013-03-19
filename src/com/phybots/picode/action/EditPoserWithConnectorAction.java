package com.phybots.picode.action;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.api.Poser;
import com.phybots.picode.api.PoserInfo;
import com.phybots.picode.api.PoserLibrary;
import com.phybots.picode.api.PoserTypeInfo;
import com.phybots.picode.api.PoserWithConnector;
import com.phybots.picode.ui.dialog.PoserEditDialog;

public class EditPoserWithConnectorAction extends AbstractAction {
	private static final long serialVersionUID = 4418251293922946830L;
	private PicodeMain picodeMain;
	private boolean isNew;

	public EditPoserWithConnectorAction(PicodeMain picodeMain) {
		this(picodeMain, false);
	}

	public EditPoserWithConnectorAction(PicodeMain picodeMain, boolean isNew) {
		this.picodeMain = picodeMain;
		this.isNew = isNew;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Poser poser = PoserLibrary.getInstance().getCurrentPoser();
		if (!isNew && !(poser instanceof PoserWithConnector)) {
			return;
		}

		// Show poser types with connector support.
		Set<PoserTypeInfo> poserTypes = PoserLibrary.getTypeInfos();
		for (Iterator<PoserTypeInfo> it = poserTypes.iterator();
				it.hasNext();) {
			PoserTypeInfo poserType = it.next();
			if (!poserType.supportsConnector) {
				it.remove();
			}
		}

		final PoserWithConnector p = isNew ? null : (PoserWithConnector) poser;
		PoserEditDialog dialog = new PoserEditDialog(poserTypes) {
			private static final long serialVersionUID = -5061323386607834944L;

			@Override
			protected PoserInfo getOriginalPoserInfo() {
				return p == null ? null : p.getInfo();
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				String command = e.getActionCommand();
				if (command.equals("OK")) {
					if (!handleOk()) {
						return;
					}
				} else if (command.equals("Remove")) {
					if (!handleRemove()) {
						return;
					}
				} // else {} // Cancelled.
				picodeMain.getFrame().setEnabled(true);
				setVisible(false);
				dispose();
			}

			private boolean handleOk() {
				PoserInfo poserInfo = contentPanel.getPoserInfo();
				if (getConnectorManager().exists(poserInfo.connector)) {
					if (p == null
							|| !p.getConnector().equals(poserInfo.connector)) {
						JOptionPane.showMessageDialog(this,
								"This connector is used by another poser.",
								"",
								JOptionPane.WARNING_MESSAGE);
						return false;
					}
				} else if (!testConnector()) {
					JOptionPane.showMessageDialog(this,
							"This connector is unreachable.",
							"",
							JOptionPane.WARNING_MESSAGE);
					return false;
				}
				if (p != null) {
					p.disconnect();
					p.setName(poserInfo.name);
					p.setConnector(poserInfo.connector);
					p.connect();
				} else {
					PoserLibrary.getInstance().newPoserInstance(poserInfo);
				}
				return true;
			}

			private boolean handleRemove() {
				p.dispose();
				return true;
			}
		};

		picodeMain.getFrame().setEnabled(false);
		dialog.setTitle(isNew ? "New poser" : "Edit poser info");
		dialog.setVisible(true);
	}

}
