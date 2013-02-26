package com.phybots.picode.api;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.text.SimpleAttributeSet;

import com.phybots.picode.PicodeSettings;
import com.phybots.picode.api.PoseLibrary;
import com.phybots.picode.camera.Camera;
import com.phybots.picode.ui.list.IconListModel;
import com.phybots.picode.ui.list.IconProvider;

public class PoseLibrary extends IconListModel<Pose> implements IconProvider {
	private static final long serialVersionUID = 4052983716336989888L;
	private Poser poser;
	private int numPoses;

	public PoseLibrary(Poser poser) {
		this.poser = poser;
		initialize();
	}

	private void initialize() {
		/*
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
		*/
		
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
		MotorManager motorManager = poser.getMotorManager();
		Pose pose = motorManager.getPose();
		while (true) {
			pose.setName(String.format("New pose (%d)", numPoses ++));
			if (!new File(
					PicodeSettings.getPoseFolderPath(),
					pose.getDataFileName()).exists()) {
				break;
			}
		}
		Camera camera = poser.getCamera();
		pose.setPhoto(camera.getImage());
		try {
			pose.save();
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
