package com.phybots.picode.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.ui.PicodeFrame;
import com.phybots.picode.ui.editor.PicodeEditor;

public class RedoAction extends AbstractAction {
	private static final long serialVersionUID = -1176709186562528175L;
	private PicodeMain picodeMain;

	public RedoAction(PicodeMain picodeMain) {
		this.picodeMain = picodeMain;
		putValue(NAME, "Redo");
		putValue(SHORT_DESCRIPTION, "Redo the last undo in the editor.");
		putValue(SMALL_ICON, new ImageIcon(PicodeFrame.class.getResource("/redo.png")));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		PicodeEditor currentEditor = picodeMain.getFrame().getCurrentEditor();
		if (currentEditor != null) {
			currentEditor.getDocumentManager().redo();
			picodeMain.getFrame().getBtnUndo().setEnabled(
					currentEditor.getDocumentManager().canUndo());
			picodeMain.getFrame().getBtnRedo().setEnabled(
					currentEditor.getDocumentManager().canRedo());
		}
	}
}
