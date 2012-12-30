package com.phybots.picode.ui.editor;

import javax.swing.JScrollPane;

public class PicodeEditorPane extends JScrollPane {
	private static final long serialVersionUID = 2560377694389172816L;
	private PicodeEditor picodeEditor;

	public PicodeEditorPane(PicodeEditor picodeEditor) {
		this.picodeEditor = picodeEditor;
		picodeEditor.setOuterScrollPane(this);
		setViewportView(picodeEditor);
	}

	public PicodeEditor getPicodeEditor() {
		return picodeEditor;
	}
}
