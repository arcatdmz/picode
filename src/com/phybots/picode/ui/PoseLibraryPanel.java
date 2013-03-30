package com.phybots.picode.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.TypeBasedPoseLibrary;
import com.phybots.picode.action.ApplySelectedPoseAction;
import com.phybots.picode.action.DeleteSelectedPoseAction;
import com.phybots.picode.action.DuplicateSelectedPoseAction;
import com.phybots.picode.action.EditSelectedPoseNameAction;
import com.phybots.picode.action.OpenPoseFolderAction;
import com.phybots.picode.action.ShowCameraFrameAction;
import com.phybots.picode.api.Pose;
import com.phybots.picode.api.Poser;
import com.phybots.picode.api.PoserLibrary;
import com.phybots.picode.ui.list.DefaultListCellEditor;
import com.phybots.picode.ui.list.IconListRenderer;
import com.phybots.picode.ui.list.JMutableList;

import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class PoseLibraryPanel extends JPanel {
	private static final long serialVersionUID = 5622163966849443710L;
	private static final Font defaultFont = PicodeMain.getDefaultFont();

	private JPanel jPanel;
	private JButton btnAddPose;
	private JButton btnDeletePose;
	private JScrollPane jScrollPane;
	private JMutableList<Pose> jList;

	private transient PicodeMain picodeMain;
	private JPopupMenu popupMenu;
	private JMenuItem mntmApplyThisPose;
	private JMenuItem mntmRenameThisPose;
	private JMenuItem mntmDeleteThisPose;
	private JMenuItem mntmDuplicateThisPose;
	private JSeparator separator;
	private JMenuItem mntmOpenPoseFolder;

	private boolean isRunnable;

	public PoseLibraryPanel(PicodeMain picodeMain) {
		super();
		this.picodeMain = picodeMain;
		this.isRunnable = true;
		initialize();
	}

	public void setPoseLibrary(TypeBasedPoseLibrary poseLibrary) {
		if (poseLibrary == null) {
			jList.setModel(new DefaultListModel<Pose>());
			jList.setCellRenderer(new IconListRenderer(null));
		} else {
			jList.setModel(poseLibrary);
			jList.setCellRenderer(new IconListRenderer(poseLibrary));
		}
	}

	public void setRunnable(boolean isRunnable) {
		getBtnAddPose().setEnabled(isRunnable);
		getBtnDeletePose().setEnabled(isRunnable);
		getJList().setEnabled(isRunnable);
		this.isRunnable = isRunnable;
	}

	public Pose getSelectedPose() {
		return getJList().getSelectedValue();
	}

	public void editPoseName(Pose pose) {
		if (pose != null) {
			getJList().setSelectedValue(pose, true);
			editSelectedPoseName();
		}
	}

	public void editSelectedPoseName() {
		if (getJList().getSelectedIndex() != -1) {
			ActionEvent e = new ActionEvent(getJList(), 0, null);
			getJList().getActionMap().get("startEditing").actionPerformed(e);
		}
	}

	private void initialize() {
		setLayout(new BorderLayout(0, 0));
		add(getJPanel(), BorderLayout.SOUTH);
		add(getJScrollPane(), BorderLayout.CENTER);
	}

	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
			GridBagLayout gbl_jPanel = new GridBagLayout();
			gbl_jPanel.columnWeights = new double[]{0.0, 0.0};
			gbl_jPanel.rowWeights = new double[]{0.0};
			jPanel.setLayout(gbl_jPanel);
			GridBagConstraints gbc_btnAddPose = new GridBagConstraints();
			gbc_btnAddPose.weightx = 1.0;
			gbc_btnAddPose.anchor = GridBagConstraints.NORTHEAST;
			gbc_btnAddPose.insets = new Insets(0, 5, 0, 5);
			gbc_btnAddPose.gridx = 0;
			gbc_btnAddPose.gridy = 0;
			jPanel.add(getBtnAddPose(), gbc_btnAddPose);
			GridBagConstraints gbc_btnDeletePose = new GridBagConstraints();
			gbc_btnDeletePose.anchor = GridBagConstraints.NORTHEAST;
			gbc_btnDeletePose.gridx = 1;
			gbc_btnDeletePose.gridy = 0;
			jPanel.add(getBtnDeletePose(), gbc_btnDeletePose);
		}
		return jPanel;
	}

	private JButton getBtnAddPose() {
		if (btnAddPose == null) {
			btnAddPose = new JButton();
			btnAddPose.setAction(new ShowCameraFrameAction());
			btnAddPose.setFont(defaultFont);
		}
		return btnAddPose;
	}

	private JButton getBtnDeletePose() {
		if (btnDeletePose == null) {
			btnDeletePose = new JButton();
			btnDeletePose.setAction(new DeleteSelectedPoseAction(picodeMain));
			btnDeletePose.setFont(defaultFont);
		}
		return btnDeletePose;
	}

	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane(getJList());
			// jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			// jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		}
		return jScrollPane;
	}

	private JMutableList<Pose> getJList() {
		if (jList == null) {
			Poser poser = PoserLibrary.getInstance().getCurrentPoser();
			if (poser == null) {
				jList = new JMutableList<Pose>(new DefaultListModel<Pose>());
				jList.setCellRenderer(new IconListRenderer(null));
			} else {
				TypeBasedPoseLibrary poseLibrary =
						picodeMain.getFrame().getTypeBasedLibrary(poser.getPoserType());
				jList = new JMutableList<Pose>(poseLibrary);
				jList.setCellRenderer(new IconListRenderer(poseLibrary));
			}
			jList.setListCellEditor(new PoseCellEditor());
			jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jList.setDragEnabled(true);
			jList.setDropMode(DropMode.INSERT);
			jList.setTransferHandler(new TransferHandler() {
				private static final long serialVersionUID = -6052595555714712415L;

				@Override
				public int getSourceActions(JComponent c) {
					return COPY;
				}

				@SuppressWarnings("unchecked")
				@Override
				protected Transferable createTransferable(JComponent c) {
					System.out.println("dnd: start");
					JList<Pose> list = (JList<Pose>) c;
					String value = list.getSelectedValue().toString();
					return new StringSelection(value);
				}

				@Override
				protected void exportDone(JComponent source, Transferable data,
						int action) {
					System.out.println("dnd: " + action);
				}
			});
			jList.setFont(defaultFont);
			addPopup(jList, getPopupMenu());
		}
		return jList;
	}

	private class PoseCellEditor extends DefaultListCellEditor<Pose> {
		private static final long serialVersionUID = -2159024607030657708L;
		JTextField textField;

		public PoseCellEditor() {
			super(new JTextField());
			textField = (JTextField) getComponent();
			delegate = new EditorDelegate() {
				private static final long serialVersionUID = 5742190676591715866L;
				private Pose pose = null;

				public void setValue(Object value) {
					String text;
					if (value == null) {
						text = "";
					} else {
						if (value instanceof Pose) {
							pose = (Pose) value;
							text = pose.getName();
						} else {
							text = value.toString();
						}
					}
					textField.setText(text);
				}

				public Object getCellEditorValue() {
					if (pose == null) {
						return textField.getText();
					} else {
						pose.rename(textField.getText());
						// P5TODO replace pose name in the text editor.
						return pose;
					}
				}
			};
		}
	}

	private JPopupMenu getPopupMenu() {
		if (popupMenu == null) {
			popupMenu = new JPopupMenu();
			popupMenu.add(getMntmApplyThisPose());
			popupMenu.add(getMntmDuplicateThisPose());
			// popupMenu.add(getMntmRenameThisPose());
			popupMenu.add(getMntmDeleteThisPose());
			popupMenu.add(getSeparator());
			popupMenu.add(getMntmOpenPoseFolder());
		}
		return popupMenu;
	}

	private void addPopup(final Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				if (component instanceof JList) {
					JList<?> jList = (JList<?>) component;
					int index = jList.locationToIndex(e.getPoint());
					if (index != -1) {
						jList.setSelectedIndex(index);
					}
				}
				if (isRunnable) {
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
	}

	private JMenuItem getMntmApplyThisPose() {
		if (mntmApplyThisPose == null) {
			mntmApplyThisPose = new JMenuItem();
			mntmApplyThisPose.setAction(new ApplySelectedPoseAction(picodeMain));
			mntmApplyThisPose.setText("Apply this pose to the robot");
			mntmApplyThisPose.setAccelerator(KeyStroke.getKeyStroke(
					KeyEvent.VK_A, 0));
		}
		return mntmApplyThisPose;
	}

	private JMenuItem getMntmRenameThisPose() {
		if (mntmRenameThisPose == null) {
			mntmRenameThisPose = new JMenuItem();
			mntmRenameThisPose.setAction(new EditSelectedPoseNameAction(picodeMain));
			mntmRenameThisPose.setText("Rename this pose");
			mntmRenameThisPose.setAccelerator(KeyStroke.getKeyStroke(
					KeyEvent.VK_R, 0));
		}
		return mntmRenameThisPose;
	}

	private JMenuItem getMntmDeleteThisPose() {
		if (mntmDeleteThisPose == null) {
			mntmDeleteThisPose = new JMenuItem();
			mntmDeleteThisPose.setAction(new DeleteSelectedPoseAction(picodeMain));
			mntmDeleteThisPose.setText("Delete this pose");
			mntmDeleteThisPose.setAccelerator(
					KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		}
		return mntmDeleteThisPose;
	}

	private JMenuItem getMntmDuplicateThisPose() {
		if (mntmDuplicateThisPose == null) {
			mntmDuplicateThisPose = new JMenuItem();
			mntmDuplicateThisPose.setAction(new DuplicateSelectedPoseAction(picodeMain));
			mntmDuplicateThisPose.setText("Duplicate this pose");
			mntmDuplicateThisPose.setAccelerator(
					KeyStroke.getKeyStroke(KeyEvent.VK_D, 0));
		}
		return mntmDuplicateThisPose;
	}

	private JSeparator getSeparator() {
		if (separator == null) {
			separator = new JSeparator();
		}
		return separator;
	}

	private JMenuItem getMntmOpenPoseFolder() {
		if (mntmOpenPoseFolder == null) {
			mntmOpenPoseFolder = new JMenuItem();
			mntmOpenPoseFolder.setAction(new OpenPoseFolderAction());
			mntmOpenPoseFolder.setText("Open the pose folder");
		}
		return mntmOpenPoseFolder;
	}
}
