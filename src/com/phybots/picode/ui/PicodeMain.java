package com.phybots.picode.ui;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.SwingUtilities;
import processing.app.PicodeSketch;
import processing.app.SketchCode;
import com.phybots.Phybots;
import com.phybots.service.Camera;
import com.phybots.picode.Human;
import com.phybots.picode.Robot;
import com.phybots.picode.action.RunAction;
import com.phybots.picode.builder.Launcher;
import com.phybots.picode.ui.library.PoseManager;

public class PicodeMain {

  public static void main(String[] args) {
    new PicodeMain(args);
  }

	private ProcessingIntegration pintegration;

  private PicodeSketch sketch;
	private PicodeFrame picodeFrame;

	private Robot robot;

	private PoseManager poseManager;
	private Launcher launcher;
	private Process kinect;
	private Camera camera;

	public PicodeMain(String[] args) {

		ProcessingIntegration.init();

		// Get parameters
    String robotType = null;
		String address = null;
		boolean debug = false;
		for (int i = 0; i < args.length; i ++) {
      if (args[i].equals("-debug")) {
        debug = true;
      } else if (i + 1 < args.length) {
  		  if (args[i].equals("-type")) {
  		    robotType = args[++ i];
  		  } else if (args[i].equals("-address")) {
  		    address = args[++ i];
  		  }
		  }
		}
		if (robotType == null ||
		    (!robotType.equals("Human") && address == null)) {
		  System.err.println("Usage: picode -type RobotTypeName -address btspp://deadbeaf");
		  return;
		}
		if (debug) {
	    Phybots.getInstance().showDebugFrame();
		}

		// Set up configuration
		try {
	    if (robotType.equals("Human")) {
	      robot = new Human(this);
	    } else {
	      robot = new Robot(this, robotType, address);
	      robot.connect();
	    }
			poseManager = new PoseManager(this);
			camera = new Camera();
		} catch (RuntimeException e) {
		  e.printStackTrace();
			System.err.println("Unsupported robot type.");
			return;
		}

		// Launch main UI
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				initGUI();
				if (!poseManager.hasInitialPose()) {
					getFrame().setStatusText("Initial pose not found.");
				}
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

  public Robot getRobot() {
    return robot;
  }

  public PicodeFrame getFrame() {
    return picodeFrame;
  }

  public PoseManager getPoseManager() {
    return poseManager;
  }

  public Camera getCamera() {
    return camera;
  }

	public void setSketch(PicodeSketch sketch) {
		this.sketch = sketch;
		getFrame().clearEditors();
		for (int i = 0; i < sketch.getCodeCount(); i ++) {
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
	 * VMを起動させるためのLauncherオブジェクトへの参照を更新する。
	 * 引数がnullでないときはVMが起動したときで、
	 * 引数がnullのときはVMがシャットダウンしたときである。
	 * 後者の場合はロボットとの接続を回復する。
	 * 
	 * @param launcher
	 * @see RunAction#actionPerformed(java.awt.event.ActionEvent)
	 * @see Launcher#launch(boolean)
	 */
	public void setLauncher(Launcher launcher) {
		this.launcher = launcher;
		if (launcher == null) {
			robot.connect();
			picodeFrame.setRunnable(true);
		} else {
      picodeFrame.setRunnable(false);
		}
	}

	public Launcher getLauncher() {
		return launcher;
	}

	public void setKinect(Process kinect) {
		this.kinect = kinect;
		if (kinect == null) {
			picodeFrame.setAlwaysOnTop(true);
			picodeFrame.setAlwaysOnTop(false);
			picodeFrame.setEnabled(true);
		} else {
			picodeFrame.setEnabled(false);
		}
	}

	public Process getKinect() {
		return kinect;
	}
	
	public void showCaptureFrame(boolean show) {
		robot.getMotorManager().showCaptureFrame(show);
	}
}
