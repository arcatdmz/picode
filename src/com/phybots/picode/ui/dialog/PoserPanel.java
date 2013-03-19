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
import java.util.Set;
import java.util.Vector;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.api.PoserInfo;
import com.phybots.picode.api.PoserTypeInfo;
import com.phybots.picode.ui.dialog.ConnectorPanel.ConnectorManager;

import javax.swing.JTextField;

public class PoserPanel extends JPanel {
	private static final long serialVersionUID = -8644965871351515498L;
	private static final Font defaultFont = PicodeMain.getDefaultFont();

	private JComboBox<PoserTypeInfo> comboBox;
	private JLabel lblName;
	private JTextField textField;
	private JLabel lblConnector;
	private ConnectorPanel panel;

	/**
	 * Create the panel.
	 */
	public PoserPanel(Set<PoserTypeInfo> poserTypeInfos) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JLabel lblRobotType = new JLabel("Poser type:");
		lblRobotType.setFont(defaultFont);
		GridBagConstraints gbc_lblRobotType = new GridBagConstraints();
		gbc_lblRobotType.insets = new Insets(0, 0, 5, 5);
		gbc_lblRobotType.anchor = GridBagConstraints.EAST;
		gbc_lblRobotType.gridx = 0;
		gbc_lblRobotType.gridy = 0;
		add(lblRobotType, gbc_lblRobotType);

		comboBox = new JComboBox<PoserTypeInfo>();
		comboBox.setFont(defaultFont);
		comboBox.setModel(new DefaultComboBoxModel<PoserTypeInfo>(
				new Vector<PoserTypeInfo>(poserTypeInfos)));
		comboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updatePanelStatus();
			}
		});
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 0;
		add(comboBox, gbc_comboBox);
		
		lblName = new JLabel("Name:");
		lblName.setFont(defaultFont);
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.fill = GridBagConstraints.VERTICAL;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 1;
		add(lblName, gbc_lblName);
		
		textField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 5, 0);
		gbc_textField.fill = GridBagConstraints.BOTH;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 1;
		add(textField, gbc_textField);
		textField.setColumns(10);

		lblConnector = new JLabel("Connector:");
		lblConnector.setFont(defaultFont);
		GridBagConstraints gbc_lblConnector = new GridBagConstraints();
		gbc_lblConnector.anchor = GridBagConstraints.EAST;
		gbc_lblConnector.insets = new Insets(0, 0, 0, 5);
		gbc_lblConnector.gridx = 0;
		gbc_lblConnector.gridy = 2;
		add(lblConnector, gbc_lblConnector);

		panel = new ConnectorPanel();
		updatePanelStatus();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 2;
		add(panel, gbc_panel);
	}

	public void setPoserTypeSelectable(boolean isSelectable) {
		comboBox.setEnabled(isSelectable);
	}

	public void setPoserInfo(PoserInfo poserInfo) {
		if (poserInfo == null) {
			return;
		}
		comboBox.setSelectedItem(poserInfo.type);
		textField.setText(poserInfo.name);
		panel.setConnectionString(poserInfo.connector);
	}

	public PoserInfo getPoserInfo() {
		PoserTypeInfo poserTypeInfo = (PoserTypeInfo) comboBox.getSelectedItem();
		if (poserTypeInfo == null) {
			return null;
		}
		PoserInfo poserInfo = new PoserInfo();
		poserInfo.type = poserTypeInfo;
		poserInfo.name = textField.getText();
		poserInfo.connector = panel.getConnectionString();
		return poserInfo;
	}

	public boolean testConnector() {
		return panel.test();
	}

	public void setConnectorManager(ConnectorManager connectorManager) {
		panel.setConnectorManager(connectorManager);
	}
	
	private void updatePanelStatus() {
		PoserTypeInfo poserTypeInfo = (PoserTypeInfo) comboBox.getSelectedItem();
		panel.setEnabled(poserTypeInfo != null
				&& poserTypeInfo.supportsConnector);
	}

}
