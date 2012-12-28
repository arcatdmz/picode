package jp.digitalmuseum.roboko.action;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;

import jp.digitalmuseum.roboko.RobokoMain;

public class SaveSketchAsAction extends AbstractAction {
	private static final long serialVersionUID = -6036135232665670857L;
	private RobokoMain robokoMain;

	public SaveSketchAsAction(RobokoMain robokoMain) {
		this.robokoMain = robokoMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			robokoMain.getSketch().saveAs();
		} catch (IOException e1) {
			// 
		}
	}
}
