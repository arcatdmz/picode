package jp.digitalmuseum.roboko.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import jp.digitalmuseum.roboko.RobokoMain;

public class RenameFileAction extends AbstractAction {
	private static final long serialVersionUID = -301527980746520731L;
	private RobokoMain robokoMain;

	public RenameFileAction(RobokoMain robokoMain) {
		this.robokoMain = robokoMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		robokoMain.getSketch().handleRenameCode();
	}
}
