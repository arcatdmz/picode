package jp.digitalmuseum.roboko;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import processing.app.RobokoSketch;
import processing.app.SketchCode;
import processing.app.SketchException;
import com.phybots.Phybots;
import com.phybots.service.Camera;
import jp.digitalmuseum.roboko.action.RunAction;
import jp.digitalmuseum.roboko.builder.Launcher;
import jp.digitalmuseum.roboko.core.Human;
import jp.digitalmuseum.roboko.core.Robot;
import jp.digitalmuseum.roboko.ui.RobokoFrame;
import jp.digitalmuseum.roboko.ui.editor.RobokoEditor;
import jp.digitalmuseum.roboko.ui.library.PoseManager;

public class RobokoMain {

	private static final boolean FOR_KINECT = true;
	private RobokoSketch sketch;
	private RobokoFrame robokoFrame;

	private Robot robot;

	private PoseManager poseManager;
	private Launcher launcher;
	private Process kinect;
	private Camera camera;

	public RobokoMain() {

		Phybots.getInstance().showDebugFrame();
		ProcessingIntegration.init();

		try {
			if (FOR_KINECT) {
				robot = new Human(this);
			} else {
				robot = new Robot(this, "MindstormsNXT", "btspp://001653047aeb");
				robot.connect();
			}
			poseManager = new PoseManager(this);
			camera = new Camera();
			sketch = RobokoSettings.getDefaultSketch(this);
		} catch (RuntimeException e) {
		  e.printStackTrace();
			System.err.println("Unsupported robot type.");
			return;
		} catch (IOException e) {
			System.err.println("Error while loading the pde file(s).");
			return;
		}

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				initGUI();
				if (!poseManager.hasInitialPose()) {
					setStatusText("Initial pose not found.");
				}
			}
		});
	}

	private void initGUI() {

		robokoFrame = new RobokoFrame(this);
		setSketch(sketch);

		Dimension d = new Dimension(840, 600);
		robokoFrame.setPreferredSize(d);
		robokoFrame.setSize(d);
		robokoFrame.setVisible(true);
		// robokoFrame.setDividerLocation(.5);
		robokoFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Phybots.getInstance().dispose();
				e.getWindow().dispose();
			}
		});
	}

	public static void main(String[] args) {
		new RobokoMain();
	}

	public void setSketch(RobokoSketch sketch) {
		this.sketch = sketch;
		robokoFrame.clearEditors();
		for (int i = 0; i < sketch.getCodeCount(); i ++) {
			SketchCode code = sketch.getCode(i);
			addEditor(code);
		}
		updateTitle();
		// JViewport viewport = frame.getJScrollPane().getViewport();
		// int caret = editor.getCaretPosition();
		// editor.setCaretPosition(caret);
		// frame.getJScrollPane().setViewport(viewport);
	}

	public RobokoSketch getSketch() {
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
			robokoFrame.setEnabled(true);
		} else {
			robokoFrame.setEnabled(false);
		}
	}

	public Launcher getLauncher() {
		return launcher;
	}

	public void setKinect(Process kinect) {
		this.kinect = kinect;
		if (kinect == null) {
			robokoFrame.setAlwaysOnTop(true);
			robokoFrame.setAlwaysOnTop(false);
			robokoFrame.setEnabled(true);
		} else {
			robokoFrame.setEnabled(false);
		}
	}

	public Process getKinect() {
		return kinect;
	}

	public void updateTitle() {
		robokoFrame.setTitle(sketch.getName() + " | Roboko");		
	}

	public void setStatusText(String statusText) {
		robokoFrame.setStatusText(statusText);
	}

	public void setNumberOfLines(int lines) {
		robokoFrame.setNumberOfLines(lines);
	}

	/**
	 * "No cookie for you" type messages. Nothing fatal or all that much of a
	 * bummer, but something to notify the user about.
	 */
	public void showMessage(String title, String message) {
		if (title == null)
			title = "Message";

		JOptionPane.showMessageDialog(robokoFrame, message, title,
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Non-fatal error message with optional stack trace side dish.
	 */
	public void showWarning(String title, String message, Exception e) {
		if (title == null)
			title = "Warning";

		JOptionPane.showMessageDialog(new Frame(), message, title,
				JOptionPane.WARNING_MESSAGE);
		if (e != null)
			e.printStackTrace();
	}

	public void showCaptureFrame(boolean show) {
		robot.getMotorManager().showCaptureFrame(show);
	}

	public void showEditor(int index) {
		robokoFrame.showEditor(index);
	}

	public int getCurrentEditorIndex() {
		return robokoFrame.getEditorIndex();
	}

	public RobokoEditor getCurrentEditor() {
		return robokoFrame.getCurrentEditor();
	}

	public void updateCurrentEditorName() {
		robokoFrame.updateCurrentEditorName();
	}

	public void addEditor(SketchCode code) {
		robokoFrame.addEditor(new RobokoEditor(this, code));
	}

	public void removeEditor(SketchCode code) {
		robokoFrame.removeEditor(code);
	}

	public void updateTabs() {
		robokoFrame.updateTabs();
	}

	public static String getErrorString(RobokoSketch sketch, SketchException se) {
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

	public void handleSketchException(SketchException se) {
		setStatusText(getErrorString(sketch, se));
	}

	public Robot getRobot() {
		return robot;
	}

	public RobokoFrame getRobokoFrame() {
		return robokoFrame;
	}

	public PoseManager getPoseManager() {
		return poseManager;
	}

	public Camera getCamera() {
		return camera;
	}

	public void dispose() {
	}
}
