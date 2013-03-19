package com.phybots.picode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.phybots.picode.api.Poser;
import com.phybots.picode.api.PoserLibrary;
import com.phybots.picode.api.PoserWithConnector;
import com.phybots.picode.camera.Camera;
import com.phybots.picode.camera.CameraManager;

public class PicodeSettings {
	private String filePath;
	
	public PicodeSettings() {
		this(System.getProperty("user.dir") + "\\settings.txt");
	}

	public PicodeSettings(String filePath) {
		this.filePath = filePath;
	}

	public boolean load() {

		// Start reading the file.
		BufferedReader br = null;
		try {
			FileReader fr = new FileReader(filePath);
			br = new BufferedReader(fr);
		} catch (FileNotFoundException e) {
			System.err.print("Config file not found: ");
			System.err.println(filePath);
			return false;
		}

		// Load settings.
		Map<Integer, Poser> posers = new HashMap<Integer, Poser>();
		String line;
		try {
			while ((line = br.readLine()) != null) {
				String[] words = line.split("=", 2);
				if (words.length < 2) continue;
				deserialize(words[0].trim(), words[1].trim(), posers);
			}
			br.close();
		} catch (IOException e) {
			System.err.print("Error while reading the config file: ");
			System.err.println(filePath);
		}

		// Connect to the poser if desired.
		for (Poser poser : posers.values()) {
			if (poser instanceof PoserWithConnector) {
				((PoserWithConnector) poser).connect();
			}
		}
		return true;
	}
	
	public void save() {

		// Save settings.
		try {
			FileWriter fw = new FileWriter(filePath);
			BufferedWriter bw = new BufferedWriter(fw);
			serialize(bw);
			bw.close();
		} catch (IOException e) {
			System.err.print("Error while writing config to the file: ");
			System.err.println(filePath);
		}
	}

	@SuppressWarnings("unchecked")
	private void deserialize(String key, String value,
			Map<Integer, Poser> posers) {
		String[] subkeys = key.split("\\.");
		if (subkeys.length == 3) {
			if ("poser".equals(subkeys[0])) {
				int index = Integer.valueOf(subkeys[1]);
				Poser poser = posers.get(index);

				// Poser id
				if (poser == null) {
					if ("id".equals(subkeys[2])) {
						poser = PoserLibrary.newInstance(value);
						if (poser != null) {
							posers.put(index, poser);
						}
					}
					return;
				}

				// Poser name
				if ("name".equals(subkeys[2])) {
					poser.setName(value);

				// Poser camera
				} else if ("camera".equals(subkeys[2])) {
					try {
						Class<?> cameraClass = Class.forName(
								String.format("%s.%s", CameraManager.packageName, value));
						if (cameraClass != null
								&& Camera.class.isAssignableFrom(cameraClass)) {
							Camera camera = PoserLibrary.getInstance().getCameraManager().getCamera(
									(Class<? extends Camera>)cameraClass);
							PoserLibrary.getInstance().getCameraManager().putCamera(poser, camera);
						}
					} catch (Exception e) {
						// Do nothing.
					}
				}
			}
		}
	}

	private void serialize(BufferedWriter bw) throws IOException {
		int i = 0;
		for (Poser poser : PoserLibrary.getInstance().getPosers()) {

			bw.write(String.format(
					"poser.%d.id = %s", i, poser.getIdentifier()));
			bw.newLine();

			if (poser.getName() != null) {
				bw.write(String.format(
						"poser.%d.name = %s", i, poser.getName()));
				bw.newLine();
			}

			if (poser.getCamera() != null) {
				bw.write(String.format(
						"poser.%d.camera = %s", i, poser.getCamera().getClass().getSimpleName()));
				bw.newLine();
			}
			i ++;
		}
	}

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
		return "file:"
				+ (File.separatorChar == '/' ? getPoseFolderPath()
						: getPoseFolderPath().replace(File.separator, "/"));
	}

	public static String getDefaultSketchPath() {
		return System.getProperty("user.dir")
				+ "\\projects\\HelloWorld\\HelloWorld.pde";
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
