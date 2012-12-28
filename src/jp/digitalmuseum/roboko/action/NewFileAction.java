package jp.digitalmuseum.roboko.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import jp.digitalmuseum.roboko.RobokoMain;

public class NewFileAction extends AbstractAction {
	private static final long serialVersionUID = -5886426071833985789L;
	private RobokoMain robokoMain;

	public NewFileAction(RobokoMain robokoMain) {
		this.robokoMain = robokoMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		robokoMain.getSketch().handleNewCode();
	}
}
