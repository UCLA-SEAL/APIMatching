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
package edu.ucsc.cse.grase.origin.entity;

import java.util.ArrayList;
import java.util.List;

import edu.ucsc.cse.grase.origin.util.ReadContent;

/**
 * Simplified method for origin analysis
 * 
 * @author hunkim
 * 
 */
public class Method {
	
	ModifierBit modifier; 
	
	String fullFuleName;

	String fileName;

	String name;

	Signature signature;

	int startLine;

	int startPos;

	int endLine;

	int endPos;

	/**
	 * @return Returns the endLine.
	 */
	public int getEndLine() {
		return endLine;
	}

	/**
	 * @param endLine
	 *            The endLine to set.
	 */
	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}

	/**
	 * @return Returns the endPos.
	 */
	public int getEndPos() {
		return endPos;
	}

	/**
	 * @param endPos
	 *            The endPos to set.
	 */
	public void setEndPos(int endPos) {
		this.endPos = endPos;
	}

	/**
	 * @return Returns the fileName.
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName
	 *            The fileName to set.
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the signature.
	 */
	public Signature getSignature() {
		return signature;
	}

	/**
	 * @param signature
	 *            The signature to set.
	 */
	public void setSignature(Signature signature) {
		this.signature = signature;
	}

	/**
	 * @return Returns the startLine.
	 */
	public int getStartLine() {
		return startLine;
	}

	/**
	 * @param startLine
	 *            The startLine to set.
	 */
	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	/**
	 * @return Returns the startPos.
	 */
	public int getStartPos() {
		return startPos;
	}

	/**
	 * @param startPos
	 *            The startPos to set.
	 */
	public void setStartPos(int startPos) {
		this.startPos = startPos;
	}

	public ModifierBit getModifier() {
		return modifier;
	}

	public void setModifier(ModifierBit modifier) {
		this.modifier = modifier;
	}

	/**
	 * Unique id for the method. If the id is same, we assume it is the same
	 * method
	 * 
	 * @return
	 */
	public String getId() {
		return getIdNoSignature() + " " + signature;
	}
	
	public String getKimmyId() { 
		return name+" "+signature;
	}

	/**
	 * Get id without signature
	 * 
	 * @return
	 */
	public String getIdNoSignature() {
		return fileName + "::" + name;
	}

	public String getBody() {
		// TODO Auto-generated method stub
		return ReadContent.getContent(fullFuleName, startPos, endPos);
	}

	/**
	 * @return Returns the fullFuleName.
	 */
	public String getFullFuleName2(int java) {
		return fullFuleName;
	}
	public String getFullFileName(){ 
		return fullFuleName;
	}

	/**
	 * @param fullFuleName
	 *            The fullFuleName to set.
	 */
	public void setFullFuleName(String fullFuleName) {
		this.fullFuleName = fullFuleName;
	}

	public List getInComingEdges() {
		// TODO Auto-generated method stub
		return new ArrayList();
	}

	public List getOutGoingEdges() {
		// TODO Auto-generated method stub
		return new ArrayList();
	}

	public String toString() {
		return getId() + "\t" + startPos + "\t" + endPos;
	}

	public boolean equals(Object obj) {
		if (obj instanceof Method) {
			Method method = (Method) obj;
			return getId().equals(method.getId());
		}
		return false;
	}
	public String getTypeSignature () { 
		String s[]  =signature.getParameterTypes();
		String temp ="";
		for (int i =0; i<s.length; i++) {
			temp = temp +s[i];
		}
		return temp+" "+ signature.getReturnType();
	}
}
