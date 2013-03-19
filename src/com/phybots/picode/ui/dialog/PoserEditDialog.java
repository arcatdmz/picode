package com.phybots.picode.ui.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.api.Poser;
import com.phybots.picode.api.PoserInfo;
import com.phybots.picode.api.PoserLibrary;
import com.phybots.picode.api.PoserTypeInfo;
import com.phybots.picode.api.PoserWithConnector;
import com.phybots.picode.ui.dialog.ConnectorPanel.ConnectorManager;

public class PoserEditDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 355467424630256025L;
	private static final Font defaultFont = PicodeMain.getDefaultFont();

	protected final PoserPanel contentPanel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			PoserEditDialog dialog = new PoserEditDialog(
					PoserLibrary.getTypeInfos());
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public PoserEditDialog(Set<PoserTypeInfo> poserTypes) {
		setBounds(100, 100, 450, 200);
		getContentPane().setLayout(new BorderLayout());
		contentPanel = new PoserPanel(poserTypes);
		contentPanel.setConnectorManager(getConnectorManager());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setPoserTypeSelectable(isNew());
		if (!isNew()) {
			contentPanel.setPoserInfo(getOriginalPoserInfo());
		}
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton btnOk = new JButton("OK");
				btnOk.setActionCommand("OK");
				btnOk.setFont(defaultFont);
				btnOk.addActionListener(this);
				buttonPane.add(btnOk);
				getRootPane().setDefaultButton(btnOk);
			}
			if (!isNew()) {
				JButton btnRemove = new JButton("Remove");
				btnRemove.setActionCommand("Remove");
				btnRemove.setFont(defaultFont);
				btnRemove.addActionListener(this);
				buttonPane.add(btnRemove);
			}
			{
				JButton btnCancel = new JButton("Cancel");
				btnCancel.setActionCommand("Cancel");
				btnCancel.setFont(defaultFont);
				btnCancel.addActionListener(this);
				buttonPane.add(btnCancel);
			}
		}
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				actionPerformed(new ActionEvent(PoserEditDialog.this,
						Integer.MIN_VALUE, "Cancel"));
			}
		});
	}

	private boolean isNew() {
		return getOriginalPoserInfo() == null;
	}

	protected PoserInfo getOriginalPoserInfo() {
		return null;
	}

	protected boolean testConnector() {
		return contentPanel.testConnector();
	}

	protected ConnectorManager getConnectorManager() {
		return new ConnectorManager() {
			@Override
			public boolean exists(String connectionString) {
				for (Poser poser : PoserLibrary.getInstance().getPosers()) {
					if (poser instanceof PoserWithConnector) {
						return connectionString.equals(
								((PoserWithConnector) poser).getConnector());
					}
				}
				return false;
			}
		};
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	}
}
