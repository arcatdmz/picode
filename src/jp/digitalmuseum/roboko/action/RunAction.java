package jp.digitalmuseum.roboko.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import processing.app.SketchException;

import jp.digitalmuseum.roboko.RobokoMain;
import jp.digitalmuseum.roboko.builder.Builder;

public class RunAction extends AbstractAction {
	private static final long serialVersionUID = -8531811907562726754L;
	private RobokoMain robokoMain;

	public RunAction(RobokoMain robokoMain) {
		this.robokoMain = robokoMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (robokoMain.getLauncher() != null) {
			new StopAction(robokoMain).actionPerformed(e);
		}
		Builder builder = new Builder(robokoMain, robokoMain.getSketch());
		try {
			robokoMain.getRobot().disconnect();
			robokoMain.setLauncher(builder.run());
		} catch (SketchException se) {
			robokoMain.handleSketchException(se);
			robokoMain.getRobot().connect();
		}
	}
}
