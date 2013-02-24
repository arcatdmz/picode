package com.phybots.picode.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import processing.app.PicodeSketch;

import com.phybots.picode.PicodeMain;

public class NewSketchAction extends AbstractAction {
	private static final long serialVersionUID = 2044354384275000188L;
	private PicodeMain picodeMain;

	public NewSketchAction(PicodeMain picodeMain) {
		this.picodeMain = picodeMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	  if (picodeMain.getPintegration().checkModified()) {
  		PicodeSketch picodeSketch = PicodeSketch.newInstance(picodeMain);
  		if (picodeSketch != null) {
  			picodeMain.setSketch(picodeSketch);
  		}
	  }
	}
}
