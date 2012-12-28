package jp.digitalmuseum.roboko.action;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;

import jp.digitalmuseum.roboko.RobokoMain;

public class SaveSketchAction extends AbstractAction {
	private static final long serialVersionUID = 3777538819909312326L;
	private RobokoMain robokoMain;

	public SaveSketchAction(RobokoMain robokoMain) {
		this.robokoMain = robokoMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			robokoMain.getSketch().save();
		} catch (IOException e1) {
			// 
		}
	}
}
