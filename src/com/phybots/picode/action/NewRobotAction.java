package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.api.Poser;
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
          Poser robot = contentPanel.newRobotInstance();
          if (robot != null) {
            picodeMain.getRobotManager().addRobot(robot);
            //picodeMain.setActiveRobot(robot);//TODO
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
