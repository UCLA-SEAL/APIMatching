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
package edu.ucsc.cse.grase.origin.kimmy;

import java.util.ArrayList;

public class Tokenize {
	
	public static void main (String arg[]) {		
		String tokens[] = Tokenize.tokenizeCapitalLetter("MiryungKimCreateBarChart");
		String str ="com.jrefinery.chart:XYZStackedHorizontalBarRenderer-StackedHorizontalBarRenderer__[]->void";
		str ="com.jrefinery.chart.axis:ValueAxis-getAutoRangeMinimumSize__[]->Number";
		
		tokens = Tokenize.tokenizeCapitalLetter(str);
		System.out.println("numTokens: "+tokens.length);
		for (int i=0 ; i<tokens.length; i++) { 
			System.out.println(tokens[i]);
		}
	}
	
	private static String [] tokenizeCapitalLetter (String a){
		String buf = "";
		ArrayList<String> list = new ArrayList<String> (); 
		for (int i=0; i<a.length(); i++) {
			if ( 
				(a.charAt(i)>='A' && a.charAt(i)<='Z' && (i+1)<a.length() && (a.charAt(i+1)<'A' || a.charAt(i+1)>'Z'))
				|| (a.charAt(i)>='A' && a.charAt(i)<='Z' && (i-1)>0 && (a.charAt(i-1) <'A' || a.charAt(i-1)>'Z'))
				|| (a.charAt(i)=='*') || a.charAt(i)==' ' || a.charAt(i)=='.'
				|| (a.charAt(i) =='-')){
				if (!buf.equals("")) {
					list.add(buf);
					buf ="";
				}
			}
			if (a.charAt(i)!=' ' && a.charAt(i)!='.') buf = buf+(a.charAt(i));
			
		}
		list.add(buf);
		
		String [] re = {};
		String [] arr = list.toArray(re);
		
		return arr;
	}
	
	private static String [] tokenizedPatterns (String a ){
		
		String [] tokens = tokenizeCapitalLetter(a);
		String [] results = new String[tokens.length+1];
		for (int i=0; i<tokens.length-1; i++) {
			tokens[i] =tokens[i]+"*";
		}
		for (int i=1; i<tokens.length; i++) { 
			tokens[i] = "*"+tokens[i];
		}
		if (tokens.length==1) return tokens;
		for (int i=0; i<tokens.length; i++) {
			results[i]=tokens[i];
		}
		results[tokens.length] = a;
 		return results;
	}
}
