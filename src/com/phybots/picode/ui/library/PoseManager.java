package com.phybots.picode.ui.library;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.text.SimpleAttributeSet;

import com.phybots.picode.Pose;
import com.phybots.picode.core.internal.MotorManager;
import com.phybots.picode.ui.PicodeMain;
import com.phybots.picode.ui.PicodeSettings;
import com.phybots.picode.ui.library.internal.IconListModel;
import com.phybots.picode.ui.library.internal.IconProvider;
import com.phybots.picode.ui.library.PoseManager;

public class PoseManager extends IconListModel<Pose> implements IconProvider {
	private static final long serialVersionUID = 4052983716336989888L;
	public static final String INITIAL_POSE_NAME = "Initial pose";
	private PicodeMain robokoMain;
	private int numPoses;

	/**
	 * Default constructor.
	 * When the parameter is null, this manager does not provide functionality to {@link #capture()} new poses.
	 *
	 * @param robokoMain
	 */
	public PoseManager(PicodeMain robokoMain) {
		this.robokoMain = robokoMain;
		initialize();
	}

	private void initialize() {
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
				load(file.getName().substring(
						0, file.getName().lastIndexOf(".jpg")));
			} catch (IOException e) {
				// e.printStackTrace();
			}
		}
	}

	public boolean hasInitialPose() {
		return get(INITIAL_POSE_NAME) != null;
	}

	public Pose load(String poseName) throws IOException {
		Pose pose = Pose.load(poseName);
		if (pose != null) {
			addElement(pose);
		}
		return pose;
	}

	public void save(Pose pose) throws IOException {
		pose.save();
	}

	public Pose duplicate(Pose pose) {
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
			save(newPose);
		} catch (IOException e) {
			return null;
		}
		addElement(newPose);
		return newPose;
	}

	/*
	@Override
	public void add(int index, Object object) {
		super.add(index, object);
		numPoses ++;
	}

	@Override
	public void addElement(Object object) {
		super.addElement(object);
		numPoses ++;
	}

	@Override
	public void insertElementAt(Object object, int index) {
		super.insertElementAt(object, index);
		numPoses ++;
	}
	*/

	@Override
	public boolean removeElement(Object object) {
		if (super.removeElement(object)) {
			if (object instanceof Pose) {
				((Pose) object).delete();
			}
			return true;
		}
		return false;
	}

	public Pose capture() {
		Pose pose = robokoMain.getRobot().getType().newPoseInstance();
		while (true) {
			pose.setName(String.format("New pose (%d)", numPoses ++));
			if (!new File(
					PicodeSettings.getPoseFolderPath(),
					pose.getDataFileName()).exists()) {
				break;
			}
		}
		MotorManager motorManager = robokoMain.getRobot().getMotorManager();
		pose.retrieveFrom(motorManager);
		pose.setPhoto(motorManager.getImage());
		try {
			save(pose);
		} catch (IOException e) {
			return null;
		}
		addElement(pose);
		return pose;
	}

	public Pose get(Object object) {
		if (object == null) {
			return null;
		}
		if (object instanceof String) {
			return get((String) object);
		} else if (object instanceof Pose) {
			return (Pose) object;
		}
		return null;
	}

	public Icon getIcon(Object object) {
		Pose pose = get(object);
		return pose != null ? pose.getIcon() : null;
	}

	public String getName(Object object) {
		Pose pose = get(object);
		return pose != null ? pose.getName() : null;
	}

	public SimpleAttributeSet getCharacterAttributes(String poseName) {
		return getCharacterAttributes(get(poseName));
	}

	public SimpleAttributeSet getCharacterAttributes(Pose pose) {
		return pose == null ? null : pose.getCharacterAttributes();
	}
}
