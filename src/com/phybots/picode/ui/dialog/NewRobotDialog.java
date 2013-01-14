package com.phybots.picode.ui.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import com.phybots.Phybots;
import com.phybots.picode.ui.PicodeMain;

public class NewRobotDialog extends JDialog implements ActionListener {

  protected final NewRobotPanel contentPanel;
  private static final Font defaultFont = Phybots.getInstance().getDefaultFont();

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    try {
      NewRobotDialog dialog = new NewRobotDialog();
      dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      dialog.setVisible(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public NewRobotDialog() {
    this(null);
  }

  /**
   * Create the dialog.
   */
  public NewRobotDialog(PicodeMain picodeMain) {
    setBounds(100, 100, 450, 200);
    getContentPane().setLayout(new BorderLayout());
    contentPanel = new NewRobotPanel(picodeMain);
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    {
      JPanel buttonPane = new JPanel();
      buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      {
        JButton okButton = new JButton("OK");
        okButton.setActionCommand("OK");
        okButton.setFont(defaultFont);
        okButton.addActionListener(this);
        buttonPane.add(okButton);
        getRootPane().setDefaultButton(okButton);
      }
      {
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("Cancel");
        cancelButton.setFont(defaultFont);
        cancelButton.addActionListener(this);
        buttonPane.add(cancelButton);
      }
    }
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        actionPerformed(
          new ActionEvent(NewRobotDialog.this, Integer.MIN_VALUE, "Cancel"));
      }
    });
  }

  @Override
  public void actionPerformed(ActionEvent e) {
  }
}
