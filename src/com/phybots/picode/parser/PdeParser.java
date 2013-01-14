package com.phybots.picode.parser;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.phybots.picode.ui.ProcessingIntegration;
import com.phybots.picode.ui.PicodeSettings;

import processing.app.PicodeSketch;
import processing.app.SketchCode;
import processing.app.SketchException;
import processing.core.PApplet;
import processing.mode.java.JavaBuild;
import processing.mode.java.preproc.PdePreprocessor;
import processing.mode.java.preproc.PdeRecognizer;
import processing.mode.java.preproc.TokenUtil;
import processing.mode.java.preproc.PdePreprocessor.Mode;
import antlr.ANTLRException;
import antlr.ASTFactory;
import antlr.CommonHiddenStreamToken;
import antlr.collections.AST;

public class PdeParser {

	private PdePreprocessor pp;
	private ASTFactory astFactory;

	private PicodeSketch sketch;
	private Mode mode;
	private int[] columns;

	public void setMode(final Mode mode) {
		// System.err.println("Setting mode to " + mode);
		this.mode = mode;
	}

	public Mode getMode() {
		return mode;
	}

	public PdePreprocessor getPreprocessor() {
		return pp;
	}

	public PdeParser(PicodeSketch sketch) {
		this.sketch = sketch;
		astFactory = new ASTFactory();
	}

	public static void main(String[] args) {
		ProcessingIntegration.init();
		PicodeSketch sketch = null;
		AST ast = null;
		try {
			sketch = PicodeSettings.getDefaultSketch();
			PdeParser parser = new PdeParser(sketch);
			ast = parser.parse(0);

			System.err.println("------------------");
			System.err.println("Source code:");
			parser.printAST(ast);
			System.err.println();
			System.err.println();

			System.err.println("------------------");
			System.err.println("Abstract syntax tree:");
			parser.debugAST(ast, true);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (SketchException se) {
			System.out.println(sketch.getCode(se.getCodeIndex()).getFileName());
			System.out.print("L" + se.getCodeLine() + ":" + se.getCodeColumn()
					+ " " + se.getMessage());
		}
	}

	/*
	public int getNumberOfLines() {
		return columns.length;
	}
	*/

	public int getLine(int index) {
		int line = 0;
		while (columns[line] < index) {
			line ++;
		}
		return line - 1;
	}

	public int getColumn(int index) {
		int line = getLine(index);
		return index - getIndex(line);
	}

	public int getIndex(int lineNumber) {
		if (lineNumber < 0 ||
				lineNumber >= columns.length) {
			return -1;
		}
		return columns[lineNumber];
	}

	public int getIndex(AST ast) {
		return getIndex(ast.getLine() - 1) + ast.getColumn() - 1;
	}

	public int getIndex(CommonHiddenStreamToken tok) {
		return getIndex(tok.getLine() - 1) + tok.getColumn() - 1;
	}

	public AST parse(int index) throws SketchException {
		return parse(sketch.getCode(index));
	}

	public AST parse(SketchCode code) throws SketchException {
		pp = new PdePreprocessor(sketch.getName());
		String program = code.getProgram();

		if (!program.endsWith("\n")) {
			program += "\n";
		}
		PdePreprocessor.checkForUnterminatedMultilineComment(program);

		// ---
		// Parse the program: copied from {@link PdePreprocessor#write(String, PrintWriter)}

		// Match on the uncommented version, otherwise code inside comments used
    // http://code.google.com/p/processing/issues/detail?id=1404
    String uncomment = PdePreprocessor.scrubComments(program);
		PdeRecognizer parser = pp.createParser(program);
		try {
			if (PdePreprocessor.PUBLIC_CLASS.matcher(uncomment).find()) {
				try {
					final PrintStream saved = System.err;
					try {
						// throw away stderr for this tentative parse
						System.setErr(new PrintStream(
								new ByteArrayOutputStream()));
						parser.javaProgram();
					} finally {
						System.setErr(saved);
					}
					setMode(Mode.JAVA);
				} catch (Exception e) {
					// I can't figure out any other way of resetting the parser.
					parser = pp.createParser(program);
					parser.pdeProgram();
				}
			} else if (PdePreprocessor.FUNCTION_DECL.matcher(uncomment).find()) {
				setMode(Mode.ACTIVE);
				parser.activeProgram();
			} else {
				parser.pdeProgram();
			}
		} catch (ANTLRException e) {
			handleParseError(e, sketch.getCodeIndex(code));
		}
		// ---

		// Count indices of the start character of each line.
		BufferedReader reader = new BufferedReader(new StringReader(program));
		List<Integer> cs = new ArrayList<Integer>();
		String line;
		try {
			int read = 0;
			while ((line = reader.readLine()) != null) {
				cs.add(read);
				read += line.length() + 1;
			}
		} catch (IOException e1) {
			//
		}
		columns = new int[cs.size()];
		Iterator<Integer> it = cs.iterator();
		for (int i = 0; i < cs.size(); i ++) {
			columns[i] = it.next();
		}

		AST parserAST = parser.getAST();
		AST rootNode = astFactory.create(PdePreprocessor.ROOT_ID, "AST ROOT");
		rootNode.setFirstChild(parserAST);
		return rootNode;
	}

	/**
	 * Copied and modified from
	 * {@link JavaBuild#preprocess(java.io.File, String, PdePreprocessor)}
	 *
	 * @param e
	 */
	private void handleParseError(ANTLRException ae, int errorFile) throws SketchException {
		try {
			throw ae;
		} catch (antlr.RecognitionException re) {
			// re also returns a column that we're not bothering with for now

		  // Picode: Unlike Processing preprocessing, here we know this is from the specific file.
			int errorLine = re.getLine() - 1;
			//int errorFile = findErrorFile(errorLine);

			// System.out.println("i found this guy snooping around..");
			// System.out.println("whatcha want me to do with 'im boss?");
			// System.out.println(errorLine + " " + errorFile + " " +
			// code[errorFile].getPreprocOffset());

			String msg = re.getMessage();

			if (msg.equals("expecting RCURLY, found 'null'")) {
				// This can be a problem since the error is sometimes listed as
				// a line
				// that's actually past the number of lines. For instance, it
				// might
				// report "line 15" of a 14 line program. Added code to
				// highlightLine()
				// inside Editor to deal with this situation (since that code is
				// also
				// useful for other similar situations).
				throw new SketchException("Found one too many { characters "
						+ "without a } to match it.", errorFile, errorLine,
						re.getColumn());
			}

			if (msg.indexOf("expecting RBRACK") != -1) {
				// System.err.println(msg);
				throw new SketchException("Syntax error, "
						+ "maybe a missing ] character?", errorFile, errorLine,
						re.getColumn());
			}

			if (msg.indexOf("expecting SEMI") != -1) {
				// System.err.println(msg);
				throw new SketchException("Syntax error, "
						+ "maybe a missing semicolon?", errorFile, errorLine,
						re.getColumn());
			}

			if (msg.indexOf("expecting RPAREN") != -1) {
				// System.err.println(msg);
				throw new SketchException("Syntax error, "
						+ "maybe a missing right parenthesis?", errorFile,
						errorLine, re.getColumn());
			}

			if (msg.indexOf("preproc.web_colors") != -1) {
				throw new SketchException("A web color (such as #ffcc00) "
						+ "must be six digits.", errorFile, errorLine,
						re.getColumn(), false);
			}

			// System.out.println("msg is " + msg);
			throw new SketchException(msg, errorFile, errorLine, re.getColumn());

		} catch (antlr.TokenStreamRecognitionException tsre) {
			// while this seems to store line and column internally,
			// there doesn't seem to be a method to grab it..
			// so instead it's done using a regexp

			// System.err.println("and then she tells me " + tsre.toString());
			// TODO not tested since removing ORO matcher.. ^ could be a problem
			String mess = "^line (\\d+):(\\d+):\\s";

			String[] matches = PApplet.match(tsre.toString(), mess);
			if (matches != null) {
				int errorLine = Integer.parseInt(matches[1]) - 1;
				int errorColumn = Integer.parseInt(matches[2]);

	      // Picode: Unlike Processing preprocessing, here we know this is from the specific file.
				/*
				int errorFile = 0;
				for (int i = 1; i < sketch.getCodeCount(); i++) {
					SketchCode sc = sketch.getCode(i);
					if (sc.isExtension("pde")
							&& (sc.getPreprocOffset() < errorLine)) {
						errorFile = i;
					}
				}
				errorLine -= sketch.getCode(errorFile).getPreprocOffset();
				*/

				throw new SketchException(tsre.getMessage(), errorFile,
						errorLine, errorColumn);

			} else {
				// this is bad, defaults to the main class.. hrm.
				String msg = tsre.toString();
				throw new SketchException(msg, 0, -1, -1);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected int findErrorFile(int errorLine) {
		for (int i = sketch.getCodeCount() - 1; i > 0; i--) {
			SketchCode sc = sketch.getCode(i);
			if (sc.isExtension("pde") && (sc.getPreprocOffset() < errorLine)) {
				// keep looping until the errorLine is past the offset
				return i;
			}
		}
		return 0; // i give up
	}

	public void printAST(AST ast) throws SketchException {
		printAST(ast, System.err);
	}

	public void printAST(AST ast, PrintStream printStream)
			throws SketchException {
		PrintWriter writer = new PrintWriter(printStream);
		new PdeWalker(pp, writer).print(ast);
		writer.flush();
	}

	public void debugAST(final AST ast, final boolean includeHidden) {
		debugAST(ast, includeHidden, 0);
	}

	private void debugAST(final AST ast, final boolean includeHidden,
			final int indent) {
		for (int i = 0; i < indent; i++)
			System.err.print("    ");
		if (includeHidden) {
			System.err.print(debugHiddenBefore(ast));
		}
		if (ast.getType() > 0 && !ast.getText().equals(TokenUtil.nameOf(ast))) {
			System.err.print(TokenUtil.nameOf(ast) + "/");
			System.err.print(getIndex(ast) + "/");
		}
		System.err.print(ast.getText().replace("\n", "\\n"));
		if (includeHidden) {
			System.err.print(debugHiddenAfter(ast));
		}
		System.err.println();
		for (AST kid = ast.getFirstChild(); kid != null; kid = kid
				.getNextSibling())
			debugAST(kid, includeHidden, indent + 1);
	}

	private String debugHiddenAfter(AST ast) {
		if (!(ast instanceof antlr.CommonASTWithHiddenTokens))
			return "";
		return debugHiddenTokens(((antlr.CommonASTWithHiddenTokens) ast)
				.getHiddenAfter());
	}

	private String debugHiddenBefore(AST ast) {
		if (!(ast instanceof antlr.CommonASTWithHiddenTokens))
			return "";
		antlr.CommonHiddenStreamToken child = null, parent = ((antlr.CommonASTWithHiddenTokens) ast)
				.getHiddenBefore();

		if (parent == null) {
			return "";
		}

		do {
			child = parent;
			parent = child.getHiddenBefore();
		} while (parent != null);

		return debugHiddenTokens(child);
	}

	private String debugHiddenTokens(antlr.CommonHiddenStreamToken t) {
		final StringBuilder sb = new StringBuilder();
		for (; t != null; t = pp.getHiddenFilter().getHiddenAfter(t)) {
			if (sb.length() == 0)
				sb.append("[");
			sb.append(t.getText().replace("\n", "\\n"));
		}
		if (sb.length() > 0)
			sb.append("]");
		return sb.toString();
	}
}
