package recplay;
import com.phybots.entity.MindstormsNXT;
import com.phybots.entity.MindstormsNXT.MindstormsNXTExtension;
import com.phybots.entity.MindstormsNXT.OutputState;


public class Pose {
	public OutputState a, b, c;
	public int aState, bState, cState;
	private Pose() {
	}

	public void applyTo(Motors motors) {
		setRotationCount(motors.a, a.rotationCount);
		setRotationCount(motors.b, b.rotationCount);
		setRotationCount(motors.c, c.rotationCount);
	}

	public boolean isActing() {
		return a.runState == MindstormsNXT.MOTOR_RUN_STATE_RUNNING ||
				b.runState == MindstormsNXT.MOTOR_RUN_STATE_RUNNING ||
				c.runState == MindstormsNXT.MOTOR_RUN_STATE_RUNNING;
	}

	public static void setRotationCount(MindstormsNXTExtension ext, int rotation) {
		OutputState os = ext.getOutputState();
		if (rotation == os.rotationCount) return;
		boolean negative = rotation - os.rotationCount < 0;
		int diff = Math.abs(rotation - os.rotationCount);
		ext.setOutputState(
				(byte) (negative ? -20 : 20),
				MindstormsNXT.MOTORON | MindstormsNXT.REGULATED | MindstormsNXT.BRAKE,
				MindstormsNXT.REGULATION_MODE_MOTOR_SPEED,
				0,
				MindstormsNXT.MOTOR_RUN_STATE_RUNNING,
				diff);
	}

	public String toString() {
		return String.format("%d,%d,%d", a.rotationCount, b.rotationCount, c.rotationCount);
	}

	public static Pose retrieveFrom(Motors motors) {
		Pose pose = new Pose();
		pose.a = motors.a.getOutputState();
		pose.b = motors.b.getOutputState();
		pose.c = motors.c.getOutputState();
		return pose;
	}
}