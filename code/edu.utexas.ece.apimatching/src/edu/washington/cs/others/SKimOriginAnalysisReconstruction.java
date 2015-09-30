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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SKimOriginAnalysisReconstruction {
	ArrayList<SKimTransaction> transactions  = new ArrayList<SKimTransaction> ();
	public static SKimOriginAnalysisReconstruction readXMLFile(String filename) {
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
		SKimOriginAnalysisReconstruction reconstruction = new SKimOriginAnalysisReconstruction();
		NodeList children = doc.getDocumentElement().getChildNodes();
		TreeSet<Integer> revisionIDSet = new TreeSet<Integer>();
		File outDir = new File("c:\\OriginAnalysisData\\jedit_transaction");
		outDir.mkdir();
		for (int i=0; i< children.getLength(); i++) { 
			Node child = children.item(i);
			if (child instanceof Element) {
				Element m = (Element) child;
				if (m.getTagName().equals("transaction")) {
					NodeList grandchildren = m.getChildNodes();
					int oldRevisionId = 0;
					int newRevisionId = 0;
					for (int j = 0; j < grandchildren.getLength(); j++) {
						if (grandchildren.item(j) instanceof Element) {
							Element grandchild = (Element) grandchildren.item(j);
							if (grandchild.getTagName().equals("oldrevision")) {
								oldRevisionId = new Integer(grandchild
										.getTextContent()).intValue();
								revisionIDSet.add(new Integer(grandchild.getTextContent()));
							}
							if (grandchild.getTagName().equals("newrevision")) {
								newRevisionId = new Integer(grandchild
										.getTextContent()).intValue();
								revisionIDSet.add(new Integer(grandchild.getTextContent()));
								break;
							}
						}
					}
					File output = new File(outDir,"transaction"+oldRevisionId+"-"+newRevisionId+".xml" );
					writeXMLElementToFile(output.getAbsolutePath(), m);
				}
			}
		}
		File index = new File("c:\\OriginAnalysisData\\jedit_revisions"); 
		try { 
			FileOutputStream outStream = new FileOutputStream(index);
			PrintStream pStream = new PrintStream(outStream);
			for (Iterator<Integer> it = revisionIDSet.iterator(); it.hasNext();){ 
				Integer i = it.next();
				pStream.println(i.intValue());
			}
		}catch (Exception e) { 
			e.printStackTrace();
		}
		return reconstruction;
	}
	public static void main(String args[]) {
		readXMLFile("c:\\OriginAnalysisData\\jedit.xml");
	}
	public static void writeXMLElementToFile(String filename, Element m){ 
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
			assert (m != null);
			serializer.serialize(m);
			outstream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
