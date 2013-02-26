package com.phybots.picode.ui;

import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.api.Poser;
import com.phybots.picode.api.PoserManager;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class PoserSelectorPanel extends JPanel {

	private static final long serialVersionUID = -3583127135224098029L;

	private static final Font defaultFont = PicodeMain.getDefaultFont();

	private JComboBox<Poser> comboBox;

	public PoserSelectorPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0};
		setLayout(gridBagLayout);
		
		comboBox = new JComboBox<Poser>();
		comboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					Poser poser = (Poser)comboBox.getSelectedItem();
					if (poser != null) {
						PoserManager.getInstance().setCurrentPoser(poser);
					}
				}
			}
		});
		comboBox.setFont(defaultFont);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 0, 5);
		gbc_comboBox.fill = GridBagConstraints.BOTH;
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 0;
		add(comboBox, gbc_comboBox);
		
		JButton btnEdit = new JButton("Edit");
		btnEdit.setFont(defaultFont);
		GridBagConstraints gbc_btnEdit = new GridBagConstraints();
		gbc_btnEdit.fill = GridBagConstraints.BOTH;
		gbc_btnEdit.gridx = 1;
		gbc_btnEdit.gridy = 0;
		add(btnEdit, gbc_btnEdit);
	}
}
