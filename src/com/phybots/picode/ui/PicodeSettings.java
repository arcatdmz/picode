package com.phybots.picode.ui;

import java.io.File;
import java.io.IOException;

import processing.app.RobokoSketch;

public class PicodeSettings {

	/**
	 * *.pdeから*.javaにしたものを一時的に保管するフォルダ
	 */
	public static String getSrcFolderPath() {
		return System.getProperty("user.dir") + "\\p5\\src";
	}

	/**
	 * *.javaをコンパイルしたクラスファイルを一時的に保管するフォルダ
	 */
	public static String getBinFolderPath() {
		return System.getProperty("user.dir") + "\\p5\\bin";
	}

  public static File getExamplesFolder() {
    return new File(getExamplesFolderPath());
  }

	/**
	 * サンプル置き場
	 */
	public static String getExamplesFolderPath() {
		return System.getProperty("user.dir") + "\\examples";
	}

	public static String getPoseFolderPath() {
		return System.getProperty("user.dir") + "\\poses";
	}

	public static String getPoseFolderURL() {
		return "file:" + (File.separatorChar == '/' ?
				getPoseFolderPath() :
				getPoseFolderPath().replace(File.separator, "/"));
	}

	public static RobokoSketch getDefaultSketch(PicodeMain robokoMain) throws IOException {
		return new RobokoSketch(robokoMain, getDefaultSketchPath());
	}

	public static RobokoSketch getDefaultSketch() throws IOException {
		return getDefaultSketch(null);
	}

	public static String getDefaultSketchPath() {
		return System.getProperty("user.dir") + "\\projects\\HelloWorld\\HelloWorld.pde";
	}

	public static String getRobokoBinaryPath() {
		return System.getProperty("user.dir") + "\\bin";
	}

	public static File getLibrariesFolder() {
		return new File(System.getProperty("user.dir") + "\\lib");
	}

	public static File getSketchbookLibrariesFolder() {
		return new File(getSketchbookLibrariesFolderPath());
	}

	public static String getSketchbookLibrariesFolderPath() {
		return System.getProperty("user.dir") + "\\libraries";
	}

	public static String getProjectsFolderPath() {
		return System.getProperty("user.dir") + "\\projects";
	}
}
