package com.phybots.picode.action;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;

import com.phybots.picode.Robot;
import com.phybots.picode.ui.PicodeMain;

public class DeleteActiveRobotAction extends AbstractAction {
  private PicodeMain picodeMain;
  public DeleteActiveRobotAction(PicodeMain picodeMain) {
    this.picodeMain = picodeMain;
  }
  @Override
  public void actionPerformed(ActionEvent e) {
    Robot robot = picodeMain.getActiveRobot();
    if (robot == null) {
      picodeMain.setActiveRobot(null);
    } else {
      List<Robot> robots = picodeMain.getRobots();
      int index = robots.indexOf(robot);
      if (index < robots.size() - 1) {
        picodeMain.setActiveRobot(robots.get(index + 1));
      } else if (index > 0) {
        picodeMain.setActiveRobot(robots.get(index - 1));
      } else {
        picodeMain.setActiveRobot(null);
      }
      picodeMain.removeRobot(robot);
    }
  }
}
