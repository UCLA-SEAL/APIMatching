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

import edu.washington.cs.rules.JavaClass;
import edu.washington.cs.rules.JavaMethod;
import edu.washington.cs.rules.JavaMethodRegex;

public class WDSymbol {
	private String symaction; 
	private String symtype; 
	
	private static String METHOD = "Method";

	private static String METHODINTERFACE = "MethodInterface";
	private static String CLASS = "Class";
	private static String ADD = "added";
	private static String REMOVE = "removed";
	private static String xmlTag = "symbol";

	private JavaMethod jm = null;
	private JavaClass classname = null;
	public static String getXMLTag(){ 
		return xmlTag;
	}
	public boolean isAdded(){ 
		if (ADD.equals(this.symaction)) return true;
		return false;
	}
	public boolean isDeleted() { 
		if (REMOVE.equals(this.symaction)) return true;
		return false;
	}
	
	public boolean isMethod() { 
		if (METHOD.equals(this.symtype) || METHODINTERFACE.equals(this.symtype)) return true;
		return false;
	}
	public JavaMethod getJavaMethod() {
		if (jm!=null) return jm;
		return null;
	} 
	public JavaClass getJavaClass() {
		if (classname!=null) return classname;
		return null;
	}
	public WDSymbol(String action, String type, String content) { 
		this.symaction = action;
		this.symtype = type;
		if (type.equals(METHOD) || type.equals(METHODINTERFACE)){ 
			content = content.replace("(default package)", "");
			content = content.replace("(", "__[");
			content = content.replace(')', ']');
			content = content.replace(":", "->");
			if (content.indexOf("->")<0) { 
				content = content +"->void";
			}
			int packageEnd = content.lastIndexOf(".");
			String packageName = content.substring(0, packageEnd);
			String classNameAfter = content.substring(packageEnd + 1);
			String jmContent = packageName + ":" + classNameAfter;
			
			boolean b= JavaMethodRegex.checkJavaMethodPattern(jmContent);
			
			if (b==false){ 
//				System.out.println(content+"\n\t"+jmContent);
				this.jm= null;
			}else { 
				this.jm = new JavaMethod(jmContent);
			}
		}else if (type.equals(CLASS)) {
			this.classname = new JavaClass(content);
		}
	}
	public static WDSymbol readElement(Element symbol) {
		if (symbol.getTagName().equals(xmlTag)) { 
			String action = symbol.getAttribute("symaction");
			String type = symbol.getAttribute("symtype");
			String text = symbol.getTextContent();
			WDSymbol s = new WDSymbol(action,type,text);
			return s;
		}
		return null;
	}
}
