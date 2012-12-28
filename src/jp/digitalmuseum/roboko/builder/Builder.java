package jp.digitalmuseum.roboko.builder;

import java.io.File;
import java.io.IOException;

import jp.digitalmuseum.roboko.ProcessingIntegration;
import jp.digitalmuseum.roboko.RobokoMain;
import jp.digitalmuseum.roboko.RobokoSettings;

import processing.app.Library;
import processing.app.RobokoSketch;
import processing.app.SketchCode;
import processing.app.SketchException;

public class Builder {

	public static void main(String[] args) {
		ProcessingIntegration.init();
		RobokoSketch sketch = null;
		try {
			sketch = RobokoSettings.getDefaultSketch();
			Builder builder = new Builder(null, sketch);
			// builder.setCodeFolder("D:\\Users\\arc\\Desktop\\BouncyBubbles\\code")
			builder.run();
		} catch (IOException e) {
			System.err.println(String.format("Can't load Processing code: %s",
					RobokoSettings.getDefaultSketchPath()));
		} catch (SketchException se) {
			System.err.println(RobokoMain.getErrorString(sketch, se));
		}
	}
	private File binFolder;
	private String classPath;
	private File codeFolder;
	private boolean foundMain = false;

	private String javaLibraryPath;
	private String mainClassName;
	private RobokoMain robokoMain;
	private RobokoSketch sketch;

	private File srcFolder;

	public Builder(RobokoMain robokoMain, RobokoSketch sketch) {
		this.robokoMain = robokoMain;
		this.sketch = sketch;
    setSrcFolder(RobokoSettings.getSrcFolderPath());
    setBinFolder(RobokoSettings.getBinFolderPath());
	}

  public Launcher run() throws SketchException {
    Preprocessor preprocessor = new Preprocessor(this);
    preprocessor.preprocess(false);
    Compiler compiler = new Compiler(this);
    if (compiler.compile()) {
      Launcher launcher = new Launcher(this);
      launcher.launch(false);
      return launcher;
    }
    return null;
  }

  public RobokoMain getRobokoMain() {
    return robokoMain;
  }

  RobokoSketch getSketch() {
    return sketch;
  }

  /**
   * Absolute path to the sketch folder. Used to set the working directry of
   * the sketch when running, i.e. so that saveFrame() goes to the right
   * location when running from the PDE, instead of the same folder as the
   * Processing.exe or the root of the user's home dir.
   */
  String getSketchPath() {
    return sketch.getFolder().getAbsolutePath();
  }

  File getSrcFolder() {
    return srcFolder;
  }

	File getBinFolder() {
		return binFolder;
	}

  File getCodeFolder() {
		return codeFolder;
	}

  String getClassPath() {
    return classPath;
  }

	String getJavaLibraryPath() {
    return javaLibraryPath;
  }

	String getMainClassName() {
    return mainClassName;
  }

  boolean isFoundMain() {
    return foundMain;
  }

  private void setSrcFolder(File srcFolder) {
    this.srcFolder = srcFolder;
  }

  private void setSrcFolder(String srcFolderPath) {
    setSrcFolder(new File(srcFolderPath));
  }
  
	private void setBinFolder(File binFolder) {
		this.binFolder = binFolder;
	}

  private void setBinFolder(String binFolderPath) {
		setBinFolder(new File(binFolderPath));
	}

  void setCodeFolder(File codeFolder) {
		this.codeFolder = codeFolder;
	}

  void setCodeFolder(String codeFolderPath) {
		setCodeFolder(new File(codeFolderPath));
	}

  void setClassPath(String classPath) {
    this.classPath = classPath;
  }

  void setJavaLibraryPath(String javaLibraryPath) {
    this.javaLibraryPath = javaLibraryPath;
  }

  void setMainClassName(String mainClassName) {
    this.mainClassName = mainClassName;
  }

  void setFoundMain(boolean foundMain) {
    this.foundMain = foundMain;
  }

  /**
   * Map an error from a set of processed .java files back to its location
   * in the actual sketch.
   * @param message The error message.
   * @param filename The .java file where the exception was found.
   * @param line Line number of the .java file for the exception (0-indexed!)
   * @return A RunnerException to be sent to the editor, or null if it wasn't
   *         possible to place the exception to the sketch code.
   */
  SketchException placeException(String message,
                                        String dotJavaFilename,
                                        int dotJavaLine) {
    int codeIndex = 0; //-1;
    int codeLine = -1;

//    System.out.println("placing " + dotJavaFilename + " " + dotJavaLine);
//    System.out.println("code count is " + getCodeCount());

    // first check to see if it's a .java file
    for (int i = 0; i < sketch.getCodeCount(); i++) {
      SketchCode code = sketch.getCode(i);
      if (code.isExtension("java")) {
        if (dotJavaFilename.equals(code.getFileName())) {
          codeIndex = i;
          codeLine = dotJavaLine;
          return new SketchException(message, codeIndex, codeLine);
        }
      }
    }

    // If not the preprocessed file at this point, then need to get out
    if (!dotJavaFilename.equals(sketch.getName() + ".java")) {
      return null;
    }

    // if it's not a .java file, codeIndex will still be 0
    // this section searches through the list of .pde files
    codeIndex = 0;
    for (int i = 0; i < sketch.getCodeCount(); i++) {
      SketchCode code = sketch.getCode(i);

      if (code.isExtension("pde")) {
//        System.out.println("preproc offset is " + code.getPreprocOffset());
//        System.out.println("looking for line " + dotJavaLine);
        if (code.getPreprocOffset() <= dotJavaLine) {
          codeIndex = i;
//          System.out.println("i'm thinkin file " + i);
          codeLine = dotJavaLine - code.getPreprocOffset();
        }
      }
    }
    // could not find a proper line number, so deal with this differently.
    // but if it was in fact the .java file we're looking for, though,
    // send the error message through.
    // this is necessary because 'import' statements will be at a line
    // that has a lower number than the preproc offset, for instance.
//    if (codeLine == -1 && !dotJavaFilename.equals(name + ".java")) {
//      return null;
//    }
//    return new SketchException(message, codeIndex, codeLine);
    return new SketchException(message, codeIndex, codeLine, -1, false);  // changed for 0194 for compile errors, but...
  }

  public Library getCoreLibrary() {
    // TODO Auto-generated method stub
    return null;
  }

  public Library getLibrary(String entry) {
    // TODO Auto-generated method stub
    return null;
  }

  public ClassLoader getClassLoader() {
    // TODO Auto-generated method stub
    return null;
  }
}
