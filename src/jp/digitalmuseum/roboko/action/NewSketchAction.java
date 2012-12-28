package jp.digitalmuseum.roboko.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import processing.app.RobokoSketch;

import jp.digitalmuseum.roboko.RobokoMain;

public class NewSketchAction extends AbstractAction {
	private static final long serialVersionUID = 2044354384275000188L;
	private RobokoMain robokoMain;

	public NewSketchAction(RobokoMain robokoMain) {
		this.robokoMain = robokoMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		RobokoSketch robokoSketch = RobokoSketch.newInstance(robokoMain);
		if (robokoSketch != null) {
			robokoMain.setSketch(robokoSketch);
		}
	}
}
