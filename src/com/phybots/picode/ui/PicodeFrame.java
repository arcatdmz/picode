package com.phybots.picode.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;

import java.awt.FlowLayout;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.TypeBasedPoseLibrary;
import com.phybots.picode.action.DeleteFileAction;
import com.phybots.picode.action.LoadSketchAction;
import com.phybots.picode.action.NewFileAction;
import com.phybots.picode.action.NewSketchAction;
import com.phybots.picode.action.PublishAsHTMLAction;
import com.phybots.picode.action.RedoAction;
import com.phybots.picode.action.RenameFileAction;
import com.phybots.picode.action.RunAction;
import com.phybots.picode.action.SaveSketchAction;
import com.phybots.picode.action.SaveSketchAsAction;
import com.phybots.picode.action.StopAction;
import com.phybots.picode.action.ToggleInlinePhotoEnabled;
import com.phybots.picode.action.UndoAction;
import com.phybots.picode.api.PicodeInterface;
import com.phybots.picode.api.Pose;
import com.phybots.picode.api.Poser;
import com.phybots.picode.api.PoserLibrary;
import com.phybots.picode.api.PoserTypeInfo;
import com.phybots.picode.api.PoserWithConnector;
import com.phybots.picode.ui.editor.PicodeEditor;
import com.phybots.picode.ui.editor.PicodeEditorPane;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import processing.app.SketchCode;

import java.awt.event.InputEvent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.border.EmptyBorder;
import javax.swing.JCheckBoxMenuItem;

public class PicodeFrame extends JFrame implements PicodeInterface {
	private static final long serialVersionUID = -7081881044895496089L;
	private static final Font defaultFont = PicodeMain.getDefaultFont();

	private JPanel contentPanel = null;
	private JSplitPane splitPane = null;

	private JPanel menuPanel = null;
	private JButton btnRun = null;
	private JButton btnStop = null;

	private JPanel statusPanel = null;
	private JLabel statusLabel = null;
	private JLabel numLineLabel = null;

	private JPanel editorContainer = null;
	private JPanel libraryPane = null;
	private PoserSelectorPanel poserPanel = null;
	private PoseLibraryPanel poseLibraryPanel = null;

	private JMenuBar menuBar;
	private JMenu mnSketch;
	private JMenuItem mntmLoad;
	private JMenuItem mntmSave;
	private JTabbedPane tabbedPane;
	private JMenuItem mntmNew;
	private JMenu mnFile;
	private JMenuItem mntmNewFile;
	private JMenuItem mntmRenameFile;
	private JMenuItem mntmDeleteFile;
	private JMenuItem mntmSaveAs;

	private transient PicodeMain picodeMain;
	private transient ArrayList<PicodeEditorPane> editorPanes;
	private transient PicodeEditor currentEditor;
	private transient int currentEditorIndex;
	private transient Map<PoserTypeInfo, TypeBasedPoseLibrary> libraries;
	private JMenu mnView;
	private JCheckBoxMenuItem chckbxmntmShowInlinePhotos;
	private JMenuItem mntmPublishHTML;
	private JButton btnUndo;
	private JButton btnRedo;
	private JPanel editorPanel;

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
		getMnView().setEnabled(isRunnable);
		getBtnRun().setEnabled(isRunnable);
		getBtnStop().setEnabled(!isRunnable);
		getPoserPanel().setRunnable(isRunnable);
		getPosePanel().setRunnable(isRunnable);
	}

	public String getStatusText() {
		return getStatusLabel().getText();
	}

	public void setErrorText(String errorText) {
		getStatusLabel().setForeground(Color.red);
		getStatusLabel().setText(errorText);
	}

	public void setStatusText(String statusText) {
		getStatusLabel().setForeground(Color.black);
		getStatusLabel().setText(statusText);
	}

	public void setNumberOfLines(int lines) {
		getNumLineLabel().setText(String.format("%d lines", lines));
	}

	public Set<PicodeEditor> getEditors() {
		Set<PicodeEditor> editors = new HashSet<PicodeEditor>();
		for (PicodeEditorPane editorPane : editorPanes) {
			editors.add(editorPane.getPicodeEditor());
		}
		return editors;
	}

	public void addEditor(SketchCode code) {
		PicodeEditor picodeEditor = new PicodeEditor(picodeMain, code);

		PicodeEditorPane picodeEditorPane = new PicodeEditorPane(picodeEditor);
		editorPanes.add(picodeEditorPane);

		String title = getTitle(picodeEditor);
		getTabbedPane().addTab(title, null, picodeEditorPane, null);

		currentEditorIndex = editorPanes.size() - 1;
		currentEditor = picodeEditor;
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
		return currentEditorIndex;
	}

	public PicodeEditor getCurrentEditor() {
		return currentEditor;
	}

	public void updateEditorNames() {
		for (int i = 0; i < editorPanes.size(); i++) {
			updateEditorName(i);
		}
	}

	public void updateCurrentEditorName() {
		updateEditorName(getCurrentEditorIndex());
	}

	private void updateEditorName(int i) {
		if (i >= 0 && i < editorPanes.size()) {
			getTabbedPane().setTitleAt(
					i,
					getTitle(getEditor(i)));
		}
	}
	
	private String getTitle(PicodeEditor picodeEditor) {
		SketchCode code = picodeEditor.getCode();
		return code.isExtension("pde") ? code.getPrettyName() : code.getFileName();
	}

	public void clearEditors() {
		Iterator<PicodeEditorPane> it = editorPanes.iterator();
		while (it.hasNext()) {
			PicodeEditorPane picodeEditorPane = it.next();
			getTabbedPane().remove(picodeEditorPane);
			it.remove();
		}
	}

	public Pose getSelectedPose() {
		return getPosePanel().getSelectedPose();
	}

	public void editSelectedPoseName() {
		getPosePanel().editSelectedPoseName();
	}

	public boolean isInlinePhotoEnabled() {
		return getChckbxmntmShowInlinePhotos().isSelected();
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
		libraries = new HashMap<PoserTypeInfo, TypeBasedPoseLibrary>();
		listPoserTypes();
		getTabbedPane().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
                if (e.getSource() instanceof JTabbedPane) {
                    JTabbedPane pane = (JTabbedPane) e.getSource();
                    currentEditorIndex = pane.getSelectedIndex();
                    if (currentEditorIndex < 0) {
                    	currentEditorIndex = 0;
                    }
                    currentEditor = editorPanes.get(currentEditorIndex).getPicodeEditor();
                }
			}
		});
	}

	private void listPoserTypes() {
		for (PoserTypeInfo poserType :
				PoserLibrary.getTypeInfos()) {
			TypeBasedPoseLibrary library = new TypeBasedPoseLibrary(poserType);
			libraries.put(poserType, library);
		}
	}

	public TypeBasedPoseLibrary getTypeBasedLibrary(PoserTypeInfo poserType) {
		return libraries.get(poserType);
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
			splitPane.setLeftComponent(getEditorContainer());
			splitPane.setRightComponent(getLibraryPane());
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
			gbl_menuPanel.columnWeights = new double[] { 0.0, 0.0 };
			gbl_menuPanel.rowWeights = new double[] { 0.0 };
			menuPanel.setLayout(gbl_menuPanel);
			GridBagConstraints gbc_runJButton = new GridBagConstraints();
			gbc_runJButton.anchor = GridBagConstraints.NORTHWEST;
			gbc_runJButton.insets = new Insets(5, 5, 5, 5);
			gbc_runJButton.gridx = 0;
			gbc_runJButton.gridy = 0;
			menuPanel.add(getBtnRun(), gbc_runJButton);
			GridBagConstraints gbc_stopJButton = new GridBagConstraints();
			gbc_stopJButton.weightx = 1.0;
			gbc_stopJButton.insets = new Insets(5, 0, 5, 5);
			gbc_stopJButton.anchor = GridBagConstraints.NORTHWEST;
			gbc_stopJButton.gridx = 1;
			gbc_stopJButton.gridy = 0;
			menuPanel.add(getBtnStop(), gbc_stopJButton);
		}
		return menuPanel;
	}

	/**
	 * This method initializes runJButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getBtnRun() {
		if (btnRun == null) {
			btnRun = new JButton();
			btnRun.setAction(new RunAction(picodeMain));
			btnRun.setFont(defaultFont);
		}
		return btnRun;
	}

	/**
	 * This method initializes stopJButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getBtnStop() {
		if (btnStop == null) {
			btnStop = new JButton();
			btnStop.setAction(new StopAction(picodeMain));
			btnStop.setFont(defaultFont);
		}
		return btnStop;
	}

	public JButton getBtnUndo() {
		if (btnUndo == null) {
			btnUndo = new JButton();
			btnUndo.setAction(new UndoAction(picodeMain));
			btnUndo.setFont(defaultFont);
			btnUndo.setEnabled(false);
		}
		return btnUndo;
	}
	public JButton getBtnRedo() {
		if (btnRedo == null) {
			btnRedo = new JButton();
			btnRedo.setAction(new RedoAction(picodeMain));
			btnRedo.setFont(defaultFont);
			btnRedo.setEnabled(false);
		}
		return btnRedo;
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

	private JPanel getEditorContainer() {
		if (editorContainer == null) {
			editorContainer = new JPanel();
			editorContainer.setLayout(new BorderLayout(0, 0));
			editorContainer.add(getTabbedPane(), BorderLayout.CENTER);
			editorContainer.add(getEditorPanel(), BorderLayout.SOUTH);
		}
		return editorContainer;
	}

	private JPanel getLibraryPane() {
		if (libraryPane == null) {
			libraryPane = new JPanel();
			libraryPane.setLayout(new BorderLayout(0, 0));
			libraryPane.add(getPoserPanel(), BorderLayout.NORTH);
			libraryPane.add(getPosePanel());
		}
		return libraryPane;
	}

	public PoserSelectorPanel getPoserPanel() {
		if (poserPanel == null) {
			poserPanel = new PoserSelectorPanel(picodeMain);
			poserPanel.setBorder(new EmptyBorder(5, 5, 0, 5));
		}
		return poserPanel;
	}

	public PoseLibraryPanel getPosePanel() {
		if (poseLibraryPanel == null) {
			poseLibraryPanel = new PoseLibraryPanel(picodeMain);
			poseLibraryPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		}
		return poseLibraryPanel;
	}

	private JMenuBar getMenuBar_1() {
		if (menuBar == null) {
			menuBar = new JMenuBar();
			menuBar.add(getMnSketch());
			menuBar.add(getMnFile());
			menuBar.add(getMnView());
		}
		return menuBar;
	}

	private JMenu getMnSketch() {
		if (mnSketch == null) {
			mnSketch = new JMenu("Sketch");
			mnSketch.setMnemonic('s');
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
			mntmNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
					InputEvent.CTRL_MASK));
			mntmNew.setText("New");
		}
		return mntmNew;
	}

	private JMenuItem getMntmLoad() {
		if (mntmLoad == null) {
			mntmLoad = new JMenuItem();
			mntmLoad.setAction(new LoadSketchAction(picodeMain));
			mntmLoad.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
					InputEvent.CTRL_MASK));
			mntmLoad.setText("Load");
		}
		return mntmLoad;
	}

	private JMenuItem getMntmSave() {
		if (mntmSave == null) {
			mntmSave = new JMenuItem();
			mntmSave.setAction(new SaveSketchAction(picodeMain));
			mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
					InputEvent.CTRL_MASK));
			mntmSave.setText("Save");
		}
		return mntmSave;
	}

	private JMenuItem getMntmSaveAs() {
		if (mntmSaveAs == null) {
			mntmSaveAs = new JMenuItem();
			mntmSaveAs.setAction(new SaveSketchAsAction(picodeMain));
			mntmSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
					InputEvent.CTRL_MASK));
			mntmSaveAs.setText("Save As…");
		}
		return mntmSaveAs;
	}

	private JTabbedPane getTabbedPane() {
		if (tabbedPane == null) {
			tabbedPane = new JTabbedPane(SwingConstants.TOP);
			tabbedPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			tabbedPane.setFont(defaultFont);
			tabbedPane.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					PicodeEditor currentEditor = getCurrentEditor();
					if (currentEditor != null) {
						picodeMain.getFrame().setNumberOfLines(
								currentEditor.getCode().getLineCount());
						getBtnUndo().setEnabled(
								currentEditor.getDocumentManager().canUndo());
						getBtnRedo().setEnabled(
								currentEditor.getDocumentManager().canRedo());
					}
				}
			});
		}
		return tabbedPane;
	}

	private JMenu getMnFile() {
		if (mnFile == null) {
			mnFile = new JMenu("File");
			mnFile.setMnemonic('f');
			mnFile.add(getMntmNewFile());
			mnFile.add(getMntmRenameFile());
			mnFile.add(getMntmDeleteFile());
		}
		return mnFile;
	}

	private JMenuItem getMntmNewFile() {
		if (mntmNewFile == null) {
			mntmNewFile = new JMenuItem();
			mntmNewFile.setAction(new NewFileAction(picodeMain));
			mntmNewFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
					InputEvent.CTRL_MASK));
			mntmNewFile.setText("New");
		}
		return mntmNewFile;
	}

	private JMenuItem getMntmRenameFile() {
		if (mntmRenameFile == null) {
			mntmRenameFile = new JMenuItem();
			mntmRenameFile.setAction(new RenameFileAction(picodeMain));
			mntmRenameFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
					InputEvent.CTRL_MASK));
			mntmRenameFile.setText("Rename");
		}
		return mntmRenameFile;
	}

	private JMenuItem getMntmDeleteFile() {
		if (mntmDeleteFile == null) {
			mntmDeleteFile = new JMenuItem();
			mntmDeleteFile.setAction(new DeleteFileAction(picodeMain));
			mntmDeleteFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
					InputEvent.CTRL_MASK));
			mntmDeleteFile.setText("Delete");
		}
		return mntmDeleteFile;
	}

	private JMenu getMnView() {
		if (mnView == null) {
			mnView = new JMenu("View");
			mnView.setMnemonic('v');
			mnView.add(getChckbxmntmShowInlinePhotos());
			mnView.add(getMntmPublishHTML());
		}
		return mnView;
	}

	private JCheckBoxMenuItem getChckbxmntmShowInlinePhotos() {
		if (chckbxmntmShowInlinePhotos == null) {
			chckbxmntmShowInlinePhotos = new JCheckBoxMenuItem();
			chckbxmntmShowInlinePhotos.setSelected(true);
			chckbxmntmShowInlinePhotos.setAction(new ToggleInlinePhotoEnabled(picodeMain));
		}
		return chckbxmntmShowInlinePhotos;
	}

	private JMenuItem getMntmPublishHTML() {
		if (mntmPublishHTML == null) {
			mntmPublishHTML = new JMenuItem();
			mntmPublishHTML.setAction(new PublishAsHTMLAction(picodeMain));
		}
		return mntmPublishHTML;
	}

	public void onAddPoser(Poser poser) {
		if (poser instanceof PoserWithConnector) {
			getPoserPanel().onAddPoserWithConnector(poser);
		}
	}

	public void onRemovePoser(Poser poser) {
		if (poser instanceof PoserWithConnector) {
			getPoserPanel().onRemovePoserWithConnector(poser);
		}
	}

	public void onCurrentPoserChange(Poser poser) {
		getPoserPanel().onCurrentPoserChange(poser);
		if (poser == null) {
			getPosePanel().setPoseLibrary(null);
		} else {
			getPosePanel().setPoseLibrary(
					libraries.get(poser.getPoserType()));
		}
	}

	public void onAddPose(Pose pose) {
		TypeBasedPoseLibrary library = libraries.get(pose.getPoserType());
		int index;
		if (library.getSize() > 0) {
			index = library.getSize();
			for (int i = 0; i < library.getSize(); i ++) {
				Pose p = library.get(i);
				if (p.getName().compareToIgnoreCase(pose.getName()) > 0) {
					index = i;
					break;
				}
			}
		} else {
			index = 0;
		}
		library.add(index, pose);
	}

	public void onRemovePose(Pose pose) {
		TypeBasedPoseLibrary library = libraries.get(pose.getPoserType());
		library.removeElement(pose);
	}

	private JPanel getEditorPanel() {
		if (editorPanel == null) {
			editorPanel = new JPanel();
			GridBagLayout gbl_editorPanel = new GridBagLayout();
			gbl_editorPanel.columnWeights = new double[]{0.0, 0.0};
			gbl_editorPanel.rowWeights = new double[]{0.0};
			editorPanel.setLayout(gbl_editorPanel);
			GridBagConstraints gbc_btnUndo = new GridBagConstraints();
			gbc_btnUndo.weightx = 1.0;
			gbc_btnUndo.anchor = GridBagConstraints.NORTHEAST;
			gbc_btnUndo.insets = new Insets(0, 5, 5, 5);
			gbc_btnUndo.gridx = 0;
			gbc_btnUndo.gridy = 0;
			editorPanel.add(getBtnUndo(), gbc_btnUndo);
			GridBagConstraints gbc_btnRedo = new GridBagConstraints();
			gbc_btnRedo.insets = new Insets(0, 0, 5, 5);
			gbc_btnRedo.anchor = GridBagConstraints.NORTHEAST;
			gbc_btnRedo.gridx = 1;
			gbc_btnRedo.gridy = 0;
			editorPanel.add(getBtnRedo(), gbc_btnRedo);
		}
		return editorPanel;
	}
}
