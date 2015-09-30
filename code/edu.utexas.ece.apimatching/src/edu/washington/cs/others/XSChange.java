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

import edu.washington.cs.rules.JavaMethod;
import edu.washington.cs.util.Pair;
import edu.washington.cs.util.SetOfPairs;

public class XSChange {

	String version;
	XSCategory category;
	String change; 
	String prev;
	String next;
	public XSChange(String s){ 
		String [] words = s.split(";");
		this.version = words[0];
		this.category = new XSCategory (words[1]);
		this.change = words[2];
		if (!isValid(words[2])) {
			this.change ="ERROR";
		}
		this.prev = words[3];
		this.next = words[4];
	}
	public XSChange(){}
	
	boolean isValid (String s){ 
		return true;
	}
	public boolean checkCodomainValidity(
			ArrayList<JavaMethod> codomain) {
		for (int j = 0; j < codomain.size(); j++) {
			if (this.isMatch(codomain.get(j), this.next)) {
				// at least one match
				return true;
			}
		}
		if (this.isRelevant()) {
			//System.out.println("Invalid Next:" + this.toString());
		}
		return false;
	}
	public boolean checkDomainValidity(
			ArrayList<JavaMethod> domain) {
		for (int j = 0; j < domain.size(); j++) {
			if (this.isMatch(domain.get(j), this.prev)) {
				// at least one match
				return true;
			}
		}
		if (this.isRelevant()) {
			//System.out.println("Invalid Prev:" + this.toString());
		}
		return false;
	}
	public SetOfPairs applyTransformation(ArrayList<JavaMethod> input, ArrayList<JavaMethod> outputDomain) {
		SetOfPairs result = new SetOfPairs();
		for (int i = 0; i < input.size(); i++) {
			JavaMethod source = input.get(i);
			// check whether it's applicable
			boolean isMatch = isMatch(source, this.prev);
			JavaMethod target = null;
			if (isMatch) {
				target = updateTarget(source, this.next);
				if (source.equals(target)) {
					System.out.println("Why Same: Source"+source+"--"+this.prev+"---"+this.next);
				}
				// only if target exists in codomain. 
				if (outputDomain.contains(target)) {
					Pair p = new Pair(source, target);
					result.addPair(p);
				}
//				else if (this instanceof XSParamList) { 
//					System.out.println("Why ParamList change:"+this.change+"\n\t("+source+","+ target+")\n"+ 
//							"\tP"+this.prev+"\tN"+this.next );
//				}
			}
		}
		return result;
	}
	public boolean isRelevant() { 
		return this.category.isRelevantToRule();
	}
	public boolean isMatch(JavaMethod source, String prevName) {
		if (this.category==null) {
			System.out.println(prevName);
		}
		XSCategory.ParseInfo pInfo = this.category.parse(prevName);
		return pInfo.isMatch(source);
	}
	public JavaMethod updateTarget(JavaMethod source, String nextName) {
		XSCategory.ParseInfo nInfo = this.category.parse(nextName);
		String packageS = source.getPackageName();
		String classS = source.getClassName();
		String procedureS = source.getProcedureName();
		String paramListS = source.getParameters().toString();
		String returnS = source.getReturntype();
		
		if (nInfo.packageName!=null) { 
			packageS= nInfo.packageName;
		}
		if (nInfo.className!= null) { 
			classS = nInfo.className;
		}
		if (nInfo.procedureName !=null) { 
			procedureS = nInfo.procedureName;
		}
		if (nInfo.paramList!= null) { 
			paramListS = nInfo.paramList;
		}
		String jmString = packageS + ":" + classS + "-"
				+ procedureS + "__" + paramListS + "->"
				+ returnS;
		return new JavaMethod(jmString);
	}
	public void tally (XSStat stat) {
		
	}
	public static void main (String args[]) {
		XSChange c = new XSChange();
		c.category=new XSCategory(XSCategory.METHOD);
		JavaMethod jm =new JavaMethod(
				"com.jrefinery.data:TimeSeriesCollection-TimeSeriesCollection__[BasicTimeSeries]->void");
		
		boolean isMatch = c.isMatch(jm,"com.jrefinery.data.TimeSeriesCollection.TimeSeriesCollection(BasicTimeSeries)");
		JavaMethod target = c.updateTarget(jm,"com.jrefinery.data.TimeSeriesCollection.TimeSeriesCollection(TimeSeries)");
		System.out.println(isMatch);
		System.out.println(jm);
		System.out.println(target);
		System.out.println(jm.equals(target));
	}
	public String toString () { 
		return (version+"\t"+category+"\t"+change+"\t"+prev+"\t"+next);
	}
}
