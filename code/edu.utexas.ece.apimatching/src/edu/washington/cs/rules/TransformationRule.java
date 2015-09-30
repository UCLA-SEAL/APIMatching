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
import java.util.Iterator;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.washington.cs.induction.RulebasedMatching;
import edu.washington.cs.util.ListOfPairs;
import edu.washington.cs.util.Pair;
import edu.washington.cs.util.SetOfPairs;

public class TransformationRule implements Comparable {
	private static final String xmlTag = "transformation_rule";

	private final ScopeDisjunction scopeDisj;

	private final Transformation transformation;

	private final Set<JavaMethod> exceptions;

	private final ListOfPairs exceptionMatches;

	private final ListOfPairs positiveMatches;

	private final RuleID ruleId;

	private boolean examineByUser = false; 
	private boolean kept = true; 
	private boolean scopeModify = false; 
	private boolean exceptionModify = false;
	private boolean modified = false; 
	
	private int numConflictException=0;
	
	public int getNumConflictException() { 
		return this.numConflictException;
	}
	private DefaultMutableTreeNode treeNode = null;
	// private final boolean forward;
	// private final int id;
	// invariant
	// all entities in the exception should be applicable to one of the scope
	// all entities in the exception should be applicable to transformation
	/*
	 * create a transformation rule with a single scope
	 */
	public TransformationRule(Scope s, Transformation t, ListOfPairs pm,
			ListOfPairs em, boolean forward, int id) {
		this.scopeDisj = new ScopeDisjunction();
		this.scopeDisj.add(s);
		this.transformation = t;
		this.exceptions = (Set<JavaMethod>) em.getLeftDomain();
		this.exceptionMatches = em;
		this.positiveMatches = pm;
		this.ruleId = new RuleID(id, forward);
		// check the invariant
	}

	/*
	 * create a transformation rule with multiple scopes connected by
	 * disjunction
	 */
	public TransformationRule(ScopeDisjunction s, Transformation t,
			ListOfPairs pm, ListOfPairs em, boolean forward, int id) {
		this.scopeDisj = s;
		this.transformation = t;
		if (em != null)
			this.exceptions = (Set<JavaMethod>) em.getLeftDomain();
		else
			this.exceptions = null;
		this.exceptionMatches = em;
		this.positiveMatches = pm;
		this.ruleId = new RuleID(id, forward);
	}

	/*
	 * Clean up scope disjunction by sorting by the number of matches, then
	 * identifying additional match power. If additional match power is 0, take
	 * them out. input: this input: RulebasedMatching output: return (true if
	 * scopeDisj has changed)
	 */
	private boolean canonizeScopeDisj(RulebasedMatching matchingResult) {
		return false;
	}

	/*
	 * convert JavaMethod if this transformation can be applied to
	 */
	public JavaMethod convert(JavaMethod source) {
		if (source == null)
			return null;
		else
			return transformation.applyTransformation(source);
	}

	/*
	 * Javamethod is applicable to this transformation, (1) if it's not
	 * exceptions, (2) scopeDisj is applicable to, (3) transformation is
	 * applicable to.
	 */
	public boolean isApplicable(JavaMethod source) {
		if (source == null)
			return false;
		return ((scopeDisj.isApplicable(source)
				&& transformation.isApplicable(source) && !exceptions
				.contains(source)));
	}

	public boolean scopeDisjTranApplicable(JavaMethod source) { 
		if (source == null)
			return false;
		return ((scopeDisj.isApplicable(source)
				&& transformation.isApplicable(source)));
	}
	/*
	 * To implement Comparable
	 */
	public int compareTo(Object obj) {
		if (obj instanceof TransformationRule) {
			TransformationRule other = (TransformationRule) obj;
			if (other.equals(this))
				return 0;
		}
		return -1;

	}

	public boolean equals(Object obj) {
		if (obj instanceof TransformationRule) {
			TransformationRule other = (TransformationRule) obj;
			boolean ex = other.exceptions.equals(this.exceptions);
			boolean t = other.transformation.equals(this.transformation);
			boolean s = other.scopeDisj.equals(this.scopeDisj);
			return (ex && t && s);
		}
		return false;
	}

	/*
	 * To serialize as an XML file.
	 */
	public void writeElement(Element parent) {
		Element thisNode = parent.getOwnerDocument().createElement(xmlTag);
		thisNode.setAttribute("ruleId.forward", new Boolean(ruleId.forward)
				.toString());
		thisNode.setAttribute("ruleId.id", new Integer(ruleId.id).toString());
		thisNode.setAttribute("examineByUser",new Boolean(examineByUser).toString());
		thisNode.setAttribute("kept",new Boolean (kept).toString());
		thisNode.setAttribute("scopeModify",new Boolean(scopeModify).toString());
		thisNode.setAttribute("exceptionModify",new Boolean(exceptionModify).toString());
		this.scopeDisj.writeElement(thisNode);
		this.transformation.writeElement(thisNode);
		Element pM = parent.getOwnerDocument().createElement("positiveMatches");
		this.positiveMatches.writeElement(pM);
		thisNode.appendChild(pM);
		Element eM = parent.getOwnerDocument()
				.createElement("exceptionMatches");
		this.exceptionMatches.writeElement(eM);
		thisNode.appendChild(eM);
		parent.appendChild(thisNode);
	}

	public static TransformationRule readElement(Element trans_rule) {
		if (!trans_rule.getTagName().equals(xmlTag))
			return null;
		NodeList children = trans_rule.getChildNodes();
		ScopeDisjunction scopeDisj = null;
		Transformation trans = null;
		ListOfPairs positiveMatches = null;
		ListOfPairs exceptionMatches = null;
		boolean forward = new Boolean(trans_rule.getAttribute("ruleId.forward"))
				.booleanValue();
		int id = new Integer(trans_rule.getAttribute("ruleId.id")).intValue();
			for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i) instanceof Element) {
				Element child = (Element) children.item(i);
				if (child.getTagName().equals(ScopeDisjunction.getXMLTag())) {
					scopeDisj = ScopeDisjunction.readElement(child);
				} else if (child.getTagName()
						.equals(Transformation.getXMLTag())) {
					trans = Transformation.readElement(child);
				} else if (child.getTagName().equals("positiveMatches")) {
					positiveMatches = ListOfPairs.readElement((Element) child
							.getFirstChild());
				} else if (child.getTagName().equals("exceptionMatches")) {
					exceptionMatches = ListOfPairs.readElement((Element) child
							.getFirstChild());
				}
			}
		}
		TransformationRule result = new TransformationRule(scopeDisj, trans,
				positiveMatches, exceptionMatches, forward, id);
		if (!trans_rule.getAttribute("examineByUser").equals("")) {
			result.examineByUser = new Boolean(trans_rule
					.getAttribute("examineByUser")).booleanValue();
		}
		if (!trans_rule.getAttribute("kept").equals("")) {
			result.kept = new Boolean(trans_rule.getAttribute("kept"))
					.booleanValue();
		}
		if (!trans_rule.getAttribute("scopeModify").equals("")) {
			result.scopeModify = new Boolean(trans_rule
					.getAttribute("scopeModify")).booleanValue();
		}
		if (!trans_rule.getAttribute("exceptionModify").equals("")) {
			result.exceptionModify = new Boolean(trans_rule
					.getAttribute("exceptionModify")).booleanValue();
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

	public static String getXMLTag() {
		return xmlTag;
	}

	public static TransformationRule readXMLFile(String filename) {
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

	/**
	 * Display why exceptions are not covered by this transformation rule,
	 * input: matchingResult output: a list of [exception, empty] (if exception
	 * is deleted) [exception, correct match] (if exception is still there, but
	 * mapped to a different entity.
	 */
	public ListOfPairs getExceptionMatches() {
		return exceptionMatches;
	}

	/**
	 * @return Returns the exceptions.
	 */
	public Set<JavaMethod> getExceptions() {
		return exceptions;
	}

	/**
	 * @return Returns the positiveMatches.
	 */
	public ListOfPairs getPositiveMatches() {
		return positiveMatches;
	}

	public String toPrint() {
		String s = scopeDisj.toString();
		s = s + "if not in " + exceptions;
		if (ruleId.forward) {
			s = s + "	->	new(x)" + transformation;
		} else {
			s = s + "	->	old(x)" + transformation;
		}
		return s;
	}

	public String toPrintTransStat() {
		String s = "P "+getPositiveMatches().size()+ "N "+getExceptionMatches().size()+ 
			"\t Rule "+ruleId.id ;
		s = s+ "\t"+transformation;
//		s= s + scopeDisj.toPrint();
//		s = s + "if not in " + exceptions;
		return s;
	}
	public String toRuleExaminePrint() { 
		String s = "Rule "+ruleId.id +"\tP "+getPositiveMatches().size()+ "N "+getExceptionMatches().size()+"\t"; 
		s= s + scopeDisj.toPrint();
		s = s+ "\t"+transformation;
	return s;
	}
	public String toString () {
		String s ="";
		if (this.examineByUser==true){ 
			s =s+"Chkd\t";		
		}
		if (this.kept==false){ 
			s = s+"Dltd\t";
		}
//		if (this.getPositiveMatches().size()==1) {
//			s=s+"P "+getPositiveMatches().size()+ "N "+getExceptionMatches().size()+ 
//			": Rule "+ruleId.id;
//			Pair<JavaMethod> pair = (Pair<JavaMethod>)getPositiveMatches().get(0);
//			double simToken = new SeedMatchGenerator().similarityToken(pair.getLeft(), pair.getRight());
//			s= s+"   "+(int)(simToken *100) +"%";
//			double simSeqToken = new SeedMatchGenerator().similaritySeqToken(pair.getLeft(),pair.getRight());
//			s = s+" SQ" + (int) (simSeqToken*100) +"%";
//			s = s+" OLD" + (int) (pair.numSharedTokenScore()*100) +"%";
//			return s;
// 		}
		return s+"P "+getPositiveMatches().size()+ "N "+getExceptionMatches().size()+ 
		": Rule "+ruleId.id ;
		
	}
	public DefaultMutableTreeNode getTreeNode() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(this);
		DefaultMutableTreeNode tranNode = null;
		if (ruleId.forward) {
			tranNode = new DefaultMutableTreeNode(" -> "+this.transformation
					.toString());
		} else {
			tranNode = new DefaultMutableTreeNode(" <- "+this.transformation
					.toString());
		}
		root.add(tranNode);
		DefaultMutableTreeNode scopeNode = new DefaultMutableTreeNode(this.scopeDisj);
		for (int i = 0; i < this.scopeDisj.size(); i++) {
			Scope scope = this.scopeDisj.get(i);
			DefaultMutableTreeNode scopeChildNode = new DefaultMutableTreeNode();
			scopeNode.add(scopeChildNode);
			int count = 0;
			this.positiveMatches.sort();
			for (int j = 0; j < this.positiveMatches.size(); j++) {
				Pair<JavaMethod> p = (Pair<JavaMethod>) positiveMatches.get(j);
				boolean b1 = (ruleId.forward == true && scope
						.match(p.getLeft()));
				boolean b2 = (ruleId.forward == false && scope.match(p
						.getRight()));
				if (b1 || b2) {
					count++;
					DefaultMutableTreeNode matchNode = new DefaultMutableTreeNode(
							p);
					DefaultMutableTreeNode matchedCounter = new DefaultMutableTreeNode(p.getRight());
					matchNode.add(matchedCounter);
					scopeChildNode.add(matchNode);
				}
			}
			scopeChildNode.setUserObject(scope);
		}
		root.add(scopeNode);
		if (this.exceptions.size() > 0) {
			DefaultMutableTreeNode exceptionNode = new DefaultMutableTreeNode(
					this.exceptionMatches.size() + " Exception Matches");
			int deleted = 0;
			this.exceptionMatches.sort();
			DefaultMutableTreeNode deletedNode = new DefaultMutableTreeNode(
						"Exception from Deletion");
			exceptionNode.add(deletedNode);
			
			for (int i = 0; i < this.exceptionMatches.size(); i++) {
				Pair<JavaMethod> pair = exceptionMatches.get(i);
				DefaultMutableTreeNode excepChildNode = new DefaultMutableTreeNode(
						exceptionMatches.get(i));
				if (pair.toString().indexOf("empty") >= 0) {
					deleted++;
					deletedNode.add(excepChildNode);
				} else {
					exceptionNode.add(excepChildNode);
					
				}
			}
			this.numConflictException = this.exceptionMatches.size()-deleted;
			
			String excNodeName = this.exceptionMatches.size()+" Exception Matches ("+ (this.exceptionMatches.size()-deleted)+ "CON, "+ deleted+"DEL)";
			exceptionNode.setUserObject( excNodeName);
			deletedNode.setUserObject(deleted+"  Exceptions from Deletion");
			root.add(exceptionNode);
		}

		return root;
	}

	public RuleID getRuleID() {
		return ruleId;
	}

	public class RuleID implements Comparable {
		public RuleID(int i, boolean f) {
			this.forward = f;
			this.id = new Integer(i);
		}

		Integer id;

		boolean forward;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(T)
		 */
		public boolean equals(Object o) {
			if (o instanceof RuleID) {
				RuleID other = (RuleID) o;
				return (other.toString().equals(this.toString()));
			}
			return false;
		}

		public int compareTo(Object o) {
			if (o.equals(this))
				return 0;
			return -1;
		}

		public String toString() {
			if (this.forward == true)
				return "forward_" + id;
			return "backward_" + id;
		}

		public RuleID(String s) {
			Integer i = new Integer(s.substring(s.lastIndexOf("_") + 1))
					.intValue();
			if (s.indexOf("forward_") >= 0) {
				this.forward = true;
			} else
				this.forward = false;
			this.id = i;
		}

		public int hashCode() {
			return toString().hashCode();
		}

	};

	public static RuleID getRuleID(String s) {
		TransformationRule empty = new TransformationRule(
				new ScopeDisjunction(), null, null, null, true, 0);
		return empty.new RuleID(s);
	}
	
	public void markScope(Scope s) {
		this.scopeModify=true;
		this.scopeDisj.mark(s);
	}
	public void setChecked () { 
		this.examineByUser=true;
	}
	public void addException(JavaMethod jm) { 
		this.exceptionModify=true;
		this.exceptions.add(jm);
	}
	public void mark () { 
		this.kept=true;
	}
	public void unmark() { 
		this.kept= false;
	}
	public void unmarkScope (Scope s){
		this.scopeModify=true;
		this.scopeDisj.unmark(s);
	}
	public void clearState () {
		// recompute positive matches
		this.positiveMatches.removeAll();
		// recompute exception matches
		this.exceptionMatches.removeAll();
		// recompute exceptions
		this.exceptions.clear();
	}
	/* 
	 * based on the "finalMatches" of matching, update this rule.
	 * manipulate positiveMatches, exceptionMatches, exceptions based on the "finalMatches"
	 * clear positiveMatches, exceptionMatches, and exceptions in the beginning
	 */
	public void recomputeMatches (RulebasedMatching matching){
		this.clearState();
		SetOfPairs finalMatches = null;
		finalMatches = matching.getMatches(this.ruleId.forward);
		this.modified=true;
		Set<JavaMethod> domain =null;
		domain= matching.getDomain(this.ruleId.forward);
		for (Iterator<JavaMethod> domainIt = domain.iterator(); domainIt
				.hasNext();) {
			JavaMethod domainItem = domainIt.next();
			if (this.scopeDisj.isApplicable(domainItem)
					&& this.transformation.isApplicable(domainItem)) {
				JavaMethod converted = this.transformation
						.applyTransformation(domainItem);
				if (this.ruleId.forward) {
					// forward direction
					Pair accepted = finalMatches
							.getFirstMatchByLeft(domainItem);
					if (accepted != null) {
						// either positive or conflict
						JavaMethod right = (JavaMethod) accepted.getRight();
						if (!exceptions.contains(domainItem)
								&& this.transformation.equivalent(right,
										converted)) {
							// (1) positive
							this.positiveMatches.addPair(accepted);
						} else {
							// (2) conflict
							exceptions.add(domainItem);
							this.exceptionMatches.addPair(accepted);
						}
					} else {
						// (3) negative 
						exceptions.add(domainItem);
						this.exceptionMatches
								.addPair(new Pair(domainItem, null));
					}
				}else { 
					Pair accepted = finalMatches.getFirstMatchByRight(
							domainItem);
					if (accepted != null) {
						// either positive or conflict
						JavaMethod left = (JavaMethod) accepted.getLeft();
						if (!exceptions.contains(domainItem)
								&& this.transformation.equivalent(left,
										converted)) {
							// (1) positive
							this.positiveMatches.addPair(accepted);
						}else { 
							// (2) conflict
							exceptions.add(domainItem);
							this.exceptionMatches.addPair(accepted);						
						}
					}else {
						// (3) negative
						exceptions.add(domainItem);
						this.exceptionMatches.addPair(new Pair(null,
								domainItem));
					}
				}
			}
		}
	}
	public boolean isModified () { 
		return modified;
	}
	public void resetModified () { 
		modified=false;
	}
	public boolean isForward () { 
		return ruleId.forward;
	}
	public boolean isKept() { 
		return kept;
	}
	public boolean isChecked() { 
		return examineByUser;
	}
	
	public Transformation getTransformation() { 
		// This method is added to support APIRuleExaminer
		return transformation;
	}
}