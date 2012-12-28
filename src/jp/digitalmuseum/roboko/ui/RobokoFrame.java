package jp.digitalmuseum.roboko.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;

import java.awt.FlowLayout;

import com.phybots.Phybots;
import jp.digitalmuseum.roboko.RobokoMain;
import jp.digitalmuseum.roboko.action.DeleteFileAction;
import jp.digitalmuseum.roboko.action.LoadSketchAction;
import jp.digitalmuseum.roboko.action.NewFileAction;
import jp.digitalmuseum.roboko.action.NewSketchAction;
import jp.digitalmuseum.roboko.action.RenameFileAction;
import jp.digitalmuseum.roboko.action.RunAction;
import jp.digitalmuseum.roboko.action.SaveSketchAction;
import jp.digitalmuseum.roboko.action.SaveSketchAsAction;
import jp.digitalmuseum.roboko.action.StopAction;
import jp.digitalmuseum.roboko.core.Pose;
import jp.digitalmuseum.roboko.ui.editor.RobokoEditor;
import jp.digitalmuseum.roboko.ui.editor.RobokoEditorPane;
import jp.digitalmuseum.roboko.ui.library.PosePanel;
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

import processing.app.Mode;
import processing.app.RobokoSketch;
import processing.app.SketchCode;

import java.awt.event.InputEvent;

public class RobokoFrame extends JFrame {
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

	private transient RobokoMain robokoMain;
	private transient ArrayList<RobokoEditorPane> editorPanes;
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
	public RobokoFrame(RobokoMain robokoMain) {
		super();
		this.robokoMain = robokoMain;
		initialize();
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
				!robokoMain.getRobot().setPose(pose)) {
			robokoMain.setStatusText("Setting pose failed.");
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

	public void addEditor(RobokoEditor robokoEditor) {
		RobokoEditorPane robokoEditorPane = new RobokoEditorPane(robokoEditor);
		addEditor(robokoEditorPane);
	}

	private void addEditor(RobokoEditorPane robokoEditorPane) {
		editorPanes.add(robokoEditorPane);
		SketchCode code = robokoEditorPane.getRobokoEditor().getCode();
		getTabbedPane().addTab(
				code.isExtension("pde") ? code.getPrettyName() : code.getFileName(),
				null, robokoEditorPane, null);
	}

	public void removeEditor(SketchCode code) {
		Iterator<RobokoEditorPane> it = editorPanes.iterator();
		while (it.hasNext()) {
			RobokoEditorPane robokoEditorPane = it.next();
			if (robokoEditorPane.getRobokoEditor().getCode() == code) {
				it.remove();
				getTabbedPane().remove(robokoEditorPane);
				return;
			}
		}
	}

	public RobokoEditor getEditor(int index) {
		return editorPanes.get(index).getRobokoEditor();
	}

	public void showEditor(int index) {
		getTabbedPane().getModel().setSelectedIndex(index);
	}

	public int getEditorIndex() {
		return getTabbedPane().getSelectedIndex();
	}

	public RobokoEditor getCurrentEditor() {
		Component selectedComponent = getTabbedPane().getSelectedComponent();
		if (selectedComponent != null &&
				selectedComponent instanceof RobokoEditorPane) {
			return ((RobokoEditorPane) selectedComponent).getRobokoEditor();
		}
		return null;
	}

	public void updateCurrentEditorName() {
		SketchCode code = getCurrentEditor().getCode();
		getTabbedPane().setTitleAt(getEditorIndex(),
				code.isExtension("pde") ? code.getPrettyName() : code.getFileName());
	}

	public void updateTabs() {
		HashMap<SketchCode, RobokoEditorPane> map
				= new HashMap<SketchCode, RobokoEditorPane>();
		for (RobokoEditorPane robokoEditorPane : editorPanes) {
			getTabbedPane().remove(robokoEditorPane);
			map.put(robokoEditorPane.getRobokoEditor().getCode(), robokoEditorPane);
		}
		editorPanes.clear();
		RobokoSketch sketch = robokoMain.getSketch();
		for (int i = 0; i < sketch.getCodeCount(); i ++) {
			addEditor(map.get(sketch.getCode(i)));
		}
	}

	public void clearEditors() {
		Iterator<RobokoEditorPane> it = editorPanes.iterator();
		while (it.hasNext()) {
			RobokoEditorPane robokoEditorPane = it.next();
			getTabbedPane().remove(robokoEditorPane);
			it.remove();
		}
	}
	
	public RobokoEditorProxy getSketchListener() {
	  return new RobokoEditorProxy();
	}

  public static class RobokoEditorProxy {

    public void statusNotice(String string) {
      // TODO 自動生成されたメソッド・スタブ

    }

    public int getSelectionStart() {
      // TODO 自動生成されたメソッド・スタブ
      return 0;
    }

    public int getSelectionStop() {
      // TODO 自動生成されたメソッド・スタブ
      return 0;
    }

    public int getScrollPosition() {
      // TODO 自動生成されたメソッド・スタブ
      return 0;
    }

    public void setCode(SketchCode current) {
      // TODO 自動生成されたメソッド・スタブ

    }

    public void statusEdit(String string, String string2) {
      // TODO Auto-generated method stub
      
    }

    public void headerRebuild() {
      // TODO Auto-generated method stub
      
    }

    public void baseHandleClose(RobokoFrame editor, boolean b) {
      // TODO Auto-generated method stub
      
    }

    public void headerRepaint() {
      // TODO Auto-generated method stub
      
    }

    public String getText() {
      // TODO Auto-generated method stub
      return null;
    }

    public void removeRecent() {
      // TODO Auto-generated method stub
      
    }

    public void addRecent() {
      // TODO Auto-generated method stub
      
    }

    public void updateTitle() {
      // TODO Auto-generated method stub
      
    }

    public void baseRebuildSketchbookMenus() {
      // TODO Auto-generated method stub
      
    }

    public Mode getMode() {
      // TODO Auto-generated method stub
      return null;
    }
  }

	@Override
	public void dispose() {
		super.dispose();
		robokoMain.dispose();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		setJMenuBar(getMenuBar_1());
		this.setContentPane(getContentPanel());
		this.setTitle("Roboko");
		editorPanes = new ArrayList<RobokoEditorPane>();
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
			runJButton.setAction(new RunAction(robokoMain));
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
			stopJButton.setAction(new StopAction(robokoMain));
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
			statusPanel.add(new JSeparator(JSeparator.VERTICAL), null);
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
			posePanel = new PosePanel(robokoMain, this);
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
			mntmNew.setAction(new NewSketchAction(robokoMain));
			mntmNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
			mntmNew.setText("New");
		}
		return mntmNew;
	}
	private JMenuItem getMntmLoad() {
		if (mntmLoad == null) {
			mntmLoad = new JMenuItem();
			mntmLoad.setAction(new LoadSketchAction(robokoMain));
			mntmLoad.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
			mntmLoad.setText("Load");
		}
		return mntmLoad;
	}
	private JMenuItem getMntmSave() {
		if (mntmSave == null) {
			mntmSave = new JMenuItem();
			mntmSave.setAction(new SaveSketchAction(robokoMain));
			mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
			mntmSave.setText("Save");
		}
		return mntmSave;
	}
	private JMenuItem getMntmSaveAs() {
		if (mntmSaveAs == null) {
			mntmSaveAs = new JMenuItem();
			mntmSaveAs.setAction(new SaveSketchAsAction(robokoMain));
			mntmSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
			mntmSaveAs.setText("Save As…");
		}
		return mntmSaveAs;
	}

	private JTabbedPane getTabbedPane() {
		if (tabbedPane == null) {
			tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			tabbedPane.setFont(defaultFont);
			tabbedPane.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					if (getCurrentEditor() != null) {
						robokoMain.setNumberOfLines(
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
			mntmNew_1.setAction(new NewFileAction(robokoMain));
			mntmNew_1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));
			mntmNew_1.setText("New");
		}
		return mntmNew_1;
	}
	private JMenuItem getMntmRename() {
		if (mntmRename == null) {
			mntmRename = new JMenuItem();
			mntmRename.setAction(new RenameFileAction(robokoMain));
			mntmRename.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
			mntmRename.setText("Rename");
		}
		return mntmRename;
	}
	private JMenuItem getMntmDelete() {
		if (mntmDelete == null) {
			mntmDelete = new JMenuItem();
			mntmDelete.setAction(new DeleteFileAction(robokoMain));
			mntmDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
			mntmDelete.setText("Delete");
		}
		return mntmDelete;
	}
}
