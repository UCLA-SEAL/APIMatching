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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaMethodRegex {

	/**
	 * @param args
	 * 
	 */

	private static final String classNameSeparator = ":";

	private static final String procedureNameSeparator = "-";

//	private static final String parameterSeparator = "%";

	private static final String parameterSeparator = "__"; 
	// MK 021611. I removed this thing again becaues I could not read my previous files. 

	
	private static final String returnSeparator = "->";

	private static final String name = "[a-zA-Z0-9\\_]+";

	public static final String packagePattern = "("+name+"){0,1}" + "(\\." + name + ")*";

	private static final String typePattern = packagePattern + "(\\[\\])*";

	public static final String classPattern = name + "(\\$" + name + ")*";

	public static final String procedurePattern = name + "(\\$" + name + ")*";

	public static final String parametersPattern = "\\[" + "(" + typePattern
			+ "){0,1}" + "(\\, " + typePattern + ")*\\]";

	public static final String returnTypePattern = typePattern;

	public static final ArrayList<String> list = new ArrayList<String>();
	
	private static final String javaMethod = packagePattern
			+ classNameSeparator + classPattern + procedureNameSeparator
			+ procedurePattern + parameterSeparator + parametersPattern
			+ returnSeparator + returnTypePattern;

	public static void main(String[] args) {
	
//		ArrayList test = list;
//		test.add("MIRYUNG");
//		System.out.println("test"+test);
//		System.out.println("org"+list);
		// TODO Auto-generated method stub
		String s[] = { "com.jrefinery.chart.junit:LocalListener-access$0__[com.jrefinery.chart.junit.PieChartTests.LocalListener]->boolean"
				,"bsh:BlockNameSpace-importClass__[String]->void",
				"bsh:BlockNameSpace-importClass__[String]->void",
				":LatestVersionPlugin-createMenuItems__[Vector]->void",
				"org.jfree.chart.axis:CategoryAxis-calculateCategoryGapSize__[int, Rectangle2D, AxisLocation]->double"
		};

//		String pattern ="com.jrefinery.*\\[*\\]:.*__.*-.*->.*";
//		String str ="com.jrefinery.job[mity]:a__a-a->a";
		
		String abc = "a[]$.$A$B$CM";
		System.out.println("KIMMY A");
		System.out.println(abc);
		System.out.println("KIMMY B");
		String def = abc.replace("$", "\\$");
		def = def.replace("[", "\\[");
		def = def.replace("]", "\\]");
		def = def.replace(".", "\\.");
		System.out.println(def);
		Pattern pdef = Pattern.compile(def);
		Matcher mdef = pdef.matcher(abc);
		System.out.println("def match with abc"+ mdef.matches() );
		
		String pattern = ".*-.*__\\[.*\\]";
//				"->*";
//		com.jrefinery.chart.axis
		String str = "TickUnit-compareTo__[Object]";
//		->int";
//		com.jrefinery.chart.axis
		String re[]=str.split(pattern);
//		System.out.print(re);
		Pattern pe = Pattern.compile(pattern);
		Matcher me = pe.matcher(str);
		System.out.println(pattern);
		System.out.println(str + "  is matched " + me.matches());
	
		for (int i = 0; i < s.length; i++) {
			Pattern p = Pattern.compile(javaMethod);
			Matcher m = p.matcher(s[i]);
			System.out.println(s[i] + "  is JavaMethod: " + m.matches());
		}

		System.out.println(name);
		Pattern p0 = Pattern.compile(name);
		Matcher m0 = p0.matcher("PSCMAblp");
		boolean b0 = m0.matches();
		System.out.println(b0);

		System.out.println(packagePattern);
		Pattern p = Pattern.compile(packagePattern);
		Matcher m = p.matcher("java.io.package");
		boolean b = m.matches();
		System.out.println(b);

		System.out.println(classPattern);
		Pattern p1 = Pattern.compile(classPattern);
		Matcher m1 = p1.matcher("A$a");
		boolean b1 = m1.matches();
		System.out.println(b1);

		System.out.println(parametersPattern);
		Pattern p2 = Pattern.compile(parametersPattern);
		Matcher m2 = p2.matcher("[s[][]]");
		boolean b2 = m2.matches();
		System.out.println(b2);

		System.out.println(returnTypePattern);
		Pattern p3 = Pattern.compile(returnTypePattern);
		Matcher m3 = p3.matcher("void[]");
		boolean b3 = m3.matches();
		System.out.println(b3);
	}

	public static boolean checkJavaMethodPattern(String s) {
		Pattern p = Pattern.compile(javaMethod);
		Matcher m = p.matcher(s);
		// System.out.println(s[i]+ " is JavaMethod: "+m.matches());
		return m.matches();
	}

	public static String generateString(String packageName, String className,
			String procedureName, List<String> parameters, String returntype) {

		String s = "";
		s = s + packageName;
		s = s + classNameSeparator + className;
		s = s + procedureNameSeparator + procedureName;
		s = s + parameterSeparator;
		ArrayList<String> simplifiedParameter = new ArrayList<String>();
		for (int i = 0; i < parameters.size(); i++) {
			String parameter = parameters.get(i);
			parameter = parameter.substring(parameter.lastIndexOf(".") + 1);
			simplifiedParameter.add(parameter);
		}
		s = s + simplifiedParameter.toString();
		s = s + returnSeparator;
		s = s + returntype.substring(returntype.lastIndexOf(".") + 1);
//		if (!checkJavaMethodPattern(s)) {
//			System.out.println("Illegal Method Sig " + s);
//		}
		return s;
	}

	public static String concatenate(String packageName, String className,
			String procedureName, String parametersName, String returntype) {

		String s = "";
		s = s + packageName;
		s = s + classNameSeparator + className;
		s = s + procedureNameSeparator + procedureName;
		s = s + parameterSeparator + parametersName;
		s = s + returnSeparator+returntype;
		
		return s;

	}

	static String getPackageName(String s) {

		if (!checkJavaMethodPattern(s)) {
			System.out.println("Illegal Method Sig in getPackageName" + s);
			System.exit(0);
		}
		return s.substring(0, s.indexOf(JavaMethodRegex.classNameSeparator));
	}

	static String getClassName(String s) {

		if (!checkJavaMethodPattern(s)) {
			System.out.println("Illegal Method Sig in getClassName " + s);
			System.exit(0);
		}
		return s.substring(s.indexOf(JavaMethodRegex.classNameSeparator)
				+ JavaMethodRegex.classNameSeparator.length(), s
				.indexOf(JavaMethodRegex.procedureNameSeparator));

	}

	static String getProcedureName(String s) {

		if (!checkJavaMethodPattern(s)) {
			System.out.println("Illegal Method Sig in getProcedureName " + s);
			System.exit(0);
		}
		return s.substring(s.indexOf(JavaMethodRegex.procedureNameSeparator)
				+ JavaMethodRegex.procedureNameSeparator.length(), s
				.indexOf(JavaMethodRegex.parameterSeparator));

	}

	static String getReturnTypeName(String s) {
		if (!checkJavaMethodPattern(s)) {
			System.out.println("Illegal Method Sig in getReturnTypeName " + s);
			System.exit(0);
		}
		return s.substring(s.indexOf(JavaMethodRegex.returnSeparator)
				+ JavaMethodRegex.returnSeparator.length());

	}

	static String getParameterString(String s) {
		if (!checkJavaMethodPattern(s)) {
			System.out.println("Illegal Method Sig in getParameterString " + s);
			System.exit(0);
		}
		String argstring = s.substring(s
				.indexOf(JavaMethodRegex.parameterSeparator)
				+ JavaMethodRegex.parameterSeparator.length() + 1, s
				.indexOf(JavaMethodRegex.returnSeparator) - 1);

		return argstring;
	}

	static ArrayList<String> getParameters(String s) {
		ArrayList<String> parameters = new ArrayList<String>();
		String argstring = s.substring(s
				.indexOf(JavaMethodRegex.parameterSeparator)
				+ JavaMethodRegex.parameterSeparator.length() + 1, s
				.indexOf(JavaMethodRegex.returnSeparator) - 1);

		if (argstring.length() > 0) {
			int itemcount = 1;
			for (int i = 0; i < argstring.length(); i++) {
				if (argstring.charAt(i) == ',') {
					itemcount++;
				}
			}
			for (int i = 0; i < itemcount; i++) {
				String arg = null;
				if (argstring.indexOf(',') > 0) {
					arg = argstring.substring(0, argstring.indexOf(", "));
					argstring = argstring
							.substring(argstring.indexOf(", ") + 2);
				} else {
					arg = argstring;
				}
				parameters.add(arg);
			}
		}
		return parameters;
	}

	static String getArgument(String s, int index) {

		if (!checkJavaMethodPattern(s)) {
			System.out.println("Illegal Method Sig in getArgument" + s);
			System.exit(0);
		}
		ArrayList<String> parameters = getParameters(s);
		if (index >= parameters.size()) {
			System.err.println("Index array bound exception");
		}
		return parameters.get(index);
	}

}