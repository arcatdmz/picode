package com.phybots.picode.ui.dialog;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import com.phybots.Phybots;
import com.phybots.picode.Robot;
import com.phybots.picode.RobotType;
import com.phybots.picode.ui.PicodeMain;

public class NewRobotPanel extends JPanel {
  private static final Font defaultFont = Phybots.getInstance().getDefaultFont();
  private JComboBox comboBox;
  private ConnectorPanel panel;
  private PicodeMain picodeMain;

  public NewRobotPanel() {
    this(null);
  }

  /**
   * Create the panel.
   */
  public NewRobotPanel(PicodeMain picodeMain) {
    GridBagLayout gridBagLayout = new GridBagLayout();
    gridBagLayout.columnWidths = new int[]{0, 0};
    gridBagLayout.rowHeights = new int[]{0, 0, 0};
    gridBagLayout.columnWeights = new double[]{0.0, 1.0};
    gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
    setLayout(gridBagLayout);
    
    JLabel lblRobotType = new JLabel("Robot type:");
    lblRobotType.setFont(defaultFont);
    GridBagConstraints gbc_lblRobotType = new GridBagConstraints();
    gbc_lblRobotType.insets = new Insets(5, 5, 5, 5);
    gbc_lblRobotType.anchor = GridBagConstraints.EAST;
    gbc_lblRobotType.gridx = 0;
    gbc_lblRobotType.gridy = 0;
    add(lblRobotType, gbc_lblRobotType);
    
    comboBox = new JComboBox<RobotType>();
    comboBox.setFont(defaultFont);
    comboBox.setModel(new DefaultComboBoxModel<RobotType>(RobotType.values()));
    comboBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (comboBox.getSelectedItem() == null) {
          return;
        }
        panel.setEnabled(comboBox.getSelectedItem() != RobotType.Human);
      }
    });
    GridBagConstraints gbc_comboBox = new GridBagConstraints();
    gbc_comboBox.insets = new Insets(5, 0, 5, 0);
    gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
    gbc_comboBox.gridx = 1;
    gbc_comboBox.gridy = 0;
    add(comboBox, gbc_comboBox);
    
    JLabel lblConnector = new JLabel("Connector:");
    lblConnector.setFont(defaultFont);
    GridBagConstraints gbc_lblConnector = new GridBagConstraints();
    gbc_lblConnector.anchor = GridBagConstraints.EAST;
    gbc_lblConnector.insets = new Insets(0, 5, 0, 5);
    gbc_lblConnector.gridx = 0;
    gbc_lblConnector.gridy = 1;
    add(lblConnector, gbc_lblConnector);
    
    panel = new ConnectorPanel();
    panel.setEnabled(comboBox.getSelectedItem() != RobotType.Human);
    GridBagConstraints gbc_panel = new GridBagConstraints();
    gbc_panel.fill = GridBagConstraints.BOTH;
    gbc_panel.gridx = 1;
    gbc_panel.gridy = 1;
    add(panel, gbc_panel);
    
    this.picodeMain = picodeMain;
  }
  
  public Robot newRobotInstance() {
    RobotType robotType = (RobotType) comboBox.getSelectedItem();
    if (robotType == null) {
      return null;
    }
    Robot robot;
    try {
      robot = new Robot(
        picodeMain,
        robotType.newRobotInstance(panel.getConnectionString()));
    } catch (InstantiationException e) {
      return null;
    }
    return robot;
  }
}
