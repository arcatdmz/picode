import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import com.phybots.entity.MindstormsNXT;
import com.phybots.entity.MindstormsNXT.MindstormsNXTExtension;
import com.phybots.entity.MindstormsNXT.OutputState;

import jp.digitalmuseum.connector.FantomConnector;


public class MindstormsNXTMotorTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new MindstormsNXTMotorTest();
	}

	public MindstormsNXTMotorTest() {
		initialize();
		test();
	}

	private void test() {
		try {
			FileWriter fw = new FileWriter("motor.log");
			for (int goal = 5; goal < 20; goal += 5) {
				test(goal, fw);
			}
			for (int goal = 20; goal < 120; goal += 10) {
				test(goal, fw);
			}
			for (int goal = 120; goal < 360; goal += 30) {
				test(goal, fw);
			}
			for (int goal = 360; goal <= 720; goal += 120) {
				test(goal, fw);
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void test(int goal, FileWriter fw) throws IOException {
		System.out.print("Testing ");
		System.out.print(goal);
		System.out.print(" degrees: ");

		// Test with power 5.
		int bestPow = 5;
		Result bestResult = multipleTest(bestPow, goal, 10);
		fw.append(String.format("%d,%d,%f,%d\n", goal, bestPow, bestResult.diff, bestResult.time));
		for (int pow = 1; pow < 9; pow ++) {

			// Test with power 10, 20, ... 80.
			Result result = multipleTest(pow * 10, goal, 10);
			fw.append(String.format("%d,%d,%f,%d\n", goal, pow * 10, result.diff, result.time));
			if (result.diff < bestResult.diff) {
				bestResult = result;
				bestPow = pow * 10;
			}
		}
		System.out.println(String.format("the most suitable power was %d with the smallest diff of %.2f degrees.", bestPow, bestResult.diff));
		fw.flush();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private MindstormsNXTExtension ext;
	private void initialize() {
		// Find connected NXT.
		String[] ids = FantomConnector.queryIdentifiers();
		if (ids == null) {
			System.err.println("No NXT found.");
			return;
		}

		// Instantiate NXT.
		FantomConnector fc = new FantomConnector(ids[0]);
		MindstormsNXT nxt = new MindstormsNXT(fc);
		nxt.removeDifferentialWheels();
		MindstormsNXTExtension ext = new MindstormsNXTExtension(nxt, MindstormsNXT.Port.A);
		nxt.addExtension(ext);
		this.ext = nxt.requestResource(MindstormsNXTExtension.class, this);
	}

	private Result multipleTest(int power, int goal, int num) {
		long startTime = new Date().getTime();
		int diff = 0;
		for (int i = 0; i < num; i ++) {
			diff += singleTest(power, goal);
		}
		long endTime = new Date().getTime();
		return new Result((float)diff / num, (endTime - startTime) / num);
	}

	private int singleTest(int power, int goal) {

		OutputState os = ext.getOutputState();
		int lastCount = os.tachoCount;

		ext.setOutputState(
				(byte) power,
				MindstormsNXT.MOTORON | MindstormsNXT.REGULATED | MindstormsNXT.BRAKE,
				MindstormsNXT.REGULATION_MODE_MOTOR_SPEED,
				0,
				MindstormsNXT.MOTOR_RUN_STATE_RUNNING,
				goal);

		while (true) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			os = ext.getOutputState();
			if (os.runState != MindstormsNXT.MOTOR_RUN_STATE_RUNNING) {
				break;
			}
		}
		// System.out.print("rotated: ");
		// System.out.println(os.tachoCount - lastCount);

		int diff = Math.abs(os.tachoCount - lastCount - goal);
		// System.out.print("diff: ");
		// System.out.println(diff);

		ext.setOutputState(
				(byte) 0,
				0,
				0,
				0,
				0,
				0);
		return diff;
	}

	private static class Result {
		public float diff;
		public long time;
		public Result(float diff, long time) {
			this.diff = diff;
			this.time = time;
		}
	}

}
