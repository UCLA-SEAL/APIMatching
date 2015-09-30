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

import edu.washington.cs.rules.JavaMethod;



public class XSCategory {
	String kind;
	public static final String PACKAGE = "Package";
	public static final String CLASS = "Class";
	public static final String INTERFACE = "Interface";
	public static final String FIELD = "Field";
	public static final String METHOD = "Method";
	public static final String CONSTRUCTOR = "Constructor";
	public static final String ARRAY = "Array";
	public XSCategory(String s ){
		if (s.equals(PACKAGE)) kind = PACKAGE;
		else if ( s.equals(CLASS)) kind = CLASS;
		else if (s.equals(INTERFACE)) kind = INTERFACE;
		else if (s.equals(FIELD)) kind = FIELD;
		else if (s.equals(METHOD)) kind = METHOD;
		else if (s.equals(CONSTRUCTOR)) kind = CONSTRUCTOR;
		else if (s.equals(ARRAY)) kind = ARRAY;
		else System.out.println("ERROR UNKNOWN KIND:" + s);
	}
	public boolean isRelevantToRule() { 
		return this.kind.equals(PACKAGE) || this.kind.equals(CLASS) || 
		this.kind.equals(INTERFACE) || this.kind.equals(METHOD) ||
		this.kind.equals(CONSTRUCTOR);
	}
	public String toString() {
		return kind;
	}
	public class ParseInfo { 
		String packageName =null; 
		String className =null;
		String procedureName=null;
		String paramList = null; // [A, B, C, D]

		public boolean isMatch(JavaMethod source) {
			boolean bPackage = source.getPackageName().equals(packageName);
			boolean bClass = source.getClassName().equals(className);
			boolean bProced = source.getProcedureName().equals(procedureName);
			boolean bParam = source.getParameters().toString()
					.equals(paramList);
			if (kind.equals(PACKAGE))
				return bPackage;
			else if (kind.equals(CLASS) || kind.equals(INTERFACE))
				return bPackage && bClass;
			else if (kind.equals(CONSTRUCTOR) || kind.equals(METHOD))
				return bPackage && bClass && bProced && bParam;
			else
				return false;
		}
	}
	public ParseInfo parse(String content) {
		ParseInfo pInfo = new ParseInfo();
		if (this.kind.equals(PACKAGE)) {
			pInfo.packageName = content;
		}else if (this.kind.equals(CLASS) || this.kind.equals(INTERFACE)) { 
			int packageEnd = content.lastIndexOf(".");
			pInfo.packageName = content.substring(0, packageEnd);
			pInfo.className = content.substring(packageEnd + 1);
		}else if (this.kind.equals(METHOD) || this.kind.equals(CONSTRUCTOR)){ 
			int classEnd = content.lastIndexOf(".");
			String fullClassName = content.substring(0, classEnd);
			String afterClassName = content.substring(classEnd + 1);
			// parse fullClassName
			int packageEnd = fullClassName.lastIndexOf(".");
			pInfo.packageName = fullClassName.substring(0, packageEnd);
			pInfo.className = fullClassName.substring(packageEnd + 1);
			int procedureEnd = afterClassName.indexOf("(");
			pInfo.procedureName = afterClassName.substring(0, procedureEnd);
			pInfo.paramList = afterClassName.substring(procedureEnd);
			pInfo.paramList = pInfo.paramList.replace(",", ", ");
			pInfo.paramList = pInfo.paramList.replace("(", "[");
			pInfo.paramList = pInfo.paramList.replace(")", "]");
		}
		return pInfo;
	}
}
