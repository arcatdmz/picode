package jp.digitalmuseum.roboko.builder;

import java.io.File;
import java.io.IOException;

import jp.digitalmuseum.roboko.ProcessingIntegration;
import jp.digitalmuseum.roboko.RobokoMain;
import jp.digitalmuseum.roboko.RobokoSettings;

import processing.app.RobokoSketch;
import processing.app.SketchException;
import processing.mode.java.preproc.PdePreprocessor;

public class Builder {

	RobokoMain robokoMain;
	RobokoSketch sketch;
	File srcFolder;
	File binFolder;
	File codeFolder;

	/**
	 * Java library path for running Processing code. Set in
	 * {@link #preprocess(PdePreprocessor)} method.
	 */
	String javaLibraryPath;

	/**
	 * Class path for compiling and running Processing code. Set in
	 * {@link #preprocess(PdePreprocessor)} method.
	 */
	String classPath;

	String packageName;

	String mainClassName;
	boolean foundMain = false;

	public Builder(RobokoMain robokoMain, RobokoSketch sketch) {
		this.robokoMain = robokoMain;
		this.sketch = sketch;
	}

	public RobokoMain getRobokoMain() {
		return robokoMain;
	}

	public static void main(String[] args) {
		ProcessingIntegration.init();
		RobokoSketch sketch = null;
		try {
			sketch = RobokoSettings.getDefaultSketch();
			Builder builder = new Builder(null, sketch)
					// .setCodeFolder("D:\\Users\\arc\\Desktop\\BouncyBubbles\\code")
					.setSrcFolder(RobokoSettings.getSrcFolderPath())
					.setBinFolder(RobokoSettings.getBinFolderPath());
			builder.run();
		} catch (IOException e) {
			System.err.println(String.format("Can't load Processing code: %s",
					RobokoSettings.getDefaultSketchPath()));
		} catch (SketchException se) {
			System.err.println(RobokoMain.getErrorString(sketch, se));
		}
	}

	public Launcher run() throws SketchException {
		Preprocessor preprocessor = new Preprocessor(this);
		preprocessor.preprocess(new PdePreprocessor(sketch.getName()));
		Compiler compiler = new Compiler(this);
		if (compiler.compile()) {
			Launcher launcher = new Launcher(this);
			launcher.launch(false);
			return launcher;
		}
		return null;
	}

	public File getSrcFolder() {
		return srcFolder;
	}

	public Builder setSrcFolder(File srcFolder) {
		this.srcFolder = srcFolder;
		return this;
	}

	public Builder setSrcFolder(String srcFolderPath) {
		return setSrcFolder(new File(srcFolderPath));
	}

	public File getBinFolder() {
		return binFolder;
	}

	public Builder setBinFolder(File binFolder) {
		this.binFolder = binFolder;
		return this;
	}

	public Builder setBinFolder(String binFolderPath) {
		return setBinFolder(new File(binFolderPath));
	}

	public File getCodeFolder() {
		return codeFolder;
	}

	public Builder setCodeFolder(File codeFolder) {
		this.codeFolder = codeFolder;
		return this;
	}

	public Builder setCodeFolder(String codeFolderPath) {
		return setCodeFolder(new File(codeFolderPath));
	}

	public String getPackageName() {
		return packageName;
	}

	public Builder setPackageName(String packageName) {
		this.packageName = packageName;
		return this;
	}

	/**
   * Absolute path to the sketch folder. Used to set the working directry of
   * the sketch when running, i.e. so that saveFrame() goes to the right
   * location when running from the PDE, instead of the same folder as the
   * Processing.exe or the root of the user's home dir.
   */
  public String getSketchPath() {
    return sketch.getFolder().getAbsolutePath();
  }
}
