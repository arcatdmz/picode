package jp.digitalmuseum.roboko.builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Method;

import processing.app.Base;
import processing.app.Editor;
import processing.app.SketchCode;
import processing.app.SketchException;
import processing.core.PApplet;


public class Compiler {
	private Builder builder;

	public Compiler(Builder builder) {
		this.builder = builder;
	}

	/**
	 * Copied from {@link processing.mode.java.Compiler}
	 * Compile with ECJ. See http://j.mp/8paifz for documentation.
	 * <dl>
	 * <dt>In:</dt>
	 * <dd>classPath, (*.java in srcFolder), binFolder</dd>
	 * <dt>Out:</dt>
	 * <dd>(*.class in binFolder)</dd>
	 * </dl>
	 *
	 * @return
	 * @throws SketchException
	 */
	public boolean compile() throws SketchException {

		// This will be filled in if anyone gets angry
		SketchException exception = null;
		boolean success = false;

		// System.out.println(builder.classPath);
		String baseCommand[] = new String[] { "-Xemacs", "-source", "1.6",
				"-target", "1.6", "-classpath", builder.classPath, "-nowarn", "-d",
				builder.binFolder.getAbsolutePath() };

		String[] sourceFiles = Base.listFiles(builder.srcFolder, false, ".java");
		String[] command = PApplet.concat(baseCommand, sourceFiles);

		try {
			// Create single method dummy writer class to slurp errors from ecj
			final StringBuffer errorBuffer = new StringBuffer();
			Writer internalWriter = new Writer() {
				public void write(char[] buf, int off, int len) {
					errorBuffer.append(buf, off, len);
				}

				public void flush() {
				}

				public void close() {
				}
			};

			// Compile source files.
			PrintWriter writer = new PrintWriter(internalWriter);
			PrintWriter outWriter = new PrintWriter(System.out);

      // Version that's not dynamically loaded
      //CompilationProgress progress = null;
      //success = BatchCompiler.compile(command, outWriter, writer, progress);
			
      // Version that *is* dynamically loaded. First gets the mode class loader
      // so that it can grab the compiler JAR files from it.
      ClassLoader loader = builder.sketch.getMode().getClassLoader();
      try {
        Class batchClass = 
          Class.forName("org.eclipse.jdt.core.compiler.batch.BatchCompiler", false, loader);
        Class progressClass = 
          Class.forName("org.eclipse.jdt.core.compiler.CompilationProgress", false, loader);
        Class[] compileArgs = 
          new Class[] { String[].class, PrintWriter.class, PrintWriter.class, progressClass };
        Method compileMethod = batchClass.getMethod("compile", compileArgs);
        success = (Boolean) 
          compileMethod.invoke(null, new Object[] { command, outWriter, writer, null });
      } catch (Exception e) {
        e.printStackTrace();
        throw new SketchException("Unknown error inside the compiler.");
      }

			writer.flush();
			writer.close();

      BufferedReader reader =
        new BufferedReader(new StringReader(errorBuffer.toString()));

			// Read compilation results.
      String line = null;
      while ((line = reader.readLine()) != null) {
        //System.out.println("got line " + line);  // debug

        // get first line, which contains file name, line number,
        // and at least the first line of the error message
        String errorFormat = "([\\w\\d_]+.java):(\\d+):\\s*(.*):\\s*(.*)\\s*";
        String[] pieces = PApplet.match(line, errorFormat);
        //PApplet.println(pieces);

        // if it's something unexpected, die and print the mess to the console
        if (pieces == null) {
          exception = new SketchException("Cannot parse error text: " + line);
          exception.hideStackTrace();
          // Send out the rest of the error message to the console.
          System.err.println(line);
          while ((line = reader.readLine()) != null) {
            System.err.println(line);
          }
          break;
        }

        // translate the java filename and line number into a un-preprocessed
        // location inside a source file or tab in the environment.
        String dotJavaFilename = pieces[1];
        // Line numbers are 1-indexed from javac
        int dotJavaLineIndex = PApplet.parseInt(pieces[2]) - 1;
        String errorMessage = pieces[4];

        exception = placeException(errorMessage,
                                         dotJavaFilename,
                                         dotJavaLineIndex);
        /*
        int codeIndex = 0; //-1;
        int codeLine = -1;

        // first check to see if it's a .java file
        for (int i = 0; i < sketch.getCodeCount(); i++) {
          SketchCode code = sketch.getCode(i);
          if (code.isExtension("java")) {
            if (dotJavaFilename.equals(code.getFileName())) {
              codeIndex = i;
              codeLine = dotJavaLineIndex;
            }
          }
        }

        // if it's not a .java file, codeIndex will still be 0
        if (codeIndex == 0) {  // main class, figure out which tab
          //for (int i = 1; i < sketch.getCodeCount(); i++) {
          for (int i = 0; i < sketch.getCodeCount(); i++) {
            SketchCode code = sketch.getCode(i);

            if (code.isExtension("pde")) {
              if (code.getPreprocOffset() <= dotJavaLineIndex) {
                codeIndex = i;
                //System.out.println("i'm thinkin file " + i);
                codeLine = dotJavaLineIndex - code.getPreprocOffset();
              }
            }
          }
        }
        //System.out.println("code line now " + codeLine);
        exception = new RunnerException(errorMessage, codeIndex, codeLine, -1, false);
        */

        if (exception == null) {
          exception = new SketchException(errorMessage);
        }

        // for a test case once message parsing is implemented,
        // use new Font(...) since that wasn't getting picked up properly.

        /*
        if (errorMessage.equals("cannot find symbol")) {
          handleCannotFindSymbol(reader, exception);

        } else if (errorMessage.indexOf("is already defined") != -1) {
          reader.readLine();  // repeats the line of code w/ error
          int codeColumn = caretColumn(reader.readLine());
          exception = new RunnerException(errorMessage,
                                          codeIndex, codeLine, codeColumn);

        } else if (errorMessage.startsWith("package") &&
                   errorMessage.endsWith("does not exist")) {
          // Because imports are stripped out and re-added to the 0th line of
          // the preprocessed code, codeLine will always be wrong for imports.
          exception = new RunnerException("P" + errorMessage.substring(1) +
                                          ". You might be missing a library.");
        } else {
          exception = new RunnerException(errorMessage);
        }
        */
        if (errorMessage.startsWith("The import ") &&
            errorMessage.endsWith("cannot be resolved")) {
          // The import poo cannot be resolved
          //import poo.shoe.blah.*;
          //String what = errorMessage.substring("The import ".length());
          String[] m = PApplet.match(errorMessage, "The import (.*) cannot be resolved");
          //what = what.substring(0, what.indexOf(' '));
          if (m != null) {
//            System.out.println("'" + m[1] + "'");
            if (m[1].equals("processing.xml")) {
              exception.setMessage("processing.xml no longer exists, this code needs to be updated for 2.0.");
              System.err.println("The processing.xml library has been replaced " +
                                 "with a new 'XML' class that's built-in.");
              handleCrustyCode();

            } else {
              exception.setMessage("The package " +
                                   "\u201C" + m[1] + "\u201D" +
                                   " does not exist. " +
                                   "You might be missing a library.");
              System.err.println("Libraries must be " +
                                 "installed in a folder named 'libraries' " +
                                 "inside the 'sketchbook' folder.");
            }
          }

//          // Actually create the folder and open it for the user
//          File sketchbookLibraries = Base.getSketchbookLibrariesFolder();
//          if (!sketchbookLibraries.exists()) {
//            if (sketchbookLibraries.mkdirs()) {
//              Base.openFolder(sketchbookLibraries);
//            }
//          }

        } else if (errorMessage.endsWith("cannot be resolved to a type")) {
          // xxx cannot be resolved to a type
          //xxx c;

          String what = errorMessage.substring(0, errorMessage.indexOf(' '));

          if (what.equals("BFont") ||
              what.equals("BGraphics") ||
              what.equals("BImage")) {
            exception.setMessage(what + " has been replaced with P" + what.substring(1));
            handleCrustyCode();

          } else {
            exception.setMessage("Cannot find a class or type " +
                                 "named \u201C" + what + "\u201D");
          }

        } else if (errorMessage.endsWith("cannot be resolved")) {
          // xxx cannot be resolved
          //println(xxx);

          String what = errorMessage.substring(0, errorMessage.indexOf(' '));

          if (what.equals("LINE_LOOP") ||
              what.equals("LINE_STRIP")) {
            exception.setMessage("LINE_LOOP and LINE_STRIP are not available, " +
                                 "please update your code.");
            handleCrustyCode();

          } else if (what.equals("framerate")) {
            exception.setMessage("framerate should be changed to frameRate.");
            handleCrustyCode();

          } else if (what.equals("screen")) {
            exception.setMessage("Change screen.width and screen.height to " +
                                 "displayWidth and displayHeight.");
            handleCrustyCode();

          } else if (what.equals("screenWidth") ||
                     what.equals("screenHeight")) {
            exception.setMessage("Change screenWidth and screenHeight to " +
                                 "displayWidth and displayHeight.");
            handleCrustyCode();

          } else {
            exception.setMessage("Cannot find anything " +
                                 "named \u201C" + what + "\u201D");
          }

        } else if (errorMessage.startsWith("Duplicate")) {
          // "Duplicate nested type xxx"
          // "Duplicate local variable xxx"

        } else {
          String[] parts = null;

          // The method xxx(String) is undefined for the type Temporary_XXXX_XXXX
          //xxx("blah");
          // The method xxx(String, int) is undefined for the type Temporary_XXXX_XXXX
          //xxx("blah", 34);
          // The method xxx(String, int) is undefined for the type PApplet
          //PApplet.sub("ding");
          String undefined =
            "The method (\\S+\\(.*\\)) is undefined for the type (.*)";
          parts = PApplet.match(errorMessage, undefined);
          if (parts != null) {
            if (parts[1].equals("framerate(int)")) {
              exception.setMessage("framerate() no longer exists, use frameRate() instead.");
              handleCrustyCode();

            } else if (parts[1].equals("push()")) {
              exception.setMessage("push() no longer exists, use pushMatrix() instead.");
              handleCrustyCode();

            } else if (parts[1].equals("pop()")) {
              exception.setMessage("pop() no longer exists, use popMatrix() instead.");
              handleCrustyCode();

            } else {
              String mess = "The function " + parts[1] + " does not exist.";
              exception.setMessage(mess);
            }
            break;
          }
        }
        if (exception != null) {
          // The stack trace just shows that this happened inside the compiler,
          // which is a red herring. Don't ever show it for compiler stuff.
          exception.hideStackTrace();
          break;
        }
      }
    } catch (IOException e) {
      String bigSigh = "Error while compiling. (" + e.getMessage() + ")";
      exception = new SketchException(bigSigh);
      e.printStackTrace();
      success = false;
    }

		// In case there was something else.
		if (exception != null) {
			throw exception;
		}

		return success;
	}

	static protected void handleCrustyCode() {
    System.err.println("This code needs to be updated " +
                       "for this version of Processing, " +
                       "please read the Changes page on the Wiki.");
    Editor.showChanges();
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
	private SketchException placeException(String message,
			String dotJavaFilename, int dotJavaLine) {
		int codeIndex = 0;
		int codeLine = -1;

		// First, check to see if it's a .java file
		for (int i = 0; i < builder.sketch.getCodeCount(); i++) {
			SketchCode code = builder.sketch.getCode(i);
			if (code.isExtension("java")) {
				if (dotJavaFilename.equals(code.getFileName())) {
					codeIndex = i;
					codeLine = dotJavaLine;
					return new SketchException(message, codeIndex, codeLine);
				}
			}
		}

		// If not the preprocessed file at this point, then need to get out
		if (!dotJavaFilename.equals(builder.sketch.getName() + ".java")) {
			return null;
		}

		// If it's not a .java file, codeIndex will still be 0
		// this section searches through the list of .pde files
		codeIndex = 0;
		for (int i = 0; i < builder.sketch.getCodeCount(); i++) {
			SketchCode code = builder.sketch.getCode(i);
			if (code.isExtension("pde")) {
				if (code.getPreprocOffset() <= dotJavaLine) {
					codeIndex = i;
					codeLine = dotJavaLine - code.getPreprocOffset();
				}
			}
		}
		return new SketchException(message, codeIndex, codeLine, -1, false);
	}
}
