package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.ui.editor.PicodeEditor;

public class ToggleInlinePhotoEnabled extends AbstractAction {
	private static final long serialVersionUID = 6991077289911482703L;
	private PicodeMain picodeMain;
	public ToggleInlinePhotoEnabled(PicodeMain picodeMain) {
		putValue(NAME, "Show inline photos");
		putValue(SHORT_DESCRIPTION, "Toggle the view between inline photos and raw text API.");
		this.picodeMain = picodeMain;
	}
	public void actionPerformed(ActionEvent e) {
		PicodeEditor editor = picodeMain.getFrame().getCurrentEditor();
		boolean isInlinePhotoEnabled = picodeMain.getFrame().isInlinePhotoEnabled();
		editor.getDocumentManager().setInlinePhotoEnabled(isInlinePhotoEnabled);
	}
}