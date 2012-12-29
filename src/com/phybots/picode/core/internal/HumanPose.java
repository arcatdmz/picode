package com.phybots.picode.core.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import com.phybots.picode.Pose;
import com.phybots.picode.RobotType;

public class HumanPose extends Pose {
	public static final int HIP_CENTER = 0;
	public static final int SPINE = 1;
	public static final int SHOULDER_CENTER = 2;
	public static final int HEAD = 3;
	public static final int SHOULDER_RIGHT = 4;
	public static final int ELBOW_RIGHT = 5;
	public static final int WRIST_RIGHT = 6;
	public static final int HAND_RIGHT = 7;
	public static final int SHOULDER_LEFT = 8;
	public static final int ELBOW_LEFT = 9;
	public static final int WRIST_LEFT = 10;
	public static final int HAND_LEFT = 11;
	public static final int HIP_RIGHT = 12;
	public static final int KNEE_RIGHT = 13;
	public static final int ANKLE_RIGHT = 14;
	public static final int FOOT_RIGHT = 15;
	public static final int HIP_LEFT = 16;
	public static final int KNEE_LEFT = 17;
	public static final int ANKLE_LEFT = 18;
	public static final int FOOT_LEFT = 19;

	private static final int ANGLE_WAIST_EULER = 0;
	private static final int ANGLE_LEFT_SHOULDER = 3;
	private static final int ANGLE_LEFT_FOOT = 5;
	private static final int ANGLE_LEFT_ELBOW = 7;
	private static final int ANGLE_LEFT_KNEE = 8;
	private static final int ANGLE_RIGHT_SHOULDER = 9;
	private static final int ANGLE_RIGHT_FOOT = 11;
	private static final int ANGLE_RIGHT_ELBOW = 13;
	private static final int ANGLE_RIGHT_KNEE = 14;

	private Vector[] joints = null;
	private double[] angles = new double[15];

	public HumanPose() {
		super(RobotType.Human);
	}

	@Override
	public void load(BufferedReader reader) throws IOException {
		joints = new Vector[20];
		boolean isError = false;
		for (int i = 0; i < joints.length; i ++) {
			reader.mark(256);
			try {
				String[] xyz = reader.readLine().trim().split(" ");
				if (xyz.length == 3) {
					joints[i] = new Vector(
							Float.valueOf(xyz[0]),
							Float.valueOf(xyz[1]),
							Float.valueOf(xyz[2]));
				} else {
					isError = true;
				}
			} catch (NumberFormatException nfe) {
				isError = true;
				reader.reset();
				break;
			}
		}
		if (isError) {
			joints = null;
		}
		calcAngles();
	}

	@Override
	public void save(BufferedWriter writer) throws IOException {
		if (joints == null) {
			throw new IOException("Pose without joint position data cannot be saved.");
		} else {
			for (int i = 0; i < joints.length; i ++) {
				writer.write(joints[i].toString());
				writer.newLine();
			}
		}
	}

	@Override
	public boolean applyTo(MotorManager motorManager) {
		return false;
	}

	@Override
	public void retrieveFrom(MotorManager motorManager) {
		float[][] data = ((HumanMotorManager) motorManager).getJoints();
		importData(data);
	}

	public void importData(float[][] data) {
		if (data == null) {
			return;
		}
		joints = new Vector[20];
		for (int i = 0; i < joints.length; i ++) {
			joints[i] = new Vector(data[i][0], data[i][1], data[i][2]);
		}
		calcAngles();
	}

	@Override
	public Pose interpolate(Pose pose, float proportion) {
		HumanPose newPose = (HumanPose) this.clone();
		HumanPose kinectPose = (HumanPose) pose;
		newPose.joints = null;
		for (int i = 0; i < angles.length; i ++) {
			double a = angles[i], b = kinectPose.angles[i];
			newPose.angles[i] = a*proportion + b*(1-proportion);
		}
		return newPose;
	}

	@Override
	public boolean eq(Pose pose) {
		return eq(pose, .5f);
	}

	@Override
	public boolean eq(Pose pose, float maxDifference) {
		if (!(pose instanceof HumanPose)) {
			return false;
		}
		HumanPose p = (HumanPose) pose;
		double distance = 0;
		for (int i = 0; i < angles.length; i ++) {
			double diff = angles[i] - p.angles[i]; // 0 ~ 2pi
			distance += diff*diff;
		}
		distance = Math.sqrt(distance/(4*Math.PI*Math.PI*angles.length));
		// System.out.println("distance: " + distance);
		return distance == Double.NaN ? false : distance < maxDifference;
	}

	private void calcAngles() {
		if (joints == null) {
			return;
		}

		// 上半身を示す座標系
		Vector shoulder = joints[SHOULDER_RIGHT].sub(joints[SHOULDER_LEFT]).normalize();
		Vector shoulderCenter = joints[SHOULDER_RIGHT].add(joints[SHOULDER_LEFT]).div(2);
		Vector bodyUpper = joints[SPINE].sub(shoulderCenter).normalize();

		// 下半身を示す座標系
		Vector hip = joints[HIP_RIGHT].sub(joints[HIP_LEFT]).normalize();
		Vector hipCenter = joints[HIP_RIGHT].add(joints[HIP_LEFT]).div(2);
		Vector bodyLower = joints[SPINE].sub(hipCenter).mul(-1).normalize();

		// 平行成分を除去して直交させる
		// b' = b - ((a・b)/|a||a|)a 
		Vector bodyUpperx = bodyUpper.sub(shoulder.mul(shoulder.dot(bodyUpper))).normalize();
		Vector bodyLowerx = bodyLower.sub(hip.mul(hip.dot(bodyLower))).normalize();

		// 体表面方向のベクトル
		Vector frontUpper = bodyUpperx.product(shoulder).normalize();
		Vector frontLower = bodyLowerx.product(hip).normalize();

		// 座標系間の回転行列を求める
		// E = { shoulder, bodyUpperx, frontUpper }
		// F = { hip,      bodyLowerx, frontLower }
		// R = E^TF
		Vector[] e = new Vector[] { shoulder, bodyUpperx, frontUpper };
		Vector[] f = new Vector[] { hip, bodyLowerx, frontLower };
		Vector[] r = productMatrix(transposeMatrix(e), f);
		// Vector[] i = productMatrix(transposeMatrix(r), r);
		// printMatrix(i);

		// 回転行列からオイラー角を求める
		// Cf. http://d.hatena.ne.jp/It_lives_vainly/20070829/1188384519
		if (r[1].z == 1) {
			angles[ANGLE_WAIST_EULER  ] = Math.PI/2;
			angles[ANGLE_WAIST_EULER+1] = 0; // 仮定
			angles[ANGLE_WAIST_EULER+2] = Math.atan2(r[0].y, r[0].x);
		} else if (r[1].z == -1) {
			angles[ANGLE_WAIST_EULER  ] = -Math.PI/2;
			angles[ANGLE_WAIST_EULER+1] = 0; // 仮定
			angles[ANGLE_WAIST_EULER+2] = Math.atan2(r[0].y, r[0].x);
		} else {
			angles[ANGLE_WAIST_EULER  ] = Math.asin(r[1].z);
			angles[ANGLE_WAIST_EULER+1] = Math.atan2(-r[0].z, r[2].z);
			angles[ANGLE_WAIST_EULER+2] = Math.atan2(-r[1].x, r[1].y);
		}

		// 腕の角度を求める
		Vector leftUpperArm = joints[ELBOW_LEFT].sub(joints[SHOULDER_LEFT]).normalize();
		angles[ANGLE_LEFT_SHOULDER  ] = Math.acos(shoulder.dot(leftUpperArm));
		angles[ANGLE_LEFT_SHOULDER+1] = Math.acos(bodyUpper.dot(leftUpperArm));
		Vector rightUpperArm = joints[ELBOW_RIGHT].sub(joints[SHOULDER_RIGHT]).normalize();
		angles[ANGLE_RIGHT_SHOULDER  ] = Math.acos(shoulder.dot(rightUpperArm));
		angles[ANGLE_LEFT_SHOULDER+1] = Math.acos(bodyUpper.dot(rightUpperArm));
		Vector leftLowerArm = joints[ELBOW_LEFT].sub(joints[WRIST_LEFT]).normalize();
		angles[ANGLE_LEFT_ELBOW] = Math.acos(leftLowerArm.dot(leftUpperArm));
		Vector rightLowerArm = joints[ELBOW_RIGHT].sub(joints[WRIST_RIGHT]).normalize();
		angles[ANGLE_RIGHT_ELBOW] = Math.acos(rightLowerArm.dot(rightUpperArm));

		// 足の角度を求める
		Vector leftThigh = joints[KNEE_LEFT].sub(joints[HIP_LEFT]).normalize();
		angles[ANGLE_LEFT_FOOT  ] = Math.acos(hip.dot(leftThigh));
		angles[ANGLE_LEFT_FOOT+1] = Math.acos(bodyLower.dot(leftThigh));
		Vector rightThigh = joints[KNEE_RIGHT].sub(joints[HIP_RIGHT]).normalize();
		angles[ANGLE_RIGHT_FOOT  ] = Math.acos(hip.dot(rightThigh));
		angles[ANGLE_RIGHT_FOOT+1] = Math.acos(bodyLower.dot(rightThigh));
		Vector leftLeg = joints[KNEE_LEFT].sub(joints[ANKLE_LEFT]).normalize();
		angles[ANGLE_LEFT_KNEE] = Math.acos(leftLeg.dot(leftThigh));
		Vector rightLeg = joints[KNEE_RIGHT].sub(joints[ANKLE_RIGHT]).normalize();
		angles[ANGLE_RIGHT_KNEE] = Math.acos(rightLeg.dot(rightThigh));
	}

	public void printMatrix(Vector[] a) {
		for (int i = 0; i < 3; i ++) {
			System.out.print("|");
			for (int j = 0; j < 3; j ++) {
				System.out.print(String.format(" %2.3f", a[j].get(i)));
			}
			System.out.println(" |");
		}
	}

	public boolean compareMatrix(Vector[] i, Vector[] ii) {
		for (int j = 0; j < 3; j ++) {
			for (int k = 0; k < 3; k ++) {
				if (i[j].get(k) != ii[j].get(k)) {
					System.err.println(i[j].get(k) - ii[j].get(k));
					return false;
				}
			}
		}
		return true;
	}

	public Vector[] productMatrix(Vector[] a, Vector[] b) {
		Vector[] c = new Vector[3];
		for (int i = 0; i < 3; i ++) {
			c[i] = new Vector();
			for (int j = 0; j < 3; j ++) {
				float t = 0;
				for (int l = 0; l < 3; l ++) {
					t += a[j].get(l)*b[l].get(i);
				}
				c[i].set(j, t);
			}
		}
		return c;
	}

	public Vector[] transposeMatrix(Vector[] m) {
		Vector[] mt = new Vector[3];
		for (int i = 0; i < 3; i ++) {
			mt[i] = new Vector(m[0].get(i), m[1].get(i), m[2].get(i));
		}
		return mt;
	}

	public double[] getWaistEulerAngle() {
		return new double[] {
				angles[ANGLE_WAIST_EULER  ],
				angles[ANGLE_WAIST_EULER+1],
				angles[ANGLE_WAIST_EULER+2]
		};
	}

	public double[] getLeftShoulderAngle() {
		return new double[] {
				angles[ANGLE_LEFT_SHOULDER  ],
				angles[ANGLE_LEFT_SHOULDER+1]
		};
	}

	public double[] getRightShoulderAngle() {
		return new double[] {
				angles[ANGLE_RIGHT_SHOULDER  ],
				angles[ANGLE_RIGHT_SHOULDER+1]
		};
	}

	public double getLeftElbowAngle() {
		return angles[ANGLE_LEFT_ELBOW];
	}

	public double getRightElbowAngle() {
		return angles[ANGLE_RIGHT_ELBOW];
	}

	public double[] getLeftFootAngle() {
		return new double[] {
				angles[ANGLE_LEFT_FOOT  ],
				angles[ANGLE_LEFT_FOOT+1]
		};
	}

	public double[] getRightFootAngle() {
		return new double[] {
				angles[ANGLE_RIGHT_FOOT  ],
				angles[ANGLE_RIGHT_FOOT+1]
		};
	}

	public double getLeftKneeAngle() {
		return angles[ANGLE_LEFT_KNEE];
	}

	public double getRightKneeAngle() {
		return angles[ANGLE_RIGHT_KNEE];
	}

	private static class Vector {
		float x, y, z;
		public Vector() {
			// Do nothing.
		}
		public Vector(Vector v) {
			this(v.x, v.y, v.z);
		}
		public Vector(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		public double norm() {
			return Math.sqrt(x*x+y*y+z*z);
		}
		public Vector normalize() {
			Vector result = new Vector();
			double norm = norm();
			result.x = (float) (x / norm);
			result.y = (float) (y / norm);
			result.z = (float) (z / norm);
			return result;
		}
		public Vector product(Vector v) {
			Vector result = new Vector();
			result.x = y*v.z-z*v.y;
			result.y = z*v.x-x*v.z;
			result.z = x*v.y-y*v.x;
			return result;
		}
		public float dot(Vector v) {
			return x*v.x + y*v.y + z*v.z;
		}
		public Vector add(Vector v) {
			Vector result = new Vector(this);
			result.x += v.x;
			result.y += v.y;
			result.z += v.z;
			return result;
		}
		public Vector sub(Vector v) {
			Vector result = new Vector(this);
			result.x -= v.x;
			result.y -= v.y;
			result.z -= v.z;
			return result;
		}
		public Vector div(float div) {
			Vector result = new Vector(this);
			result.x /= div;
			result.y /= div;
			result.z /= div;
			return result;
		}
		public Vector mul(float mul) {
			Vector result = new Vector(this);
			result.x *= mul;
			result.y *= mul;
			result.z *= mul;
			return result;
		}
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(x);
			sb.append(" ");
			sb.append(y);
			sb.append(" ");
			sb.append(z);
			return sb.toString();
		}
		public float get(int index) {
			switch (index) {
			case 0: return x;
			case 1: return y;
			case 2: return z;
			default: return Float.NaN;
			}
		}
		public void set(int index, float val) {
			switch (index) {
			case 0: x = val;
			case 1: y = val;
			case 2: z = val;
			default: break;
			}
		}
	}
}
