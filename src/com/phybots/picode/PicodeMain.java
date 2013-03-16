package com.phybots.picode;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.SwingUtilities;
import processing.app.PicodeSketch;
import processing.app.SketchCode;
import processing.app.SketchException;

import com.phybots.Phybots;
import com.phybots.picode.api.Human;
import com.phybots.picode.api.PoseLibrary;
import com.phybots.picode.api.PoserLibrary;
import com.phybots.picode.builder.Builder;
import com.phybots.picode.ui.PicodeFrame;

public class PicodeMain {

	public static void main(String[] args) {
		new PicodeMain(args);
	}

	private ProcessingIntegration pintegration;

	private PicodeSketch sketch;

	private PicodeFrame picodeFrame;

	private Builder builder;

	public PicodeMain(String[] args) {

		ProcessingIntegration.init();

		// Get parameters
		boolean debug = false;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-debug")) {
				debug = true;
			}
		}
		if (debug) {
			Phybots.getInstance().showDebugFrame();
		}

		// Launch main UI
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				initGUI();
				PoseLibrary.getInstance().attachIDE(picodeFrame);
				PoserLibrary.getInstance().attachIDE(picodeFrame);

				// Add human instance by default.
				new Human("No name");
			}
		});
	}

	private void initGUI() {

		picodeFrame = new PicodeFrame(this);
		setSketch(PicodeSketch.newInstance(this));
		picodeFrame.setRunnable(true);

		Dimension d = new Dimension(840, 600);
		picodeFrame.setPreferredSize(d);
		picodeFrame.setSize(d);
		picodeFrame.setVisible(true);
		// picodeFrame.setDividerLocation(.5);
		picodeFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Phybots.getInstance().dispose();
				PicodeMain.this.dispose();
				e.getWindow().dispose();
			}
		});
	}

	public void dispose() {
		PoserLibrary.getInstance().getCameraManager().dispose();
		if (builder != null) {
			builder.stop();
		}
	}

	public ProcessingIntegration getPintegration() {
		if (pintegration == null) {
			pintegration = new ProcessingIntegration(this);
		}
		return pintegration;
	}

	public PicodeFrame getFrame() {
		return picodeFrame;
	}

	public void setSketch(PicodeSketch sketch) {
		this.sketch = sketch;
		getFrame().clearEditors();
		for (int i = 0; i < sketch.getCodeCount(); i++) {
			SketchCode code = sketch.getCode(i);
			getFrame().addEditor(code);
		}
		getPintegration().updateTitle();
		// JViewport viewport = frame.getJScrollPane().getViewport();
		// int caret = editor.getCaretPosition();
		// editor.setCaretPosition(caret);
		// frame.getJScrollPane().setViewport(viewport);
	}

	public PicodeSketch getSketch() {
		return sketch;
	}

	public void runSketch() {
		picodeFrame.setRunnable(false);
		if (builder != null) {
			builder.stop();
		}

		// Hide capture frame before we run the app.
		// TODO To be implemented.
//		Poser poser = PoserLibrary.getInstance().getCurrentPoser();
//		if (poser != null && poser.getCamera().isFrameVisible()) {
//			poser.getCamera().showFrame(false);
//		}

		// Run the app.
		builder = new Builder(this, sketch);
		try {
			builder.run();
		} catch (SketchException se) {
			builder = null;
			picodeFrame.setRunnable(true);
			getPintegration().statusError(se);
		}
	}

	public void stopSketch() {
		if (builder != null) {
			builder.stop();
			builder = null;
			picodeFrame.setRunnable(true);
		}
	}

	public static Font getDefaultFont() {
		return Phybots.getInstance().getDefaultFont();
	}

}
