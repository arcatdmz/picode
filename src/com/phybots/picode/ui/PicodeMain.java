package com.phybots.picode.ui;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.SwingUtilities;
import processing.app.PicodeSketch;
import processing.app.SketchCode;
import processing.app.SketchException;
import com.phybots.Phybots;
import com.phybots.service.Camera;
import com.phybots.picode.Human;
import com.phybots.picode.Robot;
import com.phybots.picode.action.RunAction;
import com.phybots.picode.builder.Launcher;
import com.phybots.picode.ui.library.PoseManager;

public class PicodeMain {

	private static final boolean FOR_KINECT = false;

  public static void main(String[] args) {
    new PicodeMain();
  }

  public static String getErrorString(PicodeSketch sketch, SketchException se) {
    StringBuilder sb = new StringBuilder();
    sb.append(se.getCodeIndex() < 0 ?
        "-" :
        sketch.getCode(se.getCodeIndex()).getFileName());
    if (se.getCodeLine() >= 0) {
      sb.append(", L");
      sb.append(se.getCodeLine() + 1);
      if (se.getCodeColumn() >= 0) {
        sb.append(":");
        sb.append(se.getCodeColumn() + 1);
      }
    }
    sb.append(" ");
    sb.append(se.getMessage());
    return sb.toString();
  }

	private ProcessingIntegration pintegration;

  private PicodeSketch sketch;
	private PicodeFrame picodeFrame;

	private Robot robot;

	private PoseManager poseManager;
	private Launcher launcher;
	private Process kinect;
	private Camera camera;

	public PicodeMain() {

		Phybots.getInstance().showDebugFrame();
		ProcessingIntegration.init();

		// TODO Hard-coded for WISS'12 demonstration.
		try {
			if (FOR_KINECT) {
				robot = new Human(this);
			} else {
				robot = new Robot(this, "MindstormsNXT", "btspp://001653055d42");
				robot.connect();
			}
			poseManager = new PoseManager(this);
			camera = new Camera();
		} catch (RuntimeException e) {
		  e.printStackTrace();
			System.err.println("Unsupported robot type.");
			return;
		}

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
    sketch = PicodeSketch.newInstance(this);
		setSketch(sketch);

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
		getFrame().updateTitle();
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
			robot.connect(); // Reset BlueCove status.
			picodeFrame.setEnabled(true);
		} else {
			picodeFrame.setEnabled(false);
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
