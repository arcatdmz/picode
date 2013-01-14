package com.phybots.picode.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;

import java.awt.FlowLayout;

import com.phybots.Phybots;
import com.phybots.picode.Pose;
import com.phybots.picode.Robot;
import com.phybots.picode.action.DeleteActiveRobotAction;
import com.phybots.picode.action.DeleteFileAction;
import com.phybots.picode.action.LoadSketchAction;
import com.phybots.picode.action.NewFileAction;
import com.phybots.picode.action.NewRobotAction;
import com.phybots.picode.action.NewSketchAction;
import com.phybots.picode.action.RenameFileAction;
import com.phybots.picode.action.RunAction;
import com.phybots.picode.action.SaveSketchAction;
import com.phybots.picode.action.SaveSketchAsAction;
import com.phybots.picode.action.StopAction;
import com.phybots.picode.ui.editor.PicodeEditor;
import com.phybots.picode.ui.editor.PicodeEditorPane;
import com.phybots.picode.ui.library.PosePanel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import processing.app.PicodeSketch;
import processing.app.SketchCode;

import java.awt.event.InputEvent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JComboBox;

public class PicodeFrame extends JFrame {
	private static final long serialVersionUID = -7081881044895496089L;

	private static final Font defaultFont = Phybots.getInstance().getDefaultFont();

	private JPanel contentPanel = null;
	private JSplitPane splitPane = null;

	private JPanel menuPanel = null;
	private JButton runJButton = null;
	private JButton stopJButton = null;

	private JPanel statusPanel = null;
	private JLabel statusLabel = null;
	private JLabel numLineLabel = null;

	// private JSplitPane libraryPane = null;
	private PosePanel posePanel = null;
	// private PoseSetPanel poseSetPanel = null;

	private transient PicodeMain picodeMain;
	private transient ArrayList<PicodeEditorPane> editorPanes;
	private JMenuBar menuBar;
	private JMenu mnSketch;
	private JMenuItem mntmLoad;
	private JMenuItem mntmSave;
	private JTabbedPane tabbedPane;
	private JMenuItem mntmNew;
	private JMenu mnFile;
	private JMenuItem mntmNew_1;
	private JMenuItem mntmRename;
	private JMenuItem mntmDelete;
	private JMenuItem mntmSaveAs;
	private JLabel lblActiveRobotLabel;
	private JComboBox comboBox;
	private JButton minusButton;
	private JButton plusButton;

	/**
	 * This is the default constructor
	 */
	public PicodeFrame(PicodeMain picodeMain) {
		super();
		this.picodeMain = picodeMain;
		initialize();
	}

  public void setRunnable(boolean isRunnable) {
    getMnFile().setEnabled(isRunnable);
    getMnSketch().setEnabled(isRunnable);
    getRunJButton().setEnabled(isRunnable);
    getStopJButton().setEnabled(!isRunnable);
    getPosePanel().setRunnable(isRunnable);
  }

  public void setStatusText(String statusText) {
		getStatusLabel().setText(statusText);
	}

	public void setNumberOfLines(int lines) {
		getNumLineLabel().setText(String.format("%d lines", lines));
	}
	
	/*
	public void setDividerLocation(double proportionalLocation) {
		getLibraryPane().setDividerLocation(proportionalLocation);
	}
	*/

	public void applySelectedPose() {
		Pose pose = getPosePanel().getSelectedPose();
		if (pose == null ||
		    picodeMain.getActiveRobot() == null ||
				!picodeMain.getActiveRobot().setPose(pose)) {
			picodeMain.getFrame().setStatusText("Setting pose failed.");
		}
	}

	public void duplicateSelectedPose() {
		getPosePanel().duplicateSelectedPose();
	}

	public void removeSelectedPose() {
		getPosePanel().removeSelectedPose();
	}

	public void editSelectedPoseName() {
		getPosePanel().editSelectedPoseName();
	}

	public void editPoseName(Pose pose) {
		getPosePanel().editPoseName(pose);
	}

  public void addEditor(SketchCode code) {
    addEditor(new PicodeEditor(
      picodeMain, code));
  }

	public void addEditor(PicodeEditor picodeEditor) {
		PicodeEditorPane picodeEditorPane = new PicodeEditorPane(picodeEditor);
		addEditor(picodeEditorPane);
	}

	private void addEditor(PicodeEditorPane picodeEditorPane) {
		editorPanes.add(picodeEditorPane);
		SketchCode code = picodeEditorPane.getPicodeEditor().getCode();
		getTabbedPane().addTab(
				code.isExtension("pde") ? code.getPrettyName() : code.getFileName(),
				null, picodeEditorPane, null);
	}

	public void removeEditor(SketchCode code) {
		Iterator<PicodeEditorPane> it = editorPanes.iterator();
		while (it.hasNext()) {
			PicodeEditorPane picodeEditorPane = it.next();
			if (picodeEditorPane.getPicodeEditor().getCode() == code) {
				it.remove();
				getTabbedPane().remove(picodeEditorPane);
				return;
			}
		}
	}

	public PicodeEditor getEditor(int index) {
		return editorPanes.get(index).getPicodeEditor();
	}

	public void setCurrentEditorIndex(int index) {
	  if (index >= 0 && index < editorPanes.size()) {
	    getTabbedPane().getModel().setSelectedIndex(index);
	  }
	}

	public int getCurrentEditorIndex() {
		return getTabbedPane().getSelectedIndex();
	}

	public PicodeEditor getCurrentEditor() {
		Component selectedComponent = getTabbedPane().getSelectedComponent();
		if (selectedComponent != null &&
				selectedComponent instanceof PicodeEditorPane) {
			return ((PicodeEditorPane) selectedComponent).getPicodeEditor();
		}
		return null;
	}

	public void updateCurrentEditorName() {
		SketchCode code = getCurrentEditor().getCode();
		getTabbedPane().setTitleAt(getCurrentEditorIndex(),
				code.isExtension("pde") ? code.getPrettyName() : code.getFileName());
	}

  public void updateRobotList() {
    JComboBox comboBox = getComboBox();
    comboBox.removeAllItems();
    for (Robot robot : picodeMain.getRobots()) {
      comboBox.addItem(robot);
    }
  }

	public void updateTabs() {
	  // TODO Save current selection before clearance.
	  
		HashMap<SketchCode, PicodeEditorPane> map
				= new HashMap<SketchCode, PicodeEditorPane>();
		for (PicodeEditorPane picodeEditorPane : editorPanes) {
			getTabbedPane().remove(picodeEditorPane);
			map.put(picodeEditorPane.getPicodeEditor().getCode(), picodeEditorPane);
		}
		editorPanes.clear();
		PicodeSketch sketch = picodeMain.getSketch();
		for (int i = 0; i < sketch.getCodeCount(); i ++) {
			addEditor(map.get(sketch.getCode(i)));
		}
	}

	public void clearEditors() {
		Iterator<PicodeEditorPane> it = editorPanes.iterator();
		while (it.hasNext()) {
			PicodeEditorPane picodeEditorPane = it.next();
			getTabbedPane().remove(picodeEditorPane);
			it.remove();
		}
	}

  @Override
	public void dispose() {
		super.dispose();
		picodeMain.dispose();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		setJMenuBar(getMenuBar_1());
		this.setContentPane(getContentPanel());
		this.setTitle("Picode");
		editorPanes = new ArrayList<PicodeEditorPane>();
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getContentPanel() {
		if (contentPanel == null) {
			contentPanel = new JPanel();
			contentPanel.setLayout(new BorderLayout());
			contentPanel.add(getMenuPanel(), BorderLayout.NORTH);
			contentPanel.add(getStatusPanel(), BorderLayout.SOUTH);
			contentPanel.add(getSplitPane(), BorderLayout.CENTER);
		}
		return contentPanel;
	}

	private JSplitPane getSplitPane() {
		if (splitPane == null) {
			splitPane = new JSplitPane();
			splitPane.setLeftComponent(getTabbedPane());
			splitPane.setRightComponent(getPosePanel() /* getLibraryPane() */);
			splitPane.setResizeWeight(.6);
		}
		return splitPane;
	}

	/**
	 * This method initializes menuPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getMenuPanel() {
		if (menuPanel == null) {
			menuPanel = new JPanel();
			GridBagLayout gbl_menuPanel = new GridBagLayout();
			gbl_menuPanel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0};
			gbl_menuPanel.rowWeights = new double[]{0.0};
			menuPanel.setLayout(gbl_menuPanel);
			GridBagConstraints gbc_runJButton = new GridBagConstraints();
			gbc_runJButton.anchor = GridBagConstraints.NORTHWEST;
			gbc_runJButton.insets = new Insets(5, 5, 5, 5);
			gbc_runJButton.gridx = 0;
			gbc_runJButton.gridy = 0;
			menuPanel.add(getRunJButton(), gbc_runJButton);
			GridBagConstraints gbc_stopJButton = new GridBagConstraints();
			gbc_stopJButton.weightx = 1.0;
			gbc_stopJButton.insets = new Insets(5, 0, 5, 5);
			gbc_stopJButton.anchor = GridBagConstraints.NORTHWEST;
			gbc_stopJButton.gridx = 1;
			gbc_stopJButton.gridy = 0;
			menuPanel.add(getStopJButton(), gbc_stopJButton);
			GridBagConstraints gbc_lblActiveRobotLabel = new GridBagConstraints();
			gbc_lblActiveRobotLabel.anchor = GridBagConstraints.EAST;
			gbc_lblActiveRobotLabel.insets = new Insets(5, 0, 5, 5);
			gbc_lblActiveRobotLabel.gridx = 2;
			gbc_lblActiveRobotLabel.gridy = 0;
			menuPanel.add(getLblActiveRobotLabel(), gbc_lblActiveRobotLabel);
			GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.insets = new Insets(5, 0, 5, 5);
			gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox.gridx = 3;
			gbc_comboBox.gridy = 0;
			menuPanel.add(getComboBox(), gbc_comboBox);
			GridBagConstraints gbc_plusButton = new GridBagConstraints();
			gbc_plusButton.insets = new Insets(5, 0, 5, 5);
			gbc_plusButton.gridx = 4;
			gbc_plusButton.gridy = 0;
			menuPanel.add(getPlusButton(), gbc_plusButton);
			GridBagConstraints gbc_minusButton = new GridBagConstraints();
			gbc_minusButton.insets = new Insets(5, 0, 5, 5);
			gbc_minusButton.gridx = 5;
			gbc_minusButton.gridy = 0;
			menuPanel.add(getMinusButton(), gbc_minusButton);
		}
		return menuPanel;
	}

	/**
	 * This method initializes runJButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getRunJButton() {
		if (runJButton == null) {
			runJButton = new JButton();
			runJButton.setAction(new RunAction(picodeMain));
			runJButton.setText("Run");
			runJButton.setFont(defaultFont);
			runJButton.setToolTipText("Compile and run this script");
		}
		return runJButton;
	}

	/**
	 * This method initializes stopJButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getStopJButton() {
		if (stopJButton == null) {
			stopJButton = new JButton();
			stopJButton.setAction(new StopAction(picodeMain));
			stopJButton.setText("Stop");
			stopJButton.setFont(defaultFont);
		}
		return stopJButton;
	}

	/**
	 * This method initializes jLabel
	 * 
	 * @return javax.swing.JLabel
	 */
	private JPanel getStatusPanel() {
		if (statusPanel == null) {
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setAlignment(FlowLayout.LEFT);
			statusPanel = new JPanel();
			statusPanel.setLayout(flowLayout);
			statusPanel.add(getNumLineLabel(), null);
			statusPanel.add(new JSeparator(SwingConstants.VERTICAL), null);
			statusPanel.add(getStatusLabel(), null);
		}
		return statusPanel;
	}

	/**
	 * This method initializes statusLabel
	 * 
	 * @return javax.swing.JLabel
	 */
	private JLabel getStatusLabel() {
		if (statusLabel == null) {
			statusLabel = new JLabel();
			statusLabel.setFont(defaultFont);
			statusLabel.setForeground(Color.red);
		}
		return statusLabel;
	}

	/**
	 * This method initializes numLineLabel
	 * 
	 * @return javax.swing.JLabel
	 */
	private JLabel getNumLineLabel() {
		if (numLineLabel == null) {
			numLineLabel = new JLabel();
			numLineLabel.setFont(defaultFont);
		}
		return numLineLabel;
	}

	/*
	private JSplitPane getLibraryPane() {
		if (libraryPane == null) {
			libraryPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			libraryPane.setLeftComponent(getPosePanel());
			libraryPane.setRightComponent(getPoseSetPanel());
		}
		return libraryPane;
	}
	*/

	private PosePanel getPosePanel() {
		if (posePanel == null) {
			posePanel = new PosePanel(picodeMain, this);
		}
		return posePanel;
	}

	/*
	private PoseSetPanel getPoseSetPanel() {
		if (poseSetPanel == null) {
			poseSetPanel = new PoseSetPanel();
		}
		return poseSetPanel;
	}
	*/

	private JMenuBar getMenuBar_1() {
		if (menuBar == null) {
			menuBar = new JMenuBar();
			menuBar.add(getMnSketch());
			menuBar.add(getMnFile());
		}
		return menuBar;
	}
	private JMenu getMnSketch() {
		if (mnSketch == null) {
			mnSketch = new JMenu("Sketch");
			mnSketch.add(getMntmNew());
			mnSketch.add(getMntmLoad());
			mnSketch.add(getMntmSave());
			mnSketch.add(getMntmSaveAs());
		}
		return mnSketch;
	}
	private JMenuItem getMntmNew() {
		if (mntmNew == null) {
			mntmNew = new JMenuItem();
			mntmNew.setAction(new NewSketchAction(picodeMain));
			mntmNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
			mntmNew.setText("New");
		}
		return mntmNew;
	}
	private JMenuItem getMntmLoad() {
		if (mntmLoad == null) {
			mntmLoad = new JMenuItem();
			mntmLoad.setAction(new LoadSketchAction(picodeMain));
			mntmLoad.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
			mntmLoad.setText("Load");
		}
		return mntmLoad;
	}
	private JMenuItem getMntmSave() {
		if (mntmSave == null) {
			mntmSave = new JMenuItem();
			mntmSave.setAction(new SaveSketchAction(picodeMain));
			mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
			mntmSave.setText("Save");
		}
		return mntmSave;
	}
	private JMenuItem getMntmSaveAs() {
		if (mntmSaveAs == null) {
			mntmSaveAs = new JMenuItem();
			mntmSaveAs.setAction(new SaveSketchAsAction(picodeMain));
			mntmSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
			mntmSaveAs.setText("Save As…");
		}
		return mntmSaveAs;
	}

	private JTabbedPane getTabbedPane() {
		if (tabbedPane == null) {
			tabbedPane = new JTabbedPane(SwingConstants.TOP);
			tabbedPane.setFont(defaultFont);
			tabbedPane.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					if (getCurrentEditor() != null) {
						picodeMain.getFrame().setNumberOfLines(
								getCurrentEditor().getCode().getLineCount());
					}
				}
			});
		}
		return tabbedPane;
	}
	private JMenu getMnFile() {
		if (mnFile == null) {
			mnFile = new JMenu("File");
			mnFile.add(getMntmNew_1());
			mnFile.add(getMntmRename());
			mnFile.add(getMntmDelete());
		}
		return mnFile;
	}
	private JMenuItem getMntmNew_1() {
		if (mntmNew_1 == null) {
			mntmNew_1 = new JMenuItem();
			mntmNew_1.setAction(new NewFileAction(picodeMain));
			mntmNew_1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));
			mntmNew_1.setText("New");
		}
		return mntmNew_1;
	}
	private JMenuItem getMntmRename() {
		if (mntmRename == null) {
			mntmRename = new JMenuItem();
			mntmRename.setAction(new RenameFileAction(picodeMain));
			mntmRename.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
			mntmRename.setText("Rename");
		}
		return mntmRename;
	}
	private JMenuItem getMntmDelete() {
		if (mntmDelete == null) {
			mntmDelete = new JMenuItem();
			mntmDelete.setAction(new DeleteFileAction(picodeMain));
			mntmDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
			mntmDelete.setText("Delete");
		}
		return mntmDelete;
	}
  private JLabel getLblActiveRobotLabel() {
    if (lblActiveRobotLabel == null) {
    	lblActiveRobotLabel = new JLabel("Active robot:");
    	lblActiveRobotLabel.setFont(defaultFont);
    }
    return lblActiveRobotLabel;
  }
  private JComboBox getComboBox() {
    if (comboBox == null) {
    	comboBox = new JComboBox();
    	Dimension preferredSize = comboBox.getPreferredSize();
    	if (preferredSize == null) {
    	  preferredSize = new Dimension();
    	}
    	preferredSize.setSize(150, preferredSize.getHeight());
      comboBox.setPreferredSize(preferredSize);
    	comboBox.setFont(defaultFont);
    	comboBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            picodeMain.setActiveRobot((Robot) e.getItem());
          }
        }
      });
    }
    return comboBox;
  }
  private JButton getMinusButton() {
    if (minusButton == null) {
    	minusButton = new JButton();
    	minusButton.setAction(new DeleteActiveRobotAction(picodeMain));
    	minusButton.setFont(defaultFont);
    	minusButton.setText("-");
    }
    return minusButton;
  }
  private JButton getPlusButton() {
    if (plusButton == null) {
    	plusButton = new JButton();
    	plusButton.setAction(new NewRobotAction(picodeMain));
    	plusButton.setFont(defaultFont);
    	plusButton.setText("+");
    }
    return plusButton;
  }
}
