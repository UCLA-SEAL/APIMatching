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
package edu.washington.cs.rules;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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

import edu.washington.cs.util.ListOfPairs;
import edu.washington.cs.util.Pair;
import edu.washington.cs.util.SetOfPairs;

public class Rule implements Comparable {

	private static final String xmlTag = "rule";
	
	public static void setExceptionThreshold (double EXCEPTION_THRESHOLD) { 
		THException = EXCEPTION_THRESHOLD;
	}
	// valid proportion

	private static double THException = 0.25; // default exception value
	
	private static final int THConflict = 0;

	private final Scope scope;

	// immutable
	private final Change change;

	// immutable
	private final ListOfPairs positiveMatches;

	// immutable
	// 5.2.06
	// private final ListOfPairs remainingNegativeMatches;

	// immutable

	// in domain
	// |rule.left in domain.left && rule.isApplicable()==true && (match.right in
	// domain.right)|
	private final int positive; // truePositive applies but in domain

	// 3. positive (immutable)
	// 3.1. for the same rule, positive does not change for each iteration
	// 3.2. for the children rules, positive is decreasing for its children

	// out of domain
	// |rule.left in domain.left && rule.isApplicable()==true && match.right not
	// in domain.right|
	private final int negative; // falsePositive applies but not in domain.

	// 4. negative (immutable)
	// 4.1. for the same rule, negative does not change for each iteration
	// 4.2. for the children rules, negative is decreasing for its children.

	// 1. remainingPositive = |positive.left - acceptedMatches.left|
	// |rule.left in remaining.left && rule.isApplicable()==true && (match.righ
	// in domain.right) |
	private int remainingPositive;

	// 1.1. for the same rule, remainingPositive is decreasing for the next
	// iteration
	// 1.2. for children rules, remainingPositive is descreasing for its
	// children

	// 5. remainingNegative = |negative.left && remaining.left|
	// |rule.left in remaining.left && rule.isApplicable()==true && match.right
	// not in domain.right|
	// private int remainingNegative;
	// 5.1. for the same rule, remainingNegative is decreasing for the next
	// iteration
	// 5.2 for children rules, remainingNegative is decreasing for its children

	// 2. conflictPositive = |positive.left == acceptedMatches.left &&
	// positive.right != acceptedMatches.right |
	// 2.1. for the same rule, conflictPositive may be increasing for the next
	// iteration
	// 2.2. for children rules, conflictPositive may be decreasing for its
	// children
	private int conflictPositive;

	private boolean validity;

	private int inScopeButnotApplicable; 
	
	public ListOfPairs getNegativeMatches(RuleBase rb) {
		ListOfPairs negativeMatches = new ListOfPairs();
		ArrayList<JavaMethod> originalLeft = rb.originalLeft;
		ArrayList<JavaMethod> originalRight = rb.originalRight;
		for (Iterator<JavaMethod> leftIt = originalLeft.iterator(); leftIt
				.hasNext();) {
			JavaMethod jmLeft = leftIt.next();
			// rule.left in domain.left
			if (this.isApplicable(jmLeft)) {
				// check whether change is in the newVersion
				JavaMethod jmConverted = this.change
						.applyTransformation(jmLeft);
				Pair<JavaMethod> accepted = rb.getAcceptedMatches().getFirstMatchByLeft(
						jmLeft);
				if (accepted != null ) {
					if (this.change.equivalent(jmConverted, accepted
							.getRight())) {
//						 consistent with already accepted
					}else {
//						inconsistent with already accepted 
					 	negativeMatches.addPair(accepted);
					}
				} else if (originalRight.contains(jmConverted)) {
					// in the right domain
				} else {
					// not conflict, just not in the right domain
					Pair<JavaMethod> pair = new Pair<JavaMethod>(jmLeft,
							null);
					negativeMatches.addPair(pair);
				}
			}
		}
		return negativeMatches;
	}
	public ListOfPairs getRefreshPositiveMatches(RuleBase rb){
		// 7.3.06
		ListOfPairs result = new ListOfPairs();
		ArrayList<JavaMethod> originalLeft = rb.originalLeft;
		ArrayList<JavaMethod> originalRight = rb.originalRight;
		for (Iterator<JavaMethod> leftIt = originalLeft.iterator(); leftIt
				.hasNext();) {
			JavaMethod jmLeft = leftIt.next();
			// rule.left in domain.left
			if (this.isApplicable(jmLeft)) {
				// check whether change is in the newVersion
				JavaMethod jmConverted = this.change
						.applyTransformation(jmLeft);
				Pair<JavaMethod> accepted = rb.getAcceptedMatches().getFirstMatchByLeft(jmLeft);
				if (accepted !=null
						&& this.change.equivalent(jmConverted, accepted.getRight())) {
					result.addPair(accepted);
				} else if (originalRight.contains(jmConverted)) {
					Pair<JavaMethod> pair = new Pair<JavaMethod>(jmLeft,
							jmConverted);
					result.addPair(pair);
					// rule.left in domain.left && rule.right in domain.right
				}
			}
		}
		return result;
	}
	public Rule(Scope s, Change t, RuleBase rb) {
		this.scope = s;
		this.change = t;
		// optimization, don't recompute if the same rule is already computed
		if (rb.isHit(this.getID())) {
			Rule alreadyHit = rb.retrieveCache(this.getID());
			this.positiveMatches = alreadyHit.positiveMatches;
			// 5.2.2006
			// this.remainingNegativeMatches =
			// alreadyHit.remainingNegativeMatches;
			this.positive = alreadyHit.positive;
			this.negative = alreadyHit.negative;
			this.updateFrom(alreadyHit);
			return;
		}
		this.positiveMatches = new ListOfPairs();
		// 5.2.06
		// this.remainingNegativeMatches = new ListOfPairs();
		int positive = 0;
		int negative = 0;
		ArrayList<JavaMethod> originalLeft = rb.originalLeft;
		ArrayList<JavaMethod> originalRight = rb.originalRight;
		for (Iterator<JavaMethod> leftIt = originalLeft.iterator(); leftIt
				.hasNext();) {
			JavaMethod jmLeft = leftIt.next();
			// rule.left in domain.left
			if (this.isApplicable(jmLeft)) {
				// check whether change is in the newVersion
				JavaMethod jmConverted = this.change
						.applyTransformation(jmLeft);
				Pair<JavaMethod> accepted = rb.getAcceptedMatches().getFirstMatchByLeft(jmLeft);
				if (accepted !=null
						&& this.change.equivalent(jmConverted, accepted.getRight())) {
					
					this.positiveMatches.addPair(accepted);
					positive++;
				} else if (originalRight.contains(jmConverted)) {
					Pair<JavaMethod> pair = new Pair<JavaMethod>(jmLeft,
							jmConverted);
					this.positiveMatches.addPair(pair);
					// rule.left in domain.left && rule.right in domain.right
					positive++;
				} else {
					Pair<JavaMethod> pair = new Pair<JavaMethod>(jmLeft,
							jmConverted);
					// rule.left in domain.left && rule.right not in
					// domain.right
					negative++;
					// 5.2.06
					// this.remainingNegativeMatches.addPair(pair);
				}
			}else {
				inScopeButnotApplicable++;
			}
		}
		// set the positive and negative number
		this.positive = positive;
		this.negative = negative;
		// update remaining positive
		recompute(rb);
		rb.pushCache(this);

	}
	public Rule(JavaMethod seed, Change t, RuleBase rb) {
		// find the most general scopeDisj for this change
		this.scope = new Scope(seed, t);
		this.change = t;
		// optimization, don't recompute if the same rule is already computed
		if (rb.isHit(this.getID())) {
			Rule alreadyHit = rb.retrieveCache(this.getID());
			this.positiveMatches = alreadyHit.positiveMatches;
			// 5.2.06
			// this.remainingNegativeMatches =
			// alreadyHit.remainingNegativeMatches;
			this.positive = alreadyHit.positive;
			this.negative = alreadyHit.negative;
			this.updateFrom(alreadyHit);
			return;
		}
		this.positiveMatches = new ListOfPairs();
		// 5.2.06
		// this.remainingNegativeMatches = new ListOfPairs();
		int positive = 0;
		int negative = 0;
		ArrayList<JavaMethod> originalLeft = rb.originalLeft;
		ArrayList<JavaMethod> originalRight = rb.originalRight;
		for (Iterator<JavaMethod> leftIt = originalLeft.iterator(); leftIt
				.hasNext();) {
			JavaMethod jmLeft = leftIt.next();
			if (this.isApplicable(jmLeft)) {
				// check whether change is in the newVersion
				JavaMethod jmConverted = this.change
						.applyTransformation(jmLeft);
				if (originalRight.contains(jmConverted)) {
					Pair<JavaMethod> pair = new Pair<JavaMethod>(jmLeft,
							jmConverted);
					this.positiveMatches.addPair(pair);
					positive++;
				} else {
					Pair<JavaMethod> pair = new Pair<JavaMethod>(jmLeft,
							jmConverted);
					// 5.2.06
					// this.remainingNegativeMatches.addPair(pair);
					negative++;
				}
			}
		}
		// set the positive and negative number
		this.positive = positive;
		this.negative = negative;
		// update remaining positive
		recompute(rb);
		rb.pushCache(this);
	}

	private void recompute(RuleBase rb) {
		// recompute remainingPositive and conflictPositive
		this.remainingPositive = 0;
		// 5.2.06
		// this.remainingNegative = 0;
		this.conflictPositive = 0;
		SetOfPairs acceptedMatches = rb.getAcceptedMatches();
		Set<JavaMethod> acceptedLeft = acceptedMatches.getLeftDomain();
		// 1. remainingPositive = |positive.left - acceptedMatches.left|
		// 2. conflictPositive = |positive.left == acceptedMatches.left &&
		// positive.right != acceptedMatches.right |
		for (Iterator<Pair> positiveIt = this.positiveMatches.iterator(); positiveIt
				.hasNext();) {
			Pair positive = positiveIt.next();
			JavaMethod positiveLeft = (JavaMethod) positive.getLeft();
			JavaMethod positiveRight = (JavaMethod) positive.getRight();
			if (acceptedLeft.contains(positiveLeft)) {
				// get the match that should be compared to
				JavaMethod acceptedRight = (JavaMethod) (acceptedMatches
						.getFirstMatchByLeft(positiveLeft).getRight());
				if (!this.change.equivalent(acceptedRight, positiveRight)) {
					// conflict. -- conflicting with something that was already
					// found
					conflictPositive++;
				} else {
					// overlapping. -- finding something that was already found
					// at least in terms of transformation, they are the same.
				}
			} else {
				// additional matches.
				this.remainingPositive++;
			}
		}
		updateValidity();

	}

	void recompute(ListOfPairs additionalMatched) {
		Set<JavaMethod> additionalMatchedLeft = additionalMatched
				.getLeftDomain();
		// 1. remainingPositive = |positive.left - acceptedMatches.left|
		// 2. conflictPositive = |positive.left == acceptedMatches.left &&
		// positive.right != acceptedMatches.right |
		for (Iterator<Pair> positiveIt = this.positiveMatches.iterator(); positiveIt
				.hasNext();) {
			Pair positive = positiveIt.next();
			JavaMethod positiveLeft = (JavaMethod) positive.getLeft();
			JavaMethod positiveRight = (JavaMethod) positive.getRight();
			if (additionalMatchedLeft.contains(positiveLeft)) {
				// get the match that should be compared to
				JavaMethod acceptedRight = (JavaMethod) additionalMatched
						.getRightByLeft(positiveLeft);
				if (!this.change.equivalent(acceptedRight, positiveRight)) {
					// conflict. -- conflicting with something that was already
					// found
					conflictPositive++;
				} else {
					// overlapping. -- finding something that was already found
					// at least in terms of transformation, they are the same.
				}
				remainingPositive--;
			}
		}

		updateValidity();

	}

	private void updateValidity() {
		// 1. remainingPositive >0
		boolean cond1 = (this.remainingPositive > 0);
		// 2. positive > TH * negative
		boolean cond2 = (int)(((double) (this.positive+ this.negative)) * this.THException) >= (this.negative + this.conflictPositive);
		// 3. there's NO conflict with rb's accepted matches
//		boolean cond3 = (this.conflictPositive <= THConflict);
		boolean cond3 = true; 
		this.validity = cond1 && cond2 && cond3;
	}

	public boolean isValidAfterInference() {

		boolean cond2 = (int) (((double) (this.positive + this.negative)) * this.THException) >= (this.negative
				+ this.conflictPositive);
//		boolean cond2 = (int) (((double) (this.positive + this.negative + this.inScopeButnotApplicable)) * this.THException) >= (this.negative
//				+ this.conflictPositive + this.inScopeButnotApplicable);
		// 3. there's NO conflict with rb's accepted matches
		// boolean cond3 = (this.conflictPositive <= THConflict);
		boolean cond3 = true;
		return cond2 && cond3;
		
	}
	public boolean isValidDuringInference() {
		updateValidity();
		// the best given the transformations
		return validity;
	}
	public boolean isRemovable() {
		return (this.remainingPositive == 0);
	}

	public boolean isWorthExpandingChildren(int currentMaxRemainingPositive) {
		boolean cond1 = (this.remainingPositive > currentMaxRemainingPositive);
		// if this.remainingPositive < currentMaxRemaingPositive, we don't need
		// to expand now.

		boolean cond2 = (int)( (((double) (this.positive+this.negative)))* this.THException) < (this.negative+ this.conflictPositive);
		// if this.positive> this.THProportion * this.negative, then the rule is
		// valid, so we don't need to expand children anymore.
		// if (cond2) {
		// System.out.print("R");
		// }
//		boolean cond3 = (this.conflictPositive > THConflict);
		// if this.conflictPositive<=0, then the rule can be valid.
		// if (cond3) {
		// System.out.print("C");
		// }
		boolean invalid =  cond2;

		return (cond1 && invalid);
	}

	public void updateFrom(Rule alreadyComputed) {
		assert (alreadyComputed.getID().equals(this.getID()));
		this.conflictPositive = alreadyComputed.conflictPositive;
		this.remainingPositive = alreadyComputed.remainingPositive;
		// 05.02.06
		// this.remainingNegative = alreadyComputed.remainingNegative;
		this.validity = alreadyComputed.validity;
	}

	public ListOfPairs getMatches() {
		return this.positiveMatches;
	}

	public int numRemainingPositive() {
		return this.remainingPositive;
	}

	public int numPositive() {
		return this.positive;
	}
	public int numNegative() {
		return this.negative;
	}
	public int numConflictPositive() {
		return this.conflictPositive;
	}

	/* refine its scope by bottom up scope construction */ 
	
	public Rule getCleanerScopeRule(RuleBase rb, JavaMethod seed, ListOfPairs pos, ListOfPairs neg){
		// create a new scope by bottom up construction
		Set<JavaMethod> lefts = positiveMatches.getLeftDomain();
		Scope cleanerScope = null;
		if (lefts.size() == 1) {
			cleanerScope = new Scope(seed);
			cleanerScope.refineMostSpecific();
		} else {
			ScopeDisjunction bottomUp = new ScopeDisjunction();
			for (Iterator<JavaMethod> leftIt = lefts.iterator(); leftIt
					.hasNext();) {
				JavaMethod left = leftIt.next();
				Scope s = new Scope(left);
				s.refineMostSpecific();
				bottomUp.add(s);
			}
			cleanerScope = Scope.combine(bottomUp);
			// otherwise, set seed
			cleanerScope.setSeed(seed);
		}
		// create a new rule with the new generalized scope 
		if (cleanerScope != null) {
			Rule rule = new Rule(cleanerScope, this.change, rb);
			ListOfPairs pos2 = rule.getMatches();
			boolean b1 = pos2.includeAll(pos);
			boolean b2 = pos.includeAll(pos2);
			ListOfPairs neg2 = rule.getNegativeMatches(rb);
			boolean b3 = neg.includeAll(neg2);
			boolean b4 = neg2.includeAll(neg);
			if (b1 && b2 && b3 && b4)
				return rule;
		}
		return null;
	}
	public TreeSet<Rule> createChildrenRules(RuleBase rb,
			int currentMaxRemainingPositive, TreeSet<Scope> triedScope) {

		if (triedScope == null)
			triedScope = new TreeSet<Scope>();
		
		// recursively expand children until
		// for each rule, either
		// (1) its remainingPositive is smaller than currentMaxRemainingPositive
		// (2) or, its rule is valid
		if (this.isWorthExpandingChildren(currentMaxRemainingPositive)) {
			TreeSet<Rule> okRules = new TreeSet<Rule>();
			TreeSet<Rule> badRules = new TreeSet<Rule>();
//			System.out.println("Parent "+ this.getStat());
//			System.out.println("Tried Scope " + triedScope.size());
			List<Scope> scopes = this.scope.createNextChildren(triedScope);
			if (scopes == null)
				return null;
			triedScope.addAll(scopes);
			// if it's impossible to create more children return null;
			for (int i = 0; i < scopes.size(); i++) {
				Scope s = scopes.get(i);
				Rule r = new Rule(s, this.change, rb);
				
				// if I found a rule that gets the most possible number of
				// benefits and if that's valid rule
				if (r.numRemainingPositive() == this.numRemainingPositive()
						&& r.isValidDuringInference()) {
					// return this child only, because exploring other things
					// does not make any difference
					okRules = new TreeSet<Rule>();
					okRules.add(r);
//					System.out.println("OK Return Child "+r.getStat());
					return okRules;
				}
				if (r.isRemovable() == false) {
					if (r.numRemainingPositive() < currentMaxRemainingPositive
							|| r.isValidDuringInference()) {
//						System.out.println("OK Child " + r.getStat());
						okRules.add(r);
					} else {
						badRules.add(r);
//						System.out.println("Bad Child "+ r.getStat());
					}
				}
			}
			if (badRules.size() == 0) {
				return okRules;
			} else if (badRules.size() > 0) {
				// recursively expand more children until there are not
				// badRules.
				for (Iterator<Rule> badIt = badRules.iterator(); badIt
						.hasNext();) {
					Rule badRule = badIt.next();
					TreeSet<Rule> grandchildren = badRule.createChildrenRules(
							rb, currentMaxRemainingPositive, triedScope);
					if (grandchildren != null) {
						okRules.addAll(grandchildren);
					}
				}
				
				return okRules;
			}
		}
		return null;
	}

	// equals, compareTo, hashCode
	// sort by remainingPositive,
	// then by name
	public boolean equals(Object o) {
		Rule other = (Rule) o;
		return (this.getComparisonID().equals(other.getComparisonID()));
	}

	public int compareTo(Object o) {
		Rule other = (Rule) o;
		if (this.remainingPositive != other.remainingPositive) {
			return (this.remainingPositive - other.remainingPositive);
		} else {
			return (this.getComparisonID().compareTo(other.getComparisonID()));
		}
	}

	public int hashCode() {
		return this.getComparisonID().hashCode();
	}

	public String toString() {
		String s = scope.toString();
		s = s + "\n\t" + change.toString();
		s = s + "\n\tP" + positive + "\tN" + negative + "\trP"
				+ remainingPositive + "\tcP" + conflictPositive + "\tV"
				+ validity;
		s = s + "\n\tworthExpand\t" + isWorthExpandingChildren(0);
		s = s + "\n\t" + this.positiveMatches;
		return s;
	}

	public String getStat() {
		String s = scope.toString();
		s = s + "\n\t" + change.toString();
		s = s + "\n\tP" + positive + "\tN" + negative + "\trP"
				+ remainingPositive + "\tcP" + conflictPositive + "\tV"
				+ validity+ 
						"\t"+((double)negative *(double)100/((double)negative+(double)positive));
		s = s + "%\n\tworthExpand\t" + isWorthExpandingChildren(0);
		return s;
	}

	private String getComparisonID() {
		String s = scope.toString();
		s = s + change.toString();
		s = s + scope.getSeed();
		return s;
	}

	public String getID() {
		String s = scope.toString();
		s = s + change.toString();
		return s;
	}

	public boolean isApplicable(JavaMethod jm) {
		return this.scope.match(jm) && this.change.isApplicable(jm);
	}

//	public static String getConf() {
//		return new Double(THException).toString();
//		//+ "" + THRemainingPositive + "" + THConflict;
//	}

	public void mapByChange(HashMap<Change, ArrayList<Rule>> map) {
		if (map.containsKey(this.change)) {
			ArrayList<Rule> v = map.get(this.change);
			v.add(this);
		} else {
			ArrayList<Rule> v = new ArrayList<Rule>();
			v.add(this);
			map.put(this.change, v);
		}
	}

	public void mapByScope(HashMap<Scope, ArrayList<Rule>> map) {
		if (map.containsKey(this.scope)) {
			ArrayList<Rule> v = map.get(this.scope);
			v.add(this);
		} else {
			ArrayList<Rule> v = new ArrayList<Rule>();
			v.add(this);
			map.put(this.scope, v);
		}
	}

	public Change getChange() {
		return this.change;
	}

	public Scope getScope() {
		return this.scope;
	}

	public void mapByTransformation(HashMap<Transformation, ArrayList<Rule>> map) {
		this.change.mapByTransformation(map, this);
	}

	private Rule(boolean v, int p, int n, int rp, int cp, Scope s, Change c,
			ListOfPairs lp) {
		this.validity = v;
		this.positive = p;
		this.negative = n;
		this.remainingPositive = rp;
		this.conflictPositive = cp;
		this.scope = s;
		this.change = c;
		this.positiveMatches = lp;
	}

	public void writeElement(Element parent) {
		Element thisNode = parent.getOwnerDocument().createElement(getXMLTag());
		thisNode.setAttribute("v", new Boolean(validity).toString());
		thisNode.setAttribute("p", new Integer(positive).toString());
		thisNode.setAttribute("n", new Integer(negative).toString());
		thisNode.setAttribute("rp", new Integer(remainingPositive).toString());
		thisNode.setAttribute("cp", new Integer(conflictPositive).toString());
		this.scope.writeElement(thisNode);
		this.change.writeElement(thisNode);
		this.positiveMatches.writeElement(thisNode);
		parent.appendChild(thisNode);
	}

	public static String getXMLTag() { 
		return xmlTag;
	}
	public static Rule readElement(Element rule) {
		if (!rule.getTagName().equals(getXMLTag()))
			return null;
		boolean validity = new Boolean(rule.getAttribute("v")).booleanValue();
		int positive = new Integer(rule.getAttribute("p")).intValue();
		int negative = new Integer(rule.getAttribute("n")).intValue();
		int remainingPositive = new Integer(rule.getAttribute("rp")).intValue();
		int conflictPositive = new Integer(rule.getAttribute("cp")).intValue();
		NodeList children = rule.getChildNodes();
		Scope scope = null;
		Change change = null;
		ListOfPairs listpair = null;
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i) instanceof Element) {
				Element child = (Element) children.item(i);
				if (child.getTagName().equals(Scope.getXMLTag())) {
					scope = Scope.readElement(child);
				} else if (child.getTagName().equals(Change.getXMLTag())) {
					change = Change.readElement(child);
				} else if (child.getTagName().equals(ListOfPairs.getXMLTag())) {
					listpair = ListOfPairs.readElement(child);
				}
			}
		}
		return new Rule(validity, positive, negative, remainingPositive,
				conflictPositive, scope, change, listpair);
	}

	public void writeXMLFile(String filename) {
		Document doc = DOMImplementationImpl.getDOMImplementation()
				.createDocument("namespaceURI", "changelist", null);
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

	public static Rule readXMLFile(String filename) {
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

	public static void main(String args[]) {
		String methodPair[] = {

				"com.jrefinery.chart.axis:TickUnit-compareTo__[Object]->int",
				"com.jrefinery.chart:TickUnit-compareTo__[Object]->int",

				"com.jrefinery.chart.plot:XYPlot-draw__[Graphics2D, Rectangle2D, ChartRenderingInfo]->void",
				"com.jrefinery.chart:XYPlot-draw__[Graphics2D, Rectangle2D, ChartRenderingInfo]->void",

				"com.jrefinery.chart.renderer:StackedHorizontalBarRenderer-StackedHorizontalBarRenderer__[CategoryToolTipGenerator, CategoryURLGenerator]->void",
				"com.jrefinery.chart:StackedHorizontalBarRenderer-StackedHorizontalBarRenderer__[]->void",

				"com.jrefinery.chart.renderer:StackedVerticalBarRenderer-StackedVerticalBarRenderer__[CategoryToolTipGenerator, CategoryURLGenerator]->void",
				"com.jrefinery.chart:StackedVerticalBarRenderer-StackedVerticalBarRenderer__[]->void",

				"com.jrefinery.chart:ChartFactory-ChartFactory__[String, ChartFactory[], ChartFactory[], XYDataset, boolean]->ChartFactory[]",
				"com.jrefinery.chart:Chart-Chart__[String, Chart[], Chart[], XYDataset, boolean]->Chart[]",

				"com.jrefinery.chart:ChartFactory$A-createStackedHorizontalBarChart$B__[String, String, String, CategoryDataset, boolean, boolean, boolean]->JFreeChart",
				"com.jrefinery.chart:ChartFactory-createStackedHorizontalBarChart__[String, String, String, CategoryDataset, boolean]->JFreeChart",

				"com.jrefinery.chart:ChartFactory-createStackedVerticalBarChart__[String, String, String, CategoryDataset, boolean, boolean, boolean]->JFreeChart",
				"com.jrefinery.chart:ChartFactory-createStackedVerticalBarChart__[String, String, String, CategoryDataset, boolean]->JFreeChart",

				"com.jrefinery.chart:ChartFactory-createVerticalBarChart3D__[String, String, String, CategoryDataset, boolean, boolean, boolean]->JFreeChart",
				"com.jrefinery.chart:ChartFactory-createVerticalBarChart3D__[String, String, String, CategoryDataset, boolean]->JFreeChart",

				"com.jrefinery.chart:Legend-Legend__[JFreeChart]->void",
				"com.jrefinery.chart:Legend-Legend__[JFreeChart, int]->void",

				"com.jrefinery.chart:StandardLegend-StandardLegend__[JFreeChart, Spacer, Spacer, Paint, Stroke, Paint, Font, Paint]->void",
				"com.jrefinery.chart:StandardLegend-StandardLegend__[JFreeChart, int, Spacer, Paint, Stroke, Paint, Font, Paint]->void",

				"com.jrefinery.chart:ChartFactory-createScatterPlot__[String, String, String, XYDataset, boolean]->JFreeChart",
				"com.jrefinery.chart:ChartFactory-createScatterPlot__[String, String, String, XYDataset, boolean, boolean, boolean]->JFreeChart",

				"com.jrefinery.chart:AbstractXYItemRenderer-getPlot__[]->XYPlot",
				"com.jrefinery.chart.renderer:XYItemRenderer-getPlot__[]->Plot",

				"com.jrefinery.chart:StandardXYItemRenderer-getDefaultShapeScale__[]->double",
				"com.jrefinery.chart.renderer:StandardXYItemRenderer-getDefaultShapeFilled__[]->boolean",

				"com.jrefinery.chart:StandardXYItemRenderer-getShapeScale__[Plot, int, int, double, double]->double",
				"com.jrefinery.chart.renderer:StandardXYItemRenderer-getImage__[Plot, int, int, double, double]->Image",

				"com.jrefinery.chart:ValueAxis-getAutoRangeMinimumSize__[]->Number",
				"com.jrefinery.chart.axis:ValueAxis-getAutoRangeMinimumSize__[]->double",

				"com.jrefinery.data:DefaultCategoryDataset-setSeriesName__[int, String]->void",
				"com.jrefinery.data:DefaultIntervalCategoryDataset-setSeriesKeys__[Comparable[]]->void",

				"org.jfree.chart.labels:StandardXYItemLabelGenerator-StandardXYItemLabelGenerator__[String, String, DateFormat, NumberFormat]->void",
				"org.jfree.chart.labels:StandardXYLabelGenerator-StandardXYLabelGenerator__[String, DateFormat, DateFormat]->void",

				"org.jfree.chart.renderer:AbstractCategoryItemRenderer-getRangeType__[]->RangeType",
				"org.jfree.chart.renderer:AbstractCategoryItemRenderer-getRangeExtent__[CategoryDataset]->Range",

				"org.jfree.chart.renderer:StackedBarRenderer3D-getRangeType__[]->RangeType",
				"org.jfree.chart.renderer:StackedBarRenderer3D-getRangeExtent__[CategoryDataset]->Range",

				"org.jfree.chart.renderer:WaterfallBarRenderer-getRangeType__[]->RangeType",
				"org.jfree.chart.renderer:WaterfallBarRenderer-getRangeExtent__[CategoryDataset]->Range",

				"org.jfree.chart.renderer:XYAreaRendererState-XYAreaRendererState__[PlotRenderingInfo]->void",
				"org.jfree.chart.renderer:XYAreaRenderer$XYAreaRendererState-XYAreaRendererState__[PlotRenderingInfo]->void",

				"org.jfree.chart.axis:CategoryAxis-drawHorizontalCategoryLabels__[Graphics2D, double, Rectangle2D, Rectangle2D, RectangleEdge]->double",
				"org.jfree.chart.axis:CategoryAxis-drawCategoryLabels__[Graphics2D, Rectangle2D, Rectangle2D, RectangleEdge, AxisState]->AxisState" };
		JavaMethod[] jmList = new JavaMethod[methodPair.length];
		ArrayList<JavaMethod> oldMethods = new ArrayList<JavaMethod>();
		ArrayList<JavaMethod> newMethods = new ArrayList<JavaMethod>();
		ListOfPairs matchedPairs = new ListOfPairs();
		for (int i = 0; i < methodPair.length; i = i + 2) {
			jmList[i] = new JavaMethod(methodPair[i]);
			jmList[i + 1] = new JavaMethod(methodPair[i + 1]);
			oldMethods.add(jmList[i]);
			newMethods.add(jmList[i + 1]);
			Pair<JavaMethod> p = new Pair<JavaMethod>(jmList[i], jmList[i + 1]);
			matchedPairs.addPair(p);
		}
		RuleBase rb = new RuleBase(oldMethods, newMethods, matchedPairs,THException);
		ArrayList<Rule> candidateRules = new ArrayList<Rule>();
		for (Iterator<Pair> pairIt = matchedPairs.iterator(); pairIt.hasNext();) {
			Pair<JavaMethod> pair = (Pair<JavaMethod>) pairIt.next();
			JavaMethod oldJM = pair.getLeft();
			JavaMethod newJM = pair.getRight();
			List<Change> changes = Change.createChange(oldJM, newJM);
			for (Iterator<Change> changeIt = changes.iterator(); changeIt
					.hasNext();) {
				Change t = changeIt.next();
				Rule rule = new Rule(oldJM, t, rb);
				rule.writeXMLFile("temp");
				Rule copy = Rule.readXMLFile("temp");
				if (!rule.equals(copy)) {
					System.out.println("ERROR");
					System.exit(0);
				}

				if (rule.isRemovable() == false)
					candidateRules.add(rule);
				if (rule.isWorthExpandingChildren(0)) {
					System.out.println("Rule" + rule);
					Scope s = rule.scope;
					System.out.println(s.hierarchicalScope().size());
					TreeSet<Rule> children = new TreeSet<Rule>();
					children = rule.createChildrenRules(rb, 0,
							new TreeSet<Scope>());
					System.out.println("children\t" + children.size());
					for (Iterator<Rule> childIt = children.iterator(); childIt
							.hasNext();) {
						Rule child = childIt.next();
						System.out.println("\t\t" + child);
					}
				}
			}
		}
		for (Iterator<Rule> candIt = candidateRules.iterator(); candIt
				.hasNext();) {
			System.out.println(candIt.next());
		}
		for (int i = 0; i < oldMethods.size(); i++) {
			System.out.println(oldMethods.get(i));
		}
		System.out.println();
		for (int i = 0; i < oldMethods.size(); i++) {
			System.out.println(newMethods.get(i));
		}
	}
	
}