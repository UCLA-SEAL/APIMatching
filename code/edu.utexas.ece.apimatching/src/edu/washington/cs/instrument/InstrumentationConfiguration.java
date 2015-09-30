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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class InstrumentationConfiguration {

	private static String CONFIG_FILE = "instrument.config";
	
	public static boolean METHOD_COUNT_ON = false;
	public static boolean METHOD_STACKTRACE_ON = false;
	public static int METHOD_STACKTRACE_DEPTH = 0;
	
	private final static String METHOD_COUNT = "METHOD_COUNT";
	private final static String METHOD_STACKTRACE ="METHOD_STACKSTRACE";
	private final static String STACKTRACE_DEPTH = "STACKTRACE_DEPTH"; 
	
	static { 
		readConfigFile();
		System.err.println("INSTR_CONFIG_READ: "+METHOD_COUNT_ON);	
	}
	public static void setMethodCountOn (boolean b){  
		METHOD_COUNT_ON=b;
		writeConfigFile();
	}
	public static void setMethodStackTraceOn (boolean b){  
		METHOD_STACKTRACE_ON=b;
		writeConfigFile();
	}
	public static void setMethodStackTraceDepth (int i){  
		METHOD_STACKTRACE_DEPTH= i;
		writeConfigFile();
	}
	public static void readConfigFile(){
		//read from config file 
		File file = new File(CONFIG_FILE);
		try {
			if (file.exists()){
				FileReader fread = new FileReader(file);
				BufferedReader reader = new BufferedReader(fread);
				for (String line = reader.readLine(); line != null; line = reader.readLine()){
					if (line.indexOf(METHOD_COUNT)>=0) { 
						String option = line.substring(METHOD_COUNT.length()+1);
						METHOD_COUNT_ON = new Boolean (option).booleanValue();
					}else if (line.indexOf(METHOD_STACKTRACE)>=0){
						String option = line.substring(METHOD_STACKTRACE.length()+1);
						METHOD_STACKTRACE_ON = new Boolean (option).booleanValue();
					}else if (line.indexOf(STACKTRACE_DEPTH)>=0) { 
						String option = line.substring(STACKTRACE_DEPTH.length()+1);
						METHOD_STACKTRACE_DEPTH = new Integer(option).intValue();
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static void writeConfigFile () {
		File file = new File (CONFIG_FILE);
		try {
			if (file.exists()) {
				file.delete();
				file.createNewFile();
			}
			FileOutputStream fOut = new FileOutputStream(file);
			PrintStream p = new PrintStream(fOut);
			p.println(METHOD_COUNT+":"+METHOD_COUNT_ON);
			p.println(METHOD_STACKTRACE+":"+METHOD_STACKTRACE_ON);
			p.println(STACKTRACE_DEPTH+":"+METHOD_STACKTRACE_DEPTH);
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}
	}
	public static void main(String[] args) {
		PrintStream p = new PrintStream(System.err);
		p.println(METHOD_COUNT+":"+METHOD_COUNT_ON);
		p.println(METHOD_STACKTRACE+":"+METHOD_STACKTRACE_ON);
		p.println(STACKTRACE_DEPTH+":"+METHOD_STACKTRACE_DEPTH);
		METHOD_COUNT_ON=true;
		setMethodStackTraceDepth(3);
		writeConfigFile();
		
	}
}
