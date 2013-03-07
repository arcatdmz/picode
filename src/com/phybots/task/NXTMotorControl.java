package com.phybots.task;

import jp.digitalmuseum.connector.Connector;

import com.phybots.entity.MindstormsNXT;
import com.phybots.entity.MindstormsNXT.OutputState;
import com.phybots.entity.MindstormsNXT.MindstormsNXTExtension;
import com.phybots.entity.MindstormsNXT.Port;
import com.phybots.service.ServiceAbstractImpl;

public class NXTMotorControl extends ServiceAbstractImpl {
	private static final long serialVersionUID = 4299249304434616480L;
	private Connector connector;
	private MindstormsNXTExtension[] motors;
	private OutputState[] currentStates;
	private int[] goals;
	private int[] powers;

	public NXTMotorControl(MindstormsNXTExtension[] motors, Connector connector) {
		super();
		this.motors = new MindstormsNXTExtension[] {
				motors[0],
				motors[1],
				motors[2]
		};
		this.connector = connector;
		currentStates = new OutputState[3];
		goals = new int[3];
		powers = new int[]{ 30, 30, 30 };
		setInterval(100);
	}

	public boolean isActing() {
		return isActing(Port.A)
				|| isActing(Port.B)
				|| isActing(Port.C);
	}

	public boolean isActing(Port port) {
		int i = enumToIndex(port);
		if (i >= 0 && currentStates[i] != null)
			return currentStates[i].runState != 0;
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

	public void setRotationCount(int goal, Port port) {
		int i = enumToIndex(port);
		if (i >= 0) goals[i] = goal;
	}

	@Override
	protected void onStart() {
		for (int i = 0; i < 3; i ++) {
			goals[i] = Integer.MAX_VALUE;
		}
	}

	@Override
	protected void onStop() {
		for (int i = 0; i < 3; i ++) {
			if (motors[i] != null) {
				motors[i].setOutputState((byte) 0, 0, 0, 0, 0, 0);
			}
		}
	}

	@Override
	public void run() {
		for (int i = 0; i < 3; i ++) {
			if (motors[i] != null) {
				currentStates[i] = motors[i].getOutputState();
				System.out.println("Port " + i + ":" + currentStates[i].rotationCount + " [deg]");
			}
		}
		for (int i = 0; i < 3; i ++) {
			if (goals[i] != Integer.MAX_VALUE) {
				sleep(15);
				startMotorControl(i);
				goals[i] = Integer.MAX_VALUE;
			}
		}
	}

	private int enumToIndex(Port port) {
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

	private void sleep(int sleep) {
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			// Do nothing.
		}
	}

	private void startMotorControl(int port) {

		MindstormsNXTExtension motor = motors[port];
		if (motor == null) {
			return;
		}

		OutputState currentState = currentStates[port];
		int goal = goals[port];

		if (goal == currentState.rotationCount) {
			return;
		}

		int power = powers[port];
		if (goal < currentState.rotationCount) {
			power += 100;
		}

		int tachoLimit = Math.abs(goal - currentState.rotationCount);

		int mode = 0;
		String command = String.format("1%1d%03d%06d%1d",
				port, power, tachoLimit, mode);

		byte[] commandBytes = command.getBytes();
		MindstormsNXT.messageWrite(
				commandBytes,
				(byte)1,
				connector);
	}
}
