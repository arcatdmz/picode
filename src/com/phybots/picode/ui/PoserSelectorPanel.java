package com.phybots.picode.ui;

import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.action.EditPoserWithConnectorAction;
import com.phybots.picode.action.NewPoserWithConnectorAction;
import com.phybots.picode.action.SelectHumanAction;
import com.phybots.picode.action.SelectRobotAction;
import com.phybots.picode.api.Poser;
import com.phybots.picode.api.PoserLibrary;
import com.phybots.picode.api.PoserWithConnector;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.Action;
import javax.swing.ImageIcon;

public class PoserSelectorPanel extends JPanel {
	private static final long serialVersionUID = -3583127135224098029L;
	private static final Font defaultFont = PicodeMain.getDefaultFont();

	private JButton btnSelectHuman;
	private JButton btnSelectRobot;
	private JComboBox<Object> comboBox;
	private JButton btnEdit;
	private final Action selectHumanAction = new SelectHumanAction();
	private final Action selectRobotAction = new SelectRobotAction();

	public PoserSelectorPanel(final PicodeMain picodeMain) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0};
		setLayout(gridBagLayout);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.weighty = 1.0;
		gbc_panel.gridwidth = 2;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWeights = new double[]{0.0, 0.0};
		gbl_panel.rowWeights = new double[]{0.0};
		panel.setLayout(gbl_panel);
		
		btnSelectHuman = new JButton();
		btnSelectHuman.setAction(selectHumanAction);
		btnSelectHuman.setIcon(new ImageIcon(PoserSelectorPanel.class.getResource("/human.png")));
		btnSelectHuman.setFont(defaultFont);
		GridBagConstraints gbc_btnHuman = new GridBagConstraints();
		gbc_btnHuman.insets = new Insets(0, 0, 0, 5);
		gbc_btnHuman.weighty = 1.0;
		gbc_btnHuman.weightx = 0.5;
		gbc_btnHuman.fill = GridBagConstraints.BOTH;
		gbc_btnHuman.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnHuman.gridx = 0;
		gbc_btnHuman.gridy = 0;
		panel.add(btnSelectHuman, gbc_btnHuman);
		
		btnSelectRobot = new JButton();
		btnSelectRobot.setAction(selectRobotAction);
		btnSelectRobot.setIcon(new ImageIcon(PoserSelectorPanel.class.getResource("/robot.png")));
		btnSelectRobot.setFont(defaultFont);
		GridBagConstraints gbc_btnRobot = new GridBagConstraints();
		gbc_btnRobot.fill = GridBagConstraints.BOTH;
		gbc_btnRobot.weighty = 1.0;
		gbc_btnRobot.weightx = 0.5;
		gbc_btnRobot.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnRobot.gridx = 1;
		gbc_btnRobot.gridy = 0;
		panel.add(btnSelectRobot, gbc_btnRobot);

		comboBox = new JComboBox<Object>(new DefaultComboBoxModel<Object>());
		comboBox.addItem("New robot ...");
		comboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					Object object = comboBox.getSelectedItem();
					if (object instanceof Poser) {
						// Select the specified poser.
						Poser poser = (Poser)comboBox.getSelectedItem();
						PoserLibrary.getInstance().setCurrentPoser(poser);
					} else {
						// Show "new poser" dialog.
						comboBox.setSelectedIndex(-1);
						NewPoserWithConnectorAction a = new NewPoserWithConnectorAction(picodeMain);
						a.actionPerformed(null);
					}
				}
			}
		});
		comboBox.setFont(defaultFont);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 0, 5);
		gbc_comboBox.fill = GridBagConstraints.BOTH;
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 1;
		add(comboBox, gbc_comboBox);
		
		btnEdit = new JButton();
		btnEdit.setAction(new EditPoserWithConnectorAction(picodeMain));
		btnEdit.setText("Edit");
		btnEdit.setFont(defaultFont);
		GridBagConstraints gbc_btnEdit = new GridBagConstraints();
		gbc_btnEdit.fill = GridBagConstraints.BOTH;
		gbc_btnEdit.gridx = 1;
		gbc_btnEdit.gridy = 1;
		add(btnEdit, gbc_btnEdit);
	}

	public void setRunnable(boolean isRunnable) {
		btnSelectHuman.setEnabled(isRunnable);
		btnSelectRobot.setEnabled(isRunnable);
		comboBox.setEnabled(isRunnable);
		btnEdit.setEnabled(isRunnable);
	}

	public void onAddPoserWithConnector(Poser poser) {
		DefaultComboBoxModel<Object> model = (DefaultComboBoxModel<Object>)comboBox.getModel();
		model.insertElementAt(poser, model.getSize() - 1);
	}

	public void onRemovePoserWithConnector(Poser poser) {

		// If there will be no poser left,
		// unselect the box item so that "New poser" dialog won't show up.
		DefaultComboBoxModel<Object> model = (DefaultComboBoxModel<Object>)comboBox.getModel();
		if (model.getSize() <= 2
				&& model.getElementAt(0) == poser) {
			comboBox.setSelectedIndex(-1);
		}
		comboBox.removeItem(poser);
	}

	public void onCurrentPoserChange(Poser poser) {
		if (poser == null || poser instanceof PoserWithConnector) {
			comboBox.setVisible(true);
			comboBox.setSelectedItem(poser);
			btnEdit.setVisible(true);
		} else {
			comboBox.setVisible(false);
			btnEdit.setVisible(false);
		}
	}

}
