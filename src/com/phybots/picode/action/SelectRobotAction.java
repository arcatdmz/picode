package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.api.Poser;
import com.phybots.picode.api.PoserLibrary;
import com.phybots.picode.api.PoserWithConnector;

public class SelectRobotAction extends AbstractAction {
	private static final long serialVersionUID = 7348444593834302000L;
	public SelectRobotAction() {
		putValue(NAME, "Robot");
		putValue(SHORT_DESCRIPTION, "Select the first robot instance in the list.");
	}
	public void actionPerformed(ActionEvent e) {
		for (Poser poser : PoserLibrary.getInstance().getPosers()) {
			if (poser instanceof PoserWithConnector) {
				PoserLibrary.getInstance().setCurrentPoser(poser);
				return;
			}
		}
		PoserLibrary.getInstance().setCurrentPoser(null);
	}
}