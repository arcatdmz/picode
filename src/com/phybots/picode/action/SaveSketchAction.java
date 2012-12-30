package com.phybots.picode.action;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;

import processing.app.PicodeSketch;

import com.phybots.picode.ui.PicodeMain;

public class SaveSketchAction extends AbstractAction {
	private static final long serialVersionUID = 3777538819909312326L;
	private PicodeMain picodeMain;

	public SaveSketchAction(PicodeMain picodeMain) {
		this.picodeMain = picodeMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
		  PicodeSketch sketch = picodeMain.getSketch();
		  if (sketch.isUntitled()) {
		    sketch.saveAs();
		  } else {
		    sketch.save();
		  }
		} catch (IOException e1) {
			picodeMain.getPintegration().statusError(e1);
		}
	}
}
