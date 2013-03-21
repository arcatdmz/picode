package com.phybots.picode.parser;

import java.io.FileWriter;
import java.io.IOException;

import com.phybots.picode.PicodeSettings;
import com.phybots.picode.ProcessingIntegration;

import processing.app.PicodeSketch;
import processing.app.SketchException;
import processing.mode.java.preproc.PdeEmitter;
import processing.mode.java.preproc.PdePreprocessor;
import processing.mode.java.preproc.PdeTokenTypes;
import processing.mode.java.preproc.TokenUtil;
import antlr.CommonASTWithHiddenTokens;
import antlr.CommonHiddenStreamToken;
import antlr.collections.AST;

public class ASTtoHTMLConverter {

	private PdePreprocessor pp;
	private StringBuilder sb;

	private static final String FORMAT_COMMENT = "<font color=\"#cc3300\">%s</font>";
	private static final String FORMAT_KEYWORDS = "<font color=\"purple\"><b>%s</b></font>";

	public ASTtoHTMLConverter(PdePreprocessor pp) {
		this.pp = pp;
	}

	public static void main(String[] args) {
		ProcessingIntegration.init();
		PicodeSketch sketch = null;
		try {
			sketch = new PicodeSketch(null, PicodeSettings.getDefaultSketchPath());
			PdeParser parser = new PdeParser(sketch);
			AST ast = parser.parse(0);
			FileWriter writer = new FileWriter(sketch.getMainFilePath() + ".html");
			writer.append(
					new ASTtoHTMLConverter(parser.getPreprocessor()).convert(
							ast, sketch.getName(), "../../poses/"));
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SketchException se) {
			System.err.println(ProcessingIntegration.getErrorString(sketch, se));
		}
	}

	private String rootPath = "./";
	public String convert(AST ast, String title, String rootPath) {
		this.rootPath = rootPath;
		this.sb = new StringBuilder();
		sb.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/><title>");
		sb.append(title);
		sb.append("</title></head><body>");
		doConvert(ast);
		sb.append("</body></html>");
		return sb.toString();
	}

	/**
	 * Copied from {@link processing.mode.java.preproc.PdeEmitter}
	 *
	 * @param ast
	 * @param sb
	 */
	private void doConvert(AST ast) {
		if (ast == null) {
			return;
		}
		final AST child1 = ast.getFirstChild();
		AST child2 = null;
		AST child3 = null;
		if (child1 != null) {
			child2 = child1.getNextSibling();
			if (child2 != null) {
				child3 = child2.getNextSibling();
			}
		}

		switch (ast.getType()) {
		case PdeWalker.ROOT_ID:
			dumpHiddenTokens(pp.getInitialHiddenToken());
			printChildren(ast);
			break;

		case PdeTokenTypes.PACKAGE_DEF:
			sb.append(String.format(FORMAT_KEYWORDS, "package"));
			dumpHiddenAfter(ast);
			doConvert(ast.getFirstChild());
			break;

		// IMPORT has exactly one child
		case PdeTokenTypes.IMPORT:
			sb.append(String.format(FORMAT_KEYWORDS, "import"));
			dumpHiddenAfter(ast);
			doConvert(ast.getFirstChild());
			break;

		case PdeTokenTypes.STATIC_IMPORT:
			sb.append(String.format(FORMAT_KEYWORDS, "import static"));
			dumpHiddenAfter(ast);
			doConvert(ast.getFirstChild());
			break;

		case PdeTokenTypes.CLASS_DEF:
		case PdeTokenTypes.INTERFACE_DEF:
			doConvert(getChild(ast, PdeTokenTypes.MODIFIERS));
			sb.append(String.format(FORMAT_KEYWORDS,
					ast.getType() == PdeTokenTypes.CLASS_DEF ?
							"class" : "interface"));
			dumpHiddenBefore(getChild(ast, PdeTokenTypes.IDENT));
			doConvert(getChild(ast, PdeTokenTypes.IDENT));
			doConvert(getChild(ast, PdeTokenTypes.TYPE_PARAMETERS));
			doConvert(getChild(ast, PdeTokenTypes.EXTENDS_CLAUSE));
			doConvert(getChild(ast, PdeTokenTypes.IMPLEMENTS_CLAUSE));
			doConvert(getChild(ast, PdeTokenTypes.OBJBLOCK));
			break;

		case PdeTokenTypes.EXTENDS_CLAUSE:
			if (hasChildren(ast)) {
				sb.append(String.format(FORMAT_KEYWORDS, "extends"));
				dumpHiddenBefore(getBestPrintableNode(ast, false));
				printChildren(ast);
			}
			break;

		case PdeTokenTypes.IMPLEMENTS_CLAUSE:
			if (hasChildren(ast)) {
				sb.append(String.format(FORMAT_KEYWORDS, "implements"));
				dumpHiddenBefore(getBestPrintableNode(ast, false));
				printChildren(ast);
			}
			break;

		// DOT always has exactly two children.
		case PdeTokenTypes.DOT:
			doConvert(child1);
			sb.append(".");
			dumpHiddenAfter(ast);
			doConvert(child2);
			break;

		case PdeTokenTypes.METHOD_CALL:
			if (handlePicodeMethodCall(child1, child2, sb)) {
				break;
			}
		case PdeTokenTypes.MODIFIERS:
		case PdeTokenTypes.OBJBLOCK:
		case PdeTokenTypes.CTOR_DEF:
			// case PdeWalker.METHOD_DEF:
		case PdeTokenTypes.PARAMETERS:
		case PdeTokenTypes.PARAMETER_DEF:
		case PdeTokenTypes.VARIABLE_PARAMETER_DEF:
		case PdeTokenTypes.VARIABLE_DEF:
		case PdeTokenTypes.TYPE:
		case PdeTokenTypes.SLIST:
		case PdeTokenTypes.ELIST:
		case PdeTokenTypes.ARRAY_DECLARATOR:
		case PdeTokenTypes.TYPECAST:
		case PdeTokenTypes.EXPR:
		case PdeTokenTypes.ARRAY_INIT:
		case PdeTokenTypes.FOR_INIT:
		case PdeTokenTypes.FOR_CONDITION:
		case PdeTokenTypes.FOR_ITERATOR:
		case PdeTokenTypes.INSTANCE_INIT:
		case PdeTokenTypes.INDEX_OP:
		case PdeTokenTypes.SUPER_CTOR_CALL:
		case PdeTokenTypes.CTOR_CALL:
		case PdeTokenTypes.METHOD_DEF:
			printChildren(ast);
			break;

		// if we have two children, it's of the form "a=0"
		// if just one child, it's of the form "=0" (where the
		// lhs is above this AST).
		case PdeTokenTypes.ASSIGN:
			if (child2 != null) {
				doConvert(child1);
				sb.append("=");
				dumpHiddenAfter(ast);
				doConvert(child2);
			} else {
				sb.append("=");
				dumpHiddenAfter(ast);
				doConvert(child1);
			}
			break;

		// binary operators:
		case PdeTokenTypes.PLUS:
		case PdeTokenTypes.MINUS:
		case PdeTokenTypes.DIV:
		case PdeTokenTypes.MOD:
		case PdeTokenTypes.NOT_EQUAL:
		case PdeTokenTypes.EQUAL:
		case PdeTokenTypes.LE:
		case PdeTokenTypes.GE:
		case PdeTokenTypes.LOR:
		case PdeTokenTypes.LAND:
		case PdeTokenTypes.BOR:
		case PdeTokenTypes.BXOR:
		case PdeTokenTypes.BAND:
		case PdeTokenTypes.SL:
		case PdeTokenTypes.SR:
		case PdeTokenTypes.BSR:
		case PdeTokenTypes.LITERAL_instanceof:
		case PdeTokenTypes.PLUS_ASSIGN:
		case PdeTokenTypes.MINUS_ASSIGN:
		case PdeTokenTypes.STAR_ASSIGN:
		case PdeTokenTypes.DIV_ASSIGN:
		case PdeTokenTypes.MOD_ASSIGN:
		case PdeTokenTypes.SR_ASSIGN:
		case PdeTokenTypes.BSR_ASSIGN:
		case PdeTokenTypes.SL_ASSIGN:
		case PdeTokenTypes.BAND_ASSIGN:
		case PdeTokenTypes.BXOR_ASSIGN:
		case PdeTokenTypes.BOR_ASSIGN:

		case PdeTokenTypes.LT:
		case PdeTokenTypes.GT:
			printBinaryOperator(ast);
			break;

		case PdeTokenTypes.LITERAL_for:
			sb.append(String.format(FORMAT_KEYWORDS, "for"));
			dumpHiddenAfter(ast);
			if (child1.getType() == PdeTokenTypes.FOR_EACH_CLAUSE) {
				printChildren(child1);
				doConvert(child2);
			} else {
				printChildren(ast);
			}
			break;

		case PdeTokenTypes.POST_INC:
		case PdeTokenTypes.POST_DEC:
			doConvert(child1);
			sb.append(getSanitizedText(ast));
			dumpHiddenAfter(ast);
			break;

		// unary operators:
		case PdeTokenTypes.BNOT:
		case PdeTokenTypes.LNOT:
		case PdeTokenTypes.INC:
		case PdeTokenTypes.DEC:
		case PdeTokenTypes.UNARY_MINUS:
		case PdeTokenTypes.UNARY_PLUS:
			sb.append(getSanitizedText(ast));
			dumpHiddenAfter(ast);
			doConvert(child1);
			break;

		case PdeTokenTypes.LITERAL_new:
			sb.append(String.format(FORMAT_KEYWORDS, "new"));
			dumpHiddenAfter(ast);
			printChildren(ast);
			break;

		case PdeTokenTypes.LITERAL_return:
			sb.append(String.format(FORMAT_KEYWORDS, "return"));
			dumpHiddenAfter(ast);
			doConvert(child1);
			break;

		case PdeTokenTypes.STATIC_INIT:
			sb.append(String.format(FORMAT_KEYWORDS, "static"));
			dumpHiddenBefore(getBestPrintableNode(ast, false));
			doConvert(child1);
			break;

		case PdeTokenTypes.LITERAL_switch:
			sb.append(String.format(FORMAT_KEYWORDS, "switch"));
			dumpHiddenAfter(ast);
			printChildren(ast);
			break;

		case PdeTokenTypes.LABELED_STAT:
		case PdeTokenTypes.CASE_GROUP:
			printChildren(ast);
			break;

		case PdeTokenTypes.LITERAL_case:
			sb.append(String.format(FORMAT_KEYWORDS, "case"));
			dumpHiddenAfter(ast);
			printChildren(ast);
			break;

		case PdeTokenTypes.LITERAL_default:
			sb.append(String.format(FORMAT_KEYWORDS, "default"));
			dumpHiddenAfter(ast);
			printChildren(ast);
			break;

		case PdeTokenTypes.NUM_INT:
		case PdeTokenTypes.CHAR_LITERAL:
		case PdeTokenTypes.STRING_LITERAL:
		case PdeTokenTypes.NUM_FLOAT:
		case PdeTokenTypes.NUM_LONG:
			sb.append(getSanitizedText(ast));
			dumpHiddenAfter(ast);
			break;

		case PdeTokenTypes.LITERAL_synchronized: // 0137 to fix bug #136
		case PdeTokenTypes.LITERAL_assert:
			sb.append(String.format(FORMAT_KEYWORDS,
					getSanitizedText(ast)));
			dumpHiddenAfter(ast);
			printChildren(ast);
			break;

		case PdeTokenTypes.LITERAL_private:
		case PdeTokenTypes.LITERAL_public:
		case PdeTokenTypes.LITERAL_protected:
		case PdeTokenTypes.LITERAL_static:
		case PdeTokenTypes.LITERAL_transient:
		case PdeTokenTypes.LITERAL_native:
		case PdeTokenTypes.LITERAL_threadsafe:
			// case PdeWalker.LITERAL_synchronized: // 0137 to fix bug #136
		case PdeTokenTypes.LITERAL_volatile:
		case PdeTokenTypes.LITERAL_class: // 0176 to fix bug #1466
		case PdeTokenTypes.FINAL:
		case PdeTokenTypes.ABSTRACT:
		case PdeTokenTypes.LITERAL_package:
		case PdeTokenTypes.LITERAL_void:
		case PdeTokenTypes.LITERAL_boolean:
		case PdeTokenTypes.LITERAL_byte:
		case PdeTokenTypes.LITERAL_char:
		case PdeTokenTypes.LITERAL_short:
		case PdeTokenTypes.LITERAL_int:
		case PdeTokenTypes.LITERAL_float:
		case PdeTokenTypes.LITERAL_long:
		case PdeTokenTypes.LITERAL_double:
		case PdeTokenTypes.LITERAL_true:
		case PdeTokenTypes.LITERAL_false:
		case PdeTokenTypes.LITERAL_null:
		case PdeTokenTypes.SEMI:
		case PdeTokenTypes.LITERAL_this:
		case PdeTokenTypes.LITERAL_super:
			sb.append(String.format(FORMAT_KEYWORDS,
					getSanitizedText(ast)));
			dumpHiddenAfter(ast);
			break;

		case PdeTokenTypes.EMPTY_STAT:
		case PdeTokenTypes.EMPTY_FIELD:
			break;

		case PdeTokenTypes.LITERAL_continue:
		case PdeTokenTypes.LITERAL_break:
			sb.append(String.format(FORMAT_KEYWORDS,
					getSanitizedText(ast)));
			dumpHiddenAfter(ast);
			if (child1 != null) {// maybe label
				doConvert(child1);
			}
			break;

		// yuck: Distinguish between "import x.y.*" and "x = 1 * 3"
		case PdeTokenTypes.STAR:
			if (hasChildren(ast)) { // the binary mult. operator
				printBinaryOperator(ast);
			} else { // the special "*" in import:
				sb.append("*");
				dumpHiddenAfter(ast);
			}
			break;

		case PdeTokenTypes.LITERAL_throws:
			sb.append(String.format(FORMAT_KEYWORDS, "throws"));
			dumpHiddenAfter(ast);
			printChildren(ast);
			break;

		case PdeTokenTypes.LITERAL_if:
			printIfThenElse(ast);
			break;

		case PdeTokenTypes.LITERAL_while:
			sb.append(String.format(FORMAT_KEYWORDS, "while"));
			dumpHiddenAfter(ast);
			printChildren(ast);
			break;

		case PdeTokenTypes.LITERAL_do:
			sb.append(String.format(FORMAT_KEYWORDS, "do"));
			dumpHiddenAfter(ast);
			doConvert(child1); // an SLIST
			sb.append(String.format(FORMAT_KEYWORDS, "while"));
			dumpHiddenBefore(getBestPrintableNode(child2, false));
			doConvert(child2); // an EXPR
			break;

		case PdeTokenTypes.LITERAL_try:
			sb.append(String.format(FORMAT_KEYWORDS, "try"));
			dumpHiddenAfter(ast);
			printChildren(ast);
			break;

		case PdeTokenTypes.LITERAL_catch:
			sb.append(String.format(FORMAT_KEYWORDS, "catch"));
			dumpHiddenAfter(ast);
			printChildren(ast);
			break;

		// the first child is the "try" and the second is the SLIST
		case PdeTokenTypes.LITERAL_finally:
			sb.append(String.format(FORMAT_KEYWORDS, "finally"));
			dumpHiddenAfter(ast);
			printChildren(ast);
			break;

		case PdeTokenTypes.LITERAL_throw:
			sb.append(String.format(FORMAT_KEYWORDS, "throw"));
			dumpHiddenAfter(ast);
			doConvert(child1);
			break;

		// the dreaded trinary operator
		case PdeTokenTypes.QUESTION:
			doConvert(child1);
			sb.append("?");
			dumpHiddenAfter(ast);
			doConvert(child2);
			doConvert(child3);
			break;

		// pde specific or modified tokens start here

		// Image -> BImage, Font -> BFont as appropriate
		case PdeTokenTypes.IDENT:
			sb.append(getSanitizedText(ast));
			dumpHiddenAfter(ast);
			break;

		// the color datatype is just an alias for int
		case PdeTokenTypes.LITERAL_color:
			// sb.append("int");
			sb.append(String.format(FORMAT_KEYWORDS, "color"));
			dumpHiddenAfter(ast);
			break;

		case PdeTokenTypes.WEBCOLOR_LITERAL:
			sb.append(ast.getText());
			/*
			if (ast.getText().length() != 6) {
				System.err
						.println("Internal error: incorrect length of webcolor "
								+ "literal should have been detected sooner.");
				break;
			}
			sb.append("0xff" + ast.getText());
			*/
			dumpHiddenAfter(ast);
			break;

		// allow for stuff like int(43.2).
		case PdeTokenTypes.CONSTRUCTOR_CAST:
			final AST terminalTypeNode = child1.getFirstChild();
			final AST exprToCast = child2;
			final String pooType = terminalTypeNode.getText();
			/*
			sb.append("PApplet.parse"
					+ Character.toUpperCase(pooType.charAt(0))
					+ pooType.substring(1));
			*/
			sb.append(pooType);
			dumpHiddenAfter(terminalTypeNode); // the left paren
			doConvert(exprToCast);
			break;

		// making floating point literals default to floats, not doubles
		case PdeTokenTypes.NUM_DOUBLE:
			final String literalDouble = ast.getText(); // .toLowerCase();
			sb.append(literalDouble);
			dumpHiddenAfter(ast);
			break;

		case PdeTokenTypes.TYPE_ARGUMENTS:
		case PdeTokenTypes.TYPE_PARAMETERS:
			printChildren(ast);
			break;

		case PdeTokenTypes.TYPE_ARGUMENT:
		case PdeTokenTypes.TYPE_PARAMETER:
			printChildren(ast);
			break;

		case PdeTokenTypes.WILDCARD_TYPE:
			sb.append(getSanitizedText(ast));
			dumpHiddenAfter(ast);
			doConvert(ast.getFirstChild());
			break;

		case PdeTokenTypes.TYPE_LOWER_BOUNDS:
		case PdeTokenTypes.TYPE_UPPER_BOUNDS:
			sb.append(
					String.format(FORMAT_KEYWORDS,
					ast.getType() == PdeTokenTypes.TYPE_LOWER_BOUNDS ?
							"super" : "extends"));
			dumpHiddenBefore(getBestPrintableNode(ast, false));
			printChildren(ast);
			break;

		case PdeTokenTypes.ANNOTATION:
			sb.append("@");
			printChildren(ast);
			break;

		case PdeTokenTypes.ANNOTATION_ARRAY_INIT:
			printChildren(ast);
			break;

		case PdeTokenTypes.ANNOTATION_MEMBER_VALUE_PAIR:
			doConvert(ast.getFirstChild());
			sb.append("=");
			dumpHiddenBefore(getBestPrintableNode(ast.getFirstChild()
					.getNextSibling(), false));
			doConvert(ast.getFirstChild().getNextSibling());
			break;

		default:
			System.err.println("Unrecognized type:" + ast.getType() + " ("
					+ TokenUtil.nameOf(ast) + ")");
			break;
		}
	}

	private boolean handlePicodeMethodCall(AST dot, AST elist, StringBuilder sb) {
		if (dot.getType() == PdeTokenTypes.DOT &&
				elist.getType() == PdeTokenTypes.ELIST) {
			AST className = dot.getFirstChild();
			AST methodName = className.getNextSibling();
			if ("Picode".equals(className.getText())) {
				if ("pose".equals(methodName.getText())) {
					if (elist.getNumberOfChildren() == 1) {
						AST parameterExpression = elist.getFirstChild();
						if (parameterExpression.getNumberOfChildren() == 1) {
							AST poseFileName = parameterExpression.getFirstChild();
							if (poseFileName.getType() == PdeTokenTypes.STRING_LITERAL) {
								String fileName = poseFileName.getText();
								fileName = fileName.substring(1, fileName.length() - 1);
								String imageTag = String.format("<a href=\"%s%s.txt\"><img src=\"%s%s.jpg\" width=\"160\" border=\"1\" /></a>",
										rootPath,
										fileName,
										rootPath,
										fileName);
								// toHTML(dot); // "Picode.pose("
								// dumpHiddenBefore(poseFileName); // "("

								sb.append(imageTag);

								StringBuilder tmp = sb;
								sb = new StringBuilder();
								dumpHiddenAfter(poseFileName); // "))) }\n   "
								int i = sb.indexOf(")");
								tmp.append(sb.substring(i >= 0 ? i+1 : 0));
								sb = tmp;
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	public static String getSanitizedText(String text) {
		return text
				.replace("&", "&amp;")
				.replace("\"", "&quot;")
				.replace(">", "&gt;")
				.replace("<", "&lt;")
				.replace("\n", "<br>")
				.replace(" ", "&nbsp;");
	}

	private String getSanitizedText(AST ast) {
		return getSanitizedText(ast.getText());
	}

	/**
	 * Find a child of the given AST that has the given type
	 *
	 * @returns a child AST of the given type. If it can't find a child of the
	 *          given type, return null.
	 */
	private AST getChild(final AST ast, final int childType) {
		AST child = ast.getFirstChild();
		while (child != null) {
			if (child.getType() == childType) {
				// debug.println("getChild: found:" + name(ast));
				return child;
			}
			child = child.getNextSibling();
		}
		return null;
	}

	/**
	 * Tells whether an AST has any children or not.
	 *
	 * @return true iff the AST has at least one child
	 */
	private boolean hasChildren(final AST ast) {
		return (ast.getFirstChild() != null);
	}

	/**
	 * Gets the best node in the subtree for printing. This really means the
	 * next node which could potentially have hiddenBefore data. It's usually
	 * the first printable leaf, but not always.
	 *
	 * @param includeThisNode
	 *            Should this node be included in the search? If false, only
	 *            descendants are searched.
	 *
	 * @return the first printable leaf node in an AST
	 */
	private AST getBestPrintableNode(final AST ast,
			final boolean includeThisNode) {
		AST child;

		if (includeThisNode) {
			child = ast;
		} else {
			child = ast.getFirstChild();
		}

		if (child != null) {

			switch (child.getType()) {

			// the following node types are printing nodes that print before
			// any children, but then also recurse over children. So they
			// may have hiddenBefore chains that need to be printed first. Many
			// statements and all unary expression types qualify. Return these
			// nodes directly
			case PdeTokenTypes.CLASS_DEF:
			case PdeTokenTypes.LITERAL_if:
			case PdeTokenTypes.LITERAL_new:
			case PdeTokenTypes.LITERAL_for:
			case PdeTokenTypes.LITERAL_while:
			case PdeTokenTypes.LITERAL_do:
			case PdeTokenTypes.LITERAL_break:
			case PdeTokenTypes.LITERAL_continue:
			case PdeTokenTypes.LITERAL_return:
			case PdeTokenTypes.LITERAL_switch:
			case PdeTokenTypes.LITERAL_try:
			case PdeTokenTypes.LITERAL_throw:
			case PdeTokenTypes.LITERAL_synchronized:
			case PdeTokenTypes.LITERAL_assert:
			case PdeTokenTypes.BNOT:
			case PdeTokenTypes.LNOT:
			case PdeTokenTypes.INC:
			case PdeTokenTypes.DEC:
			case PdeTokenTypes.UNARY_MINUS:
			case PdeTokenTypes.UNARY_PLUS:
				return child;

				// Some non-terminal node types (at the moment, I only know of
				// MODIFIERS, but there may be other such types), can be
				// leaves in the tree but not have any children. If this is
				// such a node, move on to the next sibling.
			case PdeTokenTypes.MODIFIERS:
				if (child.getFirstChild() == null) {
					return getBestPrintableNode(child.getNextSibling(), false);
				}
				// new jikes doesn't like fallthrough, so just duplicated here:
				return getBestPrintableNode(child, false);

			default:
				return getBestPrintableNode(child, false);
			}
		}

		return ast;
	}

	/**
	 * Dump the list of hidden tokens linked to before the AST node passed in.
	 * The only time hidden tokens need to be dumped with this function is when
	 * dealing parts of the tree where automatic tree construction was turned
	 * off with the ! operator in the grammar file and the nodes were manually
	 * constructed in such a way that the usual tokens don't have the necessary
	 * hiddenAfter links.
	 */
	private void dumpHiddenBefore(final AST ast) {
		antlr.CommonHiddenStreamToken child = null, parent = ((CommonASTWithHiddenTokens) ast)
				.getHiddenBefore();
		if (parent == null) {
			return;
		}

		// traverse back to the head of the list of tokens before this node
		do {
			child = parent;
			parent = child.getHiddenBefore();
		} while (parent != null);

		// dump that list
		dumpHiddenTokens(child);
	}

	/**
	 * Dump the list of hidden tokens linked to after the AST node passed in.
	 * Most hidden tokens are dumped from this function.
	 */
	private void dumpHiddenAfter(final AST ast) {
		if (!(ast instanceof antlr.CommonASTWithHiddenTokens)) {
			return;
		}
		dumpHiddenTokens(((antlr.CommonASTWithHiddenTokens) ast)
				.getHiddenAfter());
	}

	/**
	 * Dump the list of hidden tokens linked to from the token passed in.
	 */
	private void dumpHiddenTokens(CommonHiddenStreamToken t) {
		for (; t != null; t = pp.getHiddenAfter(t)) {
			switch (t.getType()) {
			case PdeTokenTypes.SL_COMMENT:
			case PdeTokenTypes.ML_COMMENT:
				sb.append(String.format(
						FORMAT_COMMENT,
						getSanitizedText(t.getText())));
				break;
			default:
				sb.append(getSanitizedText(t.getText()));
				break;
			}
		}
	}

	/**
	 * Print the children of the given AST
	 *
	 * @param ast
	 *            The AST to print
	 * @returns true iff anything was printed
	 */
	private boolean printChildren(AST ast) {
		boolean ret = false;
		AST child = ast.getFirstChild();
		while (child != null) {
			ret = true;
			doConvert(child);
			child = child.getNextSibling();
		}
		return ret;
	}

	private void printBinaryOperator(AST ast) {
		doConvert(ast.getFirstChild());
		if (!PdeEmitter.OTHER_COPIED_TOKENS.get(ast.getType())) {
			sb.append(getSanitizedText(ast));
			dumpHiddenAfter(ast);
		}
		doConvert(ast.getFirstChild().getNextSibling());
	}

	private void printIfThenElse(AST literalIf) {
		sb.append(String.format(FORMAT_KEYWORDS,
				literalIf.getText()));
		dumpHiddenAfter(literalIf);

		final AST condition = literalIf.getFirstChild();
		doConvert(condition); // the "if" condition: an EXPR

		// the "then" clause is either an SLIST or an EXPR
		final AST thenPath = condition.getNextSibling();
		doConvert(thenPath);

		// optional "else" clause: an SLIST or an EXPR
		// what could be simpler?
		final AST elsePath = thenPath.getNextSibling();
		if (elsePath != null) {
			sb.append(String.format(FORMAT_KEYWORDS, "else"));
			final AST bestPrintableNode = getBestPrintableNode(elsePath, true);
			dumpHiddenBefore(bestPrintableNode);
			final CommonHiddenStreamToken hiddenBefore = ((CommonASTWithHiddenTokens) elsePath)
					.getHiddenBefore();
			if (elsePath.getType() == PdeTokenTypes.SLIST
					&& elsePath.getNumberOfChildren() == 0
					&& hiddenBefore == null) {
				sb.append("{");
				final CommonHiddenStreamToken hiddenAfter = ((CommonASTWithHiddenTokens) elsePath)
						.getHiddenAfter();
				if (hiddenAfter == null) {
					sb.append("}");
				} else {
					dumpHiddenTokens(hiddenAfter);
				}
			} else {
				doConvert(elsePath);
			}
		}
	}
}
