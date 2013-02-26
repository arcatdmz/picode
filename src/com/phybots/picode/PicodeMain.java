package com.phybots.picode;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.SwingUtilities;
import processing.app.PicodeSketch;
import processing.app.SketchCode;
import com.phybots.Phybots;
import com.phybots.picode.action.RunAction;
import com.phybots.picode.api.PoserManager;
import com.phybots.picode.api.PoserManager.PoserInfo;
import com.phybots.picode.builder.Launcher;
import com.phybots.picode.ui.PicodeFrame;

public class PicodeMain {

	public static void main(String[] args) {
		new PicodeMain(args);
	}

	private ProcessingIntegration pintegration;

	private PicodeSketch sketch;

	private PicodeFrame picodeFrame;

	private Launcher launcher;

	public PicodeMain(String[] args) {

		ProcessingIntegration.init();

		// Get parameters
		PoserInfo poserInfo = new PoserInfo();
		boolean debug = false;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-debug")) {
				debug = true;
			} else if (i + 1 < args.length) {
				if (args[i].equals("-type")) {
					poserInfo.type = PoserManager.getTypeInfo(args[++i]);
				} else if (args[i].equals("-address")) {
					poserInfo.connector = args[++i];
				}
			}
		}
		if (debug) {
			Phybots.getInstance().showDebugFrame();
		}

		// Initialize poser manager
		PoserManager poserManager = PoserManager.getInstance();
		poserManager.setIDE(this);
		poserManager.newPoserInstance(poserInfo);

		// Launch main UI
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				initGUI();
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
				e.getWindow().dispose();
			}
		});
	}

	public void dispose() {
	}

	public ProcessingIntegration getPintegration() {
		if (pintegration == null) {
			pintegration = new ProcessingIntegration(this);
		}
		return pintegration;
	}

	public void afterRun() {
	}

	public void beforeRun() {
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

	/**
	 * VMを起動させるためのLauncherオブジェクトへの参照を更新する。 引数がnullでないときはVMが起動したときで、
	 * 引数がnullのときはVMがシャットダウンしたときである。 後者の場合はロボットとの接続を回復する。
	 * 
	 * @param launcher
	 * @see RunAction#actionPerformed(java.awt.event.ActionEvent)
	 * @see Launcher#launch(boolean)
	 */
	public void setLauncher(Launcher launcher) {
		this.launcher = launcher;
		if (launcher == null) {
			// connectActiveRobot();
			picodeFrame.setRunnable(true);
		} else {
			picodeFrame.setRunnable(false);
		}
	}

	public Launcher getLauncher() {
		return launcher;
	}

	public static Font getDefaultFont() {
		return Phybots.getInstance().getDefaultFont();
	}
}
