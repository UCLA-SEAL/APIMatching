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
import java.util.TreeSet;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Change implements Comparable{
	private static final String xmlTag = "change";
	private ArrayList<Transformation> transformations =null;
	
	Change() {
		transformations = new ArrayList<Transformation>();
	}
	
	public Change(Transformation t) { 
		transformations = new ArrayList<Transformation>();
		addTransformation(t);
	}
	void addTransformation(Transformation t){
		if (t!=null) transformations.add(t);
	}
	
	protected Change clone() throws CloneNotSupportedException {
		Change newChange = new Change(); 
		for (int i=0; i<this.transformations.size(); i++) { 
			newChange.addTransformation(this.transformations.get(i));
		}
		return newChange;
	}
	public boolean includeTypeReplace () { 
		for (int i=0; i<this.transformations.size(); i++) { 
			Transformation t = this.transformations.get(i);
			if (t.getType()==Transformation.TYPE_REPLACE) return true;
		}
		return false;
	}
	public JavaMethod applyTransformation (JavaMethod jm) { 
		// apply all transformations and create a new JavaMethod
		JavaMethod result = null;
		for (int i=0; i<transformations.size();i++) { 
			Transformation t = transformations.get(i);
			if (result!=null) { 
				result = t.applyTransformation(result);
			}else { 
				result = t.applyTransformation(jm);
			}
		}
		return result;
	}
	
	public boolean isApplicable (JavaMethod jm) {
		boolean b = true;
		for (int i= 0; i<transformations.size();i++) {
			Transformation t = transformations.get(i);
			b = b && t.isApplicable(jm);
		}
		return b;
	}

	public boolean equivalent(JavaMethod one, JavaMethod two) {
		boolean b = true;
		for (int i=0; i<transformations.size(); i++) { 
			Transformation t = transformations.get(i);
			b = b && t.equivalent(one,two);
		}
		return b;
	}
	public int hashCode () {
		return this.toString().hashCode();
	}
	public int compareTo(Object o) {
		Change c = (Change)o; 
		return c.toString().compareTo(this.toString()); 
	}
	public boolean equals(Object o) {
		Change c = (Change)o;
		return c.toString().equals(this.toString());
	}
	public String toString() {
		String s = "";
		for (int i = 0; i<transformations.size(); i++) { 
			Transformation t = transformations.get(i);
			s = s +" "+t.toString();
		}
		return s;
	}

	public void writeXMLFile (String filename) { 
		Document doc =
			DOMImplementationImpl.getDOMImplementation().createDocument(
				"namespaceURI",
				"changelist",
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
	public static Change readXMLFile (String filename){
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
	
	public void writeElement(Element parent) { 
		Element change = parent.getOwnerDocument().createElement(xmlTag);
		for (int i=0; i<this.transformations.size(); i++) { 
			Transformation t =transformations.get(i);
			t.writeElement(change);
		}
		parent.appendChild(change);
	}
	public static String getXMLTag() { 
		return xmlTag;
	}
	public static Change readElement (Element change) { 
		if (!change.getTagName().endsWith(xmlTag)) return null;
		Change c = new Change();
		NodeList children = change.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i) instanceof Element) {
				Element child = (Element) children.item(i);
				if (child.getTagName().equals(Transformation.getXMLTag())) {
					Transformation t = Transformation.readElement(child);
					c.addTransformation(t);
				}
			}
		}
		return c;
	}
	String getPackagePattern() {
		return getPattern(Transformation.PACKAGE_REPLACE);
	}
	String getClassPattern(){ 
		return getPattern(Transformation.CLASS_REPLACE);
	}
	String getProcedurePattern() {
		return getPattern(Transformation.PROCEDURE_REPLACE);
	}
	String getReturnPattern() {
		return getPattern(Transformation.RETURN_REPLACE);
	}
	String getParameterPattern() { 
		return getPattern(Transformation.PARAMETERS_REPLACE);
	}
	private String getPattern(int type) { 
		for (int i = 0; i < transformations.size(); i++) {
			Transformation t = transformations.get(i);
			if (t.getType() == type) {
				return t.getPattern();
			}
		}
		return null;
	}
	public static List<Change> createChange(JavaMethod from, JavaMethod to) {
		ArrayList<Change> list = new ArrayList<Change>();
		Transformation packageT = null;
		Transformation classT = null;
		Transformation procT = null;
		Transformation returnT = null;
		Transformation paramT = null;
		if (!from.getPackageName().equals(to.getPackageName())) {
			packageT = new Transformation(
					Transformation.PACKAGE_REPLACE, from.getPackageName(),
					to.getPackageName());
			// add package replace transformation alone
			Change packChange = new Change();
			packChange.addTransformation(packageT);
			list.add(packChange);
		}
		if (!from.getClassName().equals(to.getClassName())) {
			
			if (from.getClassName().indexOf('$')<0 && to.getClassName().indexOf('$')<0) { 
				classT = new Transformation(Transformation.TYPE_REPLACE,
						from.getClassName(), to.getClassName());
			} else { 
				classT = new Transformation(Transformation.CLASS_REPLACE,
						from.getClassName(), to.getClassName());
			}
			// add class replace transformation alone
			Change classChange = new Change();
			classChange.addTransformation(classT);
			list.add(classChange);
		
			// add package, class transformation
			if (packageT != null && classT != null) {
				Change packClassChange = new Change();
				packClassChange.addTransformation(packageT);
				packClassChange.addTransformation(classT);
				list.add(packClassChange);
			}
		}
		if (!from.getProcedureName().equals(to.getProcedureName())) {
			if (!from.getProcedureName().equals(from.getClassName())) {
				// add procedure place if and only if it's not a constructor. 
				procT = new Transformation(
						Transformation.PROCEDURE_REPLACE, from
								.getProcedureName(), to.getProcedureName());
			
				// add procedure replace transformation alone
				Change procChange = new Change();
				procChange.addTransformation(procT);
				list.add(procChange);
			
				// add package, class, procedure transformation
				if (packageT != null && classT != null && procT != null) {
					Change packageClassProcChange = new Change();
					packageClassProcChange.addTransformation(packageT);
					packageClassProcChange.addTransformation(classT);
					packageClassProcChange.addTransformation(procT);
					list.add(packageClassProcChange);
				}
				
				// add class, procedure transformation
				if (classT != null && procT != null) {
					Change classProcChange = new Change();
					classProcChange.addTransformation(classT);
					classProcChange.addTransformation(procT);
					list.add(classProcChange);
				}
				
				// add package, procedure transformation
				if (packageT != null && procT != null) {
					Change packProcChange = new Change();
					packProcChange.addTransformation(packageT);
					packProcChange.addTransformation(procT);
					list.add(packProcChange);
				}
			}
		}
		if (!from.getReturntype().equals(to.getReturntype())){	
//			if (from.getReturntype().indexOf('[') > 0
//					|| to.getReturntype().indexOf(']') > 0) {
//				returnT = defaultChange.new Transformation(Transformation.RETURN_REPLACE,
//						from.getReturntype(), to.getReturntype());
//			} else {
				returnT = new Transformation(Transformation.RETURN_REPLACE, from
						.getReturntype(), to.getReturntype());
//			}
			// add return replace alone
			Change returnChange = new Change();
			returnChange.addTransformation(returnT);
			list.add(returnChange);
			
			// add package, class, procedure, return
			if (packageT != null && classT != null && procT != null
					&& returnT != null) {
				Change packClassProcReturnChange = new Change();
				packClassProcReturnChange.addTransformation(packageT);
				packClassProcReturnChange.addTransformation(classT);
				packClassProcReturnChange.addTransformation(procT);
				packClassProcReturnChange.addTransformation(returnT);
				list.add(packClassProcReturnChange);
			}
			// add class, procedure, return
			if (classT != null && procT != null && returnT != null) {
				Change classProcReturnChange = new Change();
				classProcReturnChange.addTransformation(classT);
				classProcReturnChange.addTransformation(procT);
				classProcReturnChange.addTransformation(returnT);
				list.add(classProcReturnChange);
			}
			// add package, procedure, return 
			if (packageT!=null && procT!=null && returnT!=null) { 
				Change packageProcReturnChange = new Change(); 
				packageProcReturnChange.addTransformation(packageT);
				packageProcReturnChange.addTransformation(procT);
				packageProcReturnChange.addTransformation(returnT);
				list.add(packageProcReturnChange);
			}
			// add package, class, return 
			if (packageT!=null && classT!=null && returnT!=null) {
				Change packageClassReturnChange = new Change();
				packageClassReturnChange.addTransformation(packageT);
				packageClassReturnChange.addTransformation(classT);
				packageClassReturnChange.addTransformation(returnT);
				list.add(packageClassReturnChange);
			}
			// add package, return 
			if (packageT!=null && returnT!=null ){
				Change packageReturnChange = new Change();
				packageReturnChange.addTransformation(packageT);
				packageReturnChange.addTransformation(returnT);
				list.add(packageReturnChange);
			}
			// add procedure, return
			if (procT != null && returnT != null) {
				Change procReturnChange = new Change();
				procReturnChange.addTransformation(procT);
				procReturnChange.addTransformation(returnT);
				list.add(procReturnChange);
			}
			// add class, return 
			if (classT != null && returnT != null) {
				Change classReturnChange = new Change();
				classReturnChange.addTransformation(classT);
				classReturnChange.addTransformation(returnT);
				list.add(classReturnChange);
			}
		}
		if (!from.getParameters().equals(to.getParameters())) {	
			if (from.getParameters().size() == to.getParameters().size()) {
				int diffCount =0;
				for (int i = 0; i < from.getParameters().size(); i++) {
					String fromP = from.getParameters().get(i);
					String toP = to.getParameters().get(i);
					if (!fromP.equals(toP)) {
						diffCount++;
						// basically the last one is taken care of
						if (fromP.indexOf('[') > 0 || toP.indexOf('[') > 0) {
							paramT = new Transformation(
									Transformation.ARG_REPLACE, fromP, toP);
						}
					}
				}
				if (diffCount!=1) {
					paramT=null;
				}
			} else if (from.getParameters().size() < to.getParameters().size()) {
				boolean same = true;
				for (int i = 0; i < from.getParameters().size(); i++) {
					String fromP = from.getParameters().get(i);
					String toP = to.getParameters().get(i);
					if (!fromP.equals(toP)) {
						same = false;
					}
				}
				if (same == true) {
					List<String> appendOperand = new ArrayList<String>();
					for (int i = from.getParameters().size(); i < to.getParameters()
							.size(); i++) {
						String toP = to.getParameters().get(i);
						appendOperand.add(toP);
					}
					paramT = new Transformation(
							Transformation.PARAM_APPEND, appendOperand);
				}
			} else {
				TreeSet fromSet = new TreeSet(from.getParameters());
				TreeSet toSet = new TreeSet(to.getParameters());
				if (fromSet.containsAll(toSet)) {
					// from.getParameters().size()> to.getParameters().size()
					List<String> deleteOperand = new ArrayList<String>();
					for (int i = 0; i < from.getParameters().size(); i++) {
						String fromP = from.getParameters().get(i);
						if (!to.getParameters().contains(fromP)) {
							deleteOperand.add(fromP);
						}
					}
					if (deleteOperand.size() > 0) {
						paramT = new Transformation(
								Transformation.PARAM_SET_DELETE, deleteOperand);
						JavaMethod converted=paramT.applyTransformation(from);
						if (!converted.getParameters().toString().equals(to.getParameters().toString())) { 
							paramT = null;
						}
					}
				}
			}
			if (paramT == null) {
				paramT = new Transformation(Transformation.PARAMETERS_REPLACE,
						from.getParameters(), to.getParameters());
			}
			// param only 
			Change paramChange = new Change();
			paramChange.addTransformation(paramT);
			list.add(paramChange);
			
			// package, param
			if (packageT!=null && paramT!=null) { 
				Change packageParamChange = new Change();
				packageParamChange.addTransformation(packageT);
				packageParamChange.addTransformation(paramT);
				list.add(packageParamChange);
			}
			// class, param,
			if (classT!=null && paramT!=null) { 
				 Change classParamChange = new Change(); 
				 classParamChange.addTransformation(classT);
				 classParamChange.addTransformation(paramT);
				 list.add(classParamChange);
			}
			// proc, param 
			if (procT!=null && paramT!=null) { 
				Change procParamChange = new Change();
				procParamChange.addTransformation(procT);
				procParamChange.addTransformation(paramT);
				list.add(procParamChange);
			}
			// return, param
			if (returnT !=null && paramT!=null) { 
				Change returnParamChange = new Change();
				returnParamChange.addTransformation(returnT);
				returnParamChange.addTransformation(paramT);
				list.add(returnParamChange);
			}
			// package, class, param
			if (packageT!=null && classT!= null && paramT!=null) { 
				Change packClassParamChange = new Change();
				packClassParamChange.addTransformation(packageT);
				packClassParamChange.addTransformation(classT);
				packClassParamChange.addTransformation(paramT);
				list.add(packClassParamChange);
			}
			// package, proc, param,
			if (packageT!=null && procT!=null && paramT!=null ){ 
				Change packProcParamChange = new Change();
				packProcParamChange.addTransformation(packageT);
				packProcParamChange.addTransformation(procT);
				packProcParamChange.addTransformation(paramT);
				list.add(packProcParamChange);
			}
			// package, return, param 
			if (packageT!=null && returnT!=null && paramT!=null) { 
				Change packReturnParamChange = new Change();
				packReturnParamChange.addTransformation(packageT);
				packReturnParamChange.addTransformation(returnT);
				packReturnParamChange.addTransformation(paramT);
				list.add(packReturnParamChange);
			}
			
			// class, proc, param, 
			if (classT!=null && procT!=null && paramT!=null) { 
				Change classProcParamChange = new Change();
				classProcParamChange.addTransformation(classT);
				classProcParamChange.addTransformation(procT);
				classProcParamChange.addTransformation(paramT);
				list.add(classProcParamChange);
			}
			// class, return, param, 
			if (classT!=null && returnT!=null && paramT!=null) { 
				Change classProcParamChange = new Change();
				classProcParamChange.addTransformation(classT);
				classProcParamChange.addTransformation(returnT);
				classProcParamChange.addTransformation(paramT);
				list.add(classProcParamChange);
			}
			// proc, return, param 
			if (procT!=null && returnT!=null && paramT!=null) { 
				Change procReturnParamChange = new Change();
				procReturnParamChange.addTransformation(procT);
				procReturnParamChange.addTransformation(returnT);
				procReturnParamChange.addTransformation(paramT);
				list.add(procReturnParamChange);
			}
			
			// package, class, proc, param
			if (packageT!=null && classT!=null && procT!=null && paramT!=null) {
				Change packageClassProcParamChange = new Change();
				packageClassProcParamChange.addTransformation(packageT);
				packageClassProcParamChange.addTransformation(classT);
				packageClassProcParamChange.addTransformation(procT);
				packageClassProcParamChange.addTransformation(paramT);
				list.add(packageClassProcParamChange);
			}
			// package, class, return, param 
			if (packageT!=null && classT!=null && returnT!=null && paramT!=null ){ 
				Change packClassReturnParamChange = new Change();
				packClassReturnParamChange.addTransformation(packageT);
				packClassReturnParamChange.addTransformation(classT);
				packClassReturnParamChange.addTransformation(returnT);
				packClassReturnParamChange.addTransformation(paramT);
				list.add(packClassReturnParamChange);
			}
			// package, proc, return, param, 
			if (packageT!=null && procT!=null && returnT!=null && paramT!=null) { 
				Change packProcReturnParamChange = new Change();
				packProcReturnParamChange.addTransformation(packageT);
				packProcReturnParamChange.addTransformation(procT);
				packProcReturnParamChange.addTransformation(returnT);
				packProcReturnParamChange.addTransformation(paramT);
				list.add(packProcReturnParamChange);
			}
			// class, proc, return, param
			if (classT!=null && procT!=null && returnT!=null && paramT!=null) {
				Change classProcReturnParamChange = new Change();
				classProcReturnParamChange.addTransformation(classT);
				classProcReturnParamChange.addTransformation(procT);
				classProcReturnParamChange.addTransformation(returnT);
				classProcReturnParamChange.addTransformation(paramT);
				list.add(classProcReturnParamChange);
			}
		}	
		return list;
	}
	public int size() { 
		return transformations.size();
	}
	public void mapByTransformation(HashMap<Transformation,ArrayList<Rule>> map, Rule r) {
		for (int i=0; i<transformations.size() ; i++) {
			Transformation t= transformations.get(i);
			if (map.containsKey(t)) {
				ArrayList<Rule> v = map.get(t);
				v.add(r);
			}else {
				ArrayList<Rule> v = new ArrayList<Rule>();
				v.add(r);
				map.put(t, v);
			}
		}
	}
	public static Change combine(ArrayList<Change> changes) {
		Change newC = new Change();
		for (int i=0; i< changes.size(); i++) { 
			Change c = changes.get(i); 
			for (int j=0; j<c.transformations.size();j++) {
				Transformation cj = c.transformations.get(j);
				if (!newC.transformations.contains(cj)) newC.addTransformation(cj);
			}
		}
		return newC;
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
				"org.jfree.chart.axis:CategoryAxis-drawCategoryLabels__[Graphics2D, Rectangle2D, Rectangle2D, RectangleEdge, AxisState]->AxisState"
		};
		JavaMethod[] jmList = new JavaMethod[methodPair.length];
		for (int i = 0; i < methodPair.length; i++) {
			jmList[i] = new JavaMethod(methodPair[i]);
		}

		boolean totalResult = true;
		int coverage[] = new int[10];
		for (int i = 0; i < methodPair.length; i = i + 2) {
			JavaMethod oldJM = jmList[i];
			JavaMethod newJM = jmList[i + 1];
			
			List<Change> transformations = createChange(oldJM, newJM);
			System.out.println("\n"+oldJM+"\t,\t"+newJM);
			int count =0;
			boolean atLeastOne =false;
			for (Iterator<Change> it =  transformations.iterator(); it.hasNext(); ){				
				Change transform = it.next();
				transform.writeXMLFile("temp");
				Change transform2 = Change.readXMLFile("temp");
				if (!transform2.equals(transform)) {
					System.out.println("ERROR");
					System.exit(0);
				}
				System.out.println("practice\t"+transform); 
				for (int j=0; j<transform.transformations.size();j++) { 
					Transformation t = transform.transformations.get(j);
					coverage[t.getType()]++;
				}
				Scope scope = new Scope(oldJM,transform);
				System.out.println(scope+"\n\t"+transform);
				JavaMethod transformed = transform.applyTransformation(oldJM);
				System.out.println("\tTRAN JM: " + transformed.equals(newJM));
				
				if (transformed.equals(newJM))  {
					atLeastOne= true;
				}
				if (!transform.isApplicable(oldJM)) {
					totalResult=false;
					System.out.println("\tIMPROSSIBLE");
				}
				count++;
			}
			if (atLeastOne==false) { 
				totalResult=false;
			}
		}
		System.out.println("totalResult\t"+totalResult);
		for (int i=0; i<10; i++) { 
			System.out.print(i+":"+coverage[i]+"\t");
		}
	}	
	public boolean hasOneTransformation() { 
		return (this.transformations.size()==1);
		
	}
}