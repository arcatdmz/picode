package com.phybots.picode.action;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;

import com.phybots.picode.api.Poser;
import com.phybots.picode.api.PoserLibrary;
import com.phybots.picode.api.PoserWithConnector;

public class DeleteCurrentPoserAction extends AbstractAction {
	private static final long serialVersionUID = 1526677233343155373L;

	@Override
	public void actionPerformed(ActionEvent e) {

		PoserLibrary poserLibrary = PoserLibrary.getInstance();

		// Get the selected poser.
		Poser poser = poserLibrary.getCurrentPoser();
		if (poser == null) {
			return;
		}

		// Look for the next poser (with connector i.e. robot) to be active.
		List<Poser> posers = poserLibrary.getPosers();
		int index = posers.indexOf(poser);
		Poser nextPoser = null;
		for (int i = 1; i < posers.size(); i ++) {
			int j = (i + index) % posers.size();
			Poser p = posers.get(j);
			if (p instanceof PoserWithConnector) {
				nextPoser = p;
				break;
			}
		}

		// If there's no robot, make the human instance active.
		if (nextPoser == null) {
			for (Poser p : posers) {
				if (!(p instanceof PoserWithConnector)) {
					nextPoser = p;
					break;
				}
			}
		}

		// Activate the next poser.
		poserLibrary.setCurrentPoser(nextPoser);

		// Remove the selected poser.
		poser.dispose();
	}
}
