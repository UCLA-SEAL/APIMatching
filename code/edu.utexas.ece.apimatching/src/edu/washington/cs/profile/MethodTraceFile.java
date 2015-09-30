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

public class MethodTraceFile {

	private static PrintStream print_stream = null;
	public static String TRACE_FILE ="methods.trace"; 

	public static String TRACE_SUFFIX =".trace";
	static {
		if (print_stream == null) {
			File f = new File(TRACE_FILE);
			try {
				if (!f.exists()) {
					f.createNewFile();
				}			
				FileOutputStream outstream = new FileOutputStream(f);
				print_stream = new PrintStream(outstream);
			} catch (java.io.FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static void println(String s) {
		print_stream.println(s);
	}
//	private static void createStream () {
//		if (print_stream == null) {
//			File f = new File(TRACE_FILE);
//			try {
//				if (!f.exists()) {
//					f.createNewFile();
//				}
//				FileOutputStream outstream = new FileOutputStream(f);
//				print_stream = new PrintStream(outstream);
//			} catch (java.io.FileNotFoundException e1) {
//				e1.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}

}
