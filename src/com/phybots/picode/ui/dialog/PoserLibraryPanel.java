package com.phybots.picode.ui.dialog;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import java.awt.GridBagLayout;
import javax.swing.JList;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JButton;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import javax.swing.border.EmptyBorder;

public class PoserLibraryPanel extends JPanel {

	/**
	 * Create the panel.
	 */
	public PoserLibraryPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		add(splitPane);
		
		JPanel panel = new JPanel();
		splitPane.setLeftComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		JList list = new JList();
		panel.add(list, BorderLayout.CENTER);
		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.SOUTH);
		
		JButton btnNewPoser = new JButton("New button");
		panel_1.add(btnNewPoser);
		
		JButton btnRemovePoser = new JButton("New button");
		panel_1.add(btnRemovePoser);
		
		JPanel panel_2 = new NewPoserPanel();
		panel_2.setBorder(new EmptyBorder(5, 5, 5, 5));
		splitPane.setRightComponent(panel_2);

	}

}
