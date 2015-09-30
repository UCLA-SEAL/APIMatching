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

import org.w3c.dom.Element;

import edu.washington.cs.comparison.LCS;
import edu.washington.cs.comparison.Tokenize;
import edu.washington.cs.rules.JavaMethod;

public class Pair<T extends Comparable>implements Comparable{

	T t1;

	T t2;

	private static final String xmlTag ="pair";
	
	public Pair(T tt1, T tt2) {
		this.t1 =tt1;
		this.t2 =tt2;
	}

	public int compareTo(Object o) {

		if (o instanceof Pair) {
			Pair po = (Pair) o;
			String thisString = this.toString();
			String poString = po.toString();
			return thisString.compareTo(poString);
		}
		return -1;
		
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof Pair) {
			Pair po  = (Pair)obj;
			return po.toString().equals(this.toString());
		}
		return false;
	}	
	public int hashCode() {
		return toString().hashCode();
	}

	public String toString(){ 
		String s1 ="empty", s2 ="empty"; 
		if (t1!=null) s1= t1.toString();
		if (t2!=null) s2= t2.toString();
		String s="";
//		if (t1 != null && t2 != null) {
//			double simSeqToken = new SeedMatchGenerator().similaritySeqToken(
//					(JavaMethod) this.getLeft(), (JavaMethod) this.getRight());
//			s = " SQ" + (int) (simSeqToken * 100) + "%";
//		}
		return s+"{"+s1+"\t"+s2+"}";
	}
	public T getLeft() { 
		return t1;
	}
	public T getRight() {
		return t2;
	}
	public static String getXMLTag () { 
		return xmlTag;
	}
	public void writeElement(Element parent) {
		Element pair = parent.getOwnerDocument().createElement(xmlTag);
		String s1 ="empty", s2 ="empty"; 
		if (t1!=null) s1= t1.toString();
		if (t2!=null) s2= t2.toString();
	
		pair.setAttribute("leftmethod", s1);
		pair.setAttribute("rightmethod", s2); 
	    parent.appendChild(pair);
	}
	public static Pair readElement(Element element) { 
		String left = element.getAttribute("leftmethod");
		String right = element.getAttribute("rightmethod");
		JavaMethod t1 =null;
		if (!left.equals("empty")) t1 =new JavaMethod(left);;
		
		JavaMethod t2 = null;
		if (!right.equals("empty")) t2= new JavaMethod(right);
	
		Pair p = new Pair<JavaMethod>(t1, t2);
		return p;
	}
	public static Pair makeJavaMethodPair (String s){
		try {
			String left = s.substring(s.indexOf('{') + 1, s.indexOf('\t'));
			JavaMethod t1 =null;
			if (!left.equals("empty")) t1 =new JavaMethod(left);;
			
			String right = s.substring(s.indexOf('\t') + 1, s.indexOf('}'));
			JavaMethod t2 = null;
			if (!right.equals("empty")) t2= new JavaMethod(right);
			return new Pair(t1, t2);
		
		}catch (StringIndexOutOfBoundsException e){
			e.printStackTrace();
			System.out.println(s);
		}
		return null;
	}	
	
	public double numSharedTokenScore() {
		if (!(t1 instanceof JavaMethod)) return 0; 
		if (!(t2 instanceof JavaMethod)) return 0;
		JavaMethod o = (JavaMethod) t1;
		JavaMethod n = (JavaMethod) t2;
		String oldN = o.toString(); 
		String newN = n.toString();
		
		String oldNameTokens[] = Tokenize.tokenizeOrderingLetter(oldN);
		String newNameTokens[] = Tokenize.tokenizeOrderingLetter(newN);
		double score1 =(double) LCS.getNumSharedTokens(oldNameTokens, newNameTokens);
		double max1 = (double) Math.max(oldNameTokens.length
				, newNameTokens.length
				);
		
		double result1 = score1/max1;
		return result1;
	}
}