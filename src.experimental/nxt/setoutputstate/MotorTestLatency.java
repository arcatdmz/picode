package nxt.setoutputstate;
import java.util.Date;

import com.phybots.entity.MindstormsNXT;
import com.phybots.entity.MindstormsNXT.MindstormsNXTExtension;
import com.phybots.entity.MindstormsNXT.OutputState;
import com.phybots.entity.MindstormsNXT.Port;

import jp.digitalmuseum.connector.BluetoothConnector;
import jp.digitalmuseum.connector.Connector;
import jp.digitalmuseum.connector.FantomConnector;

public class MotorTestLatency {

	public static void main(String[] args) {
		new MotorTestLatency();
	}

	public MotorTestLatency() {

		// Find connected NXT.
		String[] ids = FantomConnector.queryIdentifiers();
		if (ids == null) {
			System.err.println("No NXT found.");
			return;
		}
		test(new FantomConnector(ids[0]));

		// Connect to the specific NXT via Bluetooth.
		test(new BluetoothConnector("btspp://001653059D01"));
	}
	
	private void test(Connector con) {
		System.out.print("---");
		System.out.println(con.getConnectionString());
		con.connect();
		MindstormsNXT.latency = 0;
		MindstormsNXTExtension ext = new MindstormsNXTExtension(con, Port.A);

		long then = new Date().getTime();
		OutputState os = ext.getOutputState();
		long now = new Date().getTime();

		int lastCount = os.rotationCount;
		int power = 90;
		int goal = 180;

		System.out.print("get output state latency [ms]: ");
		System.out.println(now - then);

		then = new Date().getTime();
		ext.setOutputState(
				(byte) power,
				MindstormsNXT.MOTORON | MindstormsNXT.REGULATED | MindstormsNXT.BRAKE,
				MindstormsNXT.REGULATION_MODE_MOTOR_SPEED,
				0,
				MindstormsNXT.MOTOR_RUN_STATE_RAMPUP,
				goal);
		now = new Date().getTime();

		System.out.print("set output state latency [ms]: ");
		System.out.println(now - then);

		long latency = 0;
		for (int i = 0; i < 1000; i ++) {
			then = new Date().getTime();
			os = ext.getOutputState();
			now = new Date().getTime();
			latency += now - then;
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.print("get output state latency x500 [ms]: ");
		System.out.println(latency);

		System.out.print("tacho limit error: ");
		System.out.println(os.rotationCount - lastCount - goal);

		ext.setOutputState(
				(byte) 0,
				0,
				0,
				0,
				0,
				0);
		con.disconnect();
	}

}
