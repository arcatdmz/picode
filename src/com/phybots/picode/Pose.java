package com.phybots.picode;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.phybots.picode.ui.PicodeSettings;
import com.phybots.picode.ui.editor.DocumentManager;

public abstract class Pose implements Cloneable {
	private RobotType robotType;
	private String name;
	private BufferedImage photo;
	private Icon icon;
	private SimpleAttributeSet attrs;

	public Pose(RobotType robotType) {
		this.robotType = robotType;
	}

	public static Pose load(String name) throws IOException {

		// Load pose data.
		BufferedReader reader = new BufferedReader(new FileReader(new File(
				PicodeSettings.getPoseFolderPath(), getDataFileName(name))));
		String robotTypeString = reader.readLine().trim();
		RobotType robotType = RobotType.valueOf(robotTypeString);
		Pose pose = robotType.newPoseInstance();
		pose.robotType = robotType;
		pose.name = name;
		pose.load(reader);
		reader.close();

		// Load the corresponding photo data.
		String photoFileName = pose.getPhotoFileName();
		pose.setPhoto(ImageIO.read(new File(
				PicodeSettings.getPoseFolderPath(), photoFileName)));
		return pose;
	}

	public void delete() {
		File dataFile = new File(PicodeSettings.getPoseFolderPath(), getDataFileName());
		if (dataFile.exists()) {
			dataFile.delete();
		}
		File photoFile = new File(PicodeSettings.getPoseFolderPath(), getPhotoFileName());
		if (photoFile.exists()) {
			photoFile.delete();
		}
	}

	public void save() throws IOException {

		// Save the pose data.
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
				PicodeSettings.getPoseFolderPath(), getDataFileName(name))));
		writer.write(getRobotType().toString());
		writer.newLine();
		save(writer);
		writer.close();

		// Save the photo.
		if (getPhoto() != null) {
			ImageIO.write(getPhoto(), "JPEG", new File(
					PicodeSettings.getPoseFolderPath(), getPhotoFileName()));
		}
	}

	public RobotType getRobotType() {
		return robotType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void rename(String name) {
		File dataFile = new File(PicodeSettings.getPoseFolderPath(), getDataFileName());
		if (dataFile.exists()) {
			dataFile.renameTo(
					new File(
							PicodeSettings.getPoseFolderPath(),
							getDataFileName(name)));
		}
		File photoFile = new File(PicodeSettings.getPoseFolderPath(), getPhotoFileName());
		if (photoFile.exists()) {
			photoFile.renameTo(
					new File(
							PicodeSettings.getPoseFolderPath(),
							getPhotoFileName(name)));
		}
		this.name = name;
	}

	public String getDataFileName() {
		return getDataFileName(name);
	}

	public static String getDataFileName(String name) {
		return name + ".txt";
	}

	protected abstract void load(BufferedReader reader) throws IOException;

	protected abstract void save(BufferedWriter writer) throws IOException;

	public abstract boolean applyTo(MotorManager motorManager);

	public abstract void retrieveFrom(MotorManager motorManager);

	public abstract Pose interpolate(Pose pose, float proportion);

	public abstract boolean eq(Pose pose);

	public abstract boolean eq(Pose pose, float maxDifference);

	public static Pose interpolate(Pose poseA, Pose poseB, float proportion) {
		return poseA.interpolate(poseB, proportion);
	}

	public BufferedImage getPhoto() {
		return photo;
	}

	public void setPhoto(BufferedImage photo) {
		this.photo = photo;
		if (photo == null) {
			return;
		}
		icon = new ImageIcon(
				resizeImage(photo, 160, 120));
		attrs = new SimpleAttributeSet(
				DocumentManager.getIconAttributes());
		StyleConstants.setIcon(attrs, icon);
	}

	public String getPhotoFileName() {
		return getPhotoFileName(getName());
	}

	public static String getPhotoFileName(String name) {
		return name + ".jpg";
	}

	public Icon getIcon() {
		return icon;
	}

	public SimpleAttributeSet getCharacterAttributes() {
		return attrs;
	}

	public String getCode() {
		return String.format("Roboko.pose(\"%s\")", getName());
	}

	@Override
	public String toString() {
		return getCode();
	}

	@Override
	public Pose clone() {
		Pose pose;
		try {
			pose = (Pose) super.clone();
			return pose;
		} catch (CloneNotSupportedException e) {
			// Never happens.
			return null;
		}
	}

	public static BufferedImage resizeImage(BufferedImage srcImage, int nw, int nh) {
		double sx = (double) nw / srcImage.getWidth();
		double sy = (double) nh / srcImage.getHeight();
		AffineTransform trans = AffineTransform.getScaleInstance(sx, sy);

		BufferedImage newImage = new BufferedImage(
				nw, nh, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = newImage.createGraphics();
		g2.drawImage(srcImage, trans, null);
		g2.dispose();
		return newImage;
	}
}
