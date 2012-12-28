/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  PdePreprocessor - wrapper for default ANTLR-generated parser
  Part of the Processing project - http://processing.org

  Copyright (c) 2004-12 Ben Fry and Casey Reas
  Copyright (c) 2001-04 Massachusetts Institute of Technology

  ANTLR-generated parser and several supporting classes written
  by Dan Mosedale via funding from the Interaction Institute IVREA.

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package processing.mode.java.preproc;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import processing.app.Base;
import processing.app.Preferences;
import processing.app.SketchException;
import processing.core.PApplet;
import processing.mode.java.preproc.PdeLexer;
import processing.mode.java.preproc.PdeRecognizer;
import processing.mode.java.preproc.PdeTokenTypes;
import antlr.*;
import antlr.collections.AST;

/**
 * Class that orchestrates preprocessing p5 syntax into straight Java.
 * <P/>
 * <B>Current Preprocessor Subsitutions:</B>
 * <UL>
 * <LI>any function not specified as being protected or private will
 * be made 'public'. this means that <TT>void setup()</TT> becomes
 * <TT>public void setup()</TT>. This is important to note when
 * coding with core.jar outside of the PDE.
 * <LI><TT>compiler.substitute_floats</TT> (currently "substitute_f")
 * treat doubles as floats, i.e. 12.3 becomes 12.3f so that people
 * don't have to add f after their numbers all the time since it's
 * confusing for beginners.
 * <LI><TT>compiler.enhanced_casting</TT> byte(), char(), int(), float()
 * works for casting. this is basic in the current implementation, but
 * should be expanded as described above. color() works similarly to int(),
 * however there is also a *function* called color(r, g, b) in p5.
 * <LI><TT>compiler.color_datatype</TT> 'color' is aliased to 'int'
 * as a datatype to represent ARGB packed into a single int, commonly
 * used in p5 for pixels[] and other color operations. this is just a
 * search/replace type thing, and it can be used interchangeably with int.
 * <LI><TT>compiler.web_colors</TT> (currently "inline_web_colors")
 * color c = #cc0080; should unpack to 0xffcc0080 (the ff at the top is
 * so that the color is opaque), which is just an int.
 * </UL>
 * <B>Other preprocessor functionality</B>
 * <UL>
 * <LI>detects what 'mode' the program is in: static (no function
 * brackets at all, just assumes everything is in draw), active
 * (setup plus draw or loop), and java mode (full java support).
 * http://processing.org/reference/environment/
 * </UL>
 * <P/>
 * The PDE Preprocessor is based on the Java Grammar that comes with
 * ANTLR 2.7.2.  Moving it forward to a new version of the grammar
 * shouldn't be too difficult.
 * <P/>
 * Here's some info about the various files in this directory:
 * <P/>
 * <TT>java.g:</TT> this is the ANTLR grammar for Java 1.3/1.4 from the
 * ANTLR distribution.  It is in the public domain.  The only change to
 * this file from the original this file is the uncommenting of the
 * clauses required to support assert().
 * <P/>
 * <TT>java.tree.g:</TT> this describes the Abstract Syntax Tree (AST)
 * generated by java.g.  It is only here as a reference for coders hacking
 * on the preprocessor, it is not built or used at all.  Note that pde.g
 * overrides some of the java.g rules so that in PDE ASTs, there are a
 * few minor differences.  Also in the public domain.
 * <P/>
 * <TT>pde.g:</TT> this is the grammar and lexer for the PDE language
 * itself. It subclasses the java.g grammar and lexer.  There are a couple
 * of overrides to java.g that I hope to convince the ANTLR folks to fold
 * back into their grammar, but most of this file is highly specific to
 * PDE itself.
 * <TT>PdeEmitter.java:</TT> this class traverses the AST generated by
 * the PDE Recognizer, and emits it as Java code, doing any necessary
 * transformations along the way.  It is based on JavaEmitter.java,
 * available from antlr.org, written by Andy Tripp <atripp@comcast.net>,
 * who has given permission for it to be distributed under the GPL.
 * <P/>
 * <TT>ExtendedCommonASTWithHiddenTokens.java:</TT> this adds a necessary
 * initialize() method, as well as a number of methods to allow for XML
 * serialization of the parse tree in a such a way that the hidden tokens
 * are visible.  Much of the code is taken from the original
 * CommonASTWithHiddenTokens class.  I hope to convince the ANTLR folks
 * to fold these changes back into that class so that this file will be
 * unnecessary.
 * <P/>
 * <TT>TokenStreamCopyingHiddenTokenFilter.java:</TT> this class provides
 * TokenStreamHiddenTokenFilters with the concept of tokens which can be
 * copied so that they are seen by both the hidden token stream as well
 * as the parser itself.  This is useful when one wants to use an
 * existing parser (like the Java parser included with ANTLR) that throws
 * away some tokens to create a parse tree which can be used to spit out
 * a copy of the code with only minor modifications.  Partially derived
 * from ANTLR code.  I hope to convince the ANTLR folks to fold this
 * functionality back into ANTLR proper as well.
 * <P/>
 * <TT>whitespace_test.pde:</TT> a torture test to ensure that the
 * preprocessor is correctly preserving whitespace, comments, and other
 * hidden tokens correctly.  See the comments in the code for details about
 * how to run the test.
 * <P/>
 * All other files in this directory are generated at build time by ANTLR
 * itself.  The ANTLR manual goes into a fair amount of detail about the
 * what each type of file is for.
 * <P/>
 *
 * Hacked to death in 2010 by
 * @author Jonathan Feinberg &lt;jdf@pobox.com&gt;
 */
public class PdePreprocessor {
  protected static final String UNICODE_ESCAPES = "0123456789abcdefABCDEF";

  // used for calling the ASTFactory to get the root node
  public static final int ROOT_ID = 0;

  protected final String indent;
  private final String name;

  public static enum Mode {
    STATIC, ACTIVE, JAVA
  }

  private TokenStreamCopyingHiddenTokenFilter filter;

//  private boolean foundMain;
  private String advClassName = "";
  protected Mode mode;
  HashMap<String, Object> foundMethods;

  protected String sizeStatement;
  protected String sketchWidth;
  protected String sketchHeight;
  protected String sketchRenderer;

  /**
   * Regular expression for parsing the size() method. This should match
   * against any uses of the size() function, whether numbers or variables
   * or whatever. This way, no warning is shown if size() isn't actually used
   * in the sketch, which is the case especially for anyone who is cutting
   * and pasting from the reference.
   */
  public static final String SIZE_REGEX =
    "(?:^|\\s|;)size\\s*\\(\\s*([^\\s,]+)\\s*,\\s*([^\\s,\\)]+),?\\s*([^\\)]*)\\s*\\)\\s*\\;";
    //"(?:^|\\s|;)size\\s*\\(\\s*(\\S+)\\s*,\\s*([^\\s,\\)]+),?\\s*([^\\)]*)\\s*\\)\\s*\\;";

  
  public static final Pattern PUBLIC_CLASS =
    Pattern.compile("(^|;)\\s*public\\s+class\\s+\\S+\\s+extends\\s+PApplet", Pattern.MULTILINE);
    // Can't only match any 'public class', needs to be a PApplet
    // http://code.google.com/p/processing/issues/detail?id=551
    //Pattern.compile("(^|;)\\s*public\\s+class", Pattern.MULTILINE);

  
  public static final Pattern FUNCTION_DECL =
    Pattern.compile("(^|;)\\s*((public|private|protected|final|static)\\s+)*" +
        "(void|int|float|double|String|char|byte)" +
        "(\\s*\\[\\s*\\])?\\s+[a-zA-Z0-9]+\\s*\\(",
        Pattern.MULTILINE);


  public PdePreprocessor(final String sketchName) {
    this(sketchName, Preferences.getInteger("editor.tabs.size"));
  }


  public PdePreprocessor(final String sketchName, final int tabSize) {
    this.name = sketchName;
    final char[] indentChars = new char[tabSize];
    Arrays.fill(indentChars, ' ');
    indent = new String(indentChars);
  }


  public String[] initSketchSize(String code, boolean sizeWarning) throws SketchException {
    String[] info = parseSketchSize(code, sizeWarning);
    if (info != null) {
      sizeStatement = info[0];
      sketchWidth = info[1];
      sketchHeight = info[2];
      sketchRenderer = info[3];
    }
    return info;
  }


  /**
   * Parse a chunk of code and extract the size() command and its contents.
   * @param code Usually the code from the main tab in the sketch
   * @param fussy true if it should show an error message if bad size()
   * @return null if there was an error, otherwise an array (might contain some/all nulls)
   */
  static public String[] parseSketchSize(String code, boolean fussy) {
    // This matches against any uses of the size() function, whether numbers
    // or variables or whatever. This way, no warning is shown if size() isn't
    // actually used in the applet, which is the case especially for anyone
    // who is cutting/pasting from the reference.

//    String scrubbed = scrubComments(sketch.getCode(0).getProgram());
//    String[] matches = PApplet.match(scrubbed, SIZE_REGEX);
    String[] matches = PApplet.match(scrubComments(code), SIZE_REGEX);

    if (matches != null) {
      boolean badSize = false;

      if (matches[1].equals("screenWidth") ||
          matches[1].equals("screenHeight") ||
          matches[2].equals("screenWidth") ||
          matches[2].equals("screenHeight")) {
        final String message =
          "The screenWidth and screenHeight variables\n" +
          "are named displayWidth and displayHeight\n" +
          "in this release of Processing.";
        Base.showWarning("Time for a quick update", message, null);
        return null;
      }

      if (!matches[1].equals("displayWidth") &&
          !matches[1].equals("displayHeight") &&
          PApplet.parseInt(matches[1], -1) == -1) {
        badSize = true;
      }
      if (!matches[2].equals("displayWidth") &&
          !matches[2].equals("displayHeight") &&
          PApplet.parseInt(matches[2], -1) == -1) {
        badSize = true;
      }

      if (badSize && fussy) {
        // found a reference to size, but it didn't seem to contain numbers
        final String message =
          "The size of this applet could not automatically\n" +
          "be determined from your code. Use only numeric\n" +
          "values (not variables) for the size() command.\n" +
          "See the size() reference for an explanation.";
        Base.showWarning("Could not find sketch size", message, null);
//        new Exception().printStackTrace(System.out);
        return null;
      }

      // Remove additional space 'round the renderer
      matches[3] = matches[3].trim();

      // if the renderer entry is empty, set it to null
      if (matches[3].length() == 0) {
        matches[3] = null;
      }
      return matches;
    }
    return new String[] { null, null, null, null };  // not an error, just empty
  }


  /**
   * Replace all commented portions of a given String as spaces.
   * Utility function used here and in the preprocessor.
   */
  static public String scrubComments(String what) {
    char p[] = what.toCharArray();
    // Track quotes to avoid problems with code like: String t = "*/*";
    // http://code.google.com/p/processing/issues/detail?id=1435
    boolean insideQuote = false;

    int index = 0;
    while (index < p.length) {
      // for any double slash comments, ignore until the end of the line
      if (!insideQuote && 
          (p[index] == '/') &&
          (index < p.length - 1) &&
          (p[index+1] == '/')) {
        p[index++] = ' ';
        p[index++] = ' ';
        while ((index < p.length) &&
               (p[index] != '\n')) {
          p[index++] = ' ';
        }

        // check to see if this is the start of a new multiline comment.
        // if it is, then make sure it's actually terminated somewhere.
      } else if (!insideQuote &&
                 (p[index] == '/') &&
                 (index < p.length - 1) &&
                 (p[index+1] == '*')) {
        p[index++] = ' ';
        p[index++] = ' ';
        boolean endOfRainbow = false;
        while (index < p.length - 1) {
          if ((p[index] == '*') && (p[index+1] == '/')) {
            p[index++] = ' ';
            p[index++] = ' ';
            endOfRainbow = true;
            break;

          } else {
            // continue blanking this area
            p[index++] = ' ';
          }
        }
        if (!endOfRainbow) {
          throw new RuntimeException("Missing the */ from the end of a " +
                                     "/* comment */");
        }
      } else if (p[index] == '"' && index > 0 && p[index-1] != '\\') {
        insideQuote = !insideQuote;
        index++;
        
      } else {  // any old character, move along
        index++;
      }
    }
    return new String(p);
  }


  public void addMethod(String methodName) {
    foundMethods.put(methodName, new Object());
  }


  public boolean hasMethod(String methodName) {
    return foundMethods.containsKey(methodName);
  }


//  public void setFoundMain(boolean foundMain) {
//    this.foundMain = foundMain;
//  }


//  public boolean getFoundMain() {
//    return foundMain;
//  }


  public void setAdvClassName(final String advClassName) {
    this.advClassName = advClassName;
  }


  public void setMode(final Mode mode) {
    //System.err.println("Setting mode to " + mode);
    this.mode = mode;
  }


  public CommonHiddenStreamToken getHiddenAfter(final CommonHiddenStreamToken t) {
    return filter.getHiddenAfter(t);
  }


  public CommonHiddenStreamToken getInitialHiddenToken() {
    return filter.getInitialHiddenToken();
  }


  private static int countNewlines(final String s) {
    int count = 0;
    for (int pos = s.indexOf('\n', 0); pos >= 0; pos = s.indexOf('\n', pos + 1))
      count++;
    return count;
  }


  public static void checkForUnterminatedMultilineComment(final String program)
      throws SketchException {
    final int length = program.length();
    for (int i = 0; i < length; i++) {
      // for any double slash comments, ignore until the end of the line
      if ((program.charAt(i) == '/') && (i < length - 1)
          && (program.charAt(i + 1) == '/')) {
        i += 2;
        while ((i < length) && (program.charAt(i) != '\n')) {
          i++;
        }
        // check to see if this is the start of a new multiline comment.
        // if it is, then make sure it's actually terminated somewhere.
      } else if ((program.charAt(i) == '/') && (i < length - 1)
          && (program.charAt(i + 1) == '*')) {
        final int startOfComment = i;
        i += 2;
        boolean terminated = false;
        while (i < length - 1) {
          if ((program.charAt(i) == '*') && (program.charAt(i + 1) == '/')) {
            i += 2;
            terminated = true;
            break;
          } else {
            i++;
          }
        }
        if (!terminated) {
          throw new SketchException("Unclosed /* comment */", 0,
                                    countNewlines(program.substring(0,
                                      startOfComment)));
        }
      } else if (program.charAt(i) == '"') {
        final int stringStart = i;
        boolean terminated = false;
        for (i++; i < length; i++) {
          final char c = program.charAt(i);
          if (c == '"') {
            terminated = true;
            break;
          } else if (c == '\\') {
            if (i == length - 1) {
              break;
            }
            i++;
          } else if (c == '\n') {
            break;
          }
        }
        if (!terminated) {
          throw new SketchException("Unterminated string constant", 0,
                                    countNewlines(program.substring(0,
                                      stringStart)));
        }
      } else if (program.charAt(i) == '\'') {
        i++;  // step over the initial quote
        if (i >= length) {
          throw new SketchException("Unterminated character constant (after initial quote)", 0,
                                    countNewlines(program.substring(0, i)));
        }
        boolean escaped = false;
        if (program.charAt(i) == '\\') {
          i++;  // step over the backslash
          escaped = true;
        }
        if (i >= length) {
          throw new SketchException("Unterminated character constant (after backslash)", 0,
                                    countNewlines(program.substring(0, i)));
        }
        if (escaped && program.charAt(i) == 'u') {  // unicode escape sequence?
          i++;  // step over the u
          //i += 4;  // and the four digit unicode constant
          for (int j = 0; j < 4; j++) {
            if (UNICODE_ESCAPES.indexOf(program.charAt(i)) == -1) {
              throw new SketchException("Bad or unfinished \\uXXXX sequence " +
              		                      "(malformed Unicode character constant)", 0,
                                        countNewlines(program.substring(0, i)));
            }
            i++;
          }
        } else {
          i++;  // step over a single character
        }
        if (i >= length) {
          throw new SketchException("Unterminated character constant", 0,
                                    countNewlines(program.substring(0, i)));
        }
        if (program.charAt(i) != '\'') {
          throw new SketchException("Badly formed character constant " +
          		                      "(expecting quote, got " + program.charAt(i) + ")", 0,
                                    countNewlines(program.substring(0, i)));
        }
      }
    }
  }


  public PreprocessorResult write(final Writer out, String program)
      throws SketchException, RecognitionException, TokenStreamException {
    return write(out, program, null);
  }


  public PreprocessorResult write(Writer out, String program,
                                  String codeFolderPackages[])
      throws SketchException, RecognitionException, TokenStreamException {

    // these ones have the .* at the end, since a class name might be at the end
    // instead of .* which would make trouble other classes using this can lop
    // off the . and anything after it to produce a package name consistently.
    final ArrayList<String> programImports = new ArrayList<String>();

    // imports just from the code folder, treated differently
    // than the others, since the imports are auto-generated.
    final ArrayList<String> codeFolderImports = new ArrayList<String>();

    // need to reset whether or not this has a main()
//    foundMain = false;
    foundMethods = new HashMap<String, Object>();

    // http://processing.org/bugs/bugzilla/5.html
    if (!program.endsWith("\n")) {
      program += "\n";
    }

    checkForUnterminatedMultilineComment(program);

    if (Preferences.getBoolean("preproc.substitute_unicode")) {
      program = substituteUnicode(program);
    }

    // For 0215, adding } as a legitimate prefix to the import (along with 
    // newline and semicolon) for cases where a tab ends with } and an import
    // statement starts the next tab.
    final String importRegexp = 
      "((?:^|;|\\})\\s*)(import\\s+)((?:static\\s+)?\\S+)(\\s*;)";
    final Pattern importPattern = Pattern.compile(importRegexp);
    String scrubbed = scrubComments(program);
    Matcher m = null;
    int offset = 0;
    boolean found = false;
    do {
      m = importPattern.matcher(scrubbed);
      found = m.find(offset);
      if (found) {
//        System.out.println("found " + m.groupCount() + " groups");
        String before = m.group(1);
        String piece = m.group(2) + m.group(3) + m.group(4);
//        int len = piece.length(); // how much to trim out

        if (!ignoreImport(m.group(3))) {
          programImports.add(m.group(3)); // the package name
        }

        // find index of this import in the program
        int start = m.start() + before.length();
        int stop = start + piece.length();
//        System.out.println(start + " " + stop + " " + piece);
        //System.out.println("found " + m.group(3));
//        System.out.println("removing '" + program.substring(start, stop) + "'");

        // Remove the import from the main program
        program = program.substring(0, start) + program.substring(stop);
        scrubbed = scrubbed.substring(0, start) + scrubbed.substring(stop);
        // Set the offset to start, because everything between 
        // start and stop has been deleted.
        offset = m.start();        
      }
    } while (found);
//    System.out.println("program now:");
//    System.out.println(program);

    if (codeFolderPackages != null) {
      for (String item : codeFolderPackages) {
        codeFolderImports.add(item + ".*");
      }
    }

    final PrintWriter stream = new PrintWriter(out);
    final int headerOffset = 
      writeImports(stream, programImports, codeFolderImports);
    return new PreprocessorResult(mode, headerOffset + 2, 
                                  write(program, stream), programImports);
  }

  
  static String substituteUnicode(String program) {
    // check for non-ascii chars (these will be/must be in unicode format)
    char p[] = program.toCharArray();
    int unicodeCount = 0;
    for (int i = 0; i < p.length; i++) {
      if (p[i] > 127)
        unicodeCount++;
    }
    if (unicodeCount == 0)
      return program;
    // if non-ascii chars are in there, convert to unicode escapes
    // add unicodeCount * 5.. replacing each unicode char
    // with six digit uXXXX sequence (xxxx is in hex)
    // (except for nbsp chars which will be a replaced with a space)
    int index = 0;
    char p2[] = new char[p.length + unicodeCount * 5];
    for (int i = 0; i < p.length; i++) {
      if (p[i] < 128) {
        p2[index++] = p[i];
      } else if (p[i] == 160) { // unicode for non-breaking space
        p2[index++] = ' ';
      } else {
        int c = p[i];
        p2[index++] = '\\';
        p2[index++] = 'u';
        char str[] = Integer.toHexString(c).toCharArray();
        // add leading zeros, so that the length is 4
        //for (int i = 0; i < 4 - str.length; i++) p2[index++] = '0';
        for (int m = 0; m < 4 - str.length; m++)
          p2[index++] = '0';
        System.arraycopy(str, 0, p2, index, str.length);
        index += str.length;
      }
    }
    return new String(p2, 0, index);
  }


  /**
   * preprocesses a pde file and writes out a java file
   * @return the class name of the exported Java
   */
  private String write(final String program, final PrintWriter stream)
      throws SketchException, RecognitionException, TokenStreamException {

    // Match on the uncommented version, otherwise code inside comments used
    // http://code.google.com/p/processing/issues/detail?id=1404
    String uncomment = scrubComments(program);
    PdeRecognizer parser = createParser(program);
    if (PUBLIC_CLASS.matcher(uncomment).find()) {
      try {
        final PrintStream saved = System.err;
        try {
          // throw away stderr for this tentative parse
          System.setErr(new PrintStream(new ByteArrayOutputStream()));
          parser.javaProgram();
        } finally {
          System.setErr(saved);
        }
        setMode(Mode.JAVA);
      } catch (Exception e) {
        // I can't figure out any other way of resetting the parser.
        parser = createParser(program);
        parser.pdeProgram();
      }
    } else if (FUNCTION_DECL.matcher(uncomment).find()) {
      setMode(Mode.ACTIVE);
      parser.activeProgram();
    } else {
      parser.pdeProgram();
    }

    // set up the AST for traversal by PdeEmitter
    //
    ASTFactory factory = new ASTFactory();
    AST parserAST = parser.getAST();
    AST rootNode = factory.create(ROOT_ID, "AST ROOT");
    rootNode.setFirstChild(parserAST);

    makeSimpleMethodsPublic(rootNode);

    // unclear if this actually works, but it's worth a shot
    //
    //((CommonAST)parserAST).setVerboseStringConversion(
    //  true, parser.getTokenNames());
    // (made to use the static version because of jikes 1.22 warning)
    BaseAST.setVerboseStringConversion(true, parser.getTokenNames());

    final String className;
    if (mode == Mode.JAVA) {
      // if this is an advanced program, the classname is already defined.
      className = getFirstClassName(parserAST);
    } else {
      className = this.name;
    }

    // if 'null' was passed in for the name, but this isn't
    // a 'java' mode class, then there's a problem, so punt.
    //
    if (className == null)
      return null;

    // debug
    if (false) {
      final StringWriter buf = new StringWriter();
      final PrintWriter bufout = new PrintWriter(buf);
      writeDeclaration(bufout, className);
      new PdeEmitter(this, bufout).print(rootNode);
      writeFooter(bufout, className);
      debugAST(rootNode, true);
      System.err.println(buf.toString());
    }

    writeDeclaration(stream, className);
    new PdeEmitter(this, stream).print(rootNode);
    writeFooter(stream, className);

    // if desired, serialize the parse tree to an XML file.  can
    // be viewed usefully with Mozilla or IE
    if (Preferences.getBoolean("preproc.output_parse_tree")) {
      writeParseTree("parseTree.xml", parserAST);
    }

    return className;
  }
  

  public PdeRecognizer createParser(final String program) {
    // create a lexer with the stream reader, and tell it to handle
    // hidden tokens (eg whitespace, comments) since we want to pass these
    // through so that the line numbers when the compiler reports errors
    // match those that will be highlighted in the PDE IDE
    //
    PdeLexer lexer = new PdeLexer(new StringReader(program));
    lexer.setTokenObjectClass("antlr.CommonHiddenStreamToken");

    // create the filter for hidden tokens and specify which tokens to
    // hide and which to copy to the hidden text
    //
    filter = new TokenStreamCopyingHiddenTokenFilter(lexer);
    filter.hide(PdePartialTokenTypes.SL_COMMENT);
    filter.hide(PdePartialTokenTypes.ML_COMMENT);
    filter.hide(PdePartialTokenTypes.WS);
    filter.copy(PdePartialTokenTypes.SEMI);
    filter.copy(PdePartialTokenTypes.LPAREN);
    filter.copy(PdePartialTokenTypes.RPAREN);
    filter.copy(PdePartialTokenTypes.LCURLY);
    filter.copy(PdePartialTokenTypes.RCURLY);
    filter.copy(PdePartialTokenTypes.COMMA);
    filter.copy(PdePartialTokenTypes.RBRACK);
    filter.copy(PdePartialTokenTypes.LBRACK);
    filter.copy(PdePartialTokenTypes.COLON);
    filter.copy(PdePartialTokenTypes.TRIPLE_DOT);

    // Because the meanings of < and > are overloaded to support
    // type arguments and type parameters, we have to treat them
    // as copyable to hidden text (or else the following syntax,
    // such as (); and what not gets lost under certain circumstances)
    // -- jdf
    filter.copy(PdePartialTokenTypes.LT);
    filter.copy(PdePartialTokenTypes.GT);
    filter.copy(PdePartialTokenTypes.SR);
    filter.copy(PdePartialTokenTypes.BSR);

    // create a parser and set what sort of AST should be generated
    //
    final PdeRecognizer parser = new PdeRecognizer(this, filter);

    // use our extended AST class
    //
    parser.setASTNodeClass("antlr.ExtendedCommonASTWithHiddenTokens");
    return parser;
  }

  /**
   * Walk the tree looking for METHOD_DEFs. Any simple METHOD_DEF (one
   * without TYPE_PARAMETERS) lacking an
   * access specifier is given public access.
   * @param node
   */
  private void makeSimpleMethodsPublic(final AST node) {
    if (node.getType() == PdeTokenTypes.METHOD_DEF) {
      final AST mods = node.getFirstChild();
      final AST oldFirstMod = mods.getFirstChild();
      for (AST mod = oldFirstMod; mod != null; mod = mod.getNextSibling()) {
        final int t = mod.getType();
        if (t == PdeTokenTypes.LITERAL_private ||
            t == PdeTokenTypes.LITERAL_protected ||
            t == PdeTokenTypes.LITERAL_public) {
          return;
        }
      }
      if (mods.getNextSibling().getType() == PdeTokenTypes.TYPE_PARAMETERS) {
        return;
      }
      final CommonHiddenStreamToken publicToken =
        new CommonHiddenStreamToken(PdeTokenTypes.LITERAL_public, "public") {
        {
          setHiddenAfter(new CommonHiddenStreamToken(PdeTokenTypes.WS, " "));
        }
      };
      final AST publicNode = new CommonASTWithHiddenTokens(publicToken);
      publicNode.setNextSibling(oldFirstMod);
      mods.setFirstChild(publicNode);
    } else {
      for (AST kid = node.getFirstChild(); kid != null; kid = kid
          .getNextSibling())
        makeSimpleMethodsPublic(kid);
    }
  }

  protected void writeParseTree(String filename, AST ast) {
    try {
      PrintStream stream = new PrintStream(new FileOutputStream(filename));
      stream.println("<?xml version=\"1.0\"?>");
      stream.println("<document>");
      OutputStreamWriter writer = new OutputStreamWriter(stream);
      if (ast != null) {
        ((CommonAST) ast).xmlSerialize(writer);
      }
      writer.flush();
      stream.println("</document>");
      writer.close();
    } catch (IOException e) {

    }
  }

  /**
   *
   * @param out
   * @param programImports
   * @param codeFolderImports
   * @return the header offset
   */
  protected int writeImports(final PrintWriter out,
                             final List<String> programImports,
                             final List<String> codeFolderImports) {
    int count = writeImportList(out, getCoreImports());
    count += writeImportList(out, programImports);
    count += writeImportList(out, codeFolderImports);
    count += writeImportList(out, getDefaultImports());
    return count;
  }

  protected int writeImportList(PrintWriter out, List<String> imports) {
    return writeImportList(out, imports.toArray(new String[0]));
  }

  protected int writeImportList(PrintWriter out, String[] imports) {
    int count = 0;
    if (imports != null && imports.length != 0) {
      for (String item : imports) {
        out.println("import " + item + "; ");
        count++;
      }
      out.println();
      count++;
    }
    return count;
  }

  /**
   * Write any required header material (eg imports, class decl stuff)
   *
   * @param out                 PrintStream to write it to.
   * @param exporting           Is this being exported from PDE?
   * @param className           Name of the class being created.
   */
  protected void writeDeclaration(PrintWriter out, String className) {
    if (mode == Mode.JAVA) {
      // Print two blank lines so that the offset doesn't change
      out.println();
      out.println();

    } else if (mode == Mode.ACTIVE) {
      // Print an extra blank line so the offset is identical to the others
      out.println("public class " + className + " extends PApplet {");
      out.println();

    } else if (mode == Mode.STATIC) {
      out.println("public class " + className + " extends PApplet {");
      out.println(indent + "public void setup() {");
    }
  }

  /**
   * Write any necessary closing text.
   *
   * @param out PrintStream to write it to.
   */
  protected void writeFooter(PrintWriter out, String className) {
    if (mode == Mode.STATIC) {
      // close off setup() definition
      out.println(indent + indent + "noLoop();");
      out.println(indent + "}");
      out.println();
    }

    if ((mode == Mode.STATIC) || (mode == Mode.ACTIVE)) {
      if (sketchWidth != null && !hasMethod("sketchWidth")) {
        // Only include if it's a number (a variable will be a problem)
        if (PApplet.parseInt(sketchWidth, -1) != -1 || sketchWidth.equals("displayWidth")) {
          out.println(indent + "public int sketchWidth() { return " + sketchWidth + "; }");
        }
      }
      if (sketchHeight != null && !hasMethod("sketchHeight")) {
        // Only include if it's a number
        if (PApplet.parseInt(sketchHeight, -1) != -1 || sketchHeight.equals("displayHeight")) {
          out.println(indent + "public int sketchHeight() { return " + sketchHeight + "; }");
        }
      }
      if (sketchRenderer != null && !hasMethod("sketchRenderer")) {
        // Only include if it's a known renderer (otherwise it might be a variable)
        if (sketchRenderer.equals("P2D") ||
            sketchRenderer.equals("P3D") ||
            sketchRenderer.equals("OPENGL") ||
            sketchRenderer.equals("JAVA2D")) {
          out.println(indent + "public String sketchRenderer() { return " + sketchRenderer + "; }");
        }
      }

      if (!hasMethod("main")) {
        out.println(indent + "static public void main(String[] passedArgs) {");
        //out.print(indent + indent + "PApplet.main(new String[] { ");
        out.print(indent + indent + "String[] appletArgs = new String[] { ");

        if (Preferences.getBoolean("export.application.fullscreen")) {
          out.print("\"" + PApplet.ARGS_FULL_SCREEN + "\", ");

          String farbe = Preferences.get("run.present.bgcolor");
          out.print("\"" + PApplet.ARGS_BGCOLOR + "=" + farbe + "\", ");

          if (Preferences.getBoolean("export.application.stop")) {
            farbe = Preferences.get("run.present.stop.color");
            out.print("\"" + PApplet.ARGS_STOP_COLOR + "=" + farbe + "\", ");
          } else {
            out.print("\"" + PApplet.ARGS_HIDE_STOP + "\", ");
          }
//        } else {
//          // This is set initially based on the system control color, just
//          // sets the color for what goes behind the sketch before it's added.
//          String farbe = Preferences.get("run.window.bgcolor");
//          out.print("\"" + PApplet.ARGS_BGCOLOR + "=" + farbe + "\", ");
        }
        out.println("\"" + className + "\" };");

        out.println(indent + indent + "if (passedArgs != null) {");
        out.println(indent + indent + "  PApplet.main(concat(appletArgs, passedArgs));");
        out.println(indent + indent + "} else {");
        out.println(indent + indent + "  PApplet.main(appletArgs);");
        out.println(indent + indent + "}");

        out.println(indent + "}");
      }

      // close off the class definition
      out.println("}");
    }
  }

  public String[] getCoreImports() {
    return new String[] {
      "processing.core.*",
      "processing.data.*",
      "processing.event.*",
      "processing.opengl.*"
    };
  }

  public String[] getDefaultImports() {
    // These may change in-between (if the prefs panel adds this option)
    //String prefsLine = Preferences.get("preproc.imports");
    //return PApplet.splitTokens(prefsLine, ", ");
    return new String[] { 
      "java.util.HashMap",
      "java.util.ArrayList",
      "java.io.File",
      "java.io.BufferedReader",
      "java.io.PrintWriter",
      "java.io.InputStream",
      "java.io.OutputStream",
      "java.io.IOException"
    };
  }

  /**
   * Return true if this import should be removed from the code. This is used
   * for packages like processing.xml which no longer exist.
   * @param pkg something like processing.xml.XMLElement or processing.xml.*
   * @return true if this shouldn't be added to the final code
   */
  public boolean ignoreImport(String pkg) {
    return false;
//    return pkg.startsWith("processing.xml.");
  }

  /**
   * Find the first CLASS_DEF node in the tree, and return the name of the
   * class in question.
   *
   * TODO [dmose] right now, we're using a little hack to the grammar to get
   * this info.  In fact, we should be descending the AST passed in.
   */
  String getFirstClassName(AST ast) {
    String t = advClassName;
    advClassName = "";
    return t;
  }

  public void debugAST(final AST ast, final boolean includeHidden) {
    System.err.println("------------------");
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
    }
    System.err.print(ast.getText().replace("\n", "\\n"));
    if (includeHidden) {
      System.err.print(debugHiddenAfter(ast));
    }
    System.err.println();
    for (AST kid = ast.getFirstChild(); kid != null; kid = kid.getNextSibling())
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
    for (; t != null; t = filter.getHiddenAfter(t)) {
      if (sb.length() == 0)
        sb.append("[");
      sb.append(t.getText().replace("\n", "\\n"));
    }
    if (sb.length() > 0)
      sb.append("]");
    return sb.toString();
  }

  /**
   * Get the filter created at {@link #createParser(String)} method.
   *
   * @return
   */
  public TokenStreamCopyingHiddenTokenFilter getHiddenFilter() {
    return filter;
  }
}