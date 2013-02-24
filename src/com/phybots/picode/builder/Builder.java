package com.phybots.picode.builder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.phybots.Phybots;
import com.phybots.picode.PicodeMain;
import com.phybots.picode.PicodeSettings;
import com.phybots.picode.ProcessingIntegration;

import processing.app.Base;
import processing.app.Library;
import processing.app.PicodeSketch;
import processing.app.SketchCode;
import processing.app.SketchException;

public class Builder {

	public static void main(String[] args) {
		ProcessingIntegration.init();
		PicodeSketch sketch = null;
		try {
			sketch = PicodeSettings.getDefaultSketch();
			Builder builder = new Builder(null, sketch);
			builder.run();
		} catch (IOException e) {
			System.err.println(String.format("Can't load Processing code: %s",
					PicodeSettings.getDefaultSketchPath()));
		} catch (SketchException se) {
			System.err.println(ProcessingIntegration.getErrorString(sketch, se));
		}
	}
	private File binFolder;
	private String classPath;
	private File codeFolder;
	private boolean foundMain = false;

	private String javaLibraryPath;
	private String mainClassName;
	private PicodeMain picodeMain;
	private PicodeSketch sketch;
	
  protected HashMap<String, ArrayList<Library>> importToLibraryTable;

	private File srcFolder;

	public Builder(PicodeMain picodeMain, PicodeSketch sketch) {
		this.picodeMain = picodeMain;
		this.sketch = sketch;
    setSrcFolder(PicodeSettings.getSrcFolderPath());
    setBinFolder(PicodeSettings.getBinFolderPath());
	}

  public void run() throws SketchException {
    Preprocessor preprocessor = new Preprocessor(this);
    String mainClassName = preprocessor.preprocess(false);
    setMainClassName(mainClassName);
    Compiler compiler = new Compiler(this);
    if (compiler.compile()) {
      final Launcher launcher = new Launcher(this);
      Phybots.getInstance().submit(new Runnable() {
        public void run() {
          launcher.launch(false);
        }
      });
    }
  }

  public PicodeMain getPicodeMain() {
    return picodeMain;
  }

  PicodeSketch getSketch() {
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

  private ArrayList<Library> coreLibraries;
  public ArrayList<Library> getCoreLibraries() {
    if (coreLibraries == null) {
      coreLibraries = new ArrayList<Library>();
      coreLibraries.add(new Library(
          Base.getContentFile("lib/core"), null));
      coreLibraries.add(new Library(
          Base.getContentFile("lib/phybots"), null));
      coreLibraries.add(new Library(
          Base.getContentFile("lib/picode"), null));
      importToLibraryTable = new HashMap<String, ArrayList<Library>>();
      for (Library lib : coreLibraries) {
        lib.addPackageList(importToLibraryTable);
      }
    }
    return new ArrayList<Library>(coreLibraries);
  }

  public Library getLibrary(String pkgName) throws SketchException {
    ArrayList<Library> libraries = importToLibraryTable.get(pkgName);
    if (libraries == null) {
      return null;

    } else if (libraries.size() > 1) {
      String primary = "More than one library is competing for this sketch.";
      String secondary = "The import " + pkgName + " points to multiple libraries:<br>";
      for (Library library : libraries) {
        String location = library.getPath();
        if (location.startsWith(PicodeSettings.getLibrariesFolder().getAbsolutePath())) {
          location = "part of Processing";
        }
        secondary += "<b>" + library.getName() + "</b> (" + location + ")<br>";
      }
      secondary += "Extra libraries need to be removed before this sketch can be used.";
      Base.showWarningTiered("Duplicate Library Problem", primary, secondary, null);
      throw new SketchException("Duplicate libraries found for " + pkgName + ".");

    } else {
      return libraries.get(0);
    }
  }

  public ClassLoader getClassLoader() {
    return this.getClass().getClassLoader(); // Use the default class loader.
  }
}
