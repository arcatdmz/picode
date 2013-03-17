package com.phybots.picode.ui.camera;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.action.CapturePoseAction;
import com.phybots.picode.api.Poser;
import com.phybots.picode.api.PoserLibrary;
import com.phybots.picode.camera.Camera;
import com.phybots.picode.camera.CameraManager;

import javax.swing.ImageIcon;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.Action;

public class CameraFrame extends JFrame {
	private static final long serialVersionUID = -1065767804512646130L;
	private static final Font defaultFont = PicodeMain.getDefaultFont();

	private JPanel contentPane;
	private JPanel pnlDefaultCamera;
	private JRadioButton rdbtnPrimary;
	private JRadioButton rdbtnSecondary;

	private transient CameraPanelAbstractImpl pnlCamera;
	private transient Map<Camera, CameraPanelAbstractImpl> pnlCameras;
	private transient ExecutorService es;
	private final Action primaryCameraAction = new PrimaryCameraAction();
	private final Action action = new SecondaryCameraAction();

	/**
	 * Create the frame.
	 */
	public CameraFrame() {
		setType(Type.UTILITY);
		setAlwaysOnTop(true);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel pnlPrisec = new JPanel();
		contentPane.add(pnlPrisec, BorderLayout.NORTH);
		ButtonGroup buttonGroup = new ButtonGroup();

		rdbtnPrimary = new JRadioButton();
		rdbtnPrimary.setAction(primaryCameraAction);
		buttonGroup.add(rdbtnPrimary);
		rdbtnPrimary.setFont(defaultFont);
		pnlPrisec.add(rdbtnPrimary);

		rdbtnSecondary = new JRadioButton("Secondary camra");
		rdbtnSecondary.setAction(action);
		buttonGroup.add(rdbtnSecondary);
		rdbtnSecondary.setFont(defaultFont);
		pnlPrisec.add(rdbtnSecondary);

		JPanel pnlButtons = new JPanel();
		contentPane.add(pnlButtons, BorderLayout.SOUTH);
		pnlButtons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JButton btnCapture = new JButton();
		btnCapture.setAction(new CapturePoseAction());
		btnCapture.setIcon(new ImageIcon(CameraFrame.class.getResource("/camera.png")));
		btnCapture.setFont(defaultFont);
		pnlButtons.add(btnCapture);

		pnlDefaultCamera = new JPanel();
		contentPane.add(pnlDefaultCamera, BorderLayout.CENTER);
		pnlDefaultCamera.setPreferredSize(new Dimension(640, 480));

		pnlCameras = new HashMap<Camera, CameraPanelAbstractImpl>();
		es = Executors.newSingleThreadExecutor();

		setPoser(null);
		pack();
	}

	@Override
	public void dispose() {
		super.dispose();
		if (pnlCamera != null) {
			stopCurrentCamera();
		}
		pnlCameras.clear();
		es.shutdown();
	}

	public void setPoser(Poser poser) {

		hideCurrentCamera();

		if (poser == null) {
			contentPane.add(pnlDefaultCamera, BorderLayout.CENTER);
			pnlCamera = null;
			rdbtnPrimary.setEnabled(false);
			rdbtnSecondary.setEnabled(false);
			return;
		}

		CameraManager cm = PoserLibrary.getInstance().getCameraManager();
		Camera camera = cm.getCamera(poser);
		setCurrentCamera(poser, camera);
	}

	private void hideCurrentCamera() {
		if (pnlCamera == null) {
			contentPane.remove(pnlDefaultCamera);
		} else {
			stopCurrentCamera();
			contentPane.remove(pnlCamera);
		}
	}

	private void setCurrentCamera(Poser poser, Camera camera) {
		rdbtnPrimary.setEnabled(true);
		rdbtnPrimary.setSelected(poser.getPoserType().defaultCameraClass.isInstance(camera));

		if (poser.getPoserType().secondaryCameraClass == null) {
			rdbtnSecondary.setEnabled(false);
		} else {
			rdbtnSecondary.setEnabled(true);
			rdbtnSecondary.setSelected(poser.getPoserType().secondaryCameraClass.isInstance(camera));
		}

		pnlCamera = pnlCameras.get(camera);
		if (pnlCamera == null) {
			pnlCamera = camera.newPanelInstance();
			pnlCameras.put(camera, pnlCamera);
		}
		contentPane.add(pnlCamera, BorderLayout.CENTER);
		contentPane.repaint();
		startCurrentCamera(poser);
	}

	private void startCurrentCamera(Poser poser) {
		final CameraPanelAbstractImpl pnlCamera_ = pnlCamera;
		final Poser poser_ = poser;
		es.execute(new Runnable() {
			public void run() {
				try {
					if (pnlCamera_.start()) {
						CameraManager cm = PoserLibrary.getInstance().getCameraManager();
						cm.putCamera(poser_, pnlCamera_.getCamera());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void stopCurrentCamera() {
		final CameraPanelAbstractImpl pnlCamera_ = pnlCamera;
		es.execute(new Runnable() {
			public void run() {
				try {
					pnlCamera_.stop();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private class PrimaryCameraAction extends AbstractAction {
		private static final long serialVersionUID = -6253034046122297727L;
		public PrimaryCameraAction() {
			putValue(NAME, "Primary camera");
			putValue(SHORT_DESCRIPTION, "Use the primary (recommended) camera to capture a new pose.");
		}
		public void actionPerformed(ActionEvent e) {
			Poser poser = PoserLibrary.getInstance().getCurrentPoser();
			if (poser == null) return;
			CameraManager cm = PoserLibrary.getInstance().getCameraManager();
			Class<? extends Camera> cameraClass = poser.getPoserType().defaultCameraClass;
			if (cameraClass == null) return;
			Camera camera = cm.getCamera(cameraClass);
			if (camera == null) return;
			hideCurrentCamera();
			setCurrentCamera(poser, camera);
		}
	}
	private class SecondaryCameraAction extends AbstractAction {
		private static final long serialVersionUID = -1362637102590892688L;
		public SecondaryCameraAction() {
			putValue(NAME, "Secondary camera");
			putValue(SHORT_DESCRIPTION, "Use the secondary camera to capture a new pose.");
		}
		public void actionPerformed(ActionEvent e) {
			Poser poser = PoserLibrary.getInstance().getCurrentPoser();
			if (poser == null) return;
			CameraManager cm = PoserLibrary.getInstance().getCameraManager();
			Class<? extends Camera> cameraClass = poser.getPoserType().secondaryCameraClass;
			if (cameraClass == null) return;
			Camera camera = cm.getCamera(cameraClass);
			if (camera == null) return;
			hideCurrentCamera();
			setCurrentCamera(poser, camera);
		}
	}
}
