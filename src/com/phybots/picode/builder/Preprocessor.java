package com.phybots.picode.builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import processing.app.Base;
import processing.app.Library;
import processing.app.SketchCode;
import processing.app.SketchException;
import processing.core.PApplet;
import processing.mode.java.preproc.PdePreprocessor;
import processing.mode.java.preproc.PreprocessorResult;


public class Preprocessor {
  public static final String PACKAGE_REGEX =
    "(?:^|\\s|;)package\\s+(\\S+)\\;";
  
	private Builder builder;

	public Preprocessor(Builder builder) {
		this.builder = builder;
	}

	/**
	 * Copied from {@link processing.builder.java.JavaBuild#preprocess(File)}
	 * <dl>
	 * <dt>In:</dt>
	 * <dd>sketch, packageName, srcFolder, codeFolder</dd>
	 * <dt>Out:</dt>
	 * <dd>builder.classPath, builder.javaLibraryPath, mainClassName, foundMain, (*.java in
	 * srcFolder)</dd>
	 * </dl>
	 * 
   * Build all the code for this builder.getSketch().
   *
   * In an advanced program, the returned class name could be different,
   * which is why the className is set based on the return value.
   * A compilation error will burp up a RunnerException.
   *
   * Setting purty to 'true' will cause exception line numbers to be incorrect.
   * Unless you know the code compiles, you should first run the preprocessor
   * with purty set to false to make sure there are no errors, then once
   * successful, re-export with purty set to true.
   *
   * @param buildPath Location to copy all the .java files
   * @return null if compilation failed, main class name if not
	 *
	 * @param preprocessor
	 * @throws SketchException
	 */
//  public String preprocess() throws SketchException {
//    return preprocess(builder.getSketch().makeTempFolder());
//  }

  public String preprocess(boolean sizeWarning) throws SketchException {
    return preprocess(builder.getSrcFolder(), null, new PdePreprocessor(builder.getSketch().getName()), sizeWarning);
  }


  /**
   * @param srcFolder location where the .java source files will be placed
   * @param packageName null, or the package name that should be used as default
   * @param preprocessor the preprocessor object ready to do the work
   * @return main PApplet class name found during preprocess, or null if error
   * @throws SketchException
   */
  public String preprocess(File srcFolder,
                           String packageName,
                           PdePreprocessor preprocessor,
                           boolean sizeWarning) throws SketchException {
    // make sure the user isn't playing "hide the sketch folder"
    builder.getSketch().ensureExistence();

//    System.out.println("srcFolder is " + srcFolder);
    builder.getBinFolder().mkdirs();
    builder.setClassPath(builder.getBinFolder().getAbsolutePath());

    // figure out the contents of the code folder to see if there
    // are files that need to be added to the imports
    String[] codeFolderPackages = null;
    if (builder.getSketch().hasCodeFolder()) {
      File codeFolder = builder.getSketch().getCodeFolder();
      builder.setJavaLibraryPath(codeFolder.getAbsolutePath());

      // get a list of .jar files in the "code" folder
      // (class files in subfolders should also be picked up)
      String codeFolderClassPath =
        Base.contentsToClassPath(codeFolder);
      // append the jar files in the code folder to the class path
      builder.setClassPath(builder.getClassPath() + (File.pathSeparator + codeFolderClassPath));
      // get list of packages found in those jars
      codeFolderPackages =
        Base.packageListFromClassPath(codeFolderClassPath);

    } else {
      builder.setJavaLibraryPath("");
    }

    // 1. concatenate all .pde files to the 'main' pde
    //    store line number for starting point of each code bit

    StringBuffer bigCode = new StringBuffer();
    int bigCount = 0;
    for (SketchCode sc : builder.getSketch().getCode()) {
      if (sc.isExtension("pde")) {
        sc.setPreprocOffset(bigCount);
        bigCode.append(sc.getProgram());
        bigCode.append('\n');
        bigCount += sc.getLineCount();
      }
    }

//    // initSketchSize() sets the internal sketchWidth/Height/Renderer vars
//    // in the preprocessor. Those are used in preproc.write() so that they
//    // can be turned into sketchXxxx() methods.
//    // This also returns the size info as an array so that we can figure out
//    // if this fella is OpenGL, and if so, to add the import. It's messy and
//    // gross and someday we'll just always include OpenGL.
//    String[] sizeInfo =
//      preprocessor.initSketchSize(builder.getSketch().getMainProgram(), sizeWarning);
//      //PdePreprocessor.parseSketchSize(builder.getSketch().getMainProgram(), false);
//    if (sizeInfo != null) {
//      String sketchRenderer = sizeInfo[3];
//      if (sketchRenderer != null) {
//        if (sketchRenderer.equals("P2D") ||
//            sketchRenderer.equals("P3D") ||
//            sketchRenderer.equals("OPENGL")) {
//          bigCode.insert(0, "import processing.opengl.*; ");
//        }
//      }
//    }

    PreprocessorResult result;
    try {
      File outputFolder = (packageName == null) ?
        srcFolder : new File(srcFolder, packageName.replace('.', '/'));
      outputFolder.mkdirs();
//      Base.openFolder(outputFolder);
      final File java = new File(outputFolder, builder.getSketch().getName() + ".java");
      final PrintWriter stream = new PrintWriter(new FileWriter(java));
      try {
        result = preprocessor.write(stream, bigCode.toString(), codeFolderPackages);
      } finally {
        stream.close();
      }
    } catch (FileNotFoundException fnfe) {
      fnfe.printStackTrace();
      String msg = "Build folder disappeared or could not be written";
      throw new SketchException(msg);

    } catch (antlr.RecognitionException re) {
      // re also returns a column that we're not bothering with for now
      // first assume that it's the main file
//      int errorFile = 0;
      int errorLine = re.getLine() - 1;

      // then search through for anyone else whose preprocName is null,
      // since they've also been combined into the main pde.
      int errorFile = findErrorFile(errorLine);
//      System.out.println("error line is " + errorLine + ", file is " + errorFile);
      errorLine -= builder.getSketch().getCode(errorFile).getPreprocOffset();
//      System.out.println("  preproc offset for that file: " + builder.getSketch().getCode(errorFile).getPreprocOffset());

//      System.out.println("i found this guy snooping around..");
//      System.out.println("whatcha want me to do with 'im boss?");
//      System.out.println(errorLine + " " + errorFile + " " + code[errorFile].getPreprocOffset());

      String msg = re.getMessage();

      //System.out.println(java.getAbsolutePath());
//      System.out.println(bigCode);

      if (msg.contains("expecting RCURLY")) {
      //if (msg.equals("expecting RCURLY, found 'null'")) {
        // This can be a problem since the error is sometimes listed as a line
        // that's actually past the number of lines. For instance, it might
        // report "line 15" of a 14 line program. Added code to highlightLine()
        // inside Editor to deal with this situation (since that code is also
        // useful for other similar situations).
        throw new SketchException("Found one too many { characters " +
                                  "without a } to match it.",
                                  errorFile, errorLine, re.getColumn(), false);
      }

      if (msg.contains("expecting LCURLY")) {
        System.err.println(msg);
        String suffix = ".";
        String[] m = PApplet.match(msg, "found ('.*')");
        if (m != null) {
          suffix = ", not " + m[1] + ".";
        }
        throw new SketchException("Was expecting a { character" + suffix,
                                   errorFile, errorLine, re.getColumn(), false);
      }

      if (msg.indexOf("expecting RBRACK") != -1) {
        System.err.println(msg);
        throw new SketchException("Syntax error, " +
                                  "maybe a missing ] character?",
                                  errorFile, errorLine, re.getColumn(), false);
      }

      if (msg.indexOf("expecting SEMI") != -1) {
        System.err.println(msg);
        throw new SketchException("Syntax error, " +
                                  "maybe a missing semicolon?",
                                  errorFile, errorLine, re.getColumn(), false);
      }

      if (msg.indexOf("expecting RPAREN") != -1) {
        System.err.println(msg);
        throw new SketchException("Syntax error, " +
                                  "maybe a missing right parenthesis?",
                                  errorFile, errorLine, re.getColumn(), false);
      }

      if (msg.indexOf("preproc.web_colors") != -1) {
        throw new SketchException("A web color (such as #ffcc00) " +
                                  "must be six digits.",
                                  errorFile, errorLine, re.getColumn(), false);
      }

      //System.out.println("msg is " + msg);
      throw new SketchException(msg, errorFile,
                                errorLine, re.getColumn(), false);

    } catch (antlr.TokenStreamRecognitionException tsre) {
      // while this seems to store line and column internally,
      // there doesn't seem to be a method to grab it..
      // so instead it's done using a regexp

//      System.err.println("and then she tells me " + tsre.toString());
      // P5TODO not tested since removing ORO matcher.. ^ could be a problem
      String mess = "^line (\\d+):(\\d+):\\s";

      String[] matches = PApplet.match(tsre.toString(), mess);
      if (matches != null) {
        int errorLine = Integer.parseInt(matches[1]) - 1;
        int errorColumn = Integer.parseInt(matches[2]);

        int errorFile = 0;
        for (int i = 1; i < builder.getSketch().getCodeCount(); i++) {
          SketchCode sc = builder.getSketch().getCode(i);
          if (sc.isExtension("pde") &&
              (sc.getPreprocOffset() < errorLine)) {
            errorFile = i;
          }
        }
        errorLine -= builder.getSketch().getCode(errorFile).getPreprocOffset();

        throw new SketchException(tsre.getMessage(),
                                  errorFile, errorLine, errorColumn);

      } else {
        // this is bad, defaults to the main class.. hrm.
        String msg = tsre.toString();
        throw new SketchException(msg, 0, -1, -1);
      }

    } catch (SketchException pe) {
      // RunnerExceptions are caught here and re-thrown, so that they don't
      // get lost in the more general "Exception" handler below.
      throw pe;

    } catch (Exception ex) {
      // P5TODO better method for handling this?
      System.err.println("Uncaught exception type:" + ex.getClass());
      ex.printStackTrace();
      throw new SketchException(ex.toString());
    }

    // grab the imports from the code just preproc'd

    ArrayList<Library> importedLibraries = builder.getCoreLibraries();
    for (Library coreLibrary : importedLibraries) {
      builder.setClassPath(builder.getClassPath() + coreLibrary.getClassPath());
    }

//    System.out.println("extra imports: " + result.extraImports);
    for (String item : result.extraImports) {
      // remove things up to the last dot
      int dot = item.lastIndexOf('.');
      // http://dev.processing.org/bugs/show_bug.cgi?id=1145
      String entry = (dot == -1) ? item : item.substring(0, dot);
//      System.out.println("library searching for " + entry);
      Library library = builder.getLibrary(entry);
//      System.out.println("  found " + library);

      if (library != null) {
        if (!importedLibraries.contains(library)) {
          importedLibraries.add(library);
          builder.setClassPath(builder.getClassPath() + library.getClassPath());
          builder.setJavaLibraryPath(builder.getJavaLibraryPath()
            + (File.pathSeparator + library.getNativePath()));
        }
      } else {
        boolean found = false;
        // If someone insists on unnecessarily repeating the code folder
        // import, don't show an error for it.
        if (codeFolderPackages != null) {
          String itemPkg = item.substring(0, item.lastIndexOf('.'));
          for (String pkg : codeFolderPackages) {
            if (pkg.equals(itemPkg)) {
              found = true;
              break;
            }
          }
        }
        if (ignorableImport(item)) {
          found = true;
        }
        if (!found) {
          System.err.println("No library found for " + entry);
        }
      }
    }
//    PApplet.println(PApplet.split(libraryPath, File.pathSeparatorChar));

    // Finally, add the regular Java CLASSPATH. This contains everything
    // imported by the PDE itself (core.jar, pde.jar, quaqua.jar) which may
    // in fact be more of a problem.
    String javaClassPath = System.getProperty("java.class.path");
    // Remove quotes if any.. A messy (and frequent) Windows problem
    if (javaClassPath.startsWith("\"") && javaClassPath.endsWith("\"")) {
      javaClassPath = javaClassPath.substring(1, javaClassPath.length() - 1);
    }
    builder.setClassPath(builder.getClassPath() + (File.pathSeparator + javaClassPath));


    // 3. then loop over the code[] and save each .java file

    for (SketchCode sc : builder.getSketch().getCode()) {
      if (sc.isExtension("java")) {
        // In most cases, no pre-processing services necessary for Java files.
        // Just write the the contents of 'program' to a .java file
        // into the build directory. However, if a default package is being
        // used (as in Android), and no package is specified in the source,
        // then we need to move this code to the same package as the builder.getSketch().
        // Otherwise, the class may not be found, or at a minimum, the default
        // access across the packages will mean that things behave incorrectly.
        // For instance, desktop code that uses a .java file with no packages,
        // will be fine with the default access, but since Android's PApplet
        // requires a package, code from that (default) package (such as the
        // PApplet itself) won't have access to methods/variables from the
        // package-less .java file (unless they're all marked public).
        String filename = sc.getFileName();
        try {
          String javaCode = sc.getProgram();
          String[] packageMatch = PApplet.match(javaCode, PACKAGE_REGEX);
          // if no package, and a default package is being used
          // (i.e. on Android) we'll have to add one

          if (packageMatch == null && packageName == null) {
            sc.copyTo(new File(srcFolder, filename));

          } else {
            if (packageMatch == null) {
              // use the default package name, since mixing with package-less code will break
              packageMatch = new String[] { packageName };
              // add the package name to the source before writing it
              javaCode = "package " + packageName + ";" + javaCode;
            }
            File packageFolder = new File(srcFolder, packageMatch[0].replace('.', '/'));
            packageFolder.mkdirs();
            Base.saveFile(javaCode, new File(packageFolder, filename));
          }

        } catch (IOException e) {
          e.printStackTrace();
          String msg = "Problem moving " + filename + " to the build folder";
          throw new SketchException(msg);
        }

      } else if (sc.isExtension("pde")) {
        // The compiler and runner will need this to have a proper offset
        sc.addPreprocOffset(result.headerOffset);
      }
    }
    
    builder.setFoundMain(preprocessor.hasMethod("main"));
    return result.className;
  }

  /**
   * Returns true if this package isn't part of a library (it's a system import
   * or something like that). Don't bother complaining about java.* or javax.*
   * because it's probably in boot.class.path. But we're not checking against
   * that path since it's enormous. Unfortunately we do still have to check
   * for libraries that begin with a prefix like javax, since that includes
   * the OpenGL library, even though we're just returning true here, hrm...
   */
  protected boolean ignorableImport(String pkg) {
    if (pkg.startsWith("java.")) return true;
    if (pkg.startsWith("javax.")) return true;

    if (pkg.startsWith("processing.core.")) return true;
    if (pkg.startsWith("processing.data.")) return true;
    if (pkg.startsWith("processing.event.")) return true;
    if (pkg.startsWith("processing.opengl.")) return true;

    if (pkg.startsWith("com.phybots.p5.")) return true;
    if (pkg.startsWith("com.phybots.picode.")) return true;

//    // ignore core, data, and opengl packages
//    String[] coreImports = preprocessor.getCoreImports();
//    for (int i = 0; i < coreImports.length; i++) {
//      String imp = coreImports[i];
//      if (imp.endsWith(".*")) {
//        imp = imp.substring(0, imp.length() - 2);
//      }
//      if (pkg.startsWith(imp)) {
//        return true;
//      }
//    }

    return false;
  }


  protected int findErrorFile(int errorLine) {
    for (int i = builder.getSketch().getCodeCount() - 1; i > 0; i--) {
      SketchCode sc = builder.getSketch().getCode(i);
      if (sc.isExtension("pde") && (sc.getPreprocOffset() <= errorLine)) {
        // keep looping until the errorLine is past the offset
        return i;
      }
    }
    return 0;  // i give up
  }
}

