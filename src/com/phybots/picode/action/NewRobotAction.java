package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.Robot;
import com.phybots.picode.ui.PicodeMain;
import com.phybots.picode.ui.dialog.NewRobotDialog;

public class NewRobotAction extends AbstractAction {
  private PicodeMain picodeMain;

  public NewRobotAction(PicodeMain picodeMain) {
    this.picodeMain = picodeMain;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    NewRobotDialog dialog = new NewRobotDialog(picodeMain) {
      @Override
      public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("OK")) {
          Robot robot = contentPanel.newRobotInstance();
          if (robot != null) {
            picodeMain.addRobot(robot);
            picodeMain.setActiveRobot(robot);
          }
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
