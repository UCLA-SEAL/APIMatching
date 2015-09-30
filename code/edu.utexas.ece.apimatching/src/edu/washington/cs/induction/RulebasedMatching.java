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
package edu.washington.cs.induction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.washington.cs.extractors.ProgramSnapshot;
import edu.washington.cs.rules.Change;
import edu.washington.cs.rules.JavaMethod;
import edu.washington.cs.rules.Rule;
import edu.washington.cs.rules.RuleBase;
import edu.washington.cs.rules.Scope;
import edu.washington.cs.rules.ScopeDisjunction;
import edu.washington.cs.rules.Transformation;
import edu.washington.cs.rules.TransformationRule;
import edu.washington.cs.util.Comparison;
import edu.washington.cs.util.ListOfPairs;
import edu.washington.cs.util.Pair;
import edu.washington.cs.util.SetOfPairs;

/* 
 * input: forwardRuleBase (regular) 
 * input: backwardRuleBase (regular) 
 * output: rulebasematching result 
 */
public class RulebasedMatching {

	/*
	 * 6.27. funtionality of rule based matching (1) display all rules with
	 * matches (1.1) how are we going to visualize forward rules and backward
	 * rules together? (2) summarize and display all added and deleted items (3)
	 * check or uncheck all rules (delete or leave a rule) (4) modify the scope
	 * of rules (5) (add a rule) input a rule and test the correctness of a rule
	 */

	private static final String xmlTag = "rulebasematching";

	private static int exceptionLimit = 10;

	private double precision = 0;

	private double yield = 0;

	private double recall = 0;

	private double MRratio = 0;

	private long forwardTimeInMillis = 0;

	private long backwardTimeInMillis = 0;

	private HashMap<JavaMethod, ArrayList<TransformationRule.RuleID>> leftToRules = null;

	// left (left -right)
	private HashMap<JavaMethod, ArrayList<TransformationRule.RuleID>> rightToRules = null;

	// right (right - left)
	private HashMap<TransformationRule.RuleID, TransformationRule> forwardRules = null;

	private HashMap<TransformationRule.RuleID, TransformationRule> backwardRules = null;

	// rule.
	// private SetOfPairs finalMatches = new SetOfPairs();

	private SetOfPairs forwardMatches = null;

	private SetOfPairs backwardMatches = null;

	// private SetOfPairs manualForwardMatches = null;
	// private SetOfPairs manaulBackwardMatches = null;
	private SetOfPairs manualOneToOneMatches = null;

	// 8. xml serialization

	public RulebasedMatching(RuleBase fRuleBase, RuleBase bRuleBase, double EXCEPTION_TH) {
		
		Rule.setExceptionThreshold(EXCEPTION_TH);
		Date start = new Date();
		initialForwardTransformationRules(fRuleBase);
		Date half = new Date();
		setForwardTimeInMillis(half.getTime()-start.getTime());
		initialBackwardTransformationRules(bRuleBase);
		Date end = new Date();
		setBackwardTimeInMillis(end.getTime()-half.getTime());
	}

	private RulebasedMatching() {

	}

	public String toString() {
		String s = "";
		s = "IFF RULES\n";
		for (Iterator<Pair<TransformationRule.RuleID>> it = getIffRules()
				.iterator(); it.hasNext();) {
			Pair<TransformationRule.RuleID> pair = it.next();
			TransformationRule forwardRule = forwardRules.get(pair.getLeft());
			s = s + forwardRule.getPositiveMatches().size() + "   "
					+ forwardRule + "\n\t\t"
					+ backwardRules.get(pair.getRight()) + "\n\n";
		}
		return s;
	}

	public int exceptionThresholdViolatingRules (int TH) {
		int count = 0; 
		for (Iterator<TransformationRule.RuleID> it = forwardRules.keySet()
				.iterator(); it.hasNext();) {
			TransformationRule.RuleID fRuleid = it.next();
			TransformationRule forwardRule = this.forwardRules.get(fRuleid);
			ListOfPairs exceptionMatches = forwardRule.getExceptionMatches();
			ListOfPairs matches = forwardRule.getPositiveMatches();
			if (exceptionMatches.size()*TH > matches.size()) {
				System.out.print("\t"+fRuleid);
				count ++;
			}
		}
		return count;
	}
	/*
	 * function: make iff rules by comparing positive matches. precondition:
	 * forwardRules, and leftToRules are not null. postcondition: iffRules
	 */
	public ArrayList<Pair<TransformationRule.RuleID>> getIffRules() {
		if (forwardRules == null || backwardRules == null
				|| leftToRules == null || rightToRules == null)
			return null;
		ArrayList<Pair<TransformationRule.RuleID>> iffRules = new ArrayList<Pair<TransformationRule.RuleID>>();
		for (Iterator<TransformationRule.RuleID> it = forwardRules.keySet()
				.iterator(); it.hasNext();) {
			TransformationRule.RuleID fRuleid = it.next();
			TransformationRule forwardRule = this.forwardRules.get(fRuleid);
			for (Iterator<TransformationRule.RuleID> jt = backwardRules
					.keySet().iterator(); jt.hasNext();) {
				TransformationRule.RuleID bRuleid = jt.next();
				TransformationRule backwardRule = this.backwardRules
						.get(bRuleid);
				ListOfPairs fMatch = forwardRule.getPositiveMatches();
				ListOfPairs bMatch = backwardRule.getPositiveMatches();
				if (fMatch.size() != bMatch.size())
					continue;
				if (fMatch.includeAll(bMatch) && bMatch.includeAll(fMatch)) {
					Pair<TransformationRule.RuleID> iffRulePair = new Pair<TransformationRule.RuleID>(
							forwardRule.getRuleID(), backwardRule.getRuleID());
					iffRules.add(iffRulePair);
				}
			}
		}
		return iffRules;
	}

	/*
	 * input: jm, left or right entity input: left, if true jm is a left entity,
	 * if false, jm is a right entity precondition: leftToRules!=null,
	 * rightToRules!=null, precondition: backwardMatches!null,
	 * forwardMatches!=null return all co-assisting rules, and counter-part
	 * rules. for left item, find backward rules that generate (left, X) for
	 * right item, find forward rules that generate (X, right)
	 */
	public ArrayList<TransformationRule.RuleID> getAllRelevantRules(
			JavaMethod jm, boolean left) {
		ArrayList<TransformationRule.RuleID> relevant = new ArrayList<TransformationRule.RuleID>();
		if (left) {
			// other co-assisting rules;
			System.out.println(leftToRules.get(jm));
			relevant.addAll(leftToRules.get(jm));
			// counterpart rules
			Pair p = (Pair<JavaMethod>) forwardMatches.getFirstMatchByLeft(jm);
			System.out.println(p);
			if (p != null) {
				JavaMethod counterpart = (JavaMethod) p.getRight();
				System.out.println(counterpart);
				System.out.println(rightToRules.get(counterpart));
				if (rightToRules.get(counterpart) == null) {
					System.out.println("RulebasedMatching: Fatal Error" + p);
				} else
					relevant.addAll(rightToRules.get(counterpart));
			}
		} else {
			// other co-assisting rules;
			relevant.addAll(rightToRules.get(jm));
			// counterpart rules
			Pair p = (Pair<JavaMethod>) backwardMatches
					.getFirstMatchByRight(jm);
			if (p != null) {
				JavaMethod counterpart = (JavaMethod) p.getLeft();
				if (leftToRules.get(counterpart) == null) {
					System.out.println("RulebasedMatching: Fatal Error" + p);
				}
				relevant.addAll(leftToRules.get(counterpart));
			}
		}
		return relevant;
	}

	public ArrayList<TransformationRule.RuleID> getCodependentRules(
			JavaMethod jm, boolean forward) {
		if (forward) {
			return leftToRules.get(jm);
		} else {
			// other co-assisting rules;
			return rightToRules.get(jm);
		}
	}
	public Set<JavaMethod> getRelevantCounterParts(boolean forward,
			TransformationRule.RuleID id) {
		TransformationRule rule = null;
		if (forward) {
			rule = forwardRules.get(id);
		} else {
			rule = backwardRules.get(id);
		}
		ListOfPairs pairs = rule.getPositiveMatches();
		return (Set<JavaMethod>)pairs.getRightDomain();
	}

	// Similarly, make "transformation-rule" based matching results/
	/*
	 * function: forward (transformation based rule), scopeDisj, transformation,
	 * positive matches, exception pairs (exception, empty) or (exception,
	 * another match) input: forwardRulebase output: create an instance of
	 * forwardRules and leftToRules <left, rules>
	 */
	public void initialForwardTransformationRules(RuleBase forwardRulebase) {

		// (1) initialize forwardRules and leftToRules
		this.forwardRules = new HashMap<TransformationRule.RuleID, TransformationRule>();
		this.leftToRules = new HashMap<JavaMethod, ArrayList<TransformationRule.RuleID>>();
		this.forwardMatches = new SetOfPairs();
		for (Iterator<JavaMethod> jmIt = forwardRulebase.originalLeft
				.iterator(); jmIt.hasNext();) {
			JavaMethod left = jmIt.next();
			this.leftToRules.put(left,
					new ArrayList<TransformationRule.RuleID>());
		}
		boolean forward = true;
		postProcessToTransformationRule(forward, forwardRulebase, this.forwardRules,
				this.leftToRules, this.forwardMatches);
	}

	/*
	 * function: backward (transformation based rule), scopeDisj,
	 * transformation, positive matches, exception pairs (exception, empty) or
	 * (exception, another match) input: forwardRulebase output: create an
	 * instance of backwardRules and rightToRules <right, rules>
	 */
	public void initialBackwardTransformationRules(RuleBase backwardRulebase) {
		// (1) initialize backwardRules and rightToRules
		this.backwardRules = new HashMap<TransformationRule.RuleID, TransformationRule>();
		this.rightToRules = new HashMap<JavaMethod, ArrayList<TransformationRule.RuleID>>();
		this.backwardMatches = new SetOfPairs();
		for (Iterator<JavaMethod> jmIt = backwardRulebase.originalLeft
				.iterator(); jmIt.hasNext();) {
			JavaMethod right = jmIt.next();
			this.rightToRules.put(right,
					new ArrayList<TransformationRule.RuleID>());
		}
		boolean forward = false;
		postProcessToTransformationRule(forward, backwardRulebase, this.backwardRules,
				this.rightToRules, this.backwardMatches);
	}

	private void postProcessToTransformationRule(boolean forward, RuleBase dRulebase,
			HashMap<TransformationRule.RuleID, TransformationRule> dRules,
			HashMap<JavaMethod, ArrayList<TransformationRule.RuleID>> dToRules,
			SetOfPairs dMatches) {
		// (2) make transformation-based rules.
		HashMap<Transformation, ArrayList<Rule>> map = 
			new HashMap<Transformation, ArrayList<Rule>>();
		ArrayList<Rule> forwardAcceptedRegularRules = dRulebase
				.getAcceptedRules();
		// create a map for each transformation
		for (int i = 0; i < forwardAcceptedRegularRules.size(); i++) {
			Rule rule = forwardAcceptedRegularRules.get(i);
			rule.mapByTransformation(map);
		}
		int id = 0;
		for (Iterator<Transformation> it = map.keySet().iterator(); it
				.hasNext();) {
			Transformation singleTran = it.next();
			ArrayList<Rule> rulesWithTran = (ArrayList<Rule>) map
					.get(singleTran);
			ScopeDisjunction disjScope = new ScopeDisjunction();
			// sort rulesWithTran in descending order.
			Collections.sort(rulesWithTran, new SortRuleComparator());
			SetOfPairs positiveMatches = new SetOfPairs();
			SetOfPairs negativeMatches = new SetOfPairs();

			for (int i = 0; i < rulesWithTran.size(); i++) {
				Rule rule = rulesWithTran.get(i);
				ListOfPairs pM = rule.getRefreshPositiveMatches(dRulebase);
				ListOfPairs nM = rule.getNegativeMatches(dRulebase);
				if (!positiveMatches.includeAll(pM)) {
					if (pM.size()>1 && rule.getChange().includeTypeReplace()) { 
						Rule refinedRule = rule.getCleanerScopeRule(dRulebase,
								rule.getScope().getSeed(), pM, nM);
						if (refinedRule != null) {
							disjScope.add(refinedRule.getScope());
						} else {
							disjScope.add(rule.getScope());
						}	
					}else {
						disjScope.add(rule.getScope());
					}
					positiveMatches.addSetOfPairs(pM);
					negativeMatches.addSetOfPairs(nM);
				}
			}

			// create a transformation-base rule
			TransformationRule tranRule = null;
			if (disjScope.size() > 0 || positiveMatches.size() > 1) {
				// try to clean up disjScope possible
				// try to generalize disjScope
				ScopeDisjunction bottomUp = new ScopeDisjunction();
				for (Iterator<Pair> pIt = positiveMatches.iterator(); pIt
						.hasNext();) {
					JavaMethod jm = null;
					if (forward) {
						jm = (JavaMethod) (pIt.next()).getLeft();
					} else {
						jm = (JavaMethod) (pIt.next()).getRight();
					}
					Scope s = new Scope(jm);
					s.refineMostSpecific();
					bottomUp.add(s);
				}
				Scope newCleasScope = Scope.combine(bottomUp);
				Change change = new Change(singleTran);
				// make transformation rule with the more general scope
				Rule newCleanRule = new Rule(newCleasScope, change, dRulebase);
				boolean includeMatches = newCleanRule.getMatches().includeAll(
						positiveMatches);
				// scope cannot be bigger than the actual positive matches. 
//				boolean negativeLessThanThreshold = newCleanRule.numNegative() < exceptionLimit;
				if (includeMatches && newCleanRule.isValidAfterInference() ) {
					if (forward) {
						tranRule = new TransformationRule(newCleasScope,
								singleTran, newCleanRule.getMatches(),
								newCleanRule.getNegativeMatches(dRulebase),
								forward, id);
					} else {
						tranRule = new TransformationRule(newCleasScope,
								singleTran,
								newCleanRule.getMatches().inverse(),
								newCleanRule.getNegativeMatches(dRulebase)
										.inverse(), forward, id);
					}
				}
			}
			if (tranRule == null) {
				ListOfPairs pM = new ListOfPairs();
				pM.addSetOfPairs(positiveMatches);
				ListOfPairs nM = new ListOfPairs();
				nM.addSetOfPairs(negativeMatches);
				if (forward) {
					tranRule = new TransformationRule(disjScope, singleTran,
							pM, nM, forward, id);
				} else {
					tranRule = new TransformationRule(disjScope, singleTran, pM
							.inverse(), nM.inverse(), forward, id);
				}
			}
			id++;
			// (2.1) add a transformation-based rule to forwardRules.
			dRules.put(tranRule.getRuleID(), tranRule);
			// (2.2) associate domains with this transformation rule
			for (Iterator<JavaMethod> domainIt = getDomain(forward).iterator(); domainIt
					.hasNext();) {
				JavaMethod domain = domainIt.next();
				if (tranRule.scopeDisjTranApplicable(domain)) {
					ArrayList<TransformationRule.RuleID> tranRuleList = dToRules
							.get(domain);
					if (!tranRuleList.contains(tranRule.getRuleID()))
						tranRuleList.add(tranRule.getRuleID());
				}
			}
			// (2.3) add all positive matches to the final matches
			dMatches.addSetOfPairs(tranRule.getPositiveMatches());
		}
	}

	/*
	 * find 1:n mappings and get a list of forward transformation rules that are
	 * relevant
	 */
	public ArrayList<TransformationRule.RuleID> getSplits() {
		SetOfPairs finalMatches = new SetOfPairs();
		finalMatches.addSetOfPairs(this.backwardMatches);
		finalMatches.addSetOfPairs(this.forwardMatches);
		HashMap<JavaMethod, Integer> matchOccurence = new HashMap<JavaMethod, Integer>();
		for (Iterator<Pair> pairIt = finalMatches.iterator(); pairIt.hasNext();) {
			Pair p = pairIt.next();
			Integer i = matchOccurence.get(p.getLeft());
			if (i == null) {
				i = new Integer(1);
			} else {
				i = new Integer(i.intValue() + 1);
			}
			matchOccurence.put((JavaMethod) p.getLeft(), i);
		}
		ArrayList<TransformationRule.RuleID> relevantRules = new ArrayList<TransformationRule.RuleID>();
		for (Iterator<JavaMethod> it = matchOccurence.keySet().iterator(); it
				.hasNext();) {
			JavaMethod left = it.next();
			relevantRules.addAll(getAllRelevantRules(left, true));
		}
		return relevantRules;
	}

	/*
	 * find n:1 mappings and get a list of backward transformation rules that
	 * are relevant
	 */
	public ArrayList<TransformationRule.RuleID> getMerges() {
		SetOfPairs finalMatches = new SetOfPairs();
		finalMatches.addSetOfPairs(this.backwardMatches);
		finalMatches.addSetOfPairs(this.forwardMatches);
		HashMap<JavaMethod, Integer> matchOccurence = new HashMap<JavaMethod, Integer>();
		for (Iterator<Pair> pairIt = finalMatches.iterator(); pairIt.hasNext();) {
			Pair p = pairIt.next();
			Integer i = matchOccurence.get(p.getRight());
			if (i == null) {
				i = new Integer(1);
			} else {
				i = new Integer(i.intValue() + 1);
			}
			matchOccurence.put((JavaMethod) p.getRight(), i);
		}
		ArrayList<TransformationRule.RuleID> relevantRules = new ArrayList<TransformationRule.RuleID>();
		for (Iterator<JavaMethod> it = matchOccurence.keySet().iterator(); it
				.hasNext();) {
			JavaMethod right = it.next();
			relevantRules.addAll(getAllRelevantRules(right, true));
		}
		return relevantRules;
	}

	public static void debugPrint(String s) {
//		System.out.println(s);
	}

	/*
	 * To serialize as an XML file.
	 */
	public void writeElement(Element parent) {
		Element thisNode = parent.getOwnerDocument().createElement(xmlTag);
		thisNode.setAttribute("exceptionLimit", new Integer(exceptionLimit)
				.toString());
		if (forwardTimeInMillis !=0)
			thisNode.setAttribute("forwardTimeInMillis", new Long(forwardTimeInMillis).toString());
		if (backwardTimeInMillis !=0)
			thisNode.setAttribute("backwardTimeInMillis", new Long(backwardTimeInMillis).toString());
		
		// iterate over forward rules
		for (Iterator<TransformationRule.RuleID> it = forwardRules.keySet()
				.iterator(); it.hasNext();) {
			TransformationRule.RuleID f = it.next();
			Element fElement = parent.getOwnerDocument().createElement(
					"forwardRule");
			TransformationRule fRule = forwardRules.get(f);
			fRule.writeElement(fElement);
			thisNode.appendChild(fElement);
		}
		// iterate over backward rules
		for (Iterator<TransformationRule.RuleID> it = backwardRules.keySet()
				.iterator(); it.hasNext();) {
			TransformationRule.RuleID b = it.next();
			Element bElement = parent.getOwnerDocument().createElement(
					"backwardRule");
			TransformationRule bRule = backwardRules.get(b);
			bRule.writeElement(bElement);
			thisNode.appendChild(bElement);
		}
		// forwardMatches
		Element fMElement = parent.getOwnerDocument().createElement(
				"forwardMatches");
		forwardMatches.writeElement(fMElement);
		thisNode.appendChild(fMElement);
		// backwardMatches
		Element bMElement = parent.getOwnerDocument().createElement(
				"backwardMatches");
		backwardMatches.writeElement(bMElement);
		thisNode.appendChild(bMElement);

		// manualForwardMatches,
		if (manualOneToOneMatches != null) {
			Element oneToOneElement = parent.getOwnerDocument().createElement(
					"manualOneToOneMatches");
			manualOneToOneMatches.writeElement(oneToOneElement);
			thisNode.appendChild(oneToOneElement);
		}

		// leftToRules
		int count_1 = 0;
		for (Iterator<JavaMethod> leftIt = leftToRules.keySet().iterator(); leftIt
				.hasNext();) {
			JavaMethod jm = leftIt.next();
			ArrayList<TransformationRule.RuleID> rules = leftToRules.get(jm);
			Element jmElement = parent.getOwnerDocument().createElement("left");
			jmElement.setTextContent(jm.toString());
			thisNode.appendChild(jmElement);
			jmElement
					.setAttribute("size", new Integer(rules.size()).toString());
			for (int i = 0; i < rules.size(); i++) {
				TransformationRule.RuleID ruleID = rules.get(i);
				jmElement.setAttribute("id_" + i, ruleID.toString());
			}
		}
		// rightToRules
		int count_2 = 0;
		for (Iterator<JavaMethod> rightIt = rightToRules.keySet().iterator(); rightIt
				.hasNext();) {
			JavaMethod jm = rightIt.next();
			ArrayList<TransformationRule.RuleID> rules = rightToRules.get(jm);
			Element jmElement = parent.getOwnerDocument()
					.createElement("right");
			jmElement.setTextContent(jm.toString());
			thisNode.appendChild(jmElement);
			jmElement
					.setAttribute("size", new Integer(rules.size()).toString());
			for (int i = 0; i < rules.size(); i++) {
				TransformationRule.RuleID ruleID = rules.get(i);
				jmElement.setAttribute("id_" + i, ruleID.toString());
			}
		}
		parent.appendChild(thisNode);
	}

	public static RulebasedMatching readElement(Element rulebasematching) {
		if (!rulebasematching.getTagName().equals(xmlTag))
			return null;
		exceptionLimit = new Integer(rulebasematching
				.getAttribute("exceptionLimit")).intValue();
		// read forward Rules
		HashMap<TransformationRule.RuleID, TransformationRule> forwardRules = new HashMap<TransformationRule.RuleID, TransformationRule>();
		HashMap<TransformationRule.RuleID, TransformationRule> backwardRules = new HashMap<TransformationRule.RuleID, TransformationRule>();
		ArrayList<Pair<TransformationRule.RuleID>> iffRules = new ArrayList<Pair<TransformationRule.RuleID>>();
		SetOfPairs forwardMatches = null;
		SetOfPairs backwardMatches = null;
		HashMap<JavaMethod, ArrayList<TransformationRule.RuleID>> leftToRules = new HashMap<JavaMethod, ArrayList<TransformationRule.RuleID>>();
		HashMap<JavaMethod, ArrayList<TransformationRule.RuleID>> rightToRules = new HashMap<JavaMethod, ArrayList<TransformationRule.RuleID>>();

		SetOfPairs manualOneToOneMatches = new SetOfPairs();
		NodeList children = rulebasematching.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i) instanceof Element) {
				Element child = (Element) children.item(i);
				if (child.getTagName().equals("forwardRule")) {
					TransformationRule fRule = TransformationRule
							.readElement((Element) child.getFirstChild());
					forwardRules.put(fRule.getRuleID(), fRule);
				} else if (child.getTagName().equals("backwardRule")) {
					TransformationRule bRule = TransformationRule
							.readElement((Element) child.getFirstChild());
					backwardRules.put(bRule.getRuleID(), bRule);
				} else if (child.getTagName().equals("manualOneToOneMatches")) {
					manualOneToOneMatches = SetOfPairs
							.readElement((Element) child.getFirstChild());
				} else if (child.getTagName().equals("forwardMatches")) {
					forwardMatches = SetOfPairs.readElement((Element) child
							.getFirstChild());
				} else if (child.getTagName().equals("backwardMatches")) {
					backwardMatches = SetOfPairs.readElement((Element) child
							.getFirstChild());
				} else if (child.getTagName().equals("left")) {
					JavaMethod left = new JavaMethod(child.getTextContent());
					int size = new Integer(child.getAttribute("size"))
							.intValue();
					ArrayList<TransformationRule.RuleID> tRules = new ArrayList<TransformationRule.RuleID>();
					for (int j = 0; j < size; j++) {
						String s = child.getAttribute("id_" + j);
						// debugPrint("att"+s+":\t");
						if (s.indexOf("forward_") >= 0) {
							TransformationRule.RuleID ruleID = TransformationRule
									.getRuleID(s);
							// debugPrint(ruleID.toString());
							tRules.add(ruleID);
						} else {
							debugPrint("FATAL ERR");
							System.exit(0);
						}
					}
					// debugPrint("left:"+left);
					// debugPrint("\trules:"+tRules);
					leftToRules.put(left, tRules);
				} else if (child.getTagName().equals("right")) {
					JavaMethod right = new JavaMethod(child.getTextContent());
					int size = new Integer(child.getAttribute("size"))
							.intValue();
					ArrayList<TransformationRule.RuleID> tRules = new ArrayList<TransformationRule.RuleID>();
					for (int j = 0; j < size; j++) {
						String s = child.getAttribute("id_" + j);
						// debugPrint("att"+s+":\t");
						if (s.indexOf("backward_") >= 0) {
							tRules.add(TransformationRule.getRuleID(s));
						} else {
							// debugPrint("FATAL ERR");
							System.exit(0);
						}
					}
					// debugPrint("right:"+right);
					// debugPrint(tRules.toString());
					rightToRules.put(right, tRules);
				} else if (child.getTagName().equals("iffRule")) {
					TransformationRule.RuleID leftID = TransformationRule
							.getRuleID(child.getAttribute("if"));
					TransformationRule.RuleID rightID = TransformationRule
							.getRuleID(child.getAttribute("onlyif"));
					Pair<TransformationRule.RuleID> pair = new Pair<TransformationRule.RuleID>(
							leftID, rightID);
					iffRules.add(pair);
				}
			}
		}
		RulebasedMatching result = new RulebasedMatching();
		result.forwardRules = forwardRules;
		result.backwardRules = backwardRules;
		result.leftToRules = leftToRules;
		result.rightToRules = rightToRules;
		result.forwardMatches = forwardMatches;
		result.backwardMatches = backwardMatches;
		result.manualOneToOneMatches = manualOneToOneMatches;
		if (!rulebasematching.getAttribute("forwardTimeInMillis").equals("")) {
			result.setForwardTimeInMillis(new Long(rulebasematching.getAttribute("forwardTimeInMillis")).longValue());
		}
		if (!rulebasematching.getAttribute("backwardTimeInMillis").equals("")) {
			result.setBackwardTimeInMillis(new Long(rulebasematching.getAttribute("backwardTimeInMillis")).longValue());
		}
		return result;
	}

	public void writeXMLFile(String filename) {
		Document doc = DOMImplementationImpl.getDOMImplementation()
				.createDocument("namespaceURI", xmlTag, null);
		// update document
		writeElement(doc.getDocumentElement());
		File file = new File(filename);
		try {
			if (!file.exists())
				file.createNewFile();
			// serialize DOM document to outputfile
			XMLSerializer serializer = new XMLSerializer();
			OutputFormat format = new OutputFormat();
			format.setPreserveSpace(true);
			format.setPreserveEmptyAttributes(true);
			format.setIndenting(true);
			format.setIndent(4);
			format.setLineWidth(80);
			format.setLineSeparator("\n");
			String[] nonEscapingElements = { "\n", "\t" };
			format.setNonEscapingElements(nonEscapingElements);
			serializer = new XMLSerializer(format);
			FileOutputStream outstream = new FileOutputStream(file);
			serializer.setOutputByteStream(outstream);
			assert (doc != null);
			serializer.serialize(doc);
			outstream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static RulebasedMatching readXMLFile(String filename) {
		Document doc = null;
		DOMParser domparser = new DOMParser();
		try {
			domparser.parse(filename);
			doc = domparser.getDocument();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException s) {
			s.printStackTrace();
		}
		// parse doc and return GroupMap
		return readElement((Element) doc.getDocumentElement().getFirstChild());
	}

	public boolean equals(Object obj) {
		if (obj instanceof RulebasedMatching) {
			RulebasedMatching other = (RulebasedMatching) obj;
			boolean br = other.backwardRules.keySet().equals(
					this.backwardRules.keySet());
			boolean br1 = this.backwardRules.keySet().equals(
					other.backwardRules.keySet());
			boolean fr = other.forwardRules.keySet().equals(
					this.forwardRules.keySet());
			boolean fr1 = this.forwardRules.keySet().equals(
					other.forwardRules.keySet());
			boolean fM = this.forwardMatches.includeAll(other.forwardMatches);
			boolean bM = this.backwardMatches.includeAll(other.backwardMatches);
			debugPrint("backward rules:" + br + "\t" + br1);
			debugPrint("forward rules:" + fr + "\t" + fr1);
			for (Iterator<JavaMethod> leftIt = leftToRules.keySet().iterator(); leftIt
					.hasNext();) {
				JavaMethod jm = leftIt.next();
				ArrayList<TransformationRule.RuleID> thisrules = this.leftToRules
						.get(jm);
				ArrayList<TransformationRule.RuleID> otherrules = other.leftToRules
						.get(jm);
				if (!thisrules.equals(otherrules)) {
					debugPrint("leftToRules violation");
					return false;
				}

			}
			for (Iterator<JavaMethod> rightIt = rightToRules.keySet()
					.iterator(); rightIt.hasNext();) {
				JavaMethod jm = rightIt.next();
				ArrayList<TransformationRule.RuleID> thisrules = this.rightToRules
						.get(jm);
				ArrayList<TransformationRule.RuleID> otherrules = other.rightToRules
						.get(jm);
				if (!thisrules.equals(otherrules)) {
					debugPrint("rightToRules violation");
					return false;
				}

			}
			if (!(br && fr && br1 && fr1 && fM && bM)) {
				return false;
			}
			return true;
		}
		return false;
	}

	/*
	 * input: args[0] project input: args[1] project_list output: create a
	 * rulebaseMatching in xml format output:
	 * matchingResultDir/project/"newv-oldvRulebaseMatch100.xml" (backward rule)
	 * test == true. test with different similarity thresholds 
	 * test == false.
	 */
	

	public static void main(String[] args) {
		batchMatchingGeneration(false, "jfreechart", 0.7, 0.34, "jfreechart_list_CGE");
	}
	public static void commandLineExecution (String[] args) {
		if (args.length != 4) {
			System.exit(0);
		}
		String project = args[0];
		// (2) set threshold for name matcher
		final double SEED_TH = new Double(args[1]).doubleValue();
		// (3) set a seed selection value
		final double EXCEPTION_TH = new Double(args[2]).doubleValue();
		String dirList = args[3];
		batchMatchingGeneration(true, project, SEED_TH, EXCEPTION_TH, dirList);
	}

	private static void batchMatchingGeneration(boolean onlyOne, String project,
			double SEED_TH, double EXCEPTION_TH, String dirList) {
		File[] dirs = ReadDirectories.getDirectories(dirList);
		int loopend = 1;
		if (onlyOne == false)
			loopend = dirs.length - 1;
		for (int i = 0; i < loopend; i++) {
			ProgramSnapshot oldP = new ProgramSnapshot(project, dirs[i]);
			ProgramSnapshot newP = new ProgramSnapshot(project, dirs[i + 1]);
			File forwardRule = FileNameService.getRuleXMLFile(oldP, newP,
					SEED_TH, EXCEPTION_TH);
			File backwardRule = FileNameService.getRuleXMLFile(newP, oldP,
					SEED_TH, EXCEPTION_TH);
			RuleBase frb = RuleBase.readXMLFile(forwardRule.getAbsolutePath());
			RuleBase brb = RuleBase.readXMLFile(backwardRule.getAbsolutePath());
			if (frb != null && brb != null) {
				RulebasedMatching matchingResult = new RulebasedMatching(frb,
						brb, EXCEPTION_TH);
				File matchingFile = FileNameService.getMatchingXMLFile(oldP,
						newP, SEED_TH, EXCEPTION_TH);
				if (!matchingFile.exists()) {
					System.out.println("Writing to Matching "
							+ matchingFile.getAbsolutePath());
					matchingResult.writeXMLFile(matchingFile.getAbsolutePath());
				}
			}
		}
	}

	public static RulebasedMatching batchMatchingGenerationTwoVersion(
			boolean refresh, ProgramSnapshot oldP, ProgramSnapshot newP,
			double SEED_TH, double EXCEPTION_TH) {
		File outputResult = FileNameService.getMatchingXMLFile(oldP, newP,
				SEED_TH, EXCEPTION_TH);
		if (outputResult.exists()&& refresh==false) {
			return RulebasedMatching
					.readXMLFile(outputResult.getAbsolutePath());
		}
		// !outputResult.exists() || refresh ==true
		File forwardRule = FileNameService.getRuleXMLFile(oldP, newP, SEED_TH,
				EXCEPTION_TH);
		File backwardRule = FileNameService.getRuleXMLFile(newP, oldP, SEED_TH,
				EXCEPTION_TH);
		RuleBase frb = null;
		RuleBase brb = null;
		if (!forwardRule.exists() || refresh) {
			frb = new HillClimbingSearch().createRuleBase(refresh,oldP, newP, true,
					SEED_TH, EXCEPTION_TH);
		} else {
			frb = RuleBase.readXMLFile(forwardRule.getAbsolutePath());
		}
		if (!backwardRule.exists() || refresh) {
			brb = new HillClimbingSearch().createRuleBase(refresh,oldP, newP, false,
					SEED_TH, EXCEPTION_TH);
		} else {
			brb = RuleBase.readXMLFile(backwardRule.getAbsolutePath());
		}
		
		if (!outputResult.exists() || refresh) {
			System.out.println(forwardRule.getAbsolutePath());
			System.out.println(backwardRule.getAbsolutePath());
			RulebasedMatching matchingResult = new RulebasedMatching(frb, brb,
					EXCEPTION_TH);
			System.out.println("Writing to " + outputResult.getAbsolutePath());
			matchingResult.writeXMLFile(outputResult.getAbsolutePath());
			return matchingResult;
		}
		return null;
	}
	public class SortTransformationRuleComparator implements
			Comparator<TransformationRule> {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(T, T)
		 */
		public int compare(TransformationRule o1, TransformationRule o2) {
			int o1P = o1.getPositiveMatches().size();
			int o2P = o2.getPositiveMatches().size();
			if (o1P < o2P)
				return 1;
			else if (o1P == o2P)
				return 0;
			else
				return -1;
		}
	}

	public class SortRuleComparator implements Comparator<Rule> {
		public int compare(Rule o1, Rule o2) {
			int o1P = o1.getMatches().size();
			int o2P = o2.getMatches().size();
			if (o1P < o2P) {
				return -1;
			} else if (o1P == o2P)
				return 0;
			else
				return -1;
		}
	}

	public TransformationRule[] getSortedRules(boolean forward) {
		List<TransformationRule> ruleList = new ArrayList<TransformationRule>();
		Collection<TransformationRule> ruleCollection = null;
		if (forward) {
			ruleCollection = forwardRules.values();
		} else {
			ruleCollection = backwardRules.values();
		}
		for (Iterator<TransformationRule> it = ruleCollection.iterator(); it
				.hasNext();) {
			TransformationRule rule = it.next();
			ruleList.add(rule);
		}
		Collections.sort(ruleList, new SortTransformationRuleComparator());
		TransformationRule[] rules = new TransformationRule[ruleCollection
				.size()];
		for (int i = 0; i < ruleList.size(); i++) {
			rules[i] = ruleList.get(i);
		}
		return rules;
	}

	public void copyFrom(RulebasedMatching copyFrom) {
		for (Iterator<TransformationRule.RuleID> it = copyFrom.forwardRules
				.keySet().iterator(); it.hasNext();) {
			TransformationRule.RuleID fCopyFromID = it.next();
			TransformationRule sourceRule = copyFrom.forwardRules
					.get(fCopyFromID);
			TransformationRule targetRule = this.forwardRules.get(fCopyFromID);
			if (sourceRule.isChecked()) {
				modifyRule(true, sourceRule.isKept(), targetRule, null);
			}
		}
		for (Iterator<TransformationRule.RuleID> it = copyFrom.backwardRules
				.keySet().iterator(); it.hasNext();) {
			TransformationRule.RuleID bCopyFromID = it.next();
			TransformationRule sourceRule = copyFrom.backwardRules
					.get(bCopyFromID);
			TransformationRule targetRule = this.backwardRules.get(bCopyFromID);
			if (sourceRule.isChecked()) {
				modifyRule(false, sourceRule.isKept(), targetRule, null);
			}
		}
	}

	public void modifyRule(boolean forward, boolean keep,
			TransformationRule rule, Scope scope) {
		rule.setChecked();
		System.out.println("modifyRule" + forward + "\t" + keep + "\t" + rule
				+ "\t" + scope);
		// (1) delete/add the rule
		// (2) delete this rule's matches in the final matches
		// (3) recalculate the affected region
		ListOfPairs oldMatches = rule.getPositiveMatches();
		if (!keep) {
			// delete
			if (scope == null) {
				if (!rule.isKept())
					return;
				rule.unmark();
			} else {
				if (!scope.isKept())
					return;
				rule.unmarkScope(scope);
			}
			if (forward) {
				forwardMatches.removeListOfPairs(oldMatches);
				recalculateAffectedLeft(oldMatches.getLeftDomain());
			} else {
				backwardMatches.removeListOfPairs(oldMatches);
				recalculateAffectedRight(oldMatches.getRightDomain());
			}
		} else {
			// add
			if (scope == null) {
				if (rule.isKept())
					return;
				rule.mark();
			} else {
				if (scope.isKept())
					return;
				rule.markScope(scope);
			}

			TreeSet<JavaMethod> affectedRegion = new TreeSet<JavaMethod>();
			for (Iterator<JavaMethod> it = getDomain(forward).iterator(); it
					.hasNext();) {
				JavaMethod jm = it.next();
				if (rule.scopeDisjTranApplicable(jm)) {
					affectedRegion.add(jm);
				}
			}
			if (forward) {
				forwardMatches.removeListOfPairs(oldMatches);
				recalculateAffectedLeft(affectedRegion);
			} else {
				backwardMatches.removeListOfPairs(oldMatches);
				recalculateAffectedRight(affectedRegion);
			}
		}
	}

	/*
	 * function: update the affected region and update involved rules input:
	 * oldForwardMatches to be recalculated effect: also update all rules that
	 * have been affected by the recalculation
	 */
	private void recalculateAffectedLeft(Set<JavaMethod> lefts) {
		TreeSet<TransformationRule.RuleID> involvedRules = new TreeSet<TransformationRule.RuleID>();
		for (Iterator<JavaMethod> leftIt = lefts.iterator(); leftIt.hasNext();) {
			JavaMethod left = leftIt.next();
			ArrayList<TransformationRule.RuleID> applicableRules = leftToRules
					.get(left);
			System.out.println("recalculateAffectedLeft: \t" + left + "\n\t"
					+ applicableRules);
			involvedRules.addAll(applicableRules);
			JavaMethod right = null;
			for (Iterator<TransformationRule.RuleID> appRuleIt = applicableRules
					.iterator(); appRuleIt.hasNext();) {
				TransformationRule.RuleID appRuleID = appRuleIt.next();
				TransformationRule appRule = forwardRules.get(appRuleID);
				System.out.println("appRule" + appRule.getRuleID() + ":"
						+ appRule.isApplicable(left));
				// only apply rules where scope match, trans match, and alive
				if (right == null && appRule.scopeDisjTranApplicable(left)
						&& appRule.isKept()) {
					right = appRule.convert(left);
				} else if (appRule.scopeDisjTranApplicable(left)
						&& appRule.isKept()) {
					right = appRule.convert(right);
				}
			}
			Pair<JavaMethod> newPair = new Pair<JavaMethod>(left, right);
			System.out.println("new pair\t" + newPair);
			if (getDomain(false).contains(right)) {
				System.out.println("forwardMatches.size"
						+ forwardMatches.size());
				System.out.println(newPair);
				forwardMatches.addPair(newPair);
				System.out.println("forwardMatches.size"
						+ forwardMatches.size());
			} else {
				// don't empty the list
			}
		}
		// (5) update all involved forward rules
		updateInvolvedRules(involvedRules, true);
	}

	/*
	 * function: update the affected region and update involved rules input:
	 * oldBackwardMatches to be recalculated effect: also update all rules that
	 * have been affected by the recalculation
	 */

	private void recalculateAffectedRight(Set<JavaMethod> rights) {
		TreeSet<TransformationRule.RuleID> involvedRules = new TreeSet<TransformationRule.RuleID>();
		for (Iterator<JavaMethod> rightIt = rights.iterator(); rightIt
				.hasNext();) {
			JavaMethod right = rightIt.next();
			ArrayList<TransformationRule.RuleID> applicableRules = rightToRules
					.get(right);
			involvedRules.addAll(applicableRules);
			JavaMethod left = null;
			for (Iterator<TransformationRule.RuleID> appRuleIt = applicableRules
					.iterator(); appRuleIt.hasNext();) {
				TransformationRule.RuleID appRuleID = appRuleIt.next();
				TransformationRule appRule = backwardRules.get(appRuleID);
				appRule.clearState();
				if (left == null && appRule.scopeDisjTranApplicable(right)
						&& appRule.isKept()) {
					left = appRule.convert(right);
				} else if (appRule.scopeDisjTranApplicable(right)
						&& appRule.isKept()) {
					left = appRule.convert(left);
				}
			}
			Pair<JavaMethod> newPair = new Pair<JavaMethod>(left, right);
			if (getDomain(true).contains(left)) {
				System.out.println("backwardMatches.size"
						+ backwardMatches.size());
				System.out.println(newPair);
				backwardMatches.addPair(newPair);
				System.out.println("backwardMatches.size"
						+ backwardMatches.size());
			} else {
				// don't empty the list
			}
		}
		// (5) update all involved backward rules
		updateInvolvedRules(involvedRules, false);
	}

	private void updateInvolvedRules(
			TreeSet<TransformationRule.RuleID> involvedRules, boolean forward) {
		for (Iterator<TransformationRule.RuleID> involvedIt = involvedRules
				.iterator(); involvedIt.hasNext();) {
			if (forward) {
				TransformationRule involved = forwardRules.get(involvedIt
						.next());
				involved.recomputeMatches(this);
			} else {
				TransformationRule involved = backwardRules.get(involvedIt
						.next());
				involved.recomputeMatches(this);
			}
		}
		MatchingResultViewer.highlightRelevantRules(involvedRules);
	}

	public Set<JavaMethod> getDomain(boolean left) {
		if (left) {
			return leftToRules.keySet();
		} else {
			return rightToRules.keySet();
		}
	}

	public SetOfPairs getMatches(boolean forward) {
		if (forward) {
			return forwardMatches;
		} else {
			return backwardMatches;
		}
	}

	public SetOfPairs getUnchangedMatches() {
		SetOfPairs result = new SetOfPairs();
		Set<JavaMethod> unchanged = new TreeSet<JavaMethod>();
		unchanged.addAll(getDomain(true));
		unchanged.retainAll(getDomain(false));

		for (Iterator<JavaMethod> it = unchanged.iterator(); it.hasNext();) {
			JavaMethod jm = it.next();
			Pair p = new Pair(jm, jm);
			result.addPair(p);
		}
		return result;
	}

	public ArrayList<TransformationRule> getExplainingRules(JavaMethod jm,
			boolean left) {
		ArrayList<TransformationRule> rules = new ArrayList<TransformationRule>();
		if (left) {
			if (leftToRules.get(jm) != null) {
				for (Iterator<TransformationRule.RuleID> it = leftToRules.get(
						jm).iterator(); it.hasNext();) {
					TransformationRule.RuleID id = it.next();
					rules.add(forwardRules.get(id));
				}
			}
		} else {
			if (rightToRules.get(jm) != null) {
				for (Iterator<TransformationRule.RuleID> it = rightToRules.get(
						jm).iterator(); it.hasNext();) {
					TransformationRule.RuleID id = it.next();
					rules.add(backwardRules.get(id));
				}
			}
		}
		return rules;
	}

	public int numUnMatched(boolean left) {
		return getDomain(left).size() - getMatches(left).size();
	}

	public int numCheckedRules(boolean forward) {
		Collection<TransformationRule> rules = null;
		if (forward)
			rules = forwardRules.values();
		else
			rules = backwardRules.values();
		int count = 0;
		for (Iterator<TransformationRule> it = rules.iterator(); it.hasNext();) {
			if (it.next().isChecked())
				count++;
		}
		return count;
	}

	public int numMarkedRules(boolean forward) {
		Collection<TransformationRule> rules = null;
		if (forward)
			rules = forwardRules.values();
		else
			rules = backwardRules.values();
		int count = 0;
		for (Iterator<TransformationRule> it = rules.iterator(); it.hasNext();) {
			if (it.next().isKept())
				count++;
		}
		return count;
	}

	public int numRules(boolean forward) {
		if (forward)
			return forwardRules.size();
		else
			return backwardRules.size();
	}

	public void printStat(PrintStream p) {
		if (p == null)
			return;
		// num unmatched in left.
		int numLeft = getDomain(true).size();
		int numRight = getDomain(false).size();

		// num unmatched in right.
		int numLeftMatched = numLeft - numUnMatched(true);
		int numRightMatched = numRight - numUnMatched(false);

		// forward num rule.
		// backward num rule
		int numFRule = numMarkedRules(true);
		int numBRule = numMarkedRules(false);

		// forward match/rule ratio
		// backward match/rule ratio
		double numFRatio = (double) numLeftMatched / (double) numFRule;
		double numBRatio = (double) numRightMatched / (double) numBRule;
		p.println(numLeft + "\t" + numRight + "\t" + numLeftMatched + "\t"
				+ numRightMatched + "\t" + numFRule + "\t" + numBRule + "\t"
				+ numFRatio + "\t" + numBRatio + "\t");
	}

	double[][] getPrecisionRecallRatio(boolean forward,
			SetOfPairs evaluationDataMatches, int ceilingMatch) {
		// for all rules (not only the marked rules)
		// precision = # (matches at that time ^ correct matches) / #
		// numMatchSoFar
		// recall = # (matches at that time ^ correct matches)/ # finalMatches
		double[][] precisionRecall = null;
		TransformationRule[] rules = getSortedRules(forward);
		precisionRecall = new double[rules.length][7];
		SetOfPairs matchesSoFar = new SetOfPairs();
		SetOfPairs correctMatchesSoFar = new SetOfPairs();
		double numEvalMatch = (double) evaluationDataMatches.size();
		int index = 0;
		// int numMatchByRule = -1;
		for (int i = 0; i < rules.length; i++) {
			TransformationRule rule = rules[i];
			ListOfPairs matches = rule.getPositiveMatches();
			matchesSoFar.addSetOfPairs(matches);
			for (Iterator<Pair> pIt = matches.iterator(); pIt.hasNext();) {
				Pair p = pIt.next();
				if (evaluationDataMatches.contains(p)) {
					correctMatchesSoFar.addPair(p);
				}
			}
			double numMatchSoFar = (double) matchesSoFar.size();
			double numCorrectMatchSoFar = (double) correctMatchesSoFar.size();
			double precision = numCorrectMatchSoFar / numMatchSoFar;
			double recall = numCorrectMatchSoFar / numEvalMatch;
			double ratio = numMatchSoFar / (double) (i + 1);
			// if (numMatchByRule <0) {
			// numMatchByRule = matches.size();
			// }else if (numMatchByRule!= matches.size()) {
			// numMatchByRule= matches.size();
			precisionRecall[index][0] = i+1;
			precisionRecall[index][1] = numMatchSoFar;
			precisionRecall[index][2] = precision;
			precisionRecall[index][3] = numMatchSoFar / ceilingMatch;
			precisionRecall[index][4] = recall;
			precisionRecall[index][5] = ratio;
			precisionRecall[index][6] = numMatchSoFar/ this.getMatches(forward).size();
			index++;
			// }
		}
		return precisionRecall;
	}

	public void setManualOneToOneMatches(SetOfPairs sp) {
		this.manualOneToOneMatches = sp;
	}

	public SetOfPairs getManualOneToOneMatches() {
		return this.manualOneToOneMatches;
	}
	public double getPrecision () {
		return precision;
	}
	public double getYield () {
		return yield;
	}
	public double getMRratio() {
		return MRratio;
	}
	public double getRecall() {
		return recall;
	}

	public void setPrecYieldRecallRatioUsingSetOfPairs (SetOfPairs evalMatches, boolean forward) {
		int maxCeling = this.getDomain(forward).size();
		double[][] evalResult = this.getPrecisionRecallRatio(forward,
				evalMatches, maxCeling);
		this.precision = evalResult[evalResult.length-1][2];
		this.yield = evalResult[evalResult.length-1][3];
		this.recall = evalResult[evalResult.length-1][4];
		this.MRratio = evalResult[evalResult.length-1][5];
	}
	public void setForwardTimeInMillis(long runningTimeInMillis) {
		this.forwardTimeInMillis = runningTimeInMillis;
	}
	public long getForwardTimeInMillis() {
		return forwardTimeInMillis;
	}
	public void setBackwardTimeInMillis(long runningTimeInMillis) {
		this.backwardTimeInMillis = runningTimeInMillis;
	}
	public long getBackwardTimeInMillis() {
		return backwardTimeInMillis;
	}
	public void print3WayComparisonChart(PrintStream p,
			SetOfPairs Other, SetOfPairs nToNLabelledMatches) {
		SetOfPairs F = this.getMatches(true);
		SetOfPairs B = this.getMatches(false);
		SetOfPairs M = new SetOfPairs();
		M.addSetOfPairs(F);
		M.addSetOfPairs(B);
		p.print("\t"+F.size());
		p.print("\t"+B.size());
		p.print("\t"+M.size());
		p.print("\t"+Other.size());
		p.print("\t"+nToNLabelledMatches.size());
		print2WayPrecisionRecall(p, F, nToNLabelledMatches);
		print2WayPrecisionRecall(p, B, nToNLabelledMatches);
		print2WayPrecisionRecall(p, M, nToNLabelledMatches);
		print2WayPrecisionRecall(p, Other, nToNLabelledMatches);
		SetOfPairs F_minus_Other = Comparison.leftMinusRight(F,Other);
		print2WayPrecision(p, F_minus_Other, nToNLabelledMatches);
		SetOfPairs B_minus_Other = Comparison.leftMinusRight(B, Other);
		print2WayPrecision(p, B_minus_Other, nToNLabelledMatches);
		SetOfPairs M_minus_Other = Comparison.leftMinusRight(M, Other);
		print2WayPrecision(p, M_minus_Other, nToNLabelledMatches);
		SetOfPairs Other_minus_M = Comparison.leftMinusRight(Other, M);
		print2WayPrecision(p, Other_minus_M, nToNLabelledMatches);
		p.print("\n");
	}
	private void print2WayPrecisionRecall(PrintStream p, SetOfPairs target,
			SetOfPairs evaluator) {
		int common = Comparison.common(target, evaluator).size();
		p.print("\t"+common);
		double precision = (double) (common) / (double) (target.size());
		p.print("\t"+precision);
		double recall = (double) (common) / (double) (evaluator.size());
		p.print("\t"+recall);
	}
	private void print2WayPrecision(PrintStream p, SetOfPairs target,
			SetOfPairs evaluator) {
		p.print("\t"+target.size());
		int common = Comparison.common(target, evaluator).size();
		p.print("\t"+common);
	}
	public static void printComparisonChartRow(PrintStream p) {
		p.print("\tF");
		p.print("\tB");
		p.print("\tM");
		p.print("\tO");
		p.print("\tNtoN");
		
		p.print("\tF^NtoN");
		p.print("\tF.precision");
		p.print("\tF.recall");
		
		p.print("\tB^NtoN");
		p.print("\tB.precision");
		p.print("\tB.recall");
		
		p.print("\tM^NtoN");
		p.print("\tM.precision");
		p.print("\tM.recall");
		
		p.print("\tO^NtoN");
		p.print("\tO.precision");
		p.print("\tO.recall");
		
		p.print("\tF-O");
		p.print("\t(F-O)^NtoN");
		
		p.print("\tB-O");
		p.print("\t(B-O)^NtoN");
		
		p.print("\tM-O");
		p.print("\t(M-O)^NtoN");
		
		p.print("\tOther-M");
		p.print("\t(Other-M)^NtoN\n");
	}
}