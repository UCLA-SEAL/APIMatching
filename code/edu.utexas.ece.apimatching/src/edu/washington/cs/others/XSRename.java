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

public class XSRename extends XSChange{
	public static String RENAME = "rename";
	public XSRename(String s ){ 
		super(s);
	}
	public boolean isValid(String s) {
		return s.equals(RENAME);
	}
	@Override
	public void tally(XSStat stat) {
		stat.renameCount++;
		if (this.category.kind.equals(XSCategory.PACKAGE)) {
			stat.rename_package++;
		}else if (this.category.kind.equals(XSCategory.CLASS)) {
			stat.rename_class++;
		}else if (this.category.kind.equals(XSCategory.INTERFACE)) {
			stat.rename_interface++;
		}else if (this.category.kind.equals(XSCategory.CONSTRUCTOR)) {		
			stat.rename_constructor++;
		}else if (this.category.kind.equals(XSCategory.METHOD) ) {
			stat.rename_method++;
		}else if (this.category.kind.equals(XSCategory.FIELD) ) {
			stat.rename_field++;
		}else { 
			System.out.println("NOT RENAME PACKAGE, CLASS, INTERFACE, CONSTRUCTOR, METHOD, FIELD ");
		}
	}
}
