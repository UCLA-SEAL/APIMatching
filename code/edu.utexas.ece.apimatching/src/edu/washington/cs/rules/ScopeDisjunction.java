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

import org.apache.xerces.dom.DOMImplementationImpl;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class ScopeDisjunction {

	private final static String xmlTag = "scopeDisjunction";
	private ArrayList<Scope> scopes = new ArrayList<Scope>();
	
	public void delete (Scope s) { 
		if (this.scopes.contains(s)) this.scopes.remove(s);
	}
	public void add(Scope s) {
		if (!this.scopes.contains(s))
			this.scopes.add(s);	
	}
	public void mark(Scope s) {
		s.mark();
	}
	public void unmark(Scope s){ 
		s.unmark();
	}
	public int size() { 
		return this.scopes.size();
	}
	public Scope get(int i) { 
		return this.scopes.get(i);
	}
	public String toString () { 
		return scopes.size()+" Scope Disjunction";
	}
	public String toPrint () { 
		String s = "For all x:method-header in "; 
		int count =0;
		for (Iterator<Scope> scopeIt = scopes.iterator(); scopeIt.hasNext(); ){ 
			Scope scope = scopeIt.next();
			if (count>0) {
				s = s+ "\n";
				s = s + "\t||\t" + scope.getCanonicalScopeExpression();
			}else { 
				s = s+scope.getCanonicalScopeExpression();
			}
			count++;
		}
		s = s+ ",";
		return s;
	}
	public ArrayList<Scope> getListOfScope() { 
		return scopes;
	}
	public boolean isApplicable (JavaMethod source) {	
		for (int i=0; i<scopes.size(); i++) { 
			Scope s = this.scopes.get(i);
			if (s.isKept() && s.match(source)) return true;
		}
		return false;
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
	public static ScopeDisjunction readXMLFile (String filename){
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
	public static ScopeDisjunction readElement (Element setofpairs) {
		if (!setofpairs.getTagName().equals(xmlTag)) return null;
		ScopeDisjunction result = new ScopeDisjunction();
		NodeList children = setofpairs.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i) instanceof Element) {
				Element child = (Element) children.item(i);
				if (child.getTagName().equals(Scope.getXMLTag())) {
					Scope s = Scope.readElement(child); 
					result.add(s);
				}
			}
		}
		return result;
	}
	public static String getXMLTag() { 
		return xmlTag;
	}
	public void writeElement (Element parent) {
		Element scopeDisj = parent.getOwnerDocument().createElement(xmlTag);
		for (Iterator<Scope> it = this.scopes.iterator(); it.hasNext();) {
			Scope s = it.next();
			s.writeElement(scopeDisj);
		}
		parent.appendChild(scopeDisj);
	}

}
