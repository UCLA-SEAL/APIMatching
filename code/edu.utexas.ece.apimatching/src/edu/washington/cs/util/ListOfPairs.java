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
package edu.washington.cs.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.washington.cs.rules.JavaMethod;
import edu.washington.cs.rules.Transformation;



public class ListOfPairs {
	
	private static final String xmlTag = "listpairs";
	
	ArrayList<Pair> pairs;

	private int numDistinctLeft = 0;

	private int numDistinctRight = 0;
	
	public ListOfPairs(File f) { 
		this.pairs = new ArrayList<Pair>();
		read (f);
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
			format.setLineSeparator("\n");
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
	public static ListOfPairs readXMLFile (String filename){
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
	public static ListOfPairs readElement (Element listofpairs) {
		if (!listofpairs.getTagName().equals(xmlTag)) return null;
		ListOfPairs result = new ListOfPairs();
		NodeList children = listofpairs.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i) instanceof Element) {
				Element child = (Element) children.item(i);
				if (child.getTagName().equals(Pair.getXMLTag())) {
					String c = child.getTextContent();
					if (c!=null && !c.equals("") ) {
						Pair p = Pair.makeJavaMethodPair(c);
						result.addPair(p);
					}	else {
						Pair p = Pair.readElement(child);
						result.addPair(p);
						
					}
				}
			}
		}
		return result;
	}
	public static String getXMLTag() { 
		return xmlTag;
	}
	public Pair get(int i) { 
		return this.pairs.get(i);
	}
	public void writeElement (Element parent) {
		Element listofpairs = parent.getOwnerDocument().createElement(xmlTag);
		for (Iterator<Pair> it = this.pairs.iterator(); it.hasNext();) {
			Element pair = listofpairs.getOwnerDocument().createElement(Pair.getXMLTag());
			Pair p = it.next();
			p.writeElement(listofpairs);
		}
		parent.appendChild(listofpairs);
	}
	
//	public void writeElement(Element parent) { 
//		Element change = parent.getOwnerDocument().createElement(xmlTag);
//		for (int i=0; i<this.transformations.size(); i++) { 
//			Transformation t =transformations.get(i);
//			t.writeElement(change);
//		}
//		parent.appendChild(change);
//	}
	public void read(File f) {
		try {
			FileReader fread = new FileReader(f);
			BufferedReader reader = new BufferedReader(fread);
			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {
				Pair p = Pair.makeJavaMethodPair(line);
				this.pairs.add(p);
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}
	public void serialize(File f){
		try {
			FileOutputStream oStream = new FileOutputStream(f);
			PrintStream pStream = new PrintStream(oStream);
			for (Iterator<Pair> it = pairs.iterator(); it.hasNext();) {
				Pair p = it.next();
				pStream.println(p);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ListOfPairs(Set<Pair> ps){
		this.pairs = new ArrayList<Pair>(); 
		this.pairs.addAll(ps);
	}
	public ListOfPairs() {
		this.pairs = new ArrayList<Pair> ();
	}
	
	public void addPair (Pair p){ 
		this.pairs.add(p);
	}
	public void addSetOfPairs (ListOfPairs s){ 
		this.pairs.addAll(s.pairs);
	}
	public void addSetOfPairs (SetOfPairs s){
		
		for (Iterator<Pair> pairIt = s.iterator();pairIt.hasNext();) {
			Pair p = pairIt.next();
			this.pairs.add(p);
		}
	}
	public void removePair(Pair p) { 
		this.pairs.remove(p);
	}
	public void removeSetOfPairs(ListOfPairs s) { 
		this.pairs.removeAll(s.pairs);
	}
	public void removeAll () { 
		this.pairs = new ArrayList<Pair>();
	}
	public int numDistinctLeft() {
		HashSet lefts = new HashSet();
		for (Iterator<Pair> it = pairs.iterator();it.hasNext(); ) {
			Pair p = it.next(); 
			lefts.add(p.getLeft());
		}
		numDistinctLeft = lefts.size();
		return numDistinctLeft;
	}
	public int numDistinctRight() {
		HashSet rights = new HashSet(); 
		for (Iterator<Pair> it = pairs.iterator();it.hasNext(); ){
			Pair p = it.next(); 
			rights.add(p.getRight());
		}
		numDistinctRight = rights.size(); 
		return numDistinctRight;
	}
	public Set getLeftDomain () { 
		HashSet lefts = new HashSet();
		for (Iterator<Pair> it = pairs.iterator();it.hasNext(); ) {
			Pair p = it.next(); 
			lefts.add(p.getLeft());
		}
		return lefts;
		
	}
	public Set getRightDomain() { 
		HashSet rights = new HashSet(); 
		for (Iterator<Pair> it = pairs.iterator();it.hasNext(); ){
			Pair p = it.next(); 
			rights.add(p.getRight());
		}
		return rights;
	}
	
	public Iterator<Pair> iterator() {
		return pairs.iterator();
	}
	public boolean includeAll (ListOfPairs s){ 
		return this.pairs.containsAll(s.pairs);
	}
	public boolean includeAll (SetOfPairs s){
		for (Iterator<Pair> pairIt = s.iterator(); pairIt.hasNext();) {
			Pair p = pairIt.next();
			if (!this.pairs.contains(p)) return false;
		}
		return true;
	}
	
	public boolean includeLeft (Object o) {
		for (Iterator<Pair> it = pairs.iterator(); it.hasNext(); ){
			Pair pair = it.next();
			if (pair.getLeft().equals(o)) { 
				return true;
			}
		}
		return false;
	}
	
	public boolean includeRight (Object o) {
		for (Iterator<Pair> it = pairs.iterator(); it.hasNext();) {
			Pair pair = it.next();
			if (pair.getRight().equals(o)) { 
				return true; 
			}
		}
		return false;
	}
	public Object getRightByLeft (Object o) {
		for (Iterator<Pair> it = pairs.iterator(); it.hasNext(); ){
			Pair pair = it.next();
			if (pair.getLeft().equals(o)) { 
				return pair.getRight();
			}
		}
		return null;
	}
	public Object getLeftByRight (Object o) {
		for (Iterator<Pair> it = pairs.iterator(); it.hasNext();) {
			Pair pair = it.next();
			if (pair.getRight().equals(o)) { 
				return pair.getLeft(); 
			}
		}
		return null;
	}
	public Pair getByLeft (Object o) {
		for (Iterator<Pair> it = pairs.iterator(); it.hasNext(); ){
			Pair pair = it.next();
			if (pair.getLeft().equals(o)) { 
				return pair;
			}
		}
		return null;
	}
	public boolean contains(Object o) { 
		return pairs.contains(o);
	}
	public int size ( ) {
		return pairs.size();
	}
	public void sort () {
		Collections.sort(pairs, new PairComparator ());
	}
	public String toString () {
		String s = "";
		for (Iterator<Pair> it = pairs.iterator(); it.hasNext();) {
			Pair pair = it.next();
			s = s+pair.toString()+"\n";
		}
		return s;
	}
	public ListOfPairs inverse () { 
		ListOfPairs inverseResult = new ListOfPairs();
		for (Iterator<Pair> it = pairs.iterator(); it.hasNext();) {
			Pair pair = it.next();
			Pair inversePair = new Pair(pair.getRight(),pair.getLeft());
			inverseResult.addPair(inversePair);
		}
		return inverseResult;
	}
	public static void main(String arg[]) {
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
		ListOfPairs list = new ListOfPairs();
		for (int i = 0; i < methodPair.length; i=i+2) {
			JavaMethod oldM = new JavaMethod(methodPair[i]);
			JavaMethod newM = new JavaMethod(methodPair[i+1]);
			Pair pair  = new Pair(oldM,newM);
			list.addPair(pair);
		}	
		list.writeXMLFile("listpair.dataformat");
	
	}
}
