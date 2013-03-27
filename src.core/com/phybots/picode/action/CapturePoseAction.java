package com.phybots.picode.action;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import com.phybots.picode.api.Pose;
import com.phybots.picode.api.Poser;
import com.phybots.picode.api.PoserLibrary;
import com.phybots.picode.ui.camera.CameraFrame;

public class CapturePoseAction extends AbstractAction {
	private static final long serialVersionUID = -4081128637015341817L;
	private static boolean called;

	public CapturePoseAction() {
		putValue(NAME, "Capture");
		putValue(SHORT_DESCRIPTION, "Capture the current pose.");
		putValue(SMALL_ICON, new ImageIcon(CameraFrame.class.getResource("/camera.png")));
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		called = true;
		Poser poser = PoserLibrary.getInstance().getCurrentPoser();
		if (poser != null) {
			Pose pose = poser.capture();
			if (PoserLibrary.getInstance().isCameraFrameVisible()) {
				final CameraFrame frame = PoserLibrary.getInstance().getCameraFrame();
				if (pose == null) {
					frame.setText("Capture pose failed!", Color.red);
				} else {
					frame.setText("Capture pose succeeded!", Color.blue);
				}
				called = false;
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// Do nothing.
						} finally {
							if (!called) {
								frame.setText("");
							}
						}
					}
				}).start();
			}
		}
	}
}
