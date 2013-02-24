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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Vector;

import com.phybots.Phybots;
import com.phybots.picode.PicodeMain;
import com.phybots.picode.api.Poser;

public class NewRobotPanel extends JPanel {
	private static final long serialVersionUID = -8644965871351515498L;

	private static final Font defaultFont =
			Phybots.getInstance().getDefaultFont();

	private transient HashMap<Class<? extends Poser>, PoserInfo> map;

	private JComboBox<Class<? extends Poser>> comboBox;
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
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JLabel lblRobotType = new JLabel("Robot type:");
		lblRobotType.setFont(defaultFont);
		GridBagConstraints gbc_lblRobotType = new GridBagConstraints();
		gbc_lblRobotType.insets = new Insets(5, 5, 5, 5);
		gbc_lblRobotType.anchor = GridBagConstraints.EAST;
		gbc_lblRobotType.gridx = 0;
		gbc_lblRobotType.gridy = 0;
		add(lblRobotType, gbc_lblRobotType);

		map = enumPoserTypes();
		comboBox = new JComboBox<Class<? extends Poser>>();
		comboBox.setFont(defaultFont);
		comboBox.setModel(new DefaultComboBoxModel<Class<? extends Poser>>(
				new Vector<Class<? extends Poser>>(map.keySet())));
		comboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updatePanelStatus();
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
		updatePanelStatus();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 1;
		add(panel, gbc_panel);

		this.picodeMain = picodeMain;
	}
	
	private void updatePanelStatus() {
		PoserInfo poserInfo = map.get(comboBox.getSelectedItem());
		panel.setEnabled(poserInfo != null
				&& poserInfo.supportsConnector);
	}

	private HashMap<Class<? extends Poser>, PoserInfo> enumPoserTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	public Poser newRobotInstance() {
		PoserInfo poserInfo = map.get(comboBox.getSelectedItem());
		if (poserInfo == null) {
			return null;
		}
		try {
			Poser poser = poserInfo.constructor.newInstance(picodeMain);
			//TODO
			return poser;
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			return null;
		}
	}
	
	private static class PoserInfo {
		boolean supportsConnector;
		Constructor<? extends Poser> constructor;
	}
}
