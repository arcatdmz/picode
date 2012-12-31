package com.phybots.picode.ui.library;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import com.phybots.Phybots;
import com.phybots.picode.Pose;
import com.phybots.picode.action.ApplySelectedPoseAction;
import com.phybots.picode.action.DeleteSelectedPoseAction;
import com.phybots.picode.action.DuplicateSelectedPoseAction;
import com.phybots.picode.action.EditSelectedPoseNameAction;
import com.phybots.picode.action.OpenPoseFolderAction;
import com.phybots.picode.action.ShowCameraFrameAction;
import com.phybots.picode.ui.PicodeFrame;
import com.phybots.picode.ui.PicodeMain;
import com.phybots.picode.ui.library.internal.DefaultListCellEditor;
import com.phybots.picode.ui.library.internal.IconListRenderer;
import com.phybots.picode.ui.library.internal.JMutableList;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import javax.swing.JSeparator;
import java.awt.GridLayout;

public class PosePanel extends JPanel {
	private static final long serialVersionUID = 5622163966849443710L;

	private JPanel jPanel;
	private JPanel commandPanel;
  private JButton btnAddRobotPose;
  private JButton btnRemove;
	private JScrollPane jScrollPane;
	private JMutableList<Pose> jList;

	private static final Font defaultFont = Phybots.getInstance().getDefaultFont();

	private transient final PicodeMain picodeMain;
	private transient final PicodeFrame picodeFrame;
	private JPopupMenu popupMenu;
	private JMenuItem mntmApplyThisPose;
	private JMenuItem mntmRenameThisPose;
	private JMenuItem mntmDeleteThisPose;
	private JMenuItem mntmDuplicateThisPose;
	private JSeparator separator;
	private JMenuItem mntmOpenPoseFolder;
	
	private boolean isRunnable;

	public PosePanel(PicodeMain picodeMain, PicodeFrame picodeFrame) {
		super();
		this.picodeMain = picodeMain;
		this.picodeFrame = picodeFrame;
		this.isRunnable = true;
		initialize();
	}

  public void setRunnable(boolean isRunnable) {
    getBtnAddRobotPose().setEnabled(isRunnable);
    getBtnRemove().setEnabled(isRunnable);
    getJList().setEnabled(isRunnable);
    this.isRunnable = isRunnable;
  }

	public Pose getSelectedPose() {
		return getJList().getSelectedValue();
	}

	public void duplicateSelectedPose() {
		if (getJList().getSelectedIndex() != -1) {
			Pose pose = jList.getSelectedValue();
			if (pose != null) {
				picodeMain.getPoseManager().duplicate(pose);
			}
		}
	}

	public void removeSelectedPose() {
		if (getJList().getSelectedIndex() != -1) {
			Pose pose = jList.getSelectedValue();
			if (pose != null) {
				picodeMain.getPoseManager().removeElement(pose);
			}
		}
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
		setLayout(new BorderLayout(5, 5));
		add(getJPanel(), BorderLayout.NORTH);
		add(getJScrollPane(), BorderLayout.CENTER);
	}

	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new GridLayout(2, 2, 0, 0));
			JLabel jLabel = new JLabel("Pose library");
			jLabel.setFont(
					defaultFont.deriveFont(Font.BOLD));
			jPanel.add(jLabel);
			jPanel.add(getCommandPanel());
		}
		return jPanel;
	}

	private JPanel getCommandPanel() {
		if (commandPanel == null) {
			commandPanel = new JPanel();
			commandPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
			commandPanel.add(getBtnAddRobotPose(), null);
			commandPanel.add(getBtnRemove(), null);
			for (Component component : commandPanel.getComponents()) {
				component.setFont(defaultFont);
			}
		}
		return commandPanel;
	}

	private JButton getBtnAddRobotPose() {
    if (btnAddRobotPose == null) {
      btnAddRobotPose = new JButton();
      btnAddRobotPose.setAction(
          new ShowCameraFrameAction(picodeMain));
      btnAddRobotPose.setText("+");
    }
    return btnAddRobotPose;
  }

  private JButton getBtnRemove() {
    if (btnRemove == null) {
      btnRemove = new JButton();
      btnRemove.setAction(
          new DeleteSelectedPoseAction(picodeFrame));
      btnRemove.setText("-");
    }
    return btnRemove;
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
			jList = new JMutableList<Pose>(picodeMain.getPoseManager());
			jList.setListCellEditor(new PoseCellEditor());
			jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jList.setCellRenderer(new IconListRenderer(picodeMain.getPoseManager()));
			jList.setDragEnabled(true);
			jList.setDropMode(DropMode.INSERT);
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
						// TODO replace pose name in the text editor.
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
			popupMenu.add(getMntmRenameThisPose());
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
					JList jList = (JList) component;
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
			mntmApplyThisPose.setAction(new ApplySelectedPoseAction(picodeFrame));
			mntmApplyThisPose.setText("Apply this pose to the robot");
			mntmApplyThisPose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0));
		}
		return mntmApplyThisPose;
	}

	private JMenuItem getMntmRenameThisPose() {
		if (mntmRenameThisPose == null) {
			mntmRenameThisPose = new JMenuItem();
			mntmRenameThisPose.setAction(new EditSelectedPoseNameAction(picodeFrame));
			mntmRenameThisPose.setText("Rename this pose");
			mntmRenameThisPose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0));
		}
		return mntmRenameThisPose;
	}

	private JMenuItem getMntmDeleteThisPose() {
		if (mntmDeleteThisPose == null) {
			mntmDeleteThisPose = new JMenuItem();
			mntmDeleteThisPose.setAction(new DeleteSelectedPoseAction(picodeFrame));
			mntmDeleteThisPose.setText("Delete this pose");
			mntmDeleteThisPose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		}
		return mntmDeleteThisPose;
	}

	private JMenuItem getMntmDuplicateThisPose() {
		if (mntmDuplicateThisPose == null) {
			mntmDuplicateThisPose = new JMenuItem();
			mntmDuplicateThisPose.setAction(new DuplicateSelectedPoseAction(picodeFrame));
			mntmDuplicateThisPose.setText("Duplicate this pose");
			mntmDuplicateThisPose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0));
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
