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
import java.util.List;

import org.w3c.dom.Element;

import edu.washington.cs.induction.Refactoring;

public class Transformation implements Comparable {
	
	private static final String xmlTag= "transformation"; 
	static final int PACKAGE_REPLACE = 1;

	static final int CLASS_REPLACE = 2;

	static final int TYPE_REPLACE =9; 
		
	static final int PROCEDURE_REPLACE = 3;

	static final int RETURN_REPLACE = 4;
	
	static final int PARAMETERS_REPLACE = 8;

	static final int ARG_REPLACE = 5;

	static final int PARAM_SET_DELETE = 7;

	static final int PARAM_APPEND = 6;
	
	private final Object from;

	private final Object to;

	private final Object operand;

	private final int type;
	
	public int getLevel() {
		switch (this.type) {
		case PACKAGE_REPLACE:
			return Refactoring.PACKAGE_LEVEL;
		case CLASS_REPLACE:
			return Refactoring.CLASS_LEVEL;
		case TYPE_REPLACE:
			return Refactoring.CLASS_LEVEL;
		case PROCEDURE_REPLACE:
			return Refactoring.METHOD_LEVEL;
		case RETURN_REPLACE:
			return Refactoring.SIGNATURE_LEVEL;
		case PARAMETERS_REPLACE:
			return Refactoring.SIGNATURE_LEVEL;
		case ARG_REPLACE:
			return Refactoring.SIGNATURE_LEVEL;
		case PARAM_SET_DELETE:
			return Refactoring.SIGNATURE_LEVEL;
		case PARAM_APPEND:
			return Refactoring.SIGNATURE_LEVEL;
		}
		return -1;
	}
	public Transformation(int kind, String from, String to) {
		assert (kind != PARAM_SET_DELETE && kind != PARAM_APPEND);
		this.type = kind;
		this.from = from;
		this.to = to;
		this.operand = null;
	}

	public Transformation(int kind, List<String> from, List<String> to) {
		assert (kind != PARAM_SET_DELETE && kind != PARAM_APPEND);
		this.type = kind;
		this.from = from;
		this.to = to;
		this.operand = null;
	}

	public Transformation(int kind, Object operand) {
		assert (kind == PARAM_SET_DELETE || kind == PARAM_APPEND);
		this.type = kind;
		this.operand = operand;
		this.from = null;
		this.to = null;
	}

	public String toString() {
		String s = "";
		switch (this.type) {
		case PACKAGE_REPLACE: {
			s = s + "packageReplace( x,  " + from + " , " + to + " )";
			break;
		}
		case CLASS_REPLACE: {
			s = s + "classReplace( x, " + from + " , " + to + " ) ";
			break;
		}
		case PROCEDURE_REPLACE: {
			s = s + "procedureReplace( x, " + from + " , " + to + " ) ";
			break;
		}
		case RETURN_REPLACE: {
			s = s + "returnReplace( x, " + from + " , " + to + " ) ";
			break;
		}
		case PARAMETERS_REPLACE: {
			s = s + "parameterReplace( x, " + from + " , " + to + " ) ";
			break;
		}
		case ARG_REPLACE: {
			s = s + "argReplace( x, " + from + " , " + to + " ) ";
			break;
		}
		case PARAM_SET_DELETE: {
			s = s + "argDelete( x, " + operand + " ) ";
			break;
		}
		case PARAM_APPEND: {
			s = s + "argAppend( x, " + operand + " ) ";
			break;
		}
		case TYPE_REPLACE: {
			s = s + "typeReplace ( x, " + from + ", " + to + " ) ";
			break;
		}
		}
		return s;

	}
//
//	public String forwardToString() {
//		String s = "";
//		switch (this.type) {
//		case PACKAGE_REPLACE: {
//			s = s + "packageReplace(" + from + "," + to+")";
//			break;
//		}
//		case CLASS_REPLACE: {
//			s = s + "classReplace(" + from + "," + to+")";
//			break;
//		}
//		case PROCEDURE_REPLACE: {
//			s = s + "procedureReplace(" + from + "," + to+")";
//			break;
//		}
//		case RETURN_REPLACE: {
//			s = s + "returnReplace" + from + " -> new(x).return=" + to;
//			break;
//		}
//		case PARAMETERS_REPLACE: {
//			s = s + "x.parameters=" + from + "-> new(x).parameters=" + to;
//			break;
//		}
//		case ARG_REPLACE: {
//			s = s + "x.arg=" + from + "-> new(x).arg=" + to;
//			break;
//		}
//		case PARAM_SET_DELETE: {
//			s = s + "x.parameters= x.parameters-{" + operand+"}";
//			break;
//		}
//		case PARAM_APPEND: {
//			s = s + "new(x).parameters = x.parameters+" + operand;
//			break;
//		}
//		case TYPE_REPLACE: { 
//			s = s + "x.type="+ from + ", new(x).type="+to;
//			break;
//		}
//		}
//		return s;
//	}
//	public String backwardToString() {
//		String s = "";
//		switch (this.type) {
//		case PACKAGE_REPLACE: {
//			s = s + "x.package=" + from + " -> old(x).package=" + to;
//			break;
//		}
//		case CLASS_REPLACE: {
//			s = s + "x.class=" + from + " -> old(x).class=" + to;
//			break;
//		}
//		case PROCEDURE_REPLACE: {
//			s = s + "x.procedure=" + from + " -> old(x).procedure=" + to;
//			break;
//		}
//		case RETURN_REPLACE: {
//			s = s + "x.return=" + from + " -> old(x).return=" + to;
//			break;
//		}
//		case PARAMETERS_REPLACE: {
//			s = s + "x.parameters=" + from + "-> old(x).parameters=" + to;
//			break;
//		}
//		case ARG_REPLACE: {
//			s = s + "x.arg=" + from + "-> old(x).arg=" + to;
//			break;
//		}
//		case PARAM_SET_DELETE: {
//			s = s + "x.parameters= x.parameters-{" + operand + "}";
//			break;
//		}
//		case PARAM_APPEND: {
//			s = s + "old(x).parameters = x.parameters+" + operand;
//			break;
//		}
//		case TYPE_REPLACE: {
//			s = s + "x.type=" + from + ", old(x).type=" + to;
//			break;
//		}
//		}
//		return s;
//	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(T)
	 */
	public int compareTo(Object o) {
		if (o instanceof Transformation) {
			Transformation t = (Transformation) o;
			if (this.type!=t.type) {
				return (this.type-t.type);
			}else {
				return (this.toString().compareTo(t.toString()));
			}
		}
		return -1;		
	}
	public String getPattern () {
		switch (this.getType()) {
		case Transformation.PACKAGE_REPLACE:
		case Transformation.CLASS_REPLACE:
		case Transformation.PROCEDURE_REPLACE:
		case Transformation.RETURN_REPLACE:
			return (String) this.from;
		case Transformation.PARAMETERS_REPLACE:
			String param = ((ArrayList<String>) this.from).toString();
			return param;
//		case Transformation.TYPE_REPLACE:
		case Transformation.ARG_REPLACE:
		case Transformation.PARAM_SET_DELETE:
		case Transformation.PARAM_APPEND:
			return null;
		}
		return null;
	}
	public boolean equals(Object o){ 
		if (o instanceof Transformation) {
			Transformation t = (Transformation) o;
			boolean b1 = (this.type ==t.type);
			boolean b2;
			if (this.from!=null && t.from!=null) {
				b2 = (this.from.equals(t.from));
			}else {
				b2 = (this.from ==null && t.from==null);
			}
			boolean b3;
			if (this.to!=null && t.to !=null) { 
				b3 = (this.to.equals(t.to));
			}else {
				b3 = (this.to==null && t.to==null);
			}
			boolean b4;
			if (this.operand!=null && t.operand!=null) { 
				b4= (this.operand.equals(t.operand));
			}else {
				b4= (this.operand==null && t.operand==null);
			}
			return (b1&& b2 && b3 && b4);
		}
		return false;
	}
	
	public int hashCode(){
		return toString().hashCode();
	}
	
	
	public void writeElement (Element parent) { 
		Element trans = parent.getOwnerDocument().createElement(xmlTag);
		trans.setAttribute("type",new Integer(type).toString());
		switch (this.type) {
		// from and to
		case PACKAGE_REPLACE:
		case CLASS_REPLACE:
		case PROCEDURE_REPLACE:
		case RETURN_REPLACE:
		case ARG_REPLACE:
		case TYPE_REPLACE:
			String from = (String) this.from;
			String to = (String) this.to;
			trans.setAttribute("from",from);
			trans.setAttribute("to",to);
		break;
		case PARAMETERS_REPLACE:
			// List<String> from, to
			List<String> fromList = (List<String>) this.from;
			List<String> toList = (List<String>) this.to;
			trans.setAttribute("fsize",new Integer(fromList.size()).toString());
			for (int i= 0; i<fromList.size();i++) { 
				trans.setAttribute("f"+i,fromList.get(i));
			}
			trans.setAttribute("tsize",new Integer(toList.size()).toString());
			for (int i =0; i<toList.size(); i++) { 
				trans.setAttribute("t"+i,toList.get(i));
			}
		break;
		case PARAM_SET_DELETE:
		case PARAM_APPEND:
			// List<String> operand
			List<String> op = (List<String>) this.operand;
			trans.setAttribute("opsize",new Integer(op.size()).toString());
			for (int i =0; i<op.size(); i++) { 
				trans.setAttribute("o"+i,op.get(i));
			}
		break;
		}
		parent.appendChild(trans);
	}
	// does it change. or read it and create a new object. 
	public JavaMethod applyTransformation(JavaMethod toBeChanged){
		// deep copy all names. 
		String packageName = new String(toBeChanged.getPackageName());
		String className = new String(toBeChanged.getClassName());
		String procedureName = new String(toBeChanged.getProcedureName());
		String returnName = new String(toBeChanged.getReturntype());
		ArrayList<String> parameters = new ArrayList<String>();
		for (int i=0; i<toBeChanged.getParameters().size(); i++) {
			String arg = new String(toBeChanged.getParameters().get(i));
			parameters.add(i,arg);
		}
		
		switch (this.type) {
		case PACKAGE_REPLACE: {
			assert(this.from instanceof String);
			if (packageName.equals(this.from)) { 
				packageName = this.to.toString();
			}
			break;
		}
		case CLASS_REPLACE: {
			// changing class name only
			assert(this.from instanceof String);
			if (className.equals(this.from)) {
				className = this.to.toString();
			}
			break;
		}
		case PROCEDURE_REPLACE: {
			assert(this.from instanceof String);
			if (procedureName.equals(this.from)) { 
				procedureName = this.to.toString();
			}
			break;
		}
		case RETURN_REPLACE: {
			assert(this.from instanceof String);
			if (returnName.equals(this.from)) { 
				returnName= this.to.toString();
			}
			break;
		}
		case PARAMETERS_REPLACE: {
			assert(this.from instanceof ArrayList);
			ArrayList fromList = (ArrayList) this.from;
			if (parameters.size() == fromList.size()){
				boolean allSame = true;
				for (int i=0; i<parameters.size(); i++) {
					if (!parameters.get(i).equals(fromList.get(i))) { 
						allSame = false;
					}
				}
				
				if (allSame ==true) {
					parameters=(ArrayList)this.to;
				}
			}
			break;
		}
		case ARG_REPLACE: {
			for (int i=0; i<parameters.size(); i++) {
				if (parameters.get(i).equals(this.from)) { 
					String s = (String) this.to;
					parameters.set(i,s);
				}
			}
			break;
		}
		case PARAM_SET_DELETE: {
			// delete types in the operand from parameters
			List<String> toBeDeleted = (List<String>) operand;
			parameters = new ArrayList<String>();
			for (int i=0; i<toBeChanged.getParameters().size(); i++) {
				String arg = new String(toBeChanged.getParameters().get(i));
				if (!toBeDeleted.contains(arg)) {
					parameters.add(arg);
				}
				
			}
			break;
		}
		case PARAM_APPEND: {
			List<String> toBeAppended = (List<String>) operand;
			parameters.addAll(toBeAppended);
			break;
		}
		case TYPE_REPLACE: { 
			// replace class type, return type, arg replace
			// class 
			if (className.equals(this.from)) {
				className = this.to.toString();
				// if procedure name is equal to class name (meaning 
				// it is a constructor, then change procedure name as well. 
				if (procedureName.equals(this.from)){ 
					procedureName = this.to.toString();
				}
			}
			// return
			String typeName = returnName; String suffix ="";
			if (returnName.indexOf('[')>0) {
				typeName = returnName.substring(0, returnName.indexOf('['));
				suffix = returnName.substring(returnName.indexOf('['));
			}
			if (typeName.equals(this.from)) { 
				returnName= this.to.toString()+suffix;
			}
			// arg replace
			for (int i=0; i<parameters.size(); i++){
				String param =parameters.get(i); 
				typeName = param;
				suffix ="";
				if (param.indexOf('[')>0) {
					typeName = param.substring(0, param.indexOf('['));
					suffix = param.substring(param.indexOf('['));
				}	
				if (typeName.equals(this.from)) { 
					String s = (String) this.to;
					parameters.set(i,s+suffix);
				}
			}
			break;
		}
		}
		String s = JavaMethodRegex.generateString(packageName,className,procedureName,parameters,returnName);
		return new JavaMethod(s);
	}
	
	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}
	
	private Transformation unify(Transformation other) {
		
		if (this.equals(other)) {
			return this;
		}
		else return null;
	}
	public static final String getXMLTag() { 
		return xmlTag;
	}
	public static Transformation readElement(Element trans) { 
		if (!trans.getTagName().equals(xmlTag)) return null;
		int type = new Integer(trans.getAttribute("type")).intValue();
		switch (type) { 
		case Transformation.PACKAGE_REPLACE:
		case Transformation.CLASS_REPLACE:
		case Transformation.PROCEDURE_REPLACE:
		case Transformation.RETURN_REPLACE:
		case Transformation.ARG_REPLACE:
		case Transformation.TYPE_REPLACE:
			String from = trans.getAttribute("from");
			String to = trans.getAttribute("to");
			return new Transformation(type,from,to);
		case Transformation.PARAMETERS_REPLACE:
			// List<String> from, to
			int fromSize = new Integer(trans.getAttribute("fsize")).intValue();
			int toSize = new Integer(trans.getAttribute("tsize")).intValue();
			ArrayList<String> fromList = new ArrayList<String>(fromSize);
			ArrayList<String> toList = new ArrayList<String>(toSize);
			for (int i=0; i<fromSize; i++) {
				String fe = trans.getAttribute("f"+i);
				fromList.add(i,fe);
			}
			for (int i=0; i<toSize;i++) { 
				String te = trans.getAttribute("t"+i);
				toList.add(i,te);
			}
			return new Transformation(type,fromList,toList);
		case Transformation.PARAM_SET_DELETE:
		case Transformation.PARAM_APPEND:
			// List<String> operand
			int opSize = new Integer(trans.getAttribute("opsize")).intValue();
			ArrayList<String> opList = new ArrayList<String>(opSize);
			for (int i=0; i<opSize; i++) {
				String oe = trans.getAttribute("o"+i);
				opList.add(oe);
			}
			return new Transformation(type,opList);
		}
		return null;
	}
	
	public boolean isApplicable (JavaMethod jm){ 
		boolean applicable = false;
		switch (this.type) {
		case PACKAGE_REPLACE:
			applicable = jm.getPackageName().equals(this.from);
			break;
		case CLASS_REPLACE:
			applicable = jm.getClassName().equals(this.from);
			break;
		case PROCEDURE_REPLACE:
			applicable = jm.getProcedureName().equals(this.from);
			break;
		case RETURN_REPLACE:
			applicable = jm.getReturntype().equals(this.from);
			break;
		case PARAMETERS_REPLACE:
			ArrayList fromList = (ArrayList) this.from;
			applicable = jm.getParameters().equals(fromList);
			break;
		case ARG_REPLACE:
			applicable = jm.getParameters().contains(this.from);
			break;
		case PARAM_SET_DELETE:
			ArrayList op = (ArrayList)this.operand;
			applicable= (jm.getParameters().containsAll(op));
			break;
		case PARAM_APPEND:
			applicable = true;
			break;
		case TYPE_REPLACE:
			String fr= (String)this.from;
			applicable = jm.getClassName().equals(fr)
					|| jm.getReturntype().equals(fr) 
					|| jm.getReturntype().equals(fr+'[')
					|| jm.getParameters().contains(fr)
					|| jm.getParameters().contains(fr+'[');
			
			break;
		}
		return applicable;
	}
	private boolean equivalentWRTthis (JavaMethod jmLeft, JavaMethod jmRight) { 
		JavaMethod converted = this.applyTransformation(jmLeft);
		return this.equivalent(converted,jmRight);
	}
	
	public boolean equivalent(JavaMethod converted, JavaMethod jmRight) {
		boolean comparisonWRT = false;
		switch (this.type) {
		case PACKAGE_REPLACE: 
			comparisonWRT= jmRight.getPackageName().equals(converted.getPackageName());
			break;
		
		case CLASS_REPLACE: 
			comparisonWRT= jmRight.getClassName().equals(converted.getClassName());
			break;
		
		case PROCEDURE_REPLACE: 
			comparisonWRT= jmRight.getProcedureName().equals(converted.getProcedureName());
			break;
		
		case RETURN_REPLACE: 
			comparisonWRT = jmRight.getReturntype().equals(converted.getReturntype());
			break;
		
		case PARAMETERS_REPLACE: 
		case ARG_REPLACE:
		case PARAM_SET_DELETE:
		case PARAM_APPEND:
			comparisonWRT =true;
			comparisonWRT = (jmRight.getParameters().size() == converted
					.getParameters().size());
			if (comparisonWRT) {
				for (int i = 0; i < jmRight.getParameters().size(); i++) {
					if (!jmRight.getParameters().get(i).equals(
							converted.getParameters().get(i))) {
						comparisonWRT = false;
					}
				}
			}
			break;

		case TYPE_REPLACE: 
			comparisonWRT= jmRight.getClassName().equals(converted.getClassName());
			if (comparisonWRT==true) return true;
			break;
		}
		return comparisonWRT;
	}
	
	// added for APIRuleExaminer 
	public static String getTypeString (int kind){ 
		switch (kind) { 
		case PACKAGE_REPLACE: 
			return "packageReplace"; 
		case CLASS_REPLACE: 
			return "classReplace"; 
		case TYPE_REPLACE: 
			return "typeReplace"; 
		case PROCEDURE_REPLACE: 
			return "procedureReplace"; 
		case RETURN_REPLACE:
			return "returnReplace"; 
		case PARAMETERS_REPLACE: 
			return "inputSignatureReplace";
		case ARG_REPLACE: 
			return "argReplace"; 
		case PARAM_SET_DELETE: 
			return "argDelete"; 
		case PARAM_APPEND: 
			return "argAppend"; 
		}
		return "NULL";
	}
	
	
}
