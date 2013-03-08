package nxt.motorcontrol;
/*
 * PROJECT: Phybots at http://phybots.com/
 * ----------------------------------------------------------------------------
 *
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Phybots.
 *
 * The Initial Developer of the Original Code is Jun Kato.
 * Portions created by the Initial Developer are
 * Copyright (C) 2009 Jun Kato. All Rights Reserved.
 *
 * Contributor(s): Jun Kato
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 */


import com.phybots.entity.MindstormsNXT;
import com.phybots.entity.PhysicalRobot;
import com.phybots.entity.MindstormsNXT.MindstormsNXTExtension;

import robot.RobotInfo;

public class ExecuteMotorControl {

	public static void main(String[] args) {
		new ExecuteMotorControl();
	}

	public ExecuteMotorControl() {

		String programName = "MotorControl22.rxe";

		// Connect to the NXT robot.
		PhysicalRobot robot = RobotInfo.getRobot();
		if (robot.connect()) {
			System.out.println("connection succeeded");
		} else {
			System.out.println("connection failed");
			return;
		}
		((MindstormsNXT)robot).removeDifferentialWheels();
		((MindstormsNXT)robot).addExtension(
				"MindstormsNXTExtension", MindstormsNXT.Port.A);
		MindstormsNXTExtension ext = robot.requestResource(
				MindstormsNXTExtension.class, robot);

		// Get initial rotation count.
		int initialCount = ext.getOutputState().rotationCount;
		System.out.print("rotation count: ");
		System.out.println(initialCount);

		// Execute motor control.
		String currentProgramName = MindstormsNXT.getCurrentProgramName(robot.getConnector());
		if (!programName.equals(currentProgramName)) {
			System.out.println("motor control launching");
			MindstormsNXT.stopProgram(
					robot.getConnector());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				return;
			}
			MindstormsNXT.startProgram(
					programName,
					robot.getConnector());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				return;
			}
		}
		System.out.println("motor control launched");

		// Control the motor.
		int port = 0; // A
		int power = 20; // 30 [0~100: 0~100, 101~200: -1~-100]
		int tachoLimit = 45; // 45 degrees [0-999999]
		int mode = 0;
		String command = String.format("1%1d%03d%06d%1d",
				port, power, tachoLimit, mode);

		System.out.print("command: ");
		System.out.println(command);

		byte[] commandBytes = command.getBytes();
		MindstormsNXT.messageWrite(
				commandBytes,
				(byte)1, robot.getConnector());

		System.out.print("command (byte format):");
		for (byte b : commandBytes) {
			System.out.print(" ");
			System.out.print(b);
		}
		System.out.println();

		try {
			Thread.sleep(15);
		} catch (InterruptedException e) {
			return;
		}

		// Query the motor status.
		command = String.format("3%1d", port);
		System.out.print("command: ");
		System.out.println(command);
		commandBytes = command.getBytes();
		MindstormsNXT.messageWrite(
				commandBytes,
				(byte)1, robot.getConnector());

		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			return;
		}

		String message = MindstormsNXT.messageRead(
				(byte)0, (byte)0, true, robot.getConnector());
		System.out.print("reply: ");
		System.out.println(message); // expects 00: port A is still busy.

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			return;
		}
		
		// Query the motor status. (2nd trial)
		MindstormsNXT.messageWrite(
				commandBytes,
				(byte)1, robot.getConnector());

		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			return;
		}

		message = MindstormsNXT.messageRead(
				(byte)0, (byte)0, true, robot.getConnector());
		System.out.print("reply (2nd trial): ");
		System.out.println(message); // expects 01: port A is ready.

		// Get current rotation count.
		int finalCount = ext.getOutputState().rotationCount;
		System.out.print("rotation count: ");
		System.out.println(finalCount);
		System.out.print("error: ");
		System.out.print(finalCount - initialCount - tachoLimit);
		System.out.println(" degrees");
	}

}
