package com.phybots.picode;


import jp.digitalmuseum.connector.BluetoothConnector;

import com.intel.bluetooth.BlueCoveImpl;

import com.phybots.picode.ui.PicodeMain;

public class Robot {
	private static final String DEFAULT_ROBOT = RobotType.MindstormsNXT.toString();

	private PicodeMain picodeMain;

	private com.phybots.entity.Robot robot;
	private RobotType robotType;
	private MotorManager motorManager;

	public Robot(com.phybots.entity.Robot robot) {
		this(null, robot);
	}
	public Robot(PicodeMain picodeMain, com.phybots.entity.Robot robot) {
		this.picodeMain = picodeMain;
		this.robot = robot;
		initialize();
	}

	public Robot(String connectionString) {
		this((PicodeMain) null, connectionString);
	}
	public Robot(PicodeMain picodeMain, String connectionString) {
		this(picodeMain, DEFAULT_ROBOT, connectionString);
	}

	public Robot(String typeName, String connectionString) {
		this(null, typeName, connectionString);
	}
	public Robot(PicodeMain picodeMain, String typeName, String connectionString) {
		this.picodeMain = picodeMain;
		try {
			RobotType robotType = RobotType.valueOf(typeName);
			if (robotType == null) {
				throw new InstantiationException(String.format("No such robot type found: %s", typeName));
			}
			robot = robotType.newRobotInstance(connectionString);
			initialize();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
	}

	private void initialize() {
		robotType = RobotType.valueOf(robot.getClass());
		motorManager = robotType.newMotorManagerInstance(picodeMain, this);
    motorManager.start();
	}

	public void dispose() {
		motorManager.stop();
	}

	public com.phybots.entity.Robot getCore() {
		return robot;
	}

	public RobotType getType() {
		return robotType;
	}

	public MotorManager getMotorManager() {
		return motorManager;
	}

	public boolean setPose(Pose pose) {
		if (pose.getClass().equals(robotType.getPoseClass())) {
		  ensureConnected();
			return pose.applyTo(motorManager);
		}
		return false;
	}

	public Pose getPose() {
    ensureConnected();
		Pose pose = robotType.newPoseInstance();
		pose.retrieveFrom(motorManager);
		return pose;
	}

	public void setEditable(boolean isEditable) {
    ensureConnected();
		motorManager.setEditable(isEditable);
	}

	public boolean isActing() {
		return motorManager.isActing();
	}

	public void connect() {
		((com.phybots.entity.PhysicalRobot) robot).connect();
		motorManager.reset();
	}

	public void disconnect() {
		((com.phybots.entity.PhysicalRobot) robot).disconnect();
    if (((com.phybots.entity.PhysicalRobot) robot).getConnector() instanceof BluetoothConnector) {
      BlueCoveImpl.shutdown();
    }
	}
	
  public boolean isConnected() {
    return ((com.phybots.entity.PhysicalRobot) robot).isConnected();
  }
  
  private void ensureConnected() {
    if (!isConnected()) {
      connect();
    }
  }
	
	public Action action() {
		Action action = new Action(this);
		return action;
	}
	
	@Override
	public String toString() {
	  return robot.toString();
	}
}
