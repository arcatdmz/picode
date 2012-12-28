package jp.digitalmuseum.roboko.builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import jp.digitalmuseum.roboko.RobokoSettings;

import processing.app.Base;
import processing.app.Library;
import processing.app.SketchCode;
import processing.app.SketchException;
import processing.core.PApplet;
import processing.mode.java.JavaBuild;
import processing.mode.java.preproc.PdePreprocessor;
import processing.mode.java.preproc.PreprocessorResult;


public class Preprocessor {
	private Builder builder;

	public Preprocessor(Builder builder) {
		this.builder = builder;
	}

	/**
	 * Copied from {@link processing.mode.java.JavaBuild#preprocess(File)}
	 * <dl>
	 * <dt>In:</dt>
	 * <dd>sketch, packageName, srcFolder, codeFolder</dd>
	 * <dt>Out:</dt>
	 * <dd>classPath, javaLibraryPath, mainClassName, foundMain, (*.java in
	 * srcFolder)</dd>
	 * </dl>
	 *
	 * @param preprocessor
	 * @throws SketchException
	 */
	public void preprocess(PdePreprocessor preprocessor)
			throws SketchException {
	  builder.sketch.ensureExistence();

	  builder.classPath = builder.binFolder.getAbsolutePath();

		// Look for Jar files in the "code" folder.
		String[] codeFolderPackages = null;
		if (builder.codeFolder != null) {

			builder.javaLibraryPath = File.separator + builder.codeFolder.getAbsolutePath();
			String codeFolderClassPath = Base
					.contentsToClassPath(builder.codeFolder);
			builder.classPath += File.pathSeparator + codeFolderClassPath;
			codeFolderPackages = Base
					.packageListFromClassPath(codeFolderClassPath);
		} else {
      builder.javaLibraryPath = "";
    }

		// Merge all Processing code files as one big code string.
		// Store line numbers for their starting points.
		StringBuffer bigCode = new StringBuffer();
		int bigCount = 0;
		for (SketchCode sc : builder.sketch.getCode()) {
			if (hasExtension(sc.getFile(), "pde")) {
				sc.setPreprocOffset(bigCount);
				bigCode.append(sc.getProgram());
				bigCode.append('\n');
				bigCount += sc.getLineCount();
			}
		}

		// Export the big code as a Java file.
		PreprocessorResult result;
		try {
			File outputFolder = (builder.packageName == null) ? builder.srcFolder
					: new File(builder.srcFolder, builder.packageName.replace('.', '/'));
			outputFolder.mkdirs();
			File java = new File(outputFolder, builder.sketch.getName() + ".java");
			PrintWriter stream = new PrintWriter(new FileWriter(java));
			try {
				result = preprocessor.write(stream, bigCode.toString(),
						codeFolderPackages);
			} finally {
				stream.close();
			}
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
			String msg = "Build folder disappeared or could not be written";
			throw new SketchException(msg);
		} catch (antlr.RecognitionException re) {

			// Get the original file and the line number
			// that caused the error.
			int errorLine = re.getLine() - 1;
			int errorFile = findErrorFile(errorLine);
			errorLine -= builder.sketch.getCode(errorFile).getPreprocOffset();

			String msg = re.getMessage();

      if (msg.contains("expecting RCURLY")) {
				throw new SketchException(
						"Found one too many { characters "
								+ "without a } to match it.", errorFile,
						errorLine, re.getColumn(), false);
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

			throw new SketchException(msg, errorFile, errorLine,
					re.getColumn(), false);

		} catch (antlr.TokenStreamRecognitionException tsre) {

			String[] matches = PApplet.match(tsre.toString(),
					"^line (\\d+):(\\d+):\\s");
			if (matches != null) {

				// Get the original file and the line number
				// that caused the error.
				int errorLine = Integer.parseInt(matches[1]) - 1;
				int errorColumn = Integer.parseInt(matches[2]);
        int errorFile = 0;
        for (int i = 1; i < builder.sketch.getCodeCount(); i++) {
          SketchCode sc = builder.sketch.getCode(i);
          if (sc.isExtension("pde") &&
              (sc.getPreprocOffset() < errorLine)) {
            errorFile = i;
          }
        }
        errorLine -= builder.sketch.getCode(errorFile).getPreprocOffset();

				throw new SketchException(tsre.getMessage(), errorFile,
						errorLine, errorColumn);
			} else {

				// The line number was not found.
				String msg = tsre.toString();
				throw new SketchException(msg, 0, -1, -1);
			}

		} catch (SketchException pe) {

			// RunnerExceptions are caught here and re-thrown, so that they
			// don't get lost in the more general "Exception" handler below.
			throw pe;

		} catch (Exception ex) {
			System.err.println("Uncaught exception type:" + ex.getClass());
			ex.printStackTrace();
			throw new SketchException(ex.toString());
		}

		// Get the list of imported libraries.
		ArrayList<Library> importedLibraries = new ArrayList<Library>();
    Library core = builder.sketch.getMode().getCoreLibrary();
    if (core != null) {
      importedLibraries.add(core);
      builder.classPath += core.getClassPath();
    }

    for (String item : result.extraImports) {

			// Get an imported library.
			int dot = item.lastIndexOf('.');
			String entry = (dot == -1) ? item : item.substring(0, dot);
			Library library = builder.sketch.getMode().getLibrary(entry);

			if (library != null) {
				if (!importedLibraries.contains(library)) {
					importedLibraries.add(library);
					builder.classPath += library.getClassPath();
					builder.javaLibraryPath += File.pathSeparator
							+ library.getNativePath();
				}
			} else {

				// Library not found:
				// Prevent from printing the error for multiple times.
				boolean found = false;
				if (codeFolderPackages != null) {
					String itemPkg = item.substring(0,
							item.lastIndexOf('.'));
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

		// Append regular class path to the class path.
		String javaClassPath = System.getProperty("java.class.path");
		if (javaClassPath.startsWith("\"") && javaClassPath.endsWith("\"")) {

			// Remove quotes if any.. A messy (and frequent) Windows problem
			javaClassPath = javaClassPath.substring(1,
					javaClassPath.length() - 1);
		}
		builder.classPath += File.pathSeparator + javaClassPath;

		// Save the rest Java code in the source folder.
		for (SketchCode sc : builder.sketch.getCode()) {
			if (sc.isExtension("java")) {
				String fileName = sc.getFileName();
				try {
					String javaCode = sc.getProgram();
					String[] packageMatch = PApplet.match(javaCode,
							JavaBuild.PACKAGE_REGEX);
					if (packageMatch == null && builder.packageName == null) {

						// Save code in the source folder.
						sc.copyTo(new File(builder.srcFolder, fileName));
					} else {

						// If no package, and a default package is being
						// used,
						// we'll have to save this file in the default
						// package.
						if (packageMatch == null) {
							packageMatch = new String[] { builder.packageName };
							javaCode = "package " + builder.packageName + ";"
									+ javaCode;
						}

						// Save code in the sub folder representing the
						// package.
						File packageFolder = new File(builder.srcFolder,
								packageMatch[0].replace('.', '/'));
						packageFolder.mkdirs();
						Base.saveFile(javaCode, new File(packageFolder,
								fileName));
					}
				} catch (IOException e) {
					e.printStackTrace();
					String msg = "Problem moving " + fileName
							+ " to the build folder";
					throw new SketchException(msg);
				}

			} else if (sc.isExtension("pde")) {
				sc.addPreprocOffset(result.headerOffset);
			}
		}
    builder.foundMain = preprocessor.hasMethod("main");
		builder.mainClassName = result.className;
	}

	/**
	 * Get index for the original Processing code in the sketch from the
	 * given line number.
	 *
	 * @param lineNumber
	 * @return
	 */
  protected int findErrorFile(int errorLine) {
    for (int i = builder.sketch.getCodeCount() - 1; i > 0; i--) {
      SketchCode sc = builder.sketch.getCode(i);
      if (sc.isExtension("pde") && (sc.getPreprocOffset() <= errorLine)) {
        // keep looping until the errorLine is past the offset
        return i;
      }
    }
    return 0;  // i give up
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

    // TODO preferences.txtで指定されてるやつを取得して返すとか…ハードコードはまずい
    if (pkg.startsWith("jp.digitalmuseum.picode")) return true;

    return false;
  }

	private boolean hasExtension(File file, String extension) {
		return file.getName().endsWith("." + extension);
	}
}

