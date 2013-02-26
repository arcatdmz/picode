package com.phybots.picode.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


public class GlobalPoseLibrary {
	private Map<String, Pose> poses;
	
	public GlobalPoseLibrary() {
		poses = new HashMap<String, Pose>();
	}

	public Pose get(String poseName) {
		return poses.get(poseName);
	}

	public boolean contains(String poseName) {
		return poses.containsKey(poseName);
	}

	public void duplicatePose(Pose pose) {
	}
	
	void addPose(Pose pose) {
		
	}

	void removePose(Pose pose) {
		String key = null;
		for (Entry<String, Pose> e : poses.entrySet()) {
			if (e.getValue() == pose) {
				key = e.getKey();
			}
		}
		poses.remove(key);
		
	}

}
