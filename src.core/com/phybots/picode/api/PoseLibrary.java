package com.phybots.picode.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import com.phybots.picode.PicodeSettings;


public class PoseLibrary {
	
	private static PoseLibrary instance;
	
	public static PoseLibrary getInstance() {
		if (instance == null) {
			instance = new PoseLibrary();
		}
		return instance;
	}
	
	private Map<String, Pose> poses;
	private PicodeInterface ide;
	private int numPoses;
	
	public PoseLibrary() {
		poses = new HashMap<String, Pose>();
		listPoses();
		Picode.getInstance();
	}

	public static Pose load(String name) throws IOException {
		PoseLibrary poseLibrary = PoseLibrary.getInstance();
		if (poseLibrary.contains(name)) {
			return poseLibrary.get(name);
		}
		return poseLibrary.doLoad(name);
	}

	public void attachIDE(PicodeInterface ide) {
		this.ide = ide;
		for (Pose pose : poses.values()) {
			ide.onAddPose(pose);
		}
	}

	private void listPoses() {
		File poseFolder = new File(PicodeSettings.getPoseFolderPath());
		for (File file : poseFolder.listFiles(new FilenameFilter() {
			public boolean accept(File folder, String fileName) {
				if (fileName.toLowerCase().endsWith(".jpg")) {
					return true;
				}
				return false;
			}
		})) {
			try {
				String fileName = file.getName();
				doLoad(fileName.substring(
						0, fileName.toLowerCase().lastIndexOf(".jpg")));
			} catch (IOException e) {
				// e.printStackTrace();
			}
		}
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
		addPose(newPose);
		return newPose;
	}

	private Pose doLoad(String name) throws IOException {
		if (contains(name)) {
			return get(name);
		}

		// Load pose data.
		BufferedReader reader = new BufferedReader(new FileReader(new File(
				PicodeSettings.getPoseFolderPath(), Pose.getDataFileName(name))));
		String poserIdentifier = reader.readLine().trim();

		// Setup pose instance.
		String[] poserInfo = poserIdentifier.split(Poser.identifierSeparator, 2);
		PoserTypeInfo poserType = PoserLibrary.getTypeInfo(poserInfo[0]);
		Pose pose;
		try {
			pose = poserType.poseConstructor.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			reader.close();
			return null; // This shouldn't happen.
		}
		pose.setPoserIdentifier(poserIdentifier);
		pose.setPoserType(poserType);
		pose.setName(name);
		pose.load(reader);
		reader.close();

		// Load the corresponding photo data.
		String photoFileName = pose.getPhotoFileName();
		pose.setPhoto(ImageIO.read(new File(
				PicodeSettings.getPoseFolderPath(), photoFileName)));

		// Add the pose instance to the pose library.
		addPose(pose);
		return pose;
	}

	void addPose(Pose pose) {
		poses.put(pose.getName(), pose);
		if (ide != null) {
			ide.onAddPose(pose);
		}
	}

	void removePose(Pose pose) {
		String key = null;
		for (Entry<String, Pose> e : poses.entrySet()) {
			if (e.getValue() == pose) {
				key = e.getKey();
			}
		}
		poses.remove(key);
		if (ide != null) {
			ide.onRemovePose(pose);
		}
	}

	String newName() {
		String name = "";
		while (true) {
			name = String.format("New pose (%d)", numPoses ++);
			if (!new File(
					PicodeSettings.getPoseFolderPath(),
					Pose.getDataFileName(name)).exists()) {
				break;
			}
		}
		return name;
	}

}
