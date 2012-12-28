package jp.digitalmuseum.roboko.core;


import com.intel.bluetooth.BlueCoveImpl;

import jp.digitalmuseum.roboko.RobokoMain;
import jp.digitalmuseum.roboko.core.internal.MotorManager;

public class Robot {
	private static final String DEFAULT_ROBOT = RobotType.MindstormsNXT.toString();

	private RobokoMain robokoMain;

	private com.phybots.entity.Robot robot;
	private RobotType robotType;
	private MotorManager motorManager;

	public Robot(com.phybots.entity.Robot robot) {
		this(null, robot);
	}
	public Robot(RobokoMain robokoMain, com.phybots.entity.Robot robot) {
		this.robokoMain = robokoMain;
		this.robot = robot;
		initialize();
	}

	public Robot(String connectionString) {
		this((RobokoMain) null, connectionString);
	}
	public Robot(RobokoMain robokoMain, String connectionString) {
		this(robokoMain, DEFAULT_ROBOT, connectionString);
	}

	public Robot(String typeName, String connectionString) {
		this(null, typeName, connectionString);
	}
	public Robot(RobokoMain robokoMain, String typeName, String connectionString) {
		this.robokoMain = robokoMain;
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
		motorManager = robotType.newMotorManagerInstance(robokoMain, this);
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
			return pose.applyTo(motorManager);
		}
		return false;
	}

	public Pose getPose() {
		Pose pose = robotType.newPoseInstance();
		pose.retrieveFrom(motorManager);
		return pose;
	}

	public void setEditable(boolean isEditable) {
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
		BlueCoveImpl.shutdown();
	}
	
	public Action action() {
		Action action = new Action(this);
		return action;
	}
}
