package com.phybots.picode.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;

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
import com.phybots.picode.action.DeleteFileAction;
import com.phybots.picode.action.LoadSketchAction;
import com.phybots.picode.action.NewFileAction;
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

	/**
	 * This is the default constructor
	 */
	public PicodeFrame(PicodeMain picodeMain) {
		super();
		this.picodeMain = picodeMain;
		initialize();
	}

  public void setSketchLocation(Point point) {
    // TODO Auto-generated method stub
    
  }

  public Point getSketchLocation() {
    // TODO Auto-generated method stub
    return null;
  }

	public void setStatusText(String statusText) {
		getStatusLabel().setText(statusText);
	}

	public void setNumberOfLines(int lines) {
		getNumLineLabel().setText(String.format("%d lines", lines));
	}
	
  public void updateTitle() {
    setTitle(String.format("%s | Picode",
      picodeMain.getSketch().getName()));   
  }

	/*
	public void setDividerLocation(double proportionalLocation) {
		getLibraryPane().setDividerLocation(proportionalLocation);
	}
	*/

	public void applySelectedPose() {
		Pose pose = getPosePanel().getSelectedPose();
		if (pose == null ||
				!picodeMain.getRobot().setPose(pose)) {
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

	public void showEditor(int index) {
		getTabbedPane().getModel().setSelectedIndex(index);
	}

	public int getEditorIndex() {
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
		getTabbedPane().setTitleAt(getEditorIndex(),
				code.isExtension("pde") ? code.getPrettyName() : code.getFileName());
	}

	public void updateTabs() {
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
			FlowLayout fl_menuPanel = new FlowLayout();
			fl_menuPanel.setAlignment(FlowLayout.LEFT);
			menuPanel = new JPanel();
			menuPanel.setLayout(fl_menuPanel);
			menuPanel.add(getRunJButton(), null);
			menuPanel.add(getStopJButton(), null);
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
}
