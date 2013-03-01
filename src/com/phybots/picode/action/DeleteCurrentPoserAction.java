package com.phybots.picode.action;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;

import com.phybots.picode.api.Poser;
import com.phybots.picode.api.PoserLibrary;

public class DeleteCurrentPoserAction extends AbstractAction {
	private static final long serialVersionUID = 1526677233343155373L;

	@Override
	public void actionPerformed(ActionEvent e) {
		PoserLibrary poserLibrary = PoserLibrary.getInstance();
		Poser poser = poserLibrary.getCurrentPoser();
		if (poser == null) {
			return;
		}
		List<Poser> posers = poserLibrary.getPosers();
		int index = posers.indexOf(poser);
		if (index < posers.size() - 1) {
			poserLibrary.setCurrentPoser(posers.get(index + 1));
		} else if (index > 0) {
			poserLibrary.setCurrentPoser(posers.get(index - 1));
		} else {
			poserLibrary.setCurrentPoser(null);
		}
		poser.dispose();
	}
}
