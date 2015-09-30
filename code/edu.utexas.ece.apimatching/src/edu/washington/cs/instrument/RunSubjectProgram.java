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
package edu.washington.cs.instrument;


public class RunSubjectProgram {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		assert (args.length == 1);
//		reinstrumentJFreeChart();
//		runJFreeChartTests();
	}

	// run a single jfreechart program for a given root. 
//	public static void singleRunJFreeChart(File rootFile) {
//		File buildFile = new File("jfreechart.xml");
//		String root = rootFile.getAbsolutePath();
//		String version = root.substring(root.lastIndexOf("-") + 1);
//		
//		String testname = "org.jfree.chart.junit.JFreeChartTestSuite";
//		if (version.equals("0.9.4") || version.equals("0.9.5")
//				|| version.equals("0.9.6") || version.equals("0.9.7")) {
//			testname = "com.jrefinery.chart.junit.JFreeChartTestSuite";
//		}
//		Project p = new Project();
//		p.setUserProperty("ant.file", buildFile.getAbsolutePath());
//		p.setUserProperty("version", version);
//		p.setUserProperty("root", root);
//		p.setUserProperty("testname", testname);
//		p.init();
//		ProjectHelper helper = ProjectHelper.getProjectHelper();
//		p.addReference("ant.projectHelper", helper);
//		helper.parse(p, buildFile);
//		System.err.println("Executing " + root);
//		p.executeTarget("run-test");
//		if (InstrumentationConfiguration.METHOD_COUNT_ON) {
//			p.executeTarget("move-method-trace");
//		}
//		if (InstrumentationConfiguration.METHOD_STACKTRACE_ON){
//			p.executeTarget("move-method-stacktrace");
//		}
//	}

//	// compile (instrument) jfreechart program for a given root. 
//	public static void singleCompileJFreeChart(File rootFile) {
//		File buildFile = new File("jfreechart.xml");
//		String root = rootFile.getAbsolutePath();
//		String version = root.substring(root.lastIndexOf("-") + 1);
//		Project p = new Project();
//		p.setUserProperty("ant.file", buildFile.getAbsolutePath());
//		p.setUserProperty("version", version);
//		p.setUserProperty("root", root);
//		p.init();
//		ProjectHelper helper = ProjectHelper.getProjectHelper();
//		p.addReference("ant.projectHelper", helper);
//		helper.parse(p, buildFile);
//		System.err.println("Compiling " + root);
//		p.executeTarget("instrument");
//	
//	}
//
//	// compile all jfreechart programs 
//	
//	public static void reinstrumentJFreeChart() {
//		File dirs[] = ReadDirectories.getDirectories("jfreechart_list");
//
////		for (int i = 0; i < dirs.length; i++) {
//			for (int i = 0; i < 2; i++) {
//				
//			File rootFile = dirs[i];
//			singleCompileJFreeChart(rootFile);
//		}
//	}
//	
//	// print all commands for running tests. 
//	public static void printCommands(File dirs[]) {
//		for (int i = 0; i < dirs.length; i++) {
//			File rootFile = dirs[i];
//			String root = rootFile.getAbsolutePath();
//			String version = root.substring(root.lastIndexOf("-") + 1);
//			String cmd = "ant -f jfreechart.xml -Droot=" + root + " -Dversion"
//					+ version + " run-test";
//			System.out.println(cmd);
//		}
//	}
//
//	// run all jfreechart programs
//	public static void runJFreeChartTests() {
//		File dirs[] = ReadDirectories.getDirectories("jfreechart_list");
////		for (int i = 0; i < dirs.length; i++) {
//			for (int i = 0; i < 2; i++) {
//			File rootFile = dirs[i];
//			singleRunJFreeChart(rootFile);
//		}
//	}

}
