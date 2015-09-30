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

public class WDMove extends WDRefactoringEvent{
	private static String xmlTag = "move";
	public static String getXMLTag () {
		return xmlTag;
	}

	public WDMove(Element m){
		super(m);
		if (m.getAttribute("symtype").equals("Method")) { 
			this.type = "WDMove Method";
		}else if (m.getAttribute("symtype").equals("Class")){ 
			this.type = "WDMove Class";
		}else if (m.getAttribute("symtype").equals("Interface")){ 
			this.type = "WDMove Interface";
		}else if (m.getAttribute("symtype").equals("Field")) {
			this.type = "WDMove Field";
		}else { 
			System.out.println("NOT EXHAUSTIVE"+m);
		}
	}
	
}
