package com.phybots.picode.action;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.api.Poser;
import com.phybots.picode.api.PoserManager;

public class DeleteActiveRobotAction extends AbstractAction {
	private static final long serialVersionUID = 1526677233343155373L;
	private PoserManager poserManager;

	public DeleteActiveRobotAction(PoserManager poserManager) {
		this.poserManager = poserManager;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Poser poser = poserManager.getCurrentPoser();
		if (poser == null) {
			return;
		}
		List<Poser> posers = poserManager.getPosers();
		int index = posers.indexOf(poser);
		if (index < posers.size() - 1) {
			poserManager.setCurrentPoser(posers.get(index + 1));
		} else if (index > 0) {
			poserManager.setCurrentPoser(posers.get(index - 1));
		} else {
			poserManager.setCurrentPoser(null);
		}
		poserManager.removePoser(poser);
	}
}
