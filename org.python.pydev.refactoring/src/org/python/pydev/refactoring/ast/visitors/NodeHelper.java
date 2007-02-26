package org.python.pydev.refactoring.ast.visitors;

import java.util.ArrayList;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.SpecialStr;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.BoolOp;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.Comprehension;
import org.python.pydev.parser.jython.ast.Dict;
import org.python.pydev.parser.jython.ast.For;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.If;
import org.python.pydev.parser.jython.ast.Import;
import org.python.pydev.parser.jython.ast.ImportFrom;
import org.python.pydev.parser.jython.ast.Lambda;
import org.python.pydev.parser.jython.ast.List;
import org.python.pydev.parser.jython.ast.Module;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.Str;
import org.python.pydev.parser.jython.ast.TryExcept;
import org.python.pydev.parser.jython.ast.TryFinally;
import org.python.pydev.parser.jython.ast.Tuple;
import org.python.pydev.parser.jython.ast.While;
import org.python.pydev.parser.jython.ast.With;
import org.python.pydev.parser.jython.ast.argumentsType;
import org.python.pydev.parser.jython.ast.decoratorsType;
import org.python.pydev.parser.jython.ast.exprType;
import org.python.pydev.parser.jython.ast.keywordType;

public class NodeHelper {

	private static final String KEYWORD_FDEL = "fdel";

	private static final String KEYWORD_FSET = "fset";

	private static final String KEYWORD_FGET = "fget";

	private static final String EMPTY = "";

	public static final String KEYWORD_INIT = "__init__";

	private static final String KEYWORD_NONE = "None";

	private static final String KEYWORD_PROPERTY = "property";

	public static final String KEYWORD_SELF = "self";

	private static final String PREFIX_PRIVATE = "__";

	public static final int ACCESS_PUBLIC = 1;

	public static final int ACCESS_PSEUDO = 2;

	public static final int ACCESS_PRIVATE = 3;

	public String getAccessName(String name, int modifier) {
		switch (modifier) {
		case ACCESS_PSEUDO:
			return getPseudoAttr(name);
		case ACCESS_PRIVATE:
			return getPrivateAttr(name);
		default:
			return getPublicAttr(name);
		}
	}

	private String getFromName(SimpleNode node) {
		if (!isName(node))
			return EMPTY;
		return ((Name) node).id;
	}

	private String getFromNameTok(SimpleNode node) {
		if (node == null)
			return "";
		if (!isNameTok(node))
			return EMPTY;
		return ((NameTok) node).id;
	}

	private String getFromStr(SimpleNode node) {
		if (!isStr(node))
			return EMPTY;
		return ((Str) node).s;
	}

	public int getLineDefinition(SimpleNode node) {
		while (isAttribute(node)) {
			exprType expr = ((Attribute) node).value;
			if (!(isCall(expr))) {
				node = expr;
			} else {
				break;
			}
		}
		if (isFunctionDef(node)) {
			return ((FunctionDef) node).name.beginLine;
		}
		return node.beginLine;
	}

	public int getLineEnd(SimpleNode node) {
		if (node == null)
			return 0;
		if (isStr(node)) {
			String s = ((Str) node).s;
			int found = 0;
			for (int i = 0; i < s.length(); i++) {

				if (s.charAt(i) == '\n') {
					found += 1;
				}
			}
			return getLineDefinition(node) + found;
		}
		return getLineDefinition(node);
	}

	public String getName(SimpleNode node) {
		if (isNameTok(node)) {
			return getFromNameTok(node);
		} else if (isName(node)) {
			return getFromName(node);
		} else if (isStr(node)) {
			return getFromStr(node);
		} else if (isClassDef(node)) {
			return getName(((ClassDef) node).name);
		} else if (isFunctionDef(node)) {
			return getName(((FunctionDef) node).name);
		} else if (isCall(node))
			return getName(((Call) node).func);
		else if (isAttribute(node)) {
			String attributeName = VisitorFactory.createSourceFromAST(node,
					true);
			int subscriptOffset = attributeName.indexOf("[");
			if (subscriptOffset > 0)
				attributeName = attributeName.substring(0, subscriptOffset - 1);

			return attributeName.trim();
		}

		return EMPTY;
	}

	public String getPrivateAttr(String attributeName) {
		String privateAttr = getPublicAttr(attributeName);
		return "__" + privateAttr;
	}

	private int getPropertyMethods(exprType[] args) {
		if (args == null || args.length == 0)
			return 0;

		int propertyMethods = args.length;
		exprType lastExpr = args[args.length - 1];
		if (isStr(lastExpr)) {
			propertyMethods -= 1;
		}
		return propertyMethods;
	}

	public String getPseudoAttr(String attributeName) {
		String pseudoAttr = getPublicAttr(attributeName);
		return "_" + pseudoAttr;
	}

	public String getPublicAttr(String attributeName) {
		String publicAttr = attributeName;
		while (publicAttr.startsWith("_")) {
			publicAttr = publicAttr.substring(1);
		}
		return publicAttr;
	}

	public int getStartLine(SimpleNode n) {
		return n.beginLine;
	}

	public int getStartOffset(SimpleNode n) {
		return n.beginColumn;
	}

	public boolean hasFunctionArgument(SimpleNode node) {
		return isFunctionDef(node) || isLambda(node);
	}

	public boolean isAssign(SimpleNode node) {
		return node instanceof Assign;
	}

	public boolean isAttribute(SimpleNode node) {
		return node instanceof Attribute;
	}

	public boolean isBoolOp(SimpleNode node) {
		return node instanceof BoolOp;
	}

	public boolean isCall(SimpleNode node) {
		return node instanceof Call;
	}

	public boolean isClassDef(SimpleNode node) {
		return node instanceof ClassDef;
	}

	public boolean isComprehension(SimpleNode node) {
		return node instanceof Comprehension;
	}

	public boolean isConstant(String id) {
		return id.toUpperCase().compareTo(id) == 0;
	}

	public boolean isContextNameParentName(String contextName, SimpleNode node) {
		return contextName.compareTo(getName(node)) == 0;
	}

	public boolean isControlStatement(SimpleNode node) {
		return isForStatement(node) || isWhileStatement(node)
				|| isWithStatement(node) || isTryExceptStatement(node)
				|| isTryFinallyStatement(node) || isIfStatement(node);
	}

	public boolean isDict(SimpleNode node) {
		return node instanceof Dict;
	}

	public boolean isEmptyList(SimpleNode[] list) {
		return ((list == null) || (list.length == 0));
	}

	public boolean isFDel(keywordType kw) {
		return isKeywordName(kw, KEYWORD_FDEL) && (!(isNone(kw.value)));
	}

	public boolean isFGet(keywordType kw) {
		return isKeywordName(kw, KEYWORD_FGET) && (!(isNone(kw.value)));
	}

	public boolean isFilledList(SimpleNode[] list) {
		return (!isEmptyList(list));
	}

	public boolean isForStatement(SimpleNode node) {
		return node instanceof For;
	}

	public boolean isFSet(keywordType kw) {
		return isKeywordName(kw, KEYWORD_FSET) && (!(isNone(kw.value)));
	}

	public boolean isFullyQualified(SimpleNode contextNode, SimpleNode parent) {
		return (isContextNameParentName(getName(contextNode), parent) || isSelf(getName(contextNode)));
	}

	public boolean isFunctionArgument(SimpleNode node) {
		return node instanceof argumentsType;
	}

	public boolean isFunctionDef(SimpleNode node) {
		return node instanceof FunctionDef;
	}

	public boolean isFunctionOrClassDef(SimpleNode node) {
		return isClassDef(node) || isFunctionDef(node);
	}

	public boolean isIfStatement(SimpleNode node) {
		return node instanceof If;
	}

	public boolean isInit(SimpleNode node) {
		return isFunctionDef(node)
				&& (getName(node).compareTo(KEYWORD_INIT) == 0);
	}

	public boolean isKeyword(SimpleNode node) {
		return node instanceof keywordType;
	}

	private boolean isKeywordName(keywordType kw, String name) {
		return (getName(kw.arg).compareTo(name) == 0) && isName(kw.value);
	}

	public boolean isKeywordStr(keywordType kw) {
		return (getName(kw.arg).compareTo("doc") == 0) && isStr(kw.value);
	}

	public boolean isLambda(SimpleNode node) {
		return node instanceof Lambda;
	}

	public boolean isList(SimpleNode node) {
		return node instanceof List;
	}

	public boolean isName(SimpleNode node) {
		return node instanceof Name;
	}

	public boolean isNameTok(SimpleNode node) {
		return node instanceof NameTok;
	}

	public boolean isNone(SimpleNode node) {
		return (isName(node) && KEYWORD_NONE.compareTo(getName(node)) == 0);
	}

	public boolean isPrivate(SimpleNode node) {
		return isPrivate(getName(node));
	}

	public boolean isPrivate(String name) {
		return name.startsWith(PREFIX_PRIVATE);
	}

	public boolean isProperty(Call node) {
		return getName(node).compareTo(KEYWORD_PROPERTY) == 0
				&& isValidPropertyCall(node);
	}

	public boolean isPropertyAssign(Assign node) throws Exception {
		if (isFilledList(node.targets) && node.targets.length == 1) {
			if (isName(node.targets[0])) {
				if (isCall(node.value)) {
					return (isProperty((Call) node.value));
				}
			}
		}
		return false;
	}

	public boolean isPropertyDecorator(decoratorsType dec) {
		return getName(dec.func).compareTo(KEYWORD_PROPERTY) == 0;
	}

	private boolean isPropertyVar(keywordType kw) {
		return isFGet(kw) || isFSet(kw) || isFDel(kw);
	}

	public boolean isSelf(String id) {
		return KEYWORD_SELF.compareTo(id) == 0;
	}

	public boolean isSpecialStr(Object o) {
		return o instanceof SpecialStr;
	}

	public boolean isStr(SimpleNode node) {
		return node instanceof Str;
	}

	public boolean isTryExceptStatement(SimpleNode node) {
		return node instanceof TryExcept;
	}

	public boolean isTryFinallyStatement(SimpleNode node) {
		return node instanceof TryFinally;
	}

	public boolean isTryStatement(SimpleNode node) {
		return isTryExceptStatement(node) || isTryFinallyStatement(node);
	}

	public boolean isTuple(SimpleNode node) {
		return node instanceof Tuple;
	}

	private boolean isValidPropertyCall(Call node) {
		exprType[] args = node.args;
		keywordType[] kws = node.keywords;
		int len = args.length + kws.length;

		if (args == null || len > 4)
			return false;

		return validatePropertyArguments(node);

	}

	private boolean isValidPropertyKeyword(keywordType[] keywords) {
		if (keywords != null) {

			boolean valid = false;

			for (keywordType kw : keywords) {
				valid = isKeywordStr(kw) || isPropertyVar(kw);
				if (!(valid))
					return false;
			}
			return true;
		}

		return false;

	}

	public boolean isWhileStatement(SimpleNode node) {
		return node instanceof While;
	}

	public boolean isWithStatement(SimpleNode node) {
		return node instanceof With;
	}

	public boolean isImport(SimpleNode node) {
		return node instanceof ImportFrom || node instanceof Import;
	}

	private boolean validatePropertyArguments(Call node) {
		exprType[] args = node.args;

		if (isValidPropertyKeyword(node.keywords)) {

			int propertyMethods = getPropertyMethods(args);

			if (propertyMethods == 0)
				return true;
			for (int i = 0; i < propertyMethods; i++) {
				if (isName(args[i]))
					return true;
			}
		}

		return false;
	}

	public java.util.List<String> getBaseClassName(SimpleNode node) {
		java.util.List<String> base = new ArrayList<String>();
		if (isClassDef(node)) {

			ClassDef clazz = (ClassDef) node;
			if (isFilledList(clazz.bases)) {
				for (int i = 0; i < clazz.bases.length; i++) {
					base.add(getName(clazz.bases[i]));
				}
			}
		}
		return base;

	}

	public boolean hasSelfArgument(exprType[] args) {
		for (exprType arg : args) {
			if (isSelf(getName(arg)))
				return true;
		}
		return false;
	}

	public boolean isModule(SimpleNode node) {
		return node instanceof Module;
	}
}