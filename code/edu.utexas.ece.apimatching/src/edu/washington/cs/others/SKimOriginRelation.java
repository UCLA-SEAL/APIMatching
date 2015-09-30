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

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.washington.cs.rules.JavaMethod;
import edu.washington.cs.rules.JavaMethodRegex;
import edu.washington.cs.util.Pair;

public class SKimOriginRelation{
	final String oldFileName;
	final String newFileName;
	final JavaMethod oldMethod;
	final JavaMethod newMethod; 
	final boolean sigChange;
	final boolean isOrigin; 
	final SKimSimilarity similarity; 

	public static String xmlTag = "result";
	public static String getXMLTag () {
		return xmlTag;
	}
	public Pair getOriginPair () { 
		return new Pair(oldMethod, newMethod);
	}
	public String toString () {
		return getOriginPair().toString()+ similarity.toString();
	}
 	public SKimOriginRelation (Element tElement) {
		JavaMethod oldMethod =null;
		JavaMethod newMethod =null;
		String oldFileName = null;
		String newFileName = null;
		boolean sigChange =false;
		boolean isOrigin =false; 
		SKimSimilarity similarity =null; 
		NodeList children = tElement.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i) instanceof Element) {
				Element child = (Element) children.item(i);
				
				if (child.getTagName().equals("oldfunction")) {
					oldMethod = parseJavaMethod(child);
					oldFileName = parseFileName(child);
					
				}else if (child.getTagName().equals("newfunction")) {
					newMethod = parseJavaMethod(child);
					newFileName = parseFileName(child);
				}else if (child.getTagName().equals("is_origin")) {
					sigChange = new Boolean(child.getAttribute("sigchange"));
					isOrigin = new Boolean(child.getTextContent()).booleanValue();
				}else if (child.getTagName().equals("similarity")) {
					similarity = new SKimSimilarity(child);
				}
			}
		}
		this.oldFileName = oldFileName;
		this.newFileName = newFileName;
		this.oldMethod = oldMethod; 
		this.newMethod = newMethod;
		this.sigChange = sigChange;
		if (oldMethod.equals(newMethod)) this.isOrigin=false;
		else this.isOrigin = isOrigin;
		this.similarity = similarity;
	}
 	
 	
 	public JavaMethod parseJavaMethod(Element child ) {
 		
 		String returnType = child.getAttribute("ret");
		if (returnType.equals("")) returnType="void";
		else { 
			int dot = returnType.lastIndexOf(".");
			if (dot>0) {
				returnType = returnType.substring(dot+1);
			}
		}
		String content = child.getTextContent();
		int paramstart = content.indexOf('(');
		int paramend = content.lastIndexOf(')');
		String params = content.substring(paramstart + 1, paramend);

		int filenamesep = content.indexOf(":");
		String beforeParams = content.substring(filenamesep+1, paramstart);

		int packagesep = beforeParams.lastIndexOf(".");
		String packageName = beforeParams.substring(0, packagesep);
		String classNameProcedure = beforeParams.substring(packagesep+1);
		classNameProcedure = classNameProcedure.replace(":","-");
		String jmString = null;
		if (!params.equals("")) {
			ArrayList<String> types = new ArrayList<String>();
			params = params.replace(",", " ");
			String paramList[] = params.split(" ");
			for (int j = 0; j < paramList.length; j++) { 
				if (j %2==0){
					int dot = paramList[j].indexOf(".");
					if (dot>0) {
						types.add(paramList[j].substring(0,dot));
					}else types.add(paramList[j]);
				}
			}
			jmString = packageName+ ":"+classNameProcedure+"__"+types.toString()+"->" +returnType;
		}else { 
			jmString = packageName+ ":"+classNameProcedure+"__["+ params+"]" +"->" +returnType;
		}
		boolean b =JavaMethodRegex.checkJavaMethodPattern(jmString);
		JavaMethod jm = null;
		if (b){ 
			jm= new JavaMethod(jmString);
		}else { 
			System.out.println(jmString);
		}
		return jm;
 	}
	
 	public String parseFileName(Element child ) {
		String content = child.getTextContent();
		int filenamesep = content.indexOf(":");
		String fileName = content.substring(0,filenamesep);
		return fileName;
 	}
			
}