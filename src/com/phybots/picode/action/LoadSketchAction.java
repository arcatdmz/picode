package com.phybots.picode.action;

import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;

import processing.app.PicodeSketch;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.PicodeSettings;

public class LoadSketchAction extends AbstractAction {
	private static final long serialVersionUID = -5270448663489465136L;
	private PicodeMain picodeMain;

	public LoadSketchAction(PicodeMain picodeMain) {
		this.picodeMain = picodeMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		FileDialog fd = new FileDialog(
				picodeMain.getFrame(),
				"Open a Picode sketch...",
				FileDialog.LOAD);
		fd.setDirectory(PicodeSettings.getProjectsFolderPath());
		fd.setVisible(true);

		String newParentDir = fd.getDirectory();
	    String newName = fd.getFile();

	    // user canceled selection
	    if (newName == null) return;

	    // check to make sure that this .pde file is
	    // in a folder of the same name
	    File parentFile = new File(newParentDir);
	    String parentName = parentFile.getName();
	    String pdeName = parentName + ".pde";
	    
	    try {
			PicodeSketch picodeSketch = new PicodeSketch(picodeMain,
					parentFile.getAbsolutePath() + File.separatorChar + pdeName);
		    picodeMain.loadSketch(picodeSketch);
		} catch (IOException e1) {
			return;
		}
	}
}
