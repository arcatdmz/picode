package com.phybots.picode.ui.dialog;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JComboBox;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import jp.digitalmuseum.connector.BluetoothConnector;
import jp.digitalmuseum.connector.Connector;
import jp.digitalmuseum.connector.ConnectorFactory;
import jp.digitalmuseum.connector.RXTXConnector;
import jp.digitalmuseum.connector.SocketConnector;

import com.phybots.picode.PicodeMain;

import java.awt.GridBagLayout;

public class ConnectorPanel extends JPanel {

	private static final long serialVersionUID = 7247928302975753877L;

	private JComboBox<String> comboBox;
	private JTextField textField;
	private JButton btnTest;

	private static final Font defaultFont = PicodeMain.getDefaultFont();

	/**
	 * Create the panel.
	 */
	public ConnectorPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0 };
		gridBagLayout.rowWeights = new double[] { 0.0 };
		setLayout(gridBagLayout);

		btnTest = new JButton("Test");
		btnTest.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnTest.setEnabled(false);
				Connector connector = newConnectorInstance();
				if (connector != null && connector.connect()) {
					connector.disconnect();
					connectionSucceeded();
				} else {
					connectionFailed();
				}
			}
		});

		textField = new JTextField();
		textField.setFont(defaultFont);
		textField.getDocument().addDocumentListener(new DocumentListener() {
			public void removeUpdate(DocumentEvent e) {
				somethingChanged();
			}

			public void insertUpdate(DocumentEvent e) {
				somethingChanged();
			}

			public void changedUpdate(DocumentEvent e) {
				somethingChanged();
			}

			private void somethingChanged() {
				textField.setBackground(Color.white);
			}
		});

		comboBox = new JComboBox<String>();
		comboBox.setModel(new DefaultComboBoxModel<String>(new String[] {
				"Bluetooth", "Serial port", "HTTP", "TCP/IP" }));
		comboBox.setFont(defaultFont);
		GridBagConstraints gbc_connectorComboBox = new GridBagConstraints();
		gbc_connectorComboBox.anchor = GridBagConstraints.WEST;
		gbc_connectorComboBox.insets = new Insets(0, 0, 0, 5);
		gbc_connectorComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_connectorComboBox.gridx = 0;
		gbc_connectorComboBox.gridy = 0;
		add(comboBox, gbc_connectorComboBox);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.anchor = GridBagConstraints.WEST;
		gbc_textField.weightx = 1.0;
		gbc_textField.insets = new Insets(0, 0, 0, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		add(textField, gbc_textField);
		textField.setColumns(10);
		btnTest.setFont(defaultFont);
		GridBagConstraints gbc_btnTest = new GridBagConstraints();
		gbc_btnTest.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnTest.gridx = 2;
		gbc_btnTest.gridy = 0;
		add(btnTest, gbc_btnTest);
	}

	private void connectionSucceeded() {
		if (isEnabled()) {
			btnTest.setEnabled(true);
			textField.setBackground(new Color(230, 255, 230));
		}
	}

	private void connectionFailed() {
		if (isEnabled()) {
			textField.setBackground(new Color(255, 230, 230));
			textField.requestFocusInWindow();
		}
	}

	public Connector newConnectorInstance() {
		try {
			return ConnectorFactory.makeConnector(getConnectionString());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public void setConnectionString(String connector) {
		if (connector == null) {
			textField.setText("");
			return;
		}
		if (connector.startsWith(RXTXConnector.CON_PREFIX)) {
			comboBox.setSelectedIndex(1);
			textField.setText(
					connector.substring(RXTXConnector.CON_PREFIX.length()));
		} else if (connector.startsWith(SocketConnector.CON_PREFIX)) {
			comboBox.setSelectedIndex(2);
			textField.setText(
					connector.substring(SocketConnector.CON_PREFIX.length()));
		} else if (connector.startsWith(SocketConnector.CON_PREFIX2)) {
			comboBox.setSelectedIndex(2);
			textField.setText(
					connector.substring(SocketConnector.CON_PREFIX2.length()));
		} else if (connector.startsWith(BluetoothConnector.CON_PREFIX)) {
			comboBox.setSelectedIndex(0);
			textField.setText(
					connector.substring(BluetoothConnector.CON_PREFIX.length()));
		}
	}

	public String getConnectionString() {
		if (!isEnabled()) {
			return null;
		}
		String prefix = "";
		switch (comboBox.getSelectedIndex()) {
		case 1:
			prefix = RXTXConnector.CON_PREFIX;
			break;
		case 2:
			prefix = SocketConnector.CON_PREFIX2;
			break;
		case 3:
			prefix = SocketConnector.CON_PREFIX;
			break;
		case 0:
		default:
			prefix = BluetoothConnector.CON_PREFIX;
			break;
		}
		return prefix + textField.getText();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		comboBox.setEnabled(enabled);
		textField.setEnabled(enabled);
		btnTest.setEnabled(enabled);
	}
}
