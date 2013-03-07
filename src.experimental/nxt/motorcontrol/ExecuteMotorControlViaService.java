package nxt.motorcontrol;
import jp.digitalmuseum.connector.FantomConnector;

import com.phybots.Phybots;
import com.phybots.entity.MindstormsNXT;
import com.phybots.entity.MindstormsNXT.MindstormsNXTExtension;
import com.phybots.entity.MindstormsNXT.Port;
import com.phybots.task.NXTMotorControl;

public class ExecuteMotorControlViaService {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ExecuteMotorControlViaService();
	}

	public ExecuteMotorControlViaService() {

		// Find connected NXT.
		String[] ids = FantomConnector.queryIdentifiers();
		if (ids == null) {
			System.err.println("No NXT found.");
			return;
		}

		//
		final FantomConnector con = new FantomConnector(ids[0]);
		con.connect();

		//
		MindstormsNXT.latency = 0;
		MindstormsNXTExtension ext = new MindstormsNXTExtension(con, Port.A);

		final NXTMotorControl mc = new NXTMotorControl(new MindstormsNXTExtension[] {
			ext, null, null
		}, con);
		mc.start();

		Phybots.getInstance().submit(new Runnable() {
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// Do nothing.
				}

				int count = mc.getRotationCount(MindstormsNXT.Port.A);
				System.out.println("current count: " + count);

				int goal = count + 360;
				mc.setPower(80, MindstormsNXT.Port.A);
				mc.setRotationCount(goal, MindstormsNXT.Port.A);
				System.out.println("goal count: " + goal);

				try {
					Thread.sleep(4000);
				} catch (InterruptedException e) {
					// Do nothing.
				}

				count = mc.getRotationCount(MindstormsNXT.Port.A);
				System.out.println("final count: " + count);

				Phybots.getInstance().dispose();
			}
		});
	}
}
