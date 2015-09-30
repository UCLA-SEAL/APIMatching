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


public class XSParamList extends XSChange {

	private final String prevParamName;

	private final String prevParamType;

	private final String nextParamName;

	private final String nextParamType;

	public static String CHANGE_IDENTIFIER = "change_identifier";

	public static String RENAMEORMOVE_PARAMTYPE = "renameormove_paramtype";

	public static String REMOVE_PARAMETER = "remove_parameter";

	public static String ADD_PARAMETER = "add_parameter";

	public static String CHANGE_PARAMTYPE = "change_paramtype";

	public XSParamList(String s) {
		String[] words = s.split(";");
		this.version = words[0];
		this.category = new XSCategory (XSCategory.METHOD);
		this.change = words[1];
		if (!isValid(words[1]))
			this.change = "ERROR";
		this.prev = words[2];
		this.next = words[3];
		if (words.length > 4) {
			this.prevParamName = words[4];
		} else {
			this.prevParamName = null;
		}
		if (words.length > 5) {
			this.prevParamType = words[5];
		} else {
			this.prevParamType = null;
		}
		if (words.length > 6) {
			this.nextParamName = words[6];
		} else {
			this.nextParamName = null;
		}
		if (words.length >= 7) {
			this.nextParamType = words[7];
		} else {
			this.nextParamType = null;
		}
	}

	public boolean isValid(String s) {
		return s.equals(CHANGE_IDENTIFIER) || s.equals(RENAMEORMOVE_PARAMTYPE)
				|| s.equals(REMOVE_PARAMETER) || s.equals(ADD_PARAMETER)
				|| s.equals(CHANGE_PARAMTYPE);
	}
	public void tally(XSStat stat) {
		stat.paramCount++;
		if (this.change.equals(CHANGE_IDENTIFIER)) {
			stat.param_change_identifier++;
		}else if (this.change.equals(RENAMEORMOVE_PARAMTYPE)) {
			stat.param_renameormove_paramtype++;
		}else if (this.change.equals(REMOVE_PARAMETER)) {
			stat.param_remove_parameter++;
		}else if (this.change.equals(ADD_PARAMETER)) {		
			stat.param_add_parameter++;
		}else if (this.change.equals(CHANGE_PARAMTYPE) ) {
			stat.param_change_paramtype++;
		}else {
			System.out.println("PARAM CHANGE NONE OF ABOVE");
		}
	}
}

// if (this.change.equals(CHANGE_IDENTIFIER)) {
// }
// if (this.change.equals(RENAMEORMOVE_PARAMTYPE)) {
// }
// if (this.change.equals(REMOVE_PARAMETER)) {
// }
// if (this.change.equals(ADD_PARAMETER)) {
// }
// if (this.change.equals(CHANGE_PARAMTYPE)) {
//}
