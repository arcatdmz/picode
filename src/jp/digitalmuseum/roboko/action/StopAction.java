package jp.digitalmuseum.roboko.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jp.digitalmuseum.roboko.RobokoMain;
import jp.digitalmuseum.roboko.builder.Launcher;

public class StopAction extends AbstractAction {
	private static final long serialVersionUID = 7556364882375393913L;
	private RobokoMain robokoMain;

	public StopAction(RobokoMain robokoMain) {
		this.robokoMain = robokoMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Launcher launcher = robokoMain.getLauncher();
		if (launcher != null) {
			launcher.close();
		}
	}
}
