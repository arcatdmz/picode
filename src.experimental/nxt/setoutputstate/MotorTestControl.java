package nxt.setoutputstate;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jp.digitalmuseum.connector.FantomConnector;

import com.phybots.entity.MindstormsNXT;
import com.phybots.entity.MindstormsNXT.MindstormsNXTExtension;
import com.phybots.entity.MindstormsNXT.OutputState;
import com.phybots.entity.MindstormsNXT.Port;


public class MotorTestControl {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new MotorTestControl();
	}

	private ScheduledExecutorService ses;
	private MindstormsNXTExtension ext;
	private boolean hasRequestedToStop = false;
	public MotorTestControl() {

		ses = Executors.newSingleThreadScheduledExecutor();

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
		ext = new MindstormsNXTExtension(con, Port.A);
		System.out.println("diff: " + test(10, 100));

		ses.submit(new Runnable() {
			public void run() {
				drive(0);
				con.disconnect();
				ses.shutdown();
			}
		});
	}

	private int diff;
	private Future<?> future1;
	private Future<?> future2;
	private int test(int power, final int goal) {
		final int initialCount = ext.getOutputState().rotationCount;
		System.out.println("initial count: " + initialCount);
		drive(10);

		future1 = ses.scheduleAtFixedRate(new Runnable() {
			public void run() {
				OutputState os = ext.getOutputState();
				int d = os.rotationCount - initialCount;
				if (d > (goal - 30) && !hasRequestedToStop) {
					drive(0);
					hasRequestedToStop = true;
					
					// wait for 1 second
					future2 = ses.schedule(new Runnable() {
						public void run() {
							OutputState os = ext.getOutputState();
							System.out.println("final count: " + os.rotationCount);
							diff = os.rotationCount - initialCount - goal;
						}
					}, 1000, TimeUnit.MILLISECONDS);
					future1.cancel(false);
				}
			}
		}, 10, 10, TimeUnit.MILLISECONDS);
		try {
			try {
				future1.get();
			} catch (CancellationException ce) {
				// Do nothing.
			}
			future2.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return diff;
	}

	private void drive(int power) {
		if (power == 0) {
			ext.setOutputState((byte) 0, 0, 0, 0, 0, 0);
			return;
		}
		ext.setOutputState(
				(byte) power,
				MindstormsNXT.MOTORON | MindstormsNXT.REGULATED,
				MindstormsNXT.REGULATION_MODE_MOTOR_SPEED,
				0,
				MindstormsNXT.MOTOR_RUN_STATE_RUNNING,
				0);
	}
}
