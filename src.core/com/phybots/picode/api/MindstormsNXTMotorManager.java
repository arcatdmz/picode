package com.phybots.picode.api;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import jp.digitalmuseum.connector.Connector;

import com.phybots.Phybots;
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
	public void start() {
		motorControlService = new MotorControlService(motors, nxt.raw.getConnector());
		motorControlService.start();
		System.out.println("MotorControl: service started.");
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
		if (motorControlService == null
				|| !motorControlService.isStarted())
			return null;
		Poser poser = getPoser();
		MindstormsNXTPose pose = new MindstormsNXTPose();
		pose.setPoserIdentifier(poser.getIdentifier());
		pose.setPoserType(poser.getPoserType());
		if (!pose.importData(motorControlService.getRotationCounts()))
			return null;
		else
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
				return !currentReadinesses[i]
						|| (currentStates[i] != null && currentStates[i].runState != 0);
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
		public synchronized void launchMotorControl() {
			// ignorePreviousCommandReplies();
			String currentProgramName = MindstormsNXT.getCurrentProgramName(connector);
			if (!programName.equals(currentProgramName)) {
				System.out.print("MotorControl: nxt current program = ");
				System.out.println(currentProgramName);
				System.out.println("MotorControl: nxt program launching");
				MindstormsNXT.stopProgram(connector);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					return;
				}
				MindstormsNXT.startProgram(programName, connector);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					return;
				}
				currentProgramName = MindstormsNXT.getCurrentProgramName(connector);
				if (!programName.equals(currentProgramName)) {
					// TODO Reset nxt?
					System.out.print("MotorControl: nxt program launch failed, something wrong happened. / current program: ");
					System.out.println(currentProgramName);
					stop();
					return;
				} else {
					System.out.println("MotorControl: nxt program launched");
				}
			}
		}

		@Override
		protected void onStart() {
			launchMotorControl();
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

		private synchronized void ignorePreviousCommandReplies() {

			// Read all data if needed.
			Future<?> future = Phybots.getInstance().submit(new Runnable() {
				@Override
				public void run() {
					try {
						if (connector.getInputStream().available() > 0) {
							int i = 0;
							while ((i = connector.read()) >= 0) {
								System.out.println(i);
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});

			// Set timeout of 1000 milliseconds.
			try {
				future.get(1000, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				future.cancel(true);
			}
		}

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
				System.out.println("interrupted");

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
					return true;
				}
			}
			return false;
		}

		private synchronized boolean startMotorControl(int port) {

			// Check motor state.
			MindstormsNXTExtension motor = motors[port];
			if (motor == null) {
				return true;
			}
			OutputState currentState = currentStates[port];
			if (currentState.runState != MindstormsNXT.MOTOR_RUN_STATE_IDLE) {
				return false;
			}
			if (!localReadinesses[port]) {
				return false;
			}

			// If we've already reached the goal, do nothing.
			int goal = goals[port];
			if (Math.abs(goal - currentState.rotationCount) < 5) {
				return true;
			}

			// Start motor control.
			{
				int power = powers[port];
				if (goal < currentState.rotationCount) {
					power += 100;
				}
		
				int tachoLimit = Math.abs(goal - currentState.rotationCount);
	
				int mode = 0;
	
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
			}
			return true;
		}
	}

}
