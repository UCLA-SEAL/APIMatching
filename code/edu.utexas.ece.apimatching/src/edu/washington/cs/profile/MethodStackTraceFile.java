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
package edu.washington.cs.profile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import edu.washington.cs.instrument.InstrumentationConfiguration;
import edu.washington.cs.rules.JavaMethod;

public class MethodStackTraceFile {

	private static PrintStream stacktrace_stream = null;
	private static PrintStream depth_stream = null;
	public static String STACKTRACE_FILE ="methods.stacktrace";
	public static String STACKTRACE_SUFFIX =".stacktrace";

	public static String STACKDEPTH_FILE = "methods.depth";
	public static String STACKDEPTH_SUFFIX = ".depth";
	public static String DEPTH_TAG ="DEPTH";
	public static int DEPTH = InstrumentationConfiguration.METHOD_STACKTRACE_DEPTH;
	
	// this can be used for call stack, stack depth, immediate callers 
	
	static {
		
		if (stacktrace_stream == null) {
			File f = new File(STACKTRACE_FILE);
			try {
				if (!f.exists()) {
					f.createNewFile();
				}			
				FileOutputStream outstream = new FileOutputStream(f);
				stacktrace_stream = new PrintStream(outstream);
			} catch (java.io.FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
//		if (depth_stream == null) {
//			File f = new File(STACKDEPTH_FILE);
//			try {
//				if (!f.exists()) {
//					f.createNewFile();
//				}			
//				FileOutputStream outstream = new FileOutputStream(f);
//				depth_stream = new PrintStream(outstream);
//			} catch (java.io.FileNotFoundException e1) {
//				e1.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
	}
	public static void println(JavaMethod jm, Throwable t) {
		if (stacktrace_stream!=null){
			stacktrace_stream.println(jm);
			
			StackTraceElement[] s = t.getStackTrace();
			stacktrace_stream.println(DEPTH_TAG+s.length);
			for (int i=2; i<2+DEPTH;i++) {
				stacktrace_stream.println("\t"+s[i]);
			}
		}
	}
}
