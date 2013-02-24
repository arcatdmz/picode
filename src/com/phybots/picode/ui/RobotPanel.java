package com.phybots.picode.ui;

import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.api.Poser;

public class RobotPanel extends JPanel {
	JComboBox comboBox;
	Font defaultFont = null;
	private PicodeMain picodeMain;
	private JComboBox getComboBox() {
		if (comboBox == null) {
			comboBox = new JComboBox();
			comboBox.setFont(defaultFont);
			comboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						//picodeMain.getRobotManager().setActiveRobot(
						//		(Poser) e.getItem());
						//TODO
					}
				}
			});
		}
		return comboBox;
	}
}
