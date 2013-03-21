package com.phybots.picode.action;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.AbstractAction;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.ui.editor.PicodeEditor;

public class PublishAsHTMLAction extends AbstractAction {
	private static final long serialVersionUID = 970920955888702503L;
	private PicodeMain picodeMain;
	public PublishAsHTMLAction(PicodeMain picodeMain) {
		putValue(NAME, "Publish as a HTML page");
		putValue(SHORT_DESCRIPTION, "Publish current sketch as a HTML page.");
		this.picodeMain = picodeMain;
	}
	public void actionPerformed(ActionEvent e) {
		PicodeEditor editor = picodeMain.getFrame().getCurrentEditor();
		try {
			File file = new File(
					picodeMain.getSketch().getFolder(),
					editor.getCode().getFileName() + ".html");
			FileWriter writer = new FileWriter(file);
			writer.append(editor.getDocumentManager().getHTML("../../poses/"));
			writer.close();
			Desktop.getDesktop().open(file);
		} catch (IOException e1) {
			// Do nothing.
		}
	}
}