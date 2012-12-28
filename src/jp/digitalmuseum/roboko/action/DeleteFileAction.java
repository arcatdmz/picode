package jp.digitalmuseum.roboko.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import jp.digitalmuseum.roboko.RobokoMain;

public class DeleteFileAction extends AbstractAction {
	private static final long serialVersionUID = 8648208348407144085L;
	private RobokoMain robokoMain;

	public DeleteFileAction(RobokoMain robokoMain) {
		this.robokoMain = robokoMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		robokoMain.getSketch().handleDeleteCode();
	}
}
