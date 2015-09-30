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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.washington.cs.rules.JavaMethod;

public class UnmatchedMethod {

	private static HashSet<JavaMethod> unmatchedYet = new HashSet<JavaMethod>();
	
	private static String UNMATCHED = "method.unmatched";
	
	static {
		File file = new File(UNMATCHED);
		try {
			if (file.exists()){
				FileReader fread = new FileReader(file);
				BufferedReader reader = new BufferedReader(fread);
				for (String line = reader.readLine(); line != null; line = reader.readLine()){
					JavaMethod jm = new JavaMethod(line);
					unmatchedYet.add(jm);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace(System.err);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}
	public static void main(String[] args) {
		
	}
	public static boolean contains(JavaMethod jm) {
		return (unmatchedYet.contains(jm));
	}
	private static void print() {
		for (Iterator<JavaMethod> it = unmatchedYet.iterator(); it.hasNext(); ) { 
			JavaMethod jm = it.next();
			System.out.println(jm);
		}
	}
	private static void updateUnmatched(Set<JavaMethod> set){
		File file = new File(UNMATCHED);
		PrintStream printStream = null;
		// create an output file debugOriginAnalysisStream 
		try {
			file.createNewFile();
			FileOutputStream fOut = new FileOutputStream(file);
			printStream = new PrintStream(fOut);
		} catch (FileNotFoundException e) {
			e.printStackTrace(System.err);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}

		for (Iterator<JavaMethod> it = set.iterator(); it.hasNext(); ){ 
			JavaMethod jm = it.next();
			printStream.println(jm);
		}
	}
	
	public static void serializeUnmatched (Set<JavaMethod> set, 
			String project, String version, String tag){
		File fProject = new File (project);
		assert (fProject.exists());
		File outputFile = new File (fProject, version+"_"+tag);
		PrintStream printStream = null;
		// create an output file debugOriginAnalysisStream 
		try {
			outputFile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(outputFile);
			printStream = new PrintStream(fOut);
		} catch (FileNotFoundException e) {
			e.printStackTrace(System.err);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}

		for (Iterator<JavaMethod> it = set.iterator(); it.hasNext(); ){ 
			JavaMethod jm = it.next();
			printStream.println(jm);
		}
		updateUnmatched(set);
	}
}
