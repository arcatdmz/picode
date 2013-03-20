package com.phybots.picode;

import java.awt.Rectangle;
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
	private static final int STEP_IDE = 1;
	private static final int STEP_POSERS = 2;
	private static final String HEADER_IDE = "[Picode IDE]";
	private static final String HEADER_POSERS = "[Posers]";
	private String filePath;
	private int windowState;
	private int x, y, width, height;
	
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
			int step = -1;
			while ((line = br.readLine()) != null) {
				if (HEADER_IDE.equals(line)) {
					step = STEP_IDE;
					continue;
				} else if (HEADER_POSERS.equals(line)) {
					step = STEP_POSERS;
					continue;
				}
				String[] words = line.split("=", 2);
				if (words.length < 2) continue;
				words[0] = words[0].trim();
				words[1] = words[1].trim();
				if (step == STEP_POSERS) {
					deserializePosers(words[0], words[1], posers);
				} else if (step == STEP_IDE) {
					deserializeIde(words[0], words[1]);
				}
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
			serializeIde(bw);
			serializePosers(bw);
			bw.close();
		} catch (IOException e) {
			System.err.print("Error while writing config to the file: ");
			System.err.println(filePath);
		}
	}

	private void deserializeIde(String key, String value) {
		if ("window.state".equals(key)) {
			setIdeWindowState(Integer.valueOf(value.trim()));
		} else if ("window.bounds".equals(key)) {
			String[] metrics = value.split(",");
			if (metrics.length == 4) {
				x = Integer.valueOf(metrics[0].trim());
				y = Integer.valueOf(metrics[1].trim());
				width = Integer.valueOf(metrics[2].trim());
				height = Integer.valueOf(metrics[3].trim());
			}
		}
	}

	private void serializeIde(BufferedWriter bw) throws IOException {
		bw.write(HEADER_IDE);
		bw.newLine();
		bw.write(String.format("window.state = %d", getIdeWindowState()));
		bw.newLine();
		bw.write(String.format("window.bounds = %d, %d, %d, %d", x, y, width, height));
		bw.newLine();
		bw.newLine();
	}

	@SuppressWarnings("unchecked")
	private void deserializePosers(String key, String value,
			Map<Integer, Poser> posers) {

		// Get the poser.
		String[] subkeys = key.split("\\.");
		if (subkeys.length != 3
				|| !"poser".equals(subkeys[0])) {
			return;
		}
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

	private void serializePosers(BufferedWriter bw) throws IOException {
		bw.write(HEADER_POSERS);
		bw.newLine();
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
		bw.newLine();
	}

	public int getIdeWindowState() {
		return windowState;
	}

	public void setIdeWindowState(int windowState) {
		this.windowState = windowState;
	}

	public Rectangle getIdeWindowBounds() {
		return new Rectangle(x, y, width, height);
	}

	public void setIdeWindowBounds(Rectangle r) {
		this.x = r.x;
		this.y = r.y;
		this.width = r.width;
		this.height = r.height;
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
