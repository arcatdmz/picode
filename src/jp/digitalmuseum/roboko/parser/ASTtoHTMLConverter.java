package jp.digitalmuseum.roboko.parser;

import java.io.FileWriter;
import java.io.IOException;

import jp.digitalmuseum.roboko.ProcessingIntegration;
import jp.digitalmuseum.roboko.RobokoMain;
import jp.digitalmuseum.roboko.RobokoSettings;
import processing.app.RobokoSketch;
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
		RobokoSketch sketch = null;
		try {
			sketch = RobokoSettings.getDefaultSketch();
			PdeParser parser = new PdeParser(sketch);
			AST ast = parser.parse(0);
			FileWriter writer = new FileWriter(sketch.getMainFilePath() + ".html");
			writer.append(
					new ASTtoHTMLConverter(parser.getPreprocessor()).convert(ast));
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SketchException se) {
			System.err.println(RobokoMain.getErrorString(sketch, se));
		}
	}

	public String convert(AST ast) {
		this.sb = new StringBuilder();
		sb.append("<html><head><title></title></head><body>");
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

		case PdeWalker.PACKAGE_DEF:
			sb.append(String.format(FORMAT_KEYWORDS, "package"));
			dumpHiddenAfter(ast);
			doConvert(ast.getFirstChild());
			break;

		// IMPORT has exactly one child
		case PdeWalker.IMPORT:
			sb.append(String.format(FORMAT_KEYWORDS, "import"));
			dumpHiddenAfter(ast);
			doConvert(ast.getFirstChild());
			break;

		case PdeWalker.STATIC_IMPORT:
			sb.append(String.format(FORMAT_KEYWORDS, "import static"));
			dumpHiddenAfter(ast);
			doConvert(ast.getFirstChild());
			break;

		case PdeWalker.CLASS_DEF:
		case PdeWalker.INTERFACE_DEF:
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

		case PdeWalker.EXTENDS_CLAUSE:
			if (hasChildren(ast)) {
				sb.append(String.format(FORMAT_KEYWORDS, "extends"));
				dumpHiddenBefore(getBestPrintableNode(ast, false));
				printChildren(ast);
			}
			break;

		case PdeWalker.IMPLEMENTS_CLAUSE:
			if (hasChildren(ast)) {
				sb.append(String.format(FORMAT_KEYWORDS, "implements"));
				dumpHiddenBefore(getBestPrintableNode(ast, false));
				printChildren(ast);
			}
			break;

		// DOT always has exactly two children.
		case PdeWalker.DOT:
			doConvert(child1);
			sb.append(".");
			dumpHiddenAfter(ast);
			doConvert(child2);
			break;

		case PdeWalker.METHOD_CALL:
			if (handleRobokoMethodCall(child1, child2, sb)) {
				break;
			}
		case PdeWalker.MODIFIERS:
		case PdeWalker.OBJBLOCK:
		case PdeWalker.CTOR_DEF:
			// case PdeWalker.METHOD_DEF:
		case PdeWalker.PARAMETERS:
		case PdeWalker.PARAMETER_DEF:
		case PdeWalker.VARIABLE_PARAMETER_DEF:
		case PdeWalker.VARIABLE_DEF:
		case PdeWalker.TYPE:
		case PdeWalker.SLIST:
		case PdeWalker.ELIST:
		case PdeWalker.ARRAY_DECLARATOR:
		case PdeWalker.TYPECAST:
		case PdeWalker.EXPR:
		case PdeWalker.ARRAY_INIT:
		case PdeWalker.FOR_INIT:
		case PdeWalker.FOR_CONDITION:
		case PdeWalker.FOR_ITERATOR:
		case PdeWalker.INSTANCE_INIT:
		case PdeWalker.INDEX_OP:
		case PdeWalker.SUPER_CTOR_CALL:
		case PdeWalker.CTOR_CALL:
		case PdeWalker.METHOD_DEF:
			printChildren(ast);
			break;

		// if we have two children, it's of the form "a=0"
		// if just one child, it's of the form "=0" (where the
		// lhs is above this AST).
		case PdeWalker.ASSIGN:
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
		case PdeWalker.PLUS:
		case PdeWalker.MINUS:
		case PdeWalker.DIV:
		case PdeWalker.MOD:
		case PdeWalker.NOT_EQUAL:
		case PdeWalker.EQUAL:
		case PdeWalker.LE:
		case PdeWalker.GE:
		case PdeWalker.LOR:
		case PdeWalker.LAND:
		case PdeWalker.BOR:
		case PdeWalker.BXOR:
		case PdeWalker.BAND:
		case PdeWalker.SL:
		case PdeWalker.SR:
		case PdeWalker.BSR:
		case PdeWalker.LITERAL_instanceof:
		case PdeWalker.PLUS_ASSIGN:
		case PdeWalker.MINUS_ASSIGN:
		case PdeWalker.STAR_ASSIGN:
		case PdeWalker.DIV_ASSIGN:
		case PdeWalker.MOD_ASSIGN:
		case PdeWalker.SR_ASSIGN:
		case PdeWalker.BSR_ASSIGN:
		case PdeWalker.SL_ASSIGN:
		case PdeWalker.BAND_ASSIGN:
		case PdeWalker.BXOR_ASSIGN:
		case PdeWalker.BOR_ASSIGN:

		case PdeWalker.LT:
		case PdeWalker.GT:
			printBinaryOperator(ast);
			break;

		case PdeWalker.LITERAL_for:
			sb.append(String.format(FORMAT_KEYWORDS, "for"));
			dumpHiddenAfter(ast);
			if (child1.getType() == PdeTokenTypes.FOR_EACH_CLAUSE) {
				printChildren(child1);
				doConvert(child2);
			} else {
				printChildren(ast);
			}
			break;

		case PdeWalker.POST_INC:
		case PdeWalker.POST_DEC:
			doConvert(child1);
			sb.append(getSanitizedText(ast));
			dumpHiddenAfter(ast);
			break;

		// unary operators:
		case PdeWalker.BNOT:
		case PdeWalker.LNOT:
		case PdeWalker.INC:
		case PdeWalker.DEC:
		case PdeWalker.UNARY_MINUS:
		case PdeWalker.UNARY_PLUS:
			sb.append(getSanitizedText(ast));
			dumpHiddenAfter(ast);
			doConvert(child1);
			break;

		case PdeWalker.LITERAL_new:
			sb.append(String.format(FORMAT_KEYWORDS, "new"));
			dumpHiddenAfter(ast);
			printChildren(ast);
			break;

		case PdeWalker.LITERAL_return:
			sb.append(String.format(FORMAT_KEYWORDS, "return"));
			dumpHiddenAfter(ast);
			doConvert(child1);
			break;

		case PdeWalker.STATIC_INIT:
			sb.append(String.format(FORMAT_KEYWORDS, "static"));
			dumpHiddenBefore(getBestPrintableNode(ast, false));
			doConvert(child1);
			break;

		case PdeWalker.LITERAL_switch:
			sb.append(String.format(FORMAT_KEYWORDS, "switch"));
			dumpHiddenAfter(ast);
			printChildren(ast);
			break;

		case PdeWalker.LABELED_STAT:
		case PdeWalker.CASE_GROUP:
			printChildren(ast);
			break;

		case PdeWalker.LITERAL_case:
			sb.append(String.format(FORMAT_KEYWORDS, "case"));
			dumpHiddenAfter(ast);
			printChildren(ast);
			break;

		case PdeWalker.LITERAL_default:
			sb.append(String.format(FORMAT_KEYWORDS, "default"));
			dumpHiddenAfter(ast);
			printChildren(ast);
			break;

		case PdeWalker.NUM_INT:
		case PdeWalker.CHAR_LITERAL:
		case PdeWalker.STRING_LITERAL:
		case PdeWalker.NUM_FLOAT:
		case PdeWalker.NUM_LONG:
			sb.append(getSanitizedText(ast));
			dumpHiddenAfter(ast);
			break;

		case PdeWalker.LITERAL_synchronized: // 0137 to fix bug #136
		case PdeWalker.LITERAL_assert:
			sb.append(String.format(FORMAT_KEYWORDS,
					getSanitizedText(ast)));
			dumpHiddenAfter(ast);
			printChildren(ast);
			break;

		case PdeWalker.LITERAL_private:
		case PdeWalker.LITERAL_public:
		case PdeWalker.LITERAL_protected:
		case PdeWalker.LITERAL_static:
		case PdeWalker.LITERAL_transient:
		case PdeWalker.LITERAL_native:
		case PdeWalker.LITERAL_threadsafe:
			// case PdeWalker.LITERAL_synchronized: // 0137 to fix bug #136
		case PdeWalker.LITERAL_volatile:
		case PdeWalker.LITERAL_class: // 0176 to fix bug #1466
		case PdeWalker.FINAL:
		case PdeWalker.ABSTRACT:
		case PdeWalker.LITERAL_package:
		case PdeWalker.LITERAL_void:
		case PdeWalker.LITERAL_boolean:
		case PdeWalker.LITERAL_byte:
		case PdeWalker.LITERAL_char:
		case PdeWalker.LITERAL_short:
		case PdeWalker.LITERAL_int:
		case PdeWalker.LITERAL_float:
		case PdeWalker.LITERAL_long:
		case PdeWalker.LITERAL_double:
		case PdeWalker.LITERAL_true:
		case PdeWalker.LITERAL_false:
		case PdeWalker.LITERAL_null:
		case PdeWalker.SEMI:
		case PdeWalker.LITERAL_this:
		case PdeWalker.LITERAL_super:
			sb.append(String.format(FORMAT_KEYWORDS,
					getSanitizedText(ast)));
			dumpHiddenAfter(ast);
			break;

		case PdeWalker.EMPTY_STAT:
		case PdeWalker.EMPTY_FIELD:
			break;

		case PdeWalker.LITERAL_continue:
		case PdeWalker.LITERAL_break:
			sb.append(String.format(FORMAT_KEYWORDS,
					getSanitizedText(ast)));
			dumpHiddenAfter(ast);
			if (child1 != null) {// maybe label
				doConvert(child1);
			}
			break;

		// yuck: Distinguish between "import x.y.*" and "x = 1 * 3"
		case PdeWalker.STAR:
			if (hasChildren(ast)) { // the binary mult. operator
				printBinaryOperator(ast);
			} else { // the special "*" in import:
				sb.append("*");
				dumpHiddenAfter(ast);
			}
			break;

		case PdeWalker.LITERAL_throws:
			sb.append(String.format(FORMAT_KEYWORDS, "throws"));
			dumpHiddenAfter(ast);
			printChildren(ast);
			break;

		case PdeWalker.LITERAL_if:
			printIfThenElse(ast);
			break;

		case PdeWalker.LITERAL_while:
			sb.append(String.format(FORMAT_KEYWORDS, "while"));
			dumpHiddenAfter(ast);
			printChildren(ast);
			break;

		case PdeWalker.LITERAL_do:
			sb.append(String.format(FORMAT_KEYWORDS, "do"));
			dumpHiddenAfter(ast);
			doConvert(child1); // an SLIST
			sb.append(String.format(FORMAT_KEYWORDS, "while"));
			dumpHiddenBefore(getBestPrintableNode(child2, false));
			doConvert(child2); // an EXPR
			break;

		case PdeWalker.LITERAL_try:
			sb.append(String.format(FORMAT_KEYWORDS, "try"));
			dumpHiddenAfter(ast);
			printChildren(ast);
			break;

		case PdeWalker.LITERAL_catch:
			sb.append(String.format(FORMAT_KEYWORDS, "catch"));
			dumpHiddenAfter(ast);
			printChildren(ast);
			break;

		// the first child is the "try" and the second is the SLIST
		case PdeWalker.LITERAL_finally:
			sb.append(String.format(FORMAT_KEYWORDS, "finally"));
			dumpHiddenAfter(ast);
			printChildren(ast);
			break;

		case PdeWalker.LITERAL_throw:
			sb.append(String.format(FORMAT_KEYWORDS, "throw"));
			dumpHiddenAfter(ast);
			doConvert(child1);
			break;

		// the dreaded trinary operator
		case PdeWalker.QUESTION:
			doConvert(child1);
			sb.append("?");
			dumpHiddenAfter(ast);
			doConvert(child2);
			doConvert(child3);
			break;

		// pde specific or modified tokens start here

		// Image -> BImage, Font -> BFont as appropriate
		case PdeWalker.IDENT:
			sb.append(getSanitizedText(ast));
			dumpHiddenAfter(ast);
			break;

		// the color datatype is just an alias for int
		case PdeWalker.LITERAL_color:
			// sb.append("int");
			sb.append(String.format(FORMAT_KEYWORDS, "color"));
			dumpHiddenAfter(ast);
			break;

		case PdeWalker.WEBCOLOR_LITERAL:
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
		case PdeWalker.CONSTRUCTOR_CAST:
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
		case PdeWalker.NUM_DOUBLE:
			final String literalDouble = ast.getText(); // .toLowerCase();
			sb.append(literalDouble);
			dumpHiddenAfter(ast);
			break;

		case PdeWalker.TYPE_ARGUMENTS:
		case PdeWalker.TYPE_PARAMETERS:
			printChildren(ast);
			break;

		case PdeWalker.TYPE_ARGUMENT:
		case PdeWalker.TYPE_PARAMETER:
			printChildren(ast);
			break;

		case PdeWalker.WILDCARD_TYPE:
			sb.append(getSanitizedText(ast));
			dumpHiddenAfter(ast);
			doConvert(ast.getFirstChild());
			break;

		case PdeWalker.TYPE_LOWER_BOUNDS:
		case PdeWalker.TYPE_UPPER_BOUNDS:
			sb.append(
					String.format(FORMAT_KEYWORDS,
					ast.getType() == PdeWalker.TYPE_LOWER_BOUNDS ?
							"super" : "extends"));
			dumpHiddenBefore(getBestPrintableNode(ast, false));
			printChildren(ast);
			break;

		case PdeWalker.ANNOTATION:
			sb.append("@");
			printChildren(ast);
			break;

		case PdeWalker.ANNOTATION_ARRAY_INIT:
			printChildren(ast);
			break;

		case PdeWalker.ANNOTATION_MEMBER_VALUE_PAIR:
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

	private boolean handleRobokoMethodCall(AST dot, AST elist, StringBuilder sb) {
		if (dot.getType() == PdeWalker.DOT &&
				elist.getType() == PdeWalker.ELIST) {
			AST className = dot.getFirstChild();
			AST methodName = className.getNextSibling();
			if ("Roboko".equals(className.getText())) {
				if ("pose".equals(methodName.getText())) {
					if (elist.getNumberOfChildren() == 1) {
						AST parameterExpression = elist.getFirstChild();
						if (parameterExpression.getNumberOfChildren() == 1) {
							AST poseFileName = parameterExpression.getFirstChild();
							if (poseFileName.getType() == PdeWalker.STRING_LITERAL) {
								String fileName = poseFileName.getText();
								fileName = fileName.substring(1, fileName.length() - 1);
								String imageTag = String.format("<img src=\"%s/%s.thumb.png\" border=\"1\" align=\"middle\">",
										RobokoSettings.getPoseFolderURL(),
										fileName);
								// toHTML(dot); // "Roboko.pose("
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
			case PdeWalker.SL_COMMENT:
			case PdeWalker.ML_COMMENT:
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
