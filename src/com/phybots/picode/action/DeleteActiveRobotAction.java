package com.phybots.picode.action;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.api.Poser;

public class DeleteActiveRobotAction extends AbstractAction {
	private PicodeMain picodeMain;

	public DeleteActiveRobotAction(PicodeMain picodeMain) {
		this.picodeMain = picodeMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Poser robot = null;//TODO picodeMain.getActiveRobot();
		if (robot == null) {
			//picodeMain.setActiveRobot(null);
		} else {
			List<Poser> robots = picodeMain.getRobotManager().getRobots();
			int index = robots.indexOf(robot);
			if (index < robots.size() - 1) {
				//picodeMain.setActiveRobot(robots.get(index + 1));
			} else if (index > 0) {
				//picodeMain.setActiveRobot(robots.get(index - 1));
			} else {
				//picodeMain.setActiveRobot(null);
			}
			picodeMain.getRobotManager().removeRobot(robot);
		}
	}
}
