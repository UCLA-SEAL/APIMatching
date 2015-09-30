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

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SKimSimilarity {
	final double signature; 
	final double totalSimilarity;
	final double name;
	final double body; 
	final double complexity; 
	final double ccfinder;
	final double location;
	final double in; 
	final double out; 
	final double moss;
	public String toString () {
		String s="  sig" + signature; 
		s = s+"  tS"+ totalSimilarity;
		s = s+"  n" +name;
		s = s+"  c" +complexity;
		s = s+"  cf" +ccfinder;
		s = s+"  l"+location;
		s = s+"  i" +in;
		s = s+"  out"+out;
		s = s+"  ms"+moss;
		return s;
	}
	public SKimSimilarity(Element tElement) {
		double totalSimilarity=0;
		double signature= 0; 
		double name=0;
		double body=0; 
		double complexity=0; 
		double ccfinder=0;
		double location=0;
		double in=0; 
		double out=0; 
		double moss=0;
		NodeList children = tElement.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i) instanceof Element) {
				Element child = (Element) children.item(i);
				if (child.getTagName().equals("totalsimilarity")) {
					totalSimilarity = new Double(child.getTextContent()).doubleValue();
				} else if (child.getTagName().equals("signature")) {
					signature = new Double(child.getTextContent()).doubleValue();
				} else if (child.getTagName().equals("name")) {
					name = new Double(child.getTextContent()).doubleValue();
				} else if (child.getTagName().equals("body")) {
					body = new Double(child.getTextContent()).doubleValue();
				} else if (child.getTagName().equals("com")) {
					complexity = new Double(child.getTextContent());
				} else if (child.getTagName().equals("ccf")) {
					ccfinder = new Double(child.getTextContent());
				} else if (child.getTagName().equals("location")) {
					location = new Double(child.getTextContent());
				} else if (child.getTagName().equals("in")) {
					in = new Double(child.getTextContent());
				} else if (child.getTagName().equals("out")) {
					out = new Double(child.getTextContent());
				} else if (child.getTagName().equals("moss")) {
					moss = new Double(child.getTextContent());
				}
			}
		}
		this.totalSimilarity= totalSimilarity;
		this.signature= signature; 
		this.name=  name;
		this.body= body; 
		this.complexity = complexity; 
		this.ccfinder = ccfinder;
		this.location= location;
		this.in = in; 
		this.out = out; 
		this.moss = moss;
	}
}
