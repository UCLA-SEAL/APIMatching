/* 
*    API Matching 
*    Copyright (C) <2015>  <Dr. Miryung Kim miryung@cs.ucla.edu>
*
*    This program is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package edu.ucsc.cse.grase.origin.parser;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import edu.ucsc.cse.grase.origin.entity.Method;
import edu.ucsc.cse.grase.origin.entity.ModifierBit;
import edu.ucsc.cse.grase.origin.entity.Signature;

public class JavaParser extends ASTVisitor {
	
	CompilationUnit currCompilationUnit;

	private Stack classNames = new Stack();

	private String currentClassNames;

	private ArrayList<Method> entityList;

	private String packageName = "";

	String currentFileName = "";

	String firstClassName;

	public static void main(String[] args) {
//		String srcDir = "e:\\mandarax_archive\\mandarax3.2\\src\\";
//		String list []= { 
//				"org\\mandarax\\xkb\\framework\\XMLAdapter4MandaraxLibFunctions.java",		
//				"org\\mandarax\\lib\\math\\IntArithmetic.java",
//				"test\\org\\mandarax\\lib\\math\\IntArithmeticTests.java",
//				"test\\org\\mandarax\\jdbc\\ResultSetMetaDataTests.java",
//				"test\\org\\mandarax\\util\\AutoFactsTest.java",
//				"org\\mandarax\\xkb\\framework\\XMLAdapter4VariableTerms.java",
//				"test\\org\\mandarax\\jdbc\\ResultSetMetaDataTests.java"
//				
//		};
//		
		String srcDir = "e:\\jfreechart_archive\\jfreechart-0.9.4\\src\\";
		String list []= { 
				"com\\jrefinery\\chart\\OverlaidVerticalCategoryPlot.java"
		};
		

		List entities = null;
		JavaParser parser = new JavaParser();
		for (int i= 0; i<list.length; i++) { 
			
			File f = new File(srcDir+list[i]);
			if (f.exists()) { 
				try {
					entities= parser.parser(f.getAbsolutePath());
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				for (int j= 0; j<entities.size(); j++){ 
					System.out.println(entities.get(j));
				}
			}else{ 
				System.out.println("file does not exist:"+f.getAbsoluteFile());
			}
			
		}
		
		
	}
	public List parser(String javaFile) throws Exception {
		entityList = new ArrayList<Method>();

		if (!new File(javaFile).exists()) {
			throw new Exception(javaFile + " does not exist!");
		}

		currentFileName = javaFile;

		CodeReader codeReader = new CodeReader(javaFile);
		ASTParser parser = ASTParser.newParser(AST.JLS2);
		parser.setSource(codeReader.buffer);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);

		// Walk AST
		unit.accept(this);

		// Show error message
		// FIXME: What should we do with the message?
		String errorMsg = getErrorMessages();
		if (getErrorMessages() != null && errorMsg.length() > 0)
			System.err.println(errorMsg);

		return entityList;
	}

	public int getErrorCount() {

		int errorCount = 0;
		// If the file has a problem
		for (int i = 0; i < currCompilationUnit.getProblems().length; i++) {
			IProblem problem = currCompilationUnit.getProblems()[i];
			if (problem.isError()) {
				errorCount++;
			}
		}
		return errorCount;
	}

	public String getErrorMessages() {
		String errorMessages = "";
		// If the file has a problem
		for (int i = 0; i < currCompilationUnit.getProblems().length; i++) {
			IProblem problem = currCompilationUnit.getProblems()[i];
			if (problem.isError()) {
				errorMessages += currentFileName + " at "
						+ problem.getSourceLineNumber() + ": "
						+ problem.getMessage() + "(" + problem.getID() + ")\n";
			}
		}
		return errorMessages;
	}

	/*
	 * @see ASTVisitor#visit(CompilationUnit)
	 */
	public boolean visit(CompilationUnit node) {
		currCompilationUnit = node;

		if (node.getPackage() != null) {
			node.getPackage().accept(this);
		}
		for (Iterator it = node.imports().iterator(); it.hasNext();) {
			ImportDeclaration d = (ImportDeclaration) it.next();
			d.accept(this);
		}
		for (Iterator it = node.types().iterator(); it.hasNext();) {
			AbstractTypeDeclaration d = (AbstractTypeDeclaration) it.next();
			d.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TypeDeclaration)
	 */
	public boolean visit(TypeDeclaration node) {
		// No interest on interface?
		if (node.isInterface()) {
			// return false;
		}

		if (firstClassName == null)
			firstClassName = node.getName().getFullyQualifiedName();

		// Invalidate current stack name
		currentClassNames = null;

		// Push the name
		classNames.push(node.getName().getFullyQualifiedName());

		for (Iterator it = node.bodyDeclarations().iterator(); it.hasNext();) {
			BodyDeclaration d = (BodyDeclaration) it.next();
			// Control what kind of node we need to look into it
			
			if (d instanceof MethodDeclaration) {
				d.accept(this);
			} else if (d instanceof TypeDeclaration) {
				d.accept(this);
			} else {
			}
		}

		// Pop the name
		currentClassNames =null;
		classNames.pop();
		
		return false;
	}

	/**
	 * @param md
	 */
	public boolean visit(PackageDeclaration pd) {
		packageName = pd.getName().getFullyQualifiedName();
		return false;
	}

	/**
	 * @param md
	 */
	public boolean visit(MethodDeclaration md) {

		if (currCompilationUnit == null)
			return false;
		int modifiers = md.getModifiers();
		ModifierBit mb = new ModifierBit(modifiers);
		String methodName = packageName + "." + getClassName() + ":"
				+ md.getName().getFullyQualifiedName();
		
		String mdFullyQualified= md.getName().getFullyQualifiedName();
//		String mdClass = 
		Signature signature = new Signature();

		String returnType = "";
		if (md.getReturnType() != null && !md.isConstructor()) {
			
			returnType = md.getReturnType().toString();
			if (md.getExtraDimensions()>0) { 
				returnType = returnType +"[]";
			}
		}
		signature.setReturnType(returnType);

		// element SingleVariableDeclaration
		List params = md.parameters();
		// Write signature
		for (int i = 0; i < params.size(); i++) {
			SingleVariableDeclaration param = (SingleVariableDeclaration) params
					.get(i);

			String typeName;
			if (!param.getType().isArrayType()) {
				typeName = param.getType().toString();
				for (int j = 0; j < param.getExtraDimensions(); j++) {
					typeName = typeName + "[]";
				}
			} else {
				// an array type
				typeName = param.getType().toString();
			}
			String argumentName = param.getName().getFullyQualifiedName();
			signature.addParameter(typeName, argumentName);
		}

		// Startline and End line
		int startPos = md.getStartPosition();
		int endPos = startPos + md.getLength();
		int startno = currCompilationUnit.lineNumber(startPos);
		int endno = currCompilationUnit.lineNumber(endPos);

		// Make an eneity instance from data
		Method method = new Method();
		method.setModifier(mb);
		method.setEndLine(endno);
		method.setStartLine(startno);
		method.setStartPos(startPos);
		method.setEndPos(endPos);
		method.setSignature(signature);
		method.setName(methodName);
		method.setFullFuleName(currentFileName);
		entityList.add(method);

		return false;
	}

	/**
	 * Get class names from stack
	 */
	public String getClassName() {
		// Only if stack has been changed
		if (currentClassNames != null) {
			return currentClassNames;
		}

		for (int i = 0; i < classNames.size(); i++) {
			if (i == 0) {
				currentClassNames = classNames.get(i).toString();
			} else {
				currentClassNames += "$" + classNames.get(i).toString();
			}
		}

		// Get the last class name
		return currentClassNames;
	}
	
}
//org.mandarax.xkb.framework.XMLAdapter4MandaraxLibFunctions.importObject__
//[<unknownClassType>, org.mandarax.xkb.framework.GenericDriver, java.util.Map, org.mandarax.kernel.LogicFactory]->java.lang.Objec
//		org.mandarax.lib.math.IntArithmetic$GreaterThanOrEqual.GreaterThanOrEqual__[]->void:1
//		org.mandarax.xkb.framework.XMLAdapter4Types.XMLAdapter4Types__[]->void:6
//		org.mandarax.xkb.framework.XMLAdapter4VariableTerms.XMLAdapter4VariableTerms__[]->void:6
//		org.mandarax.lib.math.IntArithmetic$InclusiveBetween.getName__[]->java.lang.String:20
//		org.mandarax.lib.math.IntArithmetic$Abs.getName__[]->java.lang.String:6
//		test.org.mandarax.jdbc.ResultSetMetaDataTests$8.check__[java.sql.ResultSetMetaData]->boolean:1
//test.org.mandarax.util.AutoFactsTest$3.__[test.org.mandarax.util.AutoFactsTest, java.lang.String, java.lang.Class[]]->void:
		