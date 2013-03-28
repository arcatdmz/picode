package com.phybots.picode.api;

import jp.digitalmuseum.connector.Connector;

import com.phybots.entity.MindstormsNXT;
import com.phybots.entity.MindstormsNXT.MindstormsNXTExtension;
import com.phybots.entity.MindstormsNXT.OutputState;
import com.phybots.entity.MindstormsNXT.Port;
import com.phybots.service.ServiceAbstractImpl;

public class MindstormsNXTMotorManager extends MotorManager {
	private com.phybots.picode.api.MindstormsNXT nxt;
	private MindstormsNXTExtension[] motors;
	private MotorControlService motorControlService;

	public MindstormsNXTMotorManager(Poser poser) {
		super(poser);
		nxt = (com.phybots.picode.api.MindstormsNXT) poser;
		motors = new MindstormsNXTExtension[3];
		for (int i = 0; i < motors.length; i ++) {
			motors[i] = nxt.raw.requestResource(MindstormsNXTExtension.class, this);
		}
	}

	@Override
	public boolean start() {
		if (motorControlService == null
				|| motorControlService.isStarted()) {
			motorControlService = new MotorControlService(motors, nxt.raw.getConnector());
			if (!motorControlService.launchMotorControl()) {
				return false;
			}
			motorControlService.start();
			System.out.println("MotorControl: service started.");
		}
		return true;
	}

	@Override
	public void stop() {
		if (motorControlService != null) {
			motorControlService.stop();
			motorControlService = null;
			System.out.println("MotorControl: service stopped.");
		}
	}

	@Override
	public Pose getPose() {
		Poser poser = getPoser();
		MindstormsNXTPose pose = new MindstormsNXTPose();
		pose.setPoserIdentifier(poser.getIdentifier());
		pose.setPoserType(poser.getPoserType());
		if (motorControlService != null
				&& motorControlService.isStarted()) {
			pose.importData(motorControlService.getRotationCounts());
		}
		return pose;
	}

	@Override
	public boolean setPose(Pose pose) {
		if (!(pose instanceof MindstormsNXTPose)
				|| motorControlService == null
				|| !motorControlService.isStarted()) {
			return false;
		}
		motorControlService.setRotationCounts(
				((MindstormsNXTPose) pose).getData());
		return true;
	}

	@Override
	public boolean isActing() {
		if (motorControlService == null
				|| !motorControlService.isStarted()) {
			return false;
		}
		else
			return motorControlService.isActing();
	}

	@Override
	public void reset() {
		if (motorControlService != null)
			motorControlService.reset();
	}
	
	public static class MotorControlService extends ServiceAbstractImpl {
		private static final long serialVersionUID = 4299249304434616480L;
		private static final String programName = "MotorControl22.rxe";
		private Connector connector;
		private MindstormsNXTExtension[] motors;
		private OutputState[] currentStates;
		private boolean[] localReadinesses;
		private boolean[] currentReadinesses;
		private int[] previousGoals;
		private int[] goals;
		private int[] powers;
	
		public MotorControlService(MindstormsNXTExtension[] motors, Connector connector) {
			super();
			this.motors = new MindstormsNXTExtension[] {
					motors[0],
					motors[1],
					motors[2]
			};
			this.connector = connector;
			currentStates = new OutputState[3];
			localReadinesses = new boolean[3];
			currentReadinesses = new boolean[3];
			previousGoals = new int[] { Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE		 };
			goals = new int[3];
			powers = new int[]{ 30, 30, 30 };
			setInterval(200);
		}
	
		public boolean isActing() {
			return isActing(Port.A)
					|| isActing(Port.B)
					|| isActing(Port.C);
		}

		public boolean isActing(Port port) {
			int i = enumToIndex(port);
			if (i >= 0) {
				return !currentReadinesses[i];
			}
			return false;
		}
	
		public int getPower(Port port) {
			int i = enumToIndex(port);
			if (i >= 0) return powers[i];
			return Integer.MAX_VALUE;
		}
	
		public void setPower(int power, Port port) {
			int i = enumToIndex(port);
			if (i >= 0) goals[i] = power;
		}
	
		public int getRotationCount(Port port) {
			int i = enumToIndex(port);
			if (i >= 0 && currentStates[i] != null)
				return currentStates[i].rotationCount;
			return Integer.MAX_VALUE;
		}

		public int[] getRotationCounts() {
			return new int[] {
					getRotationCount(Port.A),
					getRotationCount(Port.B),
					getRotationCount(Port.C)
			};
		}

		public void setRotationCount(int goal, Port port) {
			int i = enumToIndex(port);
			if (i >= 0) goals[i] = goal;
		}

		public void setRotationCounts(int[] rotationCounts) {
			currentReadinesses[0] = false;
			setRotationCount(rotationCounts[0], Port.A);
			currentReadinesses[1] = false;
			setRotationCount(rotationCounts[1], Port.B);
			currentReadinesses[2] = false;
			setRotationCount(rotationCounts[2], Port.C);
		}

		public synchronized void reset() {
			for (int i = 0; i < 3; i ++) {
				if (motors[i] != null) {
					motors[i].setOutputState((byte) 0, 0, 0, 0, 0, 0);
				}
			}
		}

		/**
		 * Launch MotorControl program on the NXT brick.
		 */
		public synchronized boolean launchMotorControl() {
			//ignorePreviousCommandReplies();
			String currentProgramName = MindstormsNXT.getCurrentProgramName(connector);
			if (!programName.equals(currentProgramName)) {
				System.out.print("MotorControl: nxt current program = ");
				System.out.println(currentProgramName);
				System.out.println("MotorControl: nxt program launching");
				MindstormsNXT.stopProgram(connector);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					return false;
				}
				MindstormsNXT.startProgram(programName, connector);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					return false;
				}
				currentProgramName = MindstormsNXT.getCurrentProgramName(connector);
				if (!programName.equals(currentProgramName)) {
					// TODO Reset nxt?
					System.out.print("MotorControl: nxt program launch failed, something wrong happened. / current program: ");
					System.out.println(currentProgramName);
					return false;
				} else {
					System.out.println("MotorControl: nxt program launched");
				}
			}
			return true;
		}

		@Override
		protected void onStart() {
			for (int i = 0; i < 3; i ++) {
				goals[i] = Integer.MAX_VALUE;
			}
		}
	
		@Override
		protected void onStop() {
			reset();
		}

		private boolean isRunningPreviousTask;

		@Override
		public void run() {
			if (isRunningPreviousTask) {
				System.out.println("MotorControl: cancelling previous task");
				return;
			}
			isRunningPreviousTask = true;

			// Pause before reading motor data using GetOutputState.
			sleep(15);

			// Read motor data.
			for (int i = 0; i < 3; i ++) {
				if (motors[i] != null) {
					currentStates[i] = motors[i].getOutputState();
					localReadinesses[i] = isMotorReady(i);
				}
			}

			// Start motor control if needed.
			for (int i = 0; i < 3; i ++) {
				if (goals[i] == Integer.MAX_VALUE) {
					currentReadinesses[i] = localReadinesses[i];
				} else if (startMotorControl(i)) {
					goals[i] = Integer.MAX_VALUE;
					sleep(5);
				}
			}

			isRunningPreviousTask = false;
		}
	
		private static int enumToIndex(Port port) {
			switch (port) {
			case A:
				return 0;
			case B:
				return 1;
			case C:
				return 2;
			default:
				return -1;
			}
		}
	
		private static void sleep(int sleep) {
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				// Do nothing.
			}
		}

		private int[] trivialWait = new int[3];
		private static final int trivialWaitTime = 5;
		private static final int trivialWaitAngleThreshold = 10;
		private synchronized boolean isMotorReady(int port) {

			try {
				// Send a query.
				String command = String.format("3%1d", port);
				byte[] commandBytes = command.getBytes();
				MindstormsNXT.messageWrite(
						commandBytes,
						(byte)1, connector);
	
				// Pause before receiving motor status.
				Thread.sleep(10);

			} catch (InterruptedException e) {
				System.out.println("MotorControl: function call interrupted");

			} finally {
	
				// Receive motor status.
				String message = MindstormsNXT.messageRead(
						(byte)0, (byte)0, true, connector);

				// Pause after receiving motor status.
				sleep(10);
				if (message == null || message.length() != 2) {
					// Something wrong happened. Give up the motor control.
					return false;
				}
				if (message.charAt(1) == '1') {

					// Before starting the motor control...
					int diff = currentStates[port].rotationCount - previousGoals[port];
					if (previousGoals[port] != Integer.MAX_VALUE && Math.abs(diff) > trivialWaitAngleThreshold) {
						if (trivialWait[port] == 0) {
							System.out.println(String.format(
									"MotorControl: waiting for port:%d / diff: %d",
									port, diff));
							trivialWait[port] ++;
						} else if (trivialWait[port] >= trivialWaitTime) {
							System.out.println(String.format(
									"MotorControl: waiting for port:%d time out / diff: %d",
									port, diff));
							trivialWait[port] = 0;
							return true;
						} else {
							trivialWait[port] ++;
							System.out.println(String.format(
									"MotorControl: waiting for port:%d (%s trial ) / diff: %d",
									port,
									trivialWait[port] == 2 ? "2nd" :
										(trivialWait[port] == 3 ? "3rd" :
											trivialWait[port] + "th"),
									diff));
						}
						// wait more.
						return false;
					} else {
						trivialWait[port] = 0;
					}

					return true;
				} else {
					trivialWait[port] = 0;
				}
			}
			return false;
		}

		private synchronized boolean startMotorControl(int port) {

			// Check motor state.
			MindstormsNXTExtension motor = motors[port];
			if (motor == null)
				return true;

			// If we've already reached the goal, do nothing.
			OutputState currentState = currentStates[port];
			int goal = goals[port];
			if (Math.abs(goal - currentState.rotationCount) <= trivialWaitAngleThreshold) {
				return true;
			}

			// Start motor control.
			int power = powers[port];
			if (goal < currentState.rotationCount)
				power += 100;
	
			int tachoLimit = Math.abs(goal - currentState.rotationCount);

			int mode = 1; // HoldBrake

			// Pause before sending a string motor command message.
			sleep(5); // 15 - 10

			// Send a motor command message.
			String command = String.format("1%1d%03d%06d%1d",
					port, power, tachoLimit, mode);
			byte[] commandBytes = command.getBytes();
			MindstormsNXT.messageWrite(
					commandBytes,
					(byte)1,
					connector);

			System.out.println("command: " + command);
			previousGoals[port] = goal;
			return true;
		}
	}

}
