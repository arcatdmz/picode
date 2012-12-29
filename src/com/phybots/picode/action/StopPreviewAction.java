package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.Phybots;
import com.phybots.service.Camera;

public class StopPreviewAction extends AbstractAction {
	private static final long serialVersionUID = -5553183683156987089L;
	private transient Camera camera;

	public StopPreviewAction(Camera camera) {
		this.camera = camera;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Phybots.getInstance().submit(new Runnable() {
			public void run() {
				camera.stop();
			}
		});
	}
}
