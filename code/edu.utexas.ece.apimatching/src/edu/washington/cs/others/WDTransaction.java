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
package edu.washington.cs.others;

import java.io.IOException;
import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.washington.cs.extractors.ProgramSnapshot;
import edu.washington.cs.rules.JavaMethod;
import edu.washington.cs.util.Pair;
import edu.washington.cs.util.SetOfPairs;

public class WDTransaction {

	private static String xmlTag = "transaction";

	ArrayList<JavaMethod> addedMethod = new ArrayList<JavaMethod>();

	ArrayList<JavaMethod> deletedMethod = new ArrayList<JavaMethod>();

	ArrayList<WDRefactoringEvent> inferredRefactorings = new ArrayList<WDRefactoringEvent>();
	
	ArrayList<String> fileNames = new ArrayList<String>();
	String id = "";

	String nowTime = "";
	String beforeTime = "";
	public ProgramSnapshot getOldProgramSnapshot(String project) {
		String srcPath = "";
		return new ProgramSnapshot(project,id, deletedMethod, srcPath);
	}
	public ProgramSnapshot getNewProgramSnapshot(String project) {
		String srcPath = "";
		return new ProgramSnapshot(project,id+"new", addedMethod, srcPath); 
	}
	public SetOfPairs getMatchesExplainedByRefactoring(
			WDStatRefactoringReconstruction stat) {
		int initRefactoring = stat.numTotalRefactoring; 
		SetOfPairs result = new SetOfPairs();
		for (int i = 0; i < inferredRefactorings.size(); i++) {
			WDRefactoringEvent refactoring = inferredRefactorings.get(i);
			if (refactoring.type.equals("WDMove Method")) {
				if (stat.ambiguity || refactoring.AmbPos == 1) {
					stat.numMoveMethod++;
					stat.numTotalRefactoring++;
					debugPrint(i+":"+refactoring);
				}
			} else if (refactoring.type.equals("WDMove Interface")) {
				if (stat.ambiguity || refactoring.AmbPos == 1) {
					stat.numMoveInterface++;
					stat.numTotalRefactoring++;
					debugPrint(i+":"+refactoring);
			}
			} else if (refactoring.type.equals("WDRename Method")) {
				if (stat.ambiguity || refactoring.AmbPos == 1) {
					stat.numRenameMethod++;
					stat.numTotalRefactoring++;
					debugPrint(i+":"+refactoring);
				}
			} else if (refactoring.type.equals("WDRename Interface")) {
				if (stat.ambiguity || refactoring.AmbPos == 1) {
					stat.numRenameInterface++;
					stat.numTotalRefactoring++;

					debugPrint(i+":"+refactoring);
				}
			} else if (refactoring.type.equals("Add Parameter")) {
				if (stat.ambiguity || refactoring.AmbPos == 1) {
					stat.numAddParameter++;
					stat.numTotalRefactoring++;

					debugPrint(i+":"+refactoring);
				}
			} else if (refactoring.type.equals("Remove Parameter")) {
				if (stat.ambiguity || refactoring.AmbPos == 1) {
					stat.numRemoveParameter++;
					stat.numTotalRefactoring++;

					debugPrint(i+":"+refactoring);
				}
			} else if (refactoring.type.equals("WDMove Class")) {
				if (stat.ambiguity || refactoring.AmbPos == 1) {
					stat.numMoveClass++;
					stat.numTotalRefactoring++;

					debugPrint(i+":"+refactoring);
				}
			} else if (refactoring.type.equals("WDRename Class")) {
				if (stat.ambiguity || refactoring.AmbPos == 1) {
					stat.numRenameClass++;
					stat.numTotalRefactoring++;

					debugPrint(i+":"+refactoring);
				}
			}
			
			// add matches that are only different by return type
			for (int d = 0; d < deletedMethod.size(); d++) {
				JavaMethod del = deletedMethod.get(d);
				for (int a = 0; a< addedMethod.size(); a++) { 
					JavaMethod add = addedMethod.get(a);
					if (del.onlyDifferByReturn(add)) { 
						result.addPair(new Pair<JavaMethod> (del, add));
					}
				}
			}
			
			if (refactoring.type.equals("WDMove Method")
					|| refactoring.type.equals("WDMove Interface")
					|| refactoring.type.equals("WDRename Method")
					|| refactoring.type.equals("WDRename Interface")
					|| refactoring.type.equals("Add Parameter")
					|| refactoring.type.equals("Remove Parameter")) {
				if (stat.ambiguity) {
					stat.numContributingRefactorings++;
					if (refactoring.makePair().getLeft() != null) {
						result.addPair(refactoring.makePair());
					}
				} else {
					// only consider one with AmbPos 1
					if (refactoring.AmbPos == 1) {
						stat.numContributingRefactorings++;
						if (refactoring.makePair().getLeft() != null) {
							result.addPair(refactoring.makePair());
						}
					}
				}
			} else if (refactoring.type.equals("WDMove Class")
					|| refactoring.type.equals("WDMove Interface")) {
				// for that class or the interface, package was renamed\
				String fromPackageName = refactoring.removed.getJavaClass()
						.getPackageName();
				String fromClassName = refactoring.removed.getJavaClass()
						.getClassName();
				String toPackageName = refactoring.added.getJavaClass()
						.getPackageName();
				String toClassName = refactoring.added.getJavaClass()
						.getClassName();
				boolean contribution = false;
				// for ones that match fromPackage, from Class,
				for (int j = 0; j < deletedMethod.size(); j++) {
					JavaMethod from = deletedMethod.get(j);
					if (from.getPackageName().equals(fromPackageName)
							&& from.getClassName().equals(fromClassName)) {
						JavaMethod target = new JavaMethod(toPackageName,
								toClassName, from.getProcedureName(), from
										.getParameters(), from.getReturntype());

						if (addedMethod.contains(target)) {
							if (stat.ambiguity) {
								contribution = true;
								result.addPair(new Pair(from, target));
							} else {
								if (refactoring.AmbPos == 1) {
									contribution = true;
									result.addPair(new Pair(from, target));
								}
							}
						}
					}
				}
				if (contribution) {
					stat.numContributingRefactorings++;
				}

			} else if (refactoring.type.equals("WDRename Class")) {
				String fromClassName = refactoring.removed.getJavaClass()
						.getClassName();
				String toClassName = refactoring.added.getJavaClass()
						.getClassName();
				boolean contribution = false;
				for (int j = 0; j < deletedMethod.size(); j++) {
					JavaMethod from = deletedMethod.get(j);
					if (from.getClassName().equals(fromClassName)) {
						JavaMethod target = new JavaMethod(from
								.getPackageName(), toClassName, from
								.getProcedureName(), from.getParameters(), from
								.getReturntype());
						if (addedMethod.contains(target)) {
							if (stat.ambiguity) {
								contribution = true;
								result.addPair(new Pair(from, target));
							} else {
								if (refactoring.AmbPos == 1) {
									contribution = true;
									result.addPair(new Pair(from, target));
								}
							}
						}
					}
				}
				if (contribution) {
					stat.numContributingRefactorings++;
				}
			}
		}
		
		return result;
	}

	public static WDTransaction readElement(Element tElement) {
		if (!tElement.getTagName().equals(getXMLTag()))
			return null;

		WDTransaction transaction = new WDTransaction();
		transaction.id = tElement.getAttribute("id");
		
		transaction.nowTime = tElement.getAttribute("end").substring(0,10);
		transaction.beforeTime = tElement.getAttribute("start").substring(0,10);
		NodeList children = tElement.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i) instanceof Element) {
				Element child = (Element) children.item(i);
				if (child.getTagName().equals("logmessage")) {
					// do nothing
				} else if (child.getTagName().equals("versions")) {
					NodeList grandChildren = child.getChildNodes();
					for (int j = 0; j < grandChildren.getLength(); j++) {
						if (grandChildren.item(j) instanceof Element) {
							Element grandchild = (Element) grandChildren
									.item(j);
							if (grandchild.getTagName().equals(
									"version")) {

								String fileName = grandchild
										.getAttribute("filename");
								if (fileName.indexOf("java") >= 0) {
									transaction.fileNames.add(fileName);
								}
							}
						}
					}
						
				} else if (child.getTagName().equals("symbolchanges")) {
					NodeList grandChildren = child.getChildNodes();
					for (int j = 0; j < grandChildren.getLength(); j++) {
						if (grandChildren.item(j) instanceof Element) {
							Element grandchild = (Element) grandChildren
									.item(j);
							if (grandchild.getTagName().equals(
									WDSymbol.getXMLTag())) {
								WDSymbol s = WDSymbol.readElement(grandchild);
								if (s != null && s.getJavaMethod() != null) {
									if (s.isAdded()) {
										transaction.addedMethod.add(s
												.getJavaMethod());
									} else if (s.isDeleted()) {
										transaction.deletedMethod.add(s
												.getJavaMethod());
									}
								}
							}
						}
					}
				} else if (child.getTagName().equals("refactorings")) {
					NodeList grandChildren = child.getChildNodes();
					for (int j = 0; j < grandChildren.getLength(); j++) {
						if (grandChildren.item(j) instanceof Element) {
							Element grandchild = (Element) grandChildren
									.item(j);
							WDRefactoringEvent refactoring = null;
							if (grandchild.getTagName()
									.equals(WDMove.getXMLTag())) {

								refactoring = new WDMove(grandchild);
							} else if (grandchild.getTagName().equals(
									WDRename.getXMLTag())) {

								refactoring = new WDRename(grandchild);
							} else if (grandchild.getTagName().equals(
									WDParameterChange.getXMLTag())) {

								refactoring = new WDParameterChange(grandchild);
							} else if (grandchild.getTagName().equals(
									WDVisibilityChange.getXMLTag())) {

								refactoring = new WDVisibilityChange(grandchild);
							} else {
								debugPrint("ERROR:" + grandchild);
								System.exit(0);
							}
							if (refactoring != null) {
								if (refactoring.type == null) {
									debugPrint("ERORR" + refactoring);
								}
								transaction.inferredRefactorings
										.add(refactoring);
							}
						}// if
					}// for
				}
			}
		}
		// debugPrint(t.addedMethod.size()+"\t"+t.deletedMethod.size());
		return transaction;
	}

	public void printInferredRefactoring() {
		for (int i = 0; i < inferredRefactorings.size(); i++) {
			WDRefactoringEvent refactoring = inferredRefactorings.get(i);
			if (refactoring.type.indexOf("Field") < 0) {
				debugPrint(refactoring.toString());
			}
		}
	}

	public void printSVNCommand() {
		String repo = "svn export https://jedit.svn.sourceforge.net/svnroot/jedit/jEdit/trunk/";
		String time = "-r {2006-02-28}";
		for (int i = 0; i < fileNames.size(); i++) {
			String fileName = fileNames.get(i);
			String commandA = repo+fileName+" -r {"+beforeTime+"}";
			String commandB = repo+fileName+" -r {"+nowTime+"}";
			System.out.println(commandA);
			System.out.println();
			System.out.println(commandB);
			System.out.println();	
		}
	}
	public void createSourceCodeRepository () {
		String repo = "svn export https://jedit.svn.sourceforge.net/svnroot/jedit/jEdit/trunk/";
		for (int i = 0; i < fileNames.size(); i++) {
			String fileName = fileNames.get(i);
			String commandA = repo+fileName+" -r {"+beforeTime+"}";
			String commandB = repo+fileName+" -r {"+nowTime+"}";
			
			try {
				Process p1 = Runtime.getRuntime().exec(commandA);
				System.out.println(commandA);
				p1.waitFor();
			} catch (InterruptedException i1) {
				i1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				Process p1 = Runtime.getRuntime().exec(commandB);
				System.out.println(commandB);
				p1.waitFor();
			} catch (InterruptedException i2) {
				i2.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
	}
	public static String getXMLTag() {
		return xmlTag;
	}
	public static void debugPrint(String s) { 
		
	}
}
