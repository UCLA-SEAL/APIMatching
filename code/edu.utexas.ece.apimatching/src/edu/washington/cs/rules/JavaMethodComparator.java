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

import java.util.Comparator;

public class JavaMethodComparator implements Comparator<JavaMethod>{
	public int compare(JavaMethod o1, JavaMethod o2) {
		String o1Package = o1.getPackageName(); 
		String o2Package = o2.getPackageName();
		int packageCompare = o1Package.compareTo(o2Package);
		if (packageCompare!=0) return packageCompare; 
		String o1Class = o1.getClassName(); 
		String o2Class = o2.getClassName(); 
		int classCompare = o1Class.compareTo(o2Class); 
		if (classCompare!=0) return classCompare;
		String o1Procedure = o1.getProcedureName();
		String o2Procedure = o2.getProcedureName();
		int procedureCompare = o1Procedure.compareTo(o2Procedure);
		if (procedureCompare!=0) return procedureCompare;
		return (o1.toString().compareTo(o2.toString()));
	}

}
