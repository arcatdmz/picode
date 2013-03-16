package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.api.Poser;
import com.phybots.picode.api.PoserLibrary;
import com.phybots.picode.api.PoserWithConnector;

public class SelectHumanAction extends AbstractAction {
	private static final long serialVersionUID = 8971364641285872815L;
	public SelectHumanAction() {
		putValue(NAME, "Human");
		putValue(SHORT_DESCRIPTION, "Show human poses.");
	}
	public void actionPerformed(ActionEvent e) {
		for (Poser poser : PoserLibrary.getInstance().getPosers()) {
			if (!(poser instanceof PoserWithConnector)) {
				PoserLibrary.getInstance().setCurrentPoser(poser);
				break;
			}
		}
	}
}