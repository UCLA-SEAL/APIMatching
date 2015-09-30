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

public class JavaClass {
	String packageName;
	String className; 
	public JavaClass(String s) { 
		int sep = s.lastIndexOf(".");
		this.packageName = s.substring(0,sep);
		this.className = s.substring(sep+1);
	}
	public String toString () { 
		return this.packageName+":"+this.className;
	}
	public String getPackageName() { 
		return packageName;
	}
	public String getClassName() { 
		return className;
	}
}
