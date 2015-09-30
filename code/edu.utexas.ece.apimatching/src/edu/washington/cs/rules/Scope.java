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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import edu.washington.cs.comparison.LCS;
import edu.washington.cs.comparison.Tokenize;

public class Scope implements Comparable{

	private static String xmlTag = "scope";
	private JavaMethod seed;
	private String packageS = null;
	private String classS = null;
	private String procedureS = null;
	private String returnS = null;
	private String parameterS = null;

	private boolean packageFlag = false;
	private boolean classFlag = false;
	private boolean procedureFlag = false;
	private boolean returnFlag = false;
	private boolean parameterFlag = false;

	private boolean marked=true;
	
	public void mark () { 
		this.marked=true;
	}
	public void unmark() { 
		this.marked= false;
	}
	public boolean isKept() { 
		return this.marked;
	}
	// constructed from user input
	public Scope (String packageP, String classP,
			String procedureP, String returnP, String paramP) {
		this.seed = null;
		this.refinePackageExpression(packageP); 
		this.refineClassExpression(classP);
		this.refineProcedureExpression(procedureP);
		this.refineReturnExpression(returnP); 
		this.refineParameterExpression("["+paramP+"]");
	}
	// lattice top down from that scopeDisj
	public Scope(JavaMethod seed){
		this.seed = seed;
		this.resetPackageExpression();
		this.resetClassExpression();
		this.resetProcedureExpression();
		this.resetParameterExpression();
		this.resetReturnExpression();
	}

	// create the most general scopeDisj given the change 
	public Scope (JavaMethod seed, Change c) { 
		this.seed = seed;
		// create the most general scopeDisj given this change.
		if (c.getPackagePattern()!=null) { 
			this.refinePackageExpression(c.getPackagePattern());
		}else {
			this.resetPackageExpression();
		}
		if (c.getClassPattern()!=null) { 
			this.refineClassExpression(c.getClassPattern());
		}else {
			this.resetClassExpression();
		}
		if (c.getProcedurePattern()!=null) { 
			this.refineProcedureExpression(c.getProcedurePattern());
		}else {
			this.resetProcedureExpression();
		}
		if (c.getReturnPattern()!=null) { 
			this.refineReturnExpression(c.getReturnPattern());
		}else {
			this.resetReturnExpression();
		}
		if (c.getParameterPattern()!=null) { 
			this.refineParameterExpression(c.getParameterPattern());
		}else {
			this.resetParameterExpression();
		}
	}
	
	// copy the parent scopeDisj 
	private Scope (Scope parent) { 
		this.seed = parent.seed;
		this.packageFlag= parent.packageFlag;
		this.classFlag= parent.classFlag;
		this.procedureFlag= parent.procedureFlag;
		this.returnFlag = parent.returnFlag;
		this.parameterFlag=parent.parameterFlag;
		this.packageS=parent.packageS;
		this.classS=parent.classS;
		this.procedureS=parent.procedureS;
		this.returnS=parent.returnS;
		this.parameterS=parent.parameterS;
	}
	
	// create children scopeDisj 
	List<Scope> createNextChildren (TreeSet<Scope> triedScope) {
		ArrayList<Scope> result = new ArrayList<Scope>();
 		if (packageFlag==false) { 
			String packagePat [] =  Tokenize.tokenizedPatterns(this.seed.getPackageName());
			for (int i=0; i<packagePat.length; i++) { 
				Scope s = new Scope(this);
				s.refinePackageExpression(packagePat[i]);
				result.add(s);
			}
		}
		if (classFlag==false) { 
			String classPat [] = Tokenize.tokenizedPatterns(this.seed.getClassName());
			for (int i=0; i<classPat.length; i++) { 
				Scope s = new Scope(this);
				s.refineClassExpression(classPat[i]);
				result.add(s);
			}
		}
		if (procedureFlag==false) { 
			String procPat [] = Tokenize.tokenizedPatterns(this.seed.getProcedureName());
			for (int i=0; i<procPat.length; i++) { 
				Scope s = new Scope(this);
				s.refineProcedureExpression(procPat[i]);
				result.add(s);
			}
		}
		if (returnFlag==false) {
			String retPat [] = Tokenize.tokenizedPatterns(this.seed.getReturntype());
			for (int i=0; i<retPat.length;i++) {
				Scope s = new Scope(this);
				s.refineReturnExpression(retPat[i]);
				result.add(s);
			}
		}
		if (parameterFlag==false){
			Scope s = new Scope(this);
			s.refineParameterExpression(this.seed.getParameters().toString());
			result.add(s);
		}
		if (triedScope==null) return result;
		ArrayList<Scope> finalResult = new ArrayList<Scope>();
		for (int i=0 ;i<result.size(); i++) { 
			Scope s = result.get(i);
			if (!triedScope.contains(s)) finalResult.add(s);
		}
		if (finalResult.size()>0) {
			return finalResult;
		}
		return null;
	}
	// return true if JavaMethod is within the scopeDisj.
	public boolean match(JavaMethod jm) {
		String pattern = jm.toString();
		Pattern p = Pattern.compile(getScopeExpression());
		Matcher m = p.matcher(pattern);
		return m.matches();
	}

	// create all lattice 
	public static Set<Scope> createHierarchicalScopes (JavaMethod jm) { 
		Set<Scope> scopes = new TreeSet<Scope> ();
		String packagePat [] =  Tokenize.tokenizedPatterns(jm.getPackageName()); 
		String classPat [] = Tokenize.tokenizedPatterns(jm.getClassName());
		String procPat [] = Tokenize.tokenizedPatterns(jm.getProcedureName()); 
		String retPat [] = Tokenize.tokenizedPatterns(jm.getReturntype());
		for (int index1 = -1; index1 < packagePat.length; index1++) {
			Scope s = new Scope (jm);	
			if (index1 >=0) {
				s.refinePackageExpression(packagePat[index1]);
			} else { 
				s.resetPackageExpression();
			}
			for (int index2 = -1; index2 < classPat.length; index2++) {
				if (index2 >= 0) {
					s.refineClassExpression(classPat[index2]);
				} else {
					s.resetClassExpression();
				}
				for (int index3 = -1; index3 < procPat.length; index3++) {
					if (index3 >= 0) {
						s.refineProcedureExpression(procPat[index3]);
					} else {
						s.resetProcedureExpression();
					}
					for (int index4 = -1; index4 < retPat.length; index4++) {
						if (index4 >= 0) {
							s.refineReturnExpression(retPat[index4]);
						} else {
							s.resetReturnExpression();
						}
						for (int index5 = -1; index5 < 1; index5++) {
							if (index5 >= 0) {
								s.refineParameterExpression(jm.getParameters().toString());
							} else {
								s.resetParameterExpression();
							}
							Scope toAdd = new Scope(s);
							scopes.add(toAdd);
						}
					}
				}
			}
		}
		return scopes;
	}
	
	private void resetPackageExpression() {
		refinePackageExpression("*");
		packageFlag = false;
	}

	private void resetClassExpression() {
		refineClassExpression("*");
		classFlag = false;
	}

	private void resetProcedureExpression() {
		refineProcedureExpression("*");
		procedureFlag = false;
	}

	private void resetReturnExpression() {
		refineReturnExpression("*");
		returnFlag = false;
	}
	private void resetParameterExpression() {
		ArrayList<String> simplifiedParameter = new ArrayList<String>();
		simplifiedParameter.add("*");
		refineParameterExpression(simplifiedParameter.toString());
		parameterFlag=false;
	}
	private void refinePackageExpression(String pattern) {
		packageFlag = true;
		packageS = pattern;
		packageS = packageS.replace(".", "\\.");
		packageS = packageS.replace("*", ".*");
	}

	private void refineClassExpression(String pattern) {
		classFlag = true;
		classS = pattern;
		classS = classS.replace("$", "\\$");
		classS = classS.replace("*", ".*");
	}

	private void refineProcedureExpression(String pattern) {
		procedureFlag = true;
		procedureS = pattern;
		procedureS = procedureS.replace("$", "\\$");
		procedureS = procedureS.replace("*", ".*");
	}
	private void refineReturnExpression (String pattern) {
		returnFlag = true;
		returnS = pattern;
		returnS = returnS.replace("[", "\\[");
		returnS = returnS.replace("]", "\\]");
		returnS = returnS.replace("*", ".*");
	}
	private void refineParameterExpression(String pattern) {
		parameterFlag= true; 
		parameterS = pattern;
		parameterS = parameterS.replace("*", ".*");
		parameterS = parameterS.replace("[", "\\[");
		parameterS = parameterS.replace("]", "\\]");
	}
	private String getScopeExpression() {
		String pattern = JavaMethodRegex.concatenate(packageS, classS,
				procedureS, parameterS, returnS);	
		return pattern;
	}
	public String getCanonicalScopeExpression () {
		String s= getScopeExpression().replace("\\","");
		s = s.replace(".*", "*");
		return s;
	}
	public int compareTo(Object obj) {
		if (obj instanceof Scope) { 
			Scope other = (Scope) obj;
			return this.toString().compareTo(other.toString());
		}
		return -1;
	}
	private int getNumFlag() {
		int count =0; 
		if (packageFlag==false) { 
			count++;
		}
		if (classFlag==false) { 
			count++;	
		}
		if (procedureFlag==false) { 
			count++;
		}
		if (returnFlag==false) { 
			count++;
		}
		if (parameterFlag==false) {
			count++;
		}
		return count;
	}
	public JavaMethod getSeed() { 
		return seed;
	}
	public void setSeed(JavaMethod jm) { 
		this.seed =jm;
	}
	public String toString () {
		if (marked) { 
			return "For all x in "+getCanonicalScopeExpression();
		}
		return "Dltd\t"+getCanonicalScopeExpression();
		
	}
	public int hashCode() { 
		String s =this.toString();
		return s.hashCode();
	}
	public boolean equals(Object obj) {
		if (obj instanceof Scope) {
			Scope other = (Scope) obj;
			boolean b0 = true;
			boolean b1 = (other.packageS.equals(this.packageS));
			boolean b2 = (other.classS.equals(this.classS));
			boolean b3 = (other.procedureS.equals(this.procedureS));
			boolean b4 = (other.returnS.equals(this.returnS));
			boolean b5 = (other.parameterS.equals(this.parameterS));
			return (b0 && b1 && b2 && b3 && b4 && b5);
		}
		return false;
	}
	
	// xml related functions
	public void writeElement (Element parent) { 		
		Element scope = parent.getOwnerDocument().createElement(xmlTag);
		scope.setAttribute("packageS",packageS);
		scope.setAttribute("classS",classS);
		scope.setAttribute("procedureS",procedureS);
		scope.setAttribute("parameterS",parameterS);
		scope.setAttribute("returnS",returnS);
		scope.setAttribute("packageFlag",new Boolean(packageFlag).toString());
		scope.setAttribute("classFlag",new Boolean(classFlag).toString());
		scope.setAttribute("procedureFlag",new Boolean(procedureFlag).toString());
		scope.setAttribute("parameterFlag",new Boolean(parameterFlag).toString());
		scope.setAttribute("returnFalg",new Boolean(returnFlag).toString());
		scope.setAttribute("marked",new Boolean(marked).toString());
		scope.setTextContent(seed.toString());
		parent.appendChild(scope);
	}
	public static String getXMLTag () {
		return xmlTag;
	}
	public static Scope readElement (Element scope) { 
		if (!scope.getTagName().equals(xmlTag)) return null;
		JavaMethod seed = new JavaMethod(scope.getTextContent());
		Scope s = new Scope(seed);
		s.packageS = scope.getAttribute("packageS");
		s.classS = scope.getAttribute("classS");
		s.procedureS = scope.getAttribute("procedureS");
		s.parameterS = scope.getAttribute("parameterS");
		s.returnS = scope.getAttribute("returnS");
		s.packageFlag = new Boolean (scope.getAttribute("packageFlag")).booleanValue();
		s.classFlag = new Boolean (scope.getAttribute("classFlag")).booleanValue();
		s.procedureFlag = new Boolean (scope.getAttribute("procedureFlag")).booleanValue();
		s.parameterFlag = new Boolean (scope.getAttribute("parameterFlag")).booleanValue();
		s.returnFlag = new Boolean (scope.getAttribute("returnFlag")).booleanValue();
		if (scope.getAttribute("marked").equals("") || scope.getAttribute("marked")==null) {
			s.marked=true;
		}else s.marked = new Boolean(scope.getAttribute("marked")).booleanValue();
		return s;
		
	}
	public static Scope readXMLFile (String filename){
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
		return readElement((Element)doc.getDocumentElement().getFirstChild());
	}
	public void writeXMLFile (String filename) { 
		Document doc =
			DOMImplementationImpl.getDOMImplementation().createDocument(
				"namespaceURI",
				"scopelist",
				null);
		// update document
		writeElement(doc.getDocumentElement());
		File file = new File(filename);
		try {	
			if (!file.exists()) file.createNewFile();
			//serialize DOM document to outputfile 
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
			assert (doc!= null);
			serializer.serialize(doc);
			outstream.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public final int scopeUnifiable(Scope other) {
		int WEIGHT = 20;
		// for now, just consider ones that has at least one common part
		int common = 0;
		if (other.packageS.equals(this.packageS)) {
			common = common + WEIGHT;
		} else {
			common = common
					+ (LCS.getNumSharedChar(this.packageS, other.packageS) * WEIGHT)
					/ Math.max(this.packageS.length(), other.packageS.length());
		}
		if (other.classS.equals(this.classS)) {
			common = common + WEIGHT;
		} else {
			common = common + LCS.getNumSharedChar(this.classS, other.classS)
					* WEIGHT
					/ Math.max(this.classS.length(), other.classS.length());
		}
		if (other.procedureS.equals(this.procedureS)) {
			common = common + WEIGHT;
		} else {
			common = common
					+ LCS.getNumSharedChar(this.procedureS, other.procedureS)
					* WEIGHT
					/ Math.max(this.procedureS.length(), other.procedureS
							.length());
		}
		if (other.returnS.equals(this.returnS)) {
			common = common + WEIGHT;
		} else {
			common = common + LCS.getNumSharedChar(this.returnS, other.returnS)
					* WEIGHT
					/ Math.max(this.returnS.length(), other.returnS.length());
		}
		if (other.parameterS.equals(this.parameterS)) {
			common = common + WEIGHT;
		} else {
			common = common
					+ LCS.getNumSharedChar(this.parameterS, other.parameterS)
					* WEIGHT / Math.max(this.parameterS.length(), other.parameterS.length());
		}
		return common;
	}
	public Scope unify(Scope other) {
		// assert (isUnifiable(other))
		// although dot can match a special character too, let's use it.
		// anything that are different between two strings are replaced with .*
		Scope newScope = new Scope(this);
		if (!other.packageS.equals(this.packageS)) {
			newScope.packageS = LCS.getRegularExpTokenPattern(other.packageS,
					this.packageS);
			if (newScope.packageS.equals(".*")) newScope.packageFlag=false;
		}
		if (!other.classS.equals(this.classS)) {
			newScope.classS = LCS.getRegularExpTokenPattern(other.classS,
					this.classS);
			if (newScope.classS.endsWith(".*")) newScope.classFlag=false;
		}
		if (!other.procedureS.equals(this.procedureS)) {
			newScope.procedureS = LCS.getRegularExpTokenPattern(
					other.procedureS, this.procedureS);
			if (newScope.procedureS.endsWith(".*")) newScope.procedureFlag=false;
		}
		if (!other.parameterS.equals(this.parameterS)) {
			String otherparam = "";
			String thisparam = "";
			if (!other.parameterS.equals("[]")) {
				otherparam = other.parameterS.substring(2, other.parameterS
						.length() - 2);
			}
			if (!this.parameterS.equals("[]")) {
				thisparam = this.parameterS.substring(2, this.parameterS
						.length() - 2);
			}
			// take the both open bracket and close bracket out
			String common = LCS.getRegularExpTokenPattern(otherparam,thisparam);
			newScope.parameterS = "\\["
					+ common+ "\\]";
//			System.out.println("0"+otherparam+"\tT"+thisparam+"\tC"+common);
			if (common.equals(".*")) newScope.parameterFlag=false;
		}
		if (!other.returnS.equals(this.returnS)) {
			newScope.returnS = LCS.getRegularExpTokenPattern(other.returnS,
					this.returnS);
			if (newScope.returnS.equals(".*")) newScope.returnFlag=false;
		}
		return newScope;
	}
	public void refineMostSpecific () {
		refinePackageExpression(this.seed.getPackageName());
		refineClassExpression(this.seed.getClassName());
		refineProcedureExpression(this.seed.getProcedureName());
		refineReturnExpression(this.seed.getReturntype());
		refineParameterExpression(this.seed.getParameters().toString());
	}
	public static Scope combine(ScopeDisjunction sd) {
		ArrayList<Scope> scopes =sd.getListOfScope();
		Scope f = scopes.get(0);
		for (int i=1; i< scopes.size(); i++) { 
			Scope s = scopes.get(i); 
			f= f.unify(s);	
		}
		// set the flag correctly
		
		return f;
	}


	public static void main(String args[]) {
		
		generalScopeTest();
//		scopeConstructionTest(); 
//		System.exit(0);
//		Set<Scope> scopesA= hierarchicalTest();
//		Set<Scope> scopesB=hierarchicalTest2();
//		System.out.println("A contains B"+scopesA.containsAll(scopesB));
//		System.out.println("B contains A"+scopesB.containsAll(scopesA));
//		scopesA.removeAll(scopesB);
//		System.out.println(scopesA);
		
	}

	public static Set<Scope> hierarchicalTest2 () {
		JavaMethod jm1= new JavaMethod("com.jrefinery.chart:NumberAxis-getTickUnit__[]->NumberTickUnit");
		Scope scope1 = new Scope(jm1);
		Set<Scope> hScopes = scope1.hierarchicalScope();
		hScopes.add(scope1);
		for (Iterator<Scope> it1 = hScopes.iterator();it1.hasNext(); ) {
			Scope s1 = it1.next();
			System.out.println(s1+"\t"+s1.getNumFlag());
		}
		return hScopes;
	}
	
	public Set<Scope> hierarchicalScope (){
		if (this.getNumFlag()==0) return null;
		TreeSet<Scope> scopes = new TreeSet<Scope>();
		List<Scope> children = this.createNextChildren(null);
		if (children!=null) scopes.addAll(children);
		for (int i=0; i<children.size(); i++) {
			Set<Scope> hScope = children.get(i).hierarchicalScope();
			if (hScope!=null) scopes.addAll(hScope);
		}
		return scopes;
	}
	public static Set<Scope> hierarchicalTest () { 
		JavaMethod jm1= new JavaMethod("com.jrefinery.chart:NumberAxis-getTickUnit__[]->NumberTickUnit");
		Set<Scope> scopes1 = Scope.createHierarchicalScopes(jm1);
		for (Iterator<Scope> it1 = scopes1.iterator(); it1.hasNext(); ) {
			Scope s1 = it1.next();
			s1.writeXMLFile("temp");
			s1 = readXMLFile("temp");
			System.out.println(s1+"\t"+s1.getNumFlag());
		}
		return scopes1;
	}
	public static void unifyTest () { 
		JavaMethod jm1= new JavaMethod("com.jrefinery.chart:StackedHorizontalBarRenderer-StackedHorizontalBarRenderer__[]->void");
		Set<Scope> scopes1 = Scope.createHierarchicalScopes(jm1);
		JavaMethod jm2 =new JavaMethod("com.jrefinery.chart:StackedVerticalBarRenderer-StackedVerticalBarRenderer__[]->void");
		Set<Scope> scopes2 = Scope.createHierarchicalScopes(jm2);
		for (Iterator<Scope> it1 = scopes1.iterator(); it1.hasNext(); ) {
			Scope s1 = it1.next();
			for (Iterator<Scope> it2 = scopes2.iterator(); it2.hasNext(); ) {
				Scope s2 = it2.next();
				if (s1.scopeUnifiable(s2)>0){ 
					System.out.println(s1.scopeUnifiable(s2)+"Unifiable\t"+s1+"\t"+s2); 
					Scope newScope = s1.unify(s2);
					System.out.println(newScope);
				}else {
					System.out.println(s1.scopeUnifiable(s2)+"Not Unifiable\t"+s1+"\t"+s2); 
					Scope newScope = s1.unify(s2);
					System.out.println(newScope);
				}
			}
		}
	}
	public static void childrenScope () { 

		JavaMethod jm = new JavaMethod("com.jrefinery.chart.renderer:HorizontalBarRenderer-initialise__[Graphics2D, Rectangle2D, CategoryPlot, ChartRenderingInfo]->void");
		new Scope(jm);
	}
	public static void generalScopeTest () {
		String methodPair[] = {
				"com.jrefinery.chart.axis:TickUnit-compareTo__[Object]->int",
				"com.jrefinery.chart:TickUnit-compareTo__[Object]->int",
				"com.jrefinery.chart.plot:XYPlot-draw__[Graphics2D, Rectangle2D, ChartRenderingInfo]->void",
				"com.jrefinery.chart:XYPlot-draw__[Graphics2D, Rectangle2D, ChartRenderingInfo]->void",
				"com.jrefinery.chart.renderer:StackedHorizontalBarRenderer-StackedHorizontalBarRenderer__[CategoryToolTipGenerator, CategoryURLGenerator]->void",
				"com.jrefinery.chart:StackedHorizontalBarRenderer-StackedHorizontalBarRenderer__[]->void",
				"com.jrefinery.chart.renderer:StackedVerticalBarRenderer-StackedVerticalBarRenderer__[CategoryToolTipGenerator, CategoryURLGenerator]->void",
				"com.jrefinery.chart:StackedVerticalBarRenderer-StackedVerticalBarRenderer__[]->void",
				"com.jrefinery.chart:ChartFactory$A-createScatterPlot$A__[String, String[], String[], XYDataset, boolean, boolean, boolean]->JFreeChart[]",
				"com.jrefinery.chart:ChartFactory-createScatterPlot__[String, String, String, XYDataset, boolean]->JFreeChart",
				"com.jrefinery.chart:ChartFactory$A-createStackedHorizontalBarChart$B__[String, String, String, CategoryDataset, boolean, boolean, boolean]->JFreeChart",
				"com.jrefinery.chart:ChartFactory-createStackedHorizontalBarChart__[String, String, String, CategoryDataset, boolean]->JFreeChart",
				"com.jrefinery.chart:ChartFactory-createStackedVerticalBarChart__[String, String, String, CategoryDataset, boolean, boolean, boolean]->JFreeChart",
				"com.jrefinery.chart:ChartFactory-createStackedVerticalBarChart__[String, String, String, CategoryDataset, boolean]->JFreeChart",
				"com.jrefinery.chart:ChartFactory-createVerticalBarChart3D__[String, String, String, CategoryDataset, boolean, boolean, boolean]->JFreeChart",
				"com.jrefinery.chart:ChartFactory-createVerticalBarChart3D__[String, String, String, CategoryDataset, boolean]->JFreeChart",
				"com.jrefinery.chart:Legend-Legend__[JFreeChart]->void",
				"com.jrefinery.chart:Legend-Legend__[JFreeChart, int]->void",
				"com.jrefinery.chart:StandardLegend-StandardLegend__[JFreeChart, Spacer, Spacer, Paint, Stroke, Paint, Font, Paint]->void",
				"com.jrefinery.chart:StandardLegend-StandardLegend__[JFreeChart, int, Spacer, Paint, Stroke, Paint, Font, Paint]->void" 	
		};
		JavaMethod[] jmList = new JavaMethod[methodPair.length];
		for (int i = 0; i < methodPair.length; i++) {
			jmList[i] = new JavaMethod(methodPair[i]);
		}
		for (int i = 0; i < methodPair.length; i = i + 2) {
			JavaMethod oldJM = jmList[i];
			JavaMethod newJM = jmList[i + 1];
			List<Change> changes = Change.createChange(oldJM, newJM);
			System.out.println("\n"+oldJM);
			for (Iterator<Change> it = changes.iterator(); it.hasNext();) {
				Change c = it.next();
				Scope s = new Scope(oldJM, c);
				boolean matchOld = s.match(oldJM);
				if (matchOld == false) {
					System.err.println("ERROR");
					System.exit(0);
				}
				boolean matchNew = s.match(newJM);
				System.out.println(s.getCanonicalScopeExpression() + "\t"
						+ matchOld);
			}
		}
	}

	public static void scopeConstructionTest () {
		String methodPair[] = {
				"com.jrefinery.chart.axis:TickUnit-compareTo__[Object]->int",
				"com.jrefinery.chart:TickUnit-compareTo__[Object]->int",
				"com.jrefinery.chart.plot:XYPlot-draw__[Graphics2D, Rectangle2D, ChartRenderingInfo]->void",
				"com.jrefinery.chart:XYPlot-draw__[Graphics2D, Rectangle2D, ChartRenderingInfo]->void",
				"com.jrefinery.chart.renderer:StackedHorizontalBarRenderer-StackedHorizontalBarRenderer__[CategoryToolTipGenerator, CategoryURLGenerator]->void",
				"com.jrefinery.chart:StackedHorizontalBarRenderer-StackedHorizontalBarRenderer__[]->void",
				"com.jrefinery.chart.renderer:StackedVerticalBarRenderer-StackedVerticalBarRenderer__[CategoryToolTipGenerator, CategoryURLGenerator]->void",
				"com.jrefinery.chart:StackedVerticalBarRenderer-StackedVerticalBarRenderer__[]->void",
				"com.jrefinery.chart:ChartFactory$A-createScatterPlot$A__[String, String[], String[], XYDataset, boolean, boolean, boolean]->JFreeChart[]",
				"com.jrefinery.chart:ChartFactory-createScatterPlot__[String, String, String, XYDataset, boolean]->JFreeChart",
				"com.jrefinery.chart:ChartFactory$A-createStackedHorizontalBarChart$B__[String, String, String, CategoryDataset, boolean, boolean, boolean]->JFreeChart",
				"com.jrefinery.chart:ChartFactory-createStackedHorizontalBarChart__[String, String, String, CategoryDataset, boolean]->JFreeChart",
				"com.jrefinery.chart:ChartFactory-createStackedVerticalBarChart__[String, String, String, CategoryDataset, boolean, boolean, boolean]->JFreeChart",
				"com.jrefinery.chart:ChartFactory-createStackedVerticalBarChart__[String, String, String, CategoryDataset, boolean]->JFreeChart",
				"com.jrefinery.chart:ChartFactory-createVerticalBarChart3D__[String, String, String, CategoryDataset, boolean, boolean, boolean]->JFreeChart",
				"com.jrefinery.chart:ChartFactory-createVerticalBarChart3D__[String, String, String, CategoryDataset, boolean]->JFreeChart",
				"com.jrefinery.chart:Legend-Legend__[JFreeChart]->void",
				"com.jrefinery.chart:Legend-Legend__[JFreeChart, int]->void",
				"com.jrefinery.chart:StandardLegend-StandardLegend__[JFreeChart, Spacer, Spacer, Paint, Stroke, Paint, Font, Paint]->void",
				"com.jrefinery.chart:StandardLegend-StandardLegend__[JFreeChart, int, Spacer, Paint, Stroke, Paint, Font, Paint]->void" 
				
		};
		JavaMethod[] jmList = new JavaMethod[methodPair.length];
		for (int i = 0; i < methodPair.length; i++) {
			jmList[i] = new JavaMethod(methodPair[i]);
		}
		String s1pack = "com.*.chart";
		String s2clas = "*";
		String s3proc = "*Legend*";
		String s4ret = "*";
		String s5param = "*";
		Scope s = new Scope (s1pack,s2clas,s3proc,s4ret,s5param);
		System.out.println("Matching Scope" +s.getCanonicalScopeExpression());
		
		int cnt = 0; 
		for (int i = 0; i < methodPair.length; i++) {
			JavaMethod jm = jmList[i];
			if (s.match(jm)) {
				System.out.println(cnt+"\t"+jm);
				cnt++;
			}
		}
	}
}