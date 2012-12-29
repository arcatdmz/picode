package com.phybots.picode.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.phybots.Phybots;
import com.phybots.service.Camera;

public class StartPreviewAction extends AbstractAction {
	private static final long serialVersionUID = 7098817610277770499L;
	private transient Camera camera;

	public StartPreviewAction(Camera camera) {
		this.camera = camera;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Phybots.getInstance().submit(new Runnable() {
			public void run() {
				try {
					camera.start();
				} catch (Exception e) {
					// Do nothing.
				}
			}
		});
	}
}
