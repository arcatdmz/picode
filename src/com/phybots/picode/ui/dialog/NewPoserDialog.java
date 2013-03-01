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

import com.phybots.picode.PicodeMain;

public class NewPoserDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 355467424630256025L;

	protected final NewPoserPanel contentPanel;

	private static final Font defaultFont = PicodeMain.getDefaultFont();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			NewPoserDialog dialog = new NewPoserDialog();
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public NewPoserDialog() {
		setBounds(100, 100, 450, 200);
		getContentPane().setLayout(new BorderLayout());
		contentPanel = new NewPoserPanel();
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
				actionPerformed(new ActionEvent(NewPoserDialog.this,
						Integer.MIN_VALUE, "Cancel"));
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	}
}
