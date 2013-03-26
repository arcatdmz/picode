package com.phybots.picode.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.ui.PicodeFrame;
import com.phybots.picode.ui.editor.PicodeEditor;

public class UndoAction extends AbstractAction {
	private static final long serialVersionUID = -2527502040060777728L;
	private PicodeMain picodeMain;

	public UndoAction(PicodeMain picodeMain) {
		this.picodeMain = picodeMain;
		putValue(NAME, "Undo");
		putValue(SHORT_DESCRIPTION, "Undo the last edit in the editor.");
		putValue(SMALL_ICON, new ImageIcon(PicodeFrame.class.getResource("/undo.png")));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		PicodeEditor currentEditor = picodeMain.getFrame().getCurrentEditor();
		if (currentEditor != null) {
			currentEditor.undo();
			picodeMain.getFrame().getBtnUndo().setEnabled(currentEditor.canUndo());
			picodeMain.getFrame().getBtnRedo().setEnabled(currentEditor.canRedo());
		}
	}
}
