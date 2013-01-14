package com.phybots.picode;

import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.phybots.service.Service;
import com.phybots.service.ServiceGroup;
import com.phybots.task.ManageMindstormsNXTMotorState;
import com.phybots.task.ManageMindstormsNXTMotorState.PowerFunction;
import com.phybots.picode.ui.CaptureFrameAbstractImpl;
import com.phybots.picode.ui.PicodeMain;

public class MindstormsNXTMotorManager extends MotorManager {
	private ServiceGroup sg;

	public MindstormsNXTMotorManager(PicodeMain picodeMain, Robot robot) throws InstantiationException {
		super(picodeMain, robot);
		sg = new ServiceGroup();
		for (int i = 0; i < 3; i ++) {
			ManageMindstormsNXTMotorState task = new ManageMindstormsNXTMotorState();
			task.setPowerFunction(new PowerFunction() {
        @Override
        public int getPowerFromAngleDiff(int diffAbs) {
          return 16;
        }
      });
      task.setRotationThreshold(3);
      task.setRotationErrorThreshold(10);
			task.assign(robot.getCore());
			sg.add(task);
		}
		sg.setInterval(30);
	}

	public List<Service> getServices() {
		return sg.getServices();
	}

	public void start() {
		sg.start();
	}

	public void stop() {
		sg.stop();
	}

	public void setEditable(boolean isEditable) {
		for (Service service : getServices()) {
			((ManageMindstormsNXTMotorState) service).setEditable(isEditable);
		}
	}

	public boolean isActing() {
		boolean isActing = false;
		for (Service service : getServices()) {
			isActing |= ((ManageMindstormsNXTMotorState) service).isRotating();
		}
		return isActing;
	}

	public void reset() {
		for (Service service : getServices()) {
			((ManageMindstormsNXTMotorState) service).reset();
		}
	}

	protected CaptureFrameAbstractImpl newCaptureFrameInstance(PicodeMain picodeMain) {
		return new CaptureFrameAbstractImpl(picodeMain) {
			private static final long serialVersionUID = 7867260841537019765L;
			protected JComponent getConfigurationComponent() {
				JPanel motorPanel = new JPanel();
				motorPanel.setLayout(new BoxLayout(motorPanel, BoxLayout.Y_AXIS));
				for (Service service : sg.getServices()) {
					motorPanel.add(service.getConfigurationComponent());
				}
				motorPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
				return motorPanel;
			}
		};
	}
}
