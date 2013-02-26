package com.phybots.picode.api;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.phybots.picode.PicodeSettings;


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

	public Pose duplicatePose(Pose pose) {
		Pose newPose = pose.clone();
		int i = 0;
		while (true) {
			if (i == 0) {
				newPose.setName(String.format("Copy of %s", pose.getName()));
			} else {
				newPose.setName(String.format("Copy (%d) of %s", i ++, pose.getName()));
			}
			if (!new File(
					PicodeSettings.getPoseFolderPath(),
					newPose.getDataFileName()).exists()) {
				break;
			}
		}
		try {
			newPose.save();
		} catch (IOException e) {
			return null;
		}
		poses.put(newPose.getName(), newPose);
		return newPose;
	}

	void addPose(Pose pose) {
		poses.put(pose.getName(), pose);
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
