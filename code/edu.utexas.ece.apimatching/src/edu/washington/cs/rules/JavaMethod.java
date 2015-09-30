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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.ucsc.cse.grase.origin.entity.Method;
import edu.ucsc.cse.grase.origin.entity.ModifierBit;

public class JavaMethod implements Comparable<JavaMethod> {
	
	private final String packageName;

	private final String className;

	private final String procedureName;

	private final String returntype;

	private final ArrayList<String> parameters;

	private ModifierBit modifers = null;
	
	public  boolean conflict = false; 
	public JavaMethod(boolean conflict) { 
		this.conflict = conflict;
		this.packageName = "";
		this.className = "";
		this.procedureName = "";
		this.parameters = new ArrayList<String>();
		this.returntype = "";
	}
	public JavaMethod(String packageName, String className,
			String procedureName, ArrayList<String> parameters,
			String returntype) {
		this.packageName = packageName;
		this.className = className;
		this.procedureName = procedureName;
		this.parameters = parameters;
		this.returntype = returntype;
	}
	public static void main(String[] args) {
		String s[] = new String[0];
		JavaMethod jms[] = new JavaMethod[s.length];
		for (int i = 0; i < s.length; i++) {
			jms[i] = new JavaMethod(s[i]);
			System.out.println(jms[i].toString());
		}
		for (int i = 0; i < s.length; i++) {
			JavaMethod ja = new JavaMethod(s[i]);
			for (int j = i + 1; j < s.length; j++) {
				JavaMethod jb = new JavaMethod(s[j]);
				if (ja.equals(jb)) {
					System.out.println("match");
					System.out.println(i + ":" + ja + ja.hashCode());
					System.out.println(j + ":" + jb + jb.hashCode());
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return id().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(T)
	 */
	public int compareTo(JavaMethod other) {
		if (this.equals(other))
			return 0;
		else {
			String thisid = this.id();
			String thatid = other.id();
			return thisid.compareTo(thatid);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		// TODO Auto-generated method stub
		if (other instanceof JavaMethod) {
			JavaMethod jm = (JavaMethod) (other);
			boolean bClass = this.className.equals(jm.className);
			boolean bPackage = this.packageName.equals(jm.packageName);
			boolean bProcedure = this.procedureName.equals(jm.procedureName);
			boolean bParamSize = (this.parameters.size() == jm.parameters
					.size());
			// take care of the case where returntype is long type.
			
//			boolean bReturn = this.returntype.equals(jm.returntype);
			boolean b4 = bClass && bPackage && bProcedure && bParamSize;
			if (!b4)
				return false;
			for (int i = 0; i < this.parameters.size(); i++) {
				String thisPar = this.parameters.get(i);
				String otherPar = jm.parameters.get(i);
				if (!thisPar.equals(otherPar))
					return false;
			}
			return true;
		}
		if (other instanceof String) {
			String thisString = this.toString();
			if (thisString.equals(other))
				return true;
		}
		return false;
	}
	public boolean onlyDifferByReturn(JavaMethod jm) { 

		boolean bClass = this.className.equals(jm.className);
		boolean bPackage = this.packageName.equals(jm.packageName);
		boolean bProcedure = this.procedureName.equals(jm.procedureName);
		boolean bParamSize = (this.parameters.size() == jm.parameters
				.size());
		
		boolean b5 = bClass && bPackage && bProcedure && bParamSize;
		
		if (!b5)
			return false;
		for (int i = 0; i < this.parameters.size(); i++) {
			String thisPar = this.parameters.get(i);
			String otherPar = jm.parameters.get(i);
			if (!thisPar.equals(otherPar))
				return false;
		}
		return true;
	}

//	public JavaMethod(ConstructorSignature cs) {
//		java.lang.Class classObj = cs.getDeclaringType();
//		this.parameters = new ArrayList<String>();
//		this.packageName = classObj.getPackage().getName();
//		this.className = classObj.getSimpleName();
//		this.procedureName = classObj.getSimpleName();
//		this.returntype = "void";
//		for (int i = 0; i < cs.getParameterTypes().length; i++) {
//			Class c = cs.getParameterTypes()[i];
//			parameters.add(c.getCanonicalName());
//		}
//	}
//
//	public JavaMethod(MethodSignature ms) {
//		Class classObj = ms.getDeclaringType();
//		this.packageName = classObj.getPackage().getName();
//		this.className = classObj.getSimpleName();
//		this.procedureName = ms.getName();
//		this.returntype = ms.getReturnType().getCanonicalName();
//		this.parameters = new ArrayList<String>();
//		for (int i = 0; i < ms.getParameterTypes().length; i++) {
//			Class c = ms.getParameterTypes()[i];
//			parameters.add(c.getCanonicalName());
//		}
//	}

	public JavaMethod(Method ms) {
		if (ms.getModifier()!=null) { 
			this.modifers = ms.getModifier();
		}
		String returnType = ms.getSignature().getReturnType();
		if (returnType.equals("")) {
			this.returntype = "void";
		} else {
			this.returntype = returnType;
		}
		String name = ms.getName();
		this.procedureName = name.substring(name.indexOf(":") + 1);
		String packageClass = name.substring(0, name.indexOf(":"));
		this.packageName = packageClass.substring(0, name.lastIndexOf("."));
		this.className = packageClass.substring(name.lastIndexOf(".") + 1);
		
		String[] args = ms.getSignature().getParameterTypes();
		this.parameters = new ArrayList<String>();
		for (int i = 0; i < args.length; i++) {
			parameters.add(args[i]);
		}
	}

	@Override
	public String toString() {
		return JavaMethodRegex.generateString (packageName,className,procedureName,
				parameters, returntype); 
	}

	public String id(){
		return JavaMethodRegex.generateString (packageName,className,procedureName,
				parameters, "null");
	}
	public JavaMethod(String s) {	
		this.packageName = JavaMethodRegex.getPackageName(s);
		this.className = JavaMethodRegex.getClassName(s);
		this.procedureName= JavaMethodRegex.getProcedureName(s);
		this.returntype = JavaMethodRegex.getReturnTypeName(s);
		this.parameters= JavaMethodRegex.getParameters(s);
	}

	/**
	 * @return Returns the className.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return Returns the packageName.
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @return Returns the parameters.
	 */
	public ArrayList<String> getParameters() {
		return parameters;
	}

	/**
	 * @return Returns the procedureName.
	 */
	public String getProcedureName() {
		return procedureName;
	}

	/**
	 * @return Returns the returntype.
	 */
	public String getReturntype() {
		return returntype;
	}
	public static Set<String> filterClass(Collection<JavaMethod> jmSet) {
		HashSet<String> sClass = new HashSet<String>();
		for (Iterator<JavaMethod> it = jmSet.iterator(); it.hasNext();) {
			JavaMethod jm = it.next();
			sClass.add(jm.getClassName());
		}
		return sClass;
	}
}

// public JavaMethod(String s){
//
// String name = s.substring(0,s.indexOf("__"));
// this.procedureName = name.substring(s.lastIndexOf(".")+1);
// name = name.substring(0,s.lastIndexOf("."));
// this.clasName =
// this.returntype = s.substring(s.lastIndexOf("->")+"->".length(),s.length());
// int arg_start = 0;
// int arg_end = 0;
// if (s.indexOf("__[")>0){
// arg_start =s.indexOf("__[")+"__[".length();
// }else {
// arg_start = s.indexOf("__")+1;
// }
// if (s.indexOf("]->")>0){
// arg_end = s.indexOf("]->");
// }else {
// arg_end = s.lastIndexOf("->");
// }
// String argstring = s.substring(arg_start, arg_end);
// this.parameters= new ArrayList<String> ();
// int itemcount = 1;
// for (int i= 0; i< argstring.length(); i++){
// if (argstring.charAt(i)==',') {
// itemcount++;
// }
// }
// for (int i= 0; i< itemcount ; i++){
// String arg =null;
// if (argstring.indexOf(',')>0){
// arg = argstring.substring(0,argstring.indexOf(", "));
// argstring= argstring.substring(argstring.indexOf(", ")+2);
// }else {
// arg = argstring;
// }
// this.parameters.add(arg);
// }
// }
