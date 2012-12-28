package jp.digitalmuseum.roboko.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jp.digitalmuseum.roboko.RobokoMain;

public class HideCameraFrameAction extends AbstractAction {
	private static final long serialVersionUID = -2297070480522312162L;
	private transient RobokoMain robokoMain;

	public HideCameraFrameAction(RobokoMain robokoMain) {
		this.robokoMain = robokoMain;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		robokoMain.showCaptureFrame(false);
	}
}
