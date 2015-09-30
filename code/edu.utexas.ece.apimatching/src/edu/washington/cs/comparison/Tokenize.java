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
package edu.washington.cs.comparison;

import java.util.ArrayList;

public class Tokenize {
	
	public static void main (String arg[]) {
		
		String tokensXY[] = tokenizedPatterns("XYItemRenderer");
		
		String tokens[] = Tokenize
				.tokenizeCapitalLetter("MiryungKimCreateBarChart");
		 String str
		 ="com.jrefinery.chart:XYZStackedHorizontalBarRenderer-stackedHorizontalBarRenderer__[]->void";
		 str="com.jrefinery.chart.axis:HorizontalAxis-reserveHeight__[Graphics2D, Plot, Rectangle2D, int, double, int]->double";
//		String str = "" +
//				"{com.jrefinery.chart:AreaCategoryItemRenderer-AreaCategoryItemRenderer__[CategoryToolTipGenerator, CategoryURLGenerator]->void";

		tokens = Tokenize.tokenizeCapitalLetter(str);
		System.out.println("numTokens: "+tokens.length);
		for (int i=0 ; i<tokensXY.length; i++) { 
			System.out.println(tokensXY[i]);
		}
	}
	public static String [] tokenizeCapitalLetter (String a){
		String buf = "";
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < a.length(); i++) {
			if ((a.charAt(i) >= 'A' && a.charAt(i) <= 'Z'
					&& (i + 1) < a.length() && (a.charAt(i + 1) < 'A' || a
					.charAt(i + 1) > 'Z'))
					|| (a.charAt(i) >= 'A' && a.charAt(i) <= 'Z' && (i - 1) > 0 && (a
							.charAt(i - 1) < 'A' || a.charAt(i - 1) > 'Z'))
					|| (a.charAt(i) == '*')
					|| (a.charAt(i) == ',')
					|| (a.charAt(i) == ' ')) {
				if (!buf.equals("")) {
					list.add(buf);
					buf = "";
				}
			}
			buf = buf + (a.charAt(i));
		}
		list.add(buf);
		
		String [] re = {};
		String [] arr = list.toArray(re);
		
		return arr;
	}
	public static String [] tokenizeOrderingLetter (String a){
		String buf = "";
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < a.length(); i++) {
			if ((a.charAt(i) >= 'A' && a.charAt(i) <= 'Z'
					&& (i + 1) < a.length() && (a.charAt(i + 1) < 'A' || a
					.charAt(i + 1) > 'Z'))
					|| (a.charAt(i) >= 'A' && a.charAt(i) <= 'Z' && (i - 1) > 0 && (a
							.charAt(i - 1) < 'A' || a.charAt(i - 1) > 'Z'))
					|| (a.charAt(i) == '*')
					|| (a.charAt(i) == ',')
					|| (a.charAt(i) == ' ')) {
				if (!buf.equals("")) {
					list.add(buf);
					buf = "";
				}
			}
			buf = buf + (a.charAt(i));
		}
		list.add(buf);
		
		String [] re = {};
		String [] arr = list.toArray(re);
		
		return arr;
	}
	// used by only seed match generator 
	public static String [] breakCapitalLetter (String a){
		String buf = "";
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < a.length(); i++) {
			
			if ((a.charAt(i) >= 'A' && a.charAt(i) <= 'Z'
					&& (i + 1) < a.length() && (a.charAt(i + 1) < 'A' || a
					.charAt(i + 1) > 'Z'))
					|| (a.charAt(i) >= 'A' && a.charAt(i) <= 'Z' && (i - 1) > 0 && (a
							.charAt(i - 1) < 'A' || a.charAt(i - 1) > 'Z'))
					|| (a.charAt(i) == '*')
					|| (a.charAt(i) =='.')
					|| (a.charAt(i) == '>')
					|| (a.charAt(i) == '-')
					|| (a.charAt(i) ==':')
					|| (a.charAt(i)==' ')) {
				if (!buf.equals("")) {
					list.add(buf);
					buf = "";
				}
			}
			if ( (a.charAt(i) == ',')
					|| (a.charAt(i) == ' ')
					|| (a.charAt(i) == '-')
					|| (a.charAt(i) =='.')
					|| (a.charAt(i) == ':')
					|| (a.charAt(i) == '_')
					|| (a.charAt(i) == ' ')
					|| (a.charAt(i) == '>')
					|| (a.charAt(i) == '[')
					|| (a.charAt(i) == ']')) {
				//nothing
			}
			else buf = buf + (a.charAt(i));
		}
		list.add(buf);
		
		String [] re = {};
		String [] arr = list.toArray(re);
		
		return arr;
	}
	public static String [] tokenizedPatterns (String a ){
	
		String [] tokens = tokenizeCapitalLetter(a);
		String [] results = new String[tokens.length+1];
		for (int i=0; i<tokens.length-1; i++) {
			tokens[i] =tokens[i]+"*";
		}
		for (int i=1; i<tokens.length; i++) { 
			tokens[i] = "*"+tokens[i];
		}
		if (tokens.length==1) return tokens;
		results[0] = a;
		for (int i=0; i<tokens.length; i++) {
			results[i+1]=tokens[i];
		}
 		return results;
//		String [] results = new String[1];
//		results[0]=a;
//		return results;
	}
}
