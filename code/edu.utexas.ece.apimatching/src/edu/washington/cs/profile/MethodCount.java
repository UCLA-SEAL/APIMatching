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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.washington.cs.rules.JavaMethod;
import edu.washington.cs.util.Bin;
import edu.washington.cs.util.MatchingStrategy;

public class MethodCount implements MatchingStrategy<JavaMethod, Count> {

	private static String COUNT_SUFFIX = ".count";

	private Hashtable<JavaMethod, Count> methodcounts;

	public static void main(String[] args) {
		
	}
	public MethodCount(String project, String version) {
		methodcounts = new Hashtable<JavaMethod, Count>();
		File fProject = new File(project);
		assert (fProject.exists());
		File fTrace = new File(fProject, version + MethodTraceFile.TRACE_SUFFIX);
		assert (fTrace.exists());
		File fCount = new File(fProject, version + COUNT_SUFFIX);
		readFromTrace(fTrace);
		writeToCount(fCount);
//		if (!fCount.exists()) {
//			readFromTrace(fTrace);
//			writeToCount(fCount);
//		} else {
//			readFromCount(fCount);
//		}
	}
	
	private MethodCount(Hashtable<JavaMethod, Count> table) { 
		methodcounts = table;
	}
	private Hashtable<JavaMethod,Count> getMethodCounts() { 
		return methodcounts;
	}
	public boolean readFromTrace(File fTrace) {
		try {
			if (fTrace.exists()){
				FileReader fread = new FileReader(fTrace);
				BufferedReader reader = new BufferedReader(fread);
				for (String line = reader.readLine(); line != null; line = reader.readLine()){
					JavaMethod jm = new JavaMethod(line);
					if (methodcounts.containsKey(jm)) {
						Count count = methodcounts.get(jm);
						count.incrementCount();
						methodcounts.put(jm, count);

					} else {
						Count count = new Count(1);
						methodcounts.put(jm, count);
					}
				}
			}else{ 
				return false;
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return true;
	}
		
	public void writeToCount(File fCount) {
		PrintStream print_stream = null;
		try {
			if (!fCount.exists()) {
				fCount.createNewFile();
			}
			FileOutputStream outstream = new FileOutputStream(fCount);
			print_stream = new PrintStream(outstream);
		} catch (java.io.FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Set<JavaMethod> sigs = methodcounts.keySet();
		Iterator<JavaMethod> it = sigs.iterator();
		while (it.hasNext()) {
			JavaMethod sig = it.next();
			Count count = methodcounts.get(sig);
			if (print_stream != null) {
				print_stream.println(sig + ":" + count);
				System.out.println(sig + "\t" + count);
			}
		}
	}


	
	public void printStrategy(PrintStream print_stream) {
		assert (print_stream!=null);
		Set<JavaMethod> sigs = methodcounts.keySet();
		Iterator<JavaMethod> it = sigs.iterator();
		while (it.hasNext()) {
			JavaMethod sig = it.next();
			Count count = methodcounts.get(sig);
			if (print_stream != null) {
				print_stream.println(sig + ":" + count);
			}
		}
	}
	public boolean readFromCount (File fCount){
		
		try {
			if (!fCount.exists()) {
				return false;

			}
			FileReader freader = new FileReader(fCount);
			BufferedReader reader = new BufferedReader(freader);
			for (String s = reader.readLine(); s != null; s = reader.readLine()) {
				String name = s.substring(0,s.indexOf(":"));
				JavaMethod jm = new JavaMethod(name);
				Count count = new Count(s.substring(s.indexOf(":")+1));
				methodcounts.put(jm,count);
			}
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}
		return true;
	}
	/* (non-Javadoc)
	 * @see edu.washington.cs.compare.MatchingStrategy#getValue(T)
	 */
	public Count getValue(JavaMethod t1) {
		// TODO Auto-generated method stub
		return methodcounts.get(t1);
	}
	/* (non-Javadoc)
	 * @see edu.washington.cs.compare.MatchingStrategy#match(V)
	 */
	public Set<JavaMethod> match(Count value) {
		Set<JavaMethod> s = methodcounts.keySet();
		Set<JavaMethod> result = new TreeSet<JavaMethod> ();
		Iterator<JavaMethod> sit= s.iterator();
		while (sit.hasNext()){
			JavaMethod jm = sit.next();
			Count v = methodcounts.get(jm);
			if (v.equals(value)) {
				result.add(jm);
			}
		}
		return result;
	}
	public int size(){
		return methodcounts.size();
	}
	public MatchingStrategy removeMatchedOnes(Set<JavaMethod> toBeRemoved) {
		Hashtable<JavaMethod,Count> newMethodCounts = new Hashtable<JavaMethod,Count> ();
		for (Iterator<JavaMethod>it = methodcounts.keySet().iterator(); it.hasNext(); ){ 
			JavaMethod jm = it.next(); 
			if (!toBeRemoved.contains(jm)) {
				newMethodCounts.put(jm,methodcounts.get(jm));
			}
		}
		return new MethodCount(newMethodCounts);
	}

	public Bin createBin(int binsize) {
		Bin bin = new Bin(binsize);
		Set<JavaMethod> set = methodcounts.keySet();
		for (Iterator<JavaMethod> it = set.iterator(); it.hasNext(); ){ 
			JavaMethod jm = it.next();
			Count i = methodcounts.get(jm);
			bin.add(i.getCount());
		}
		return bin;
	}
	/* (non-Javadoc)
	 * @see edu.washington.cs.compare.MatchingStrategy#retainMatchableOnes(java.util.Set)
	 */
	public MatchingStrategy retainMatchableOnes(Set<JavaMethod> toBeRetained) {
		Hashtable<JavaMethod,Count> newMethodCounts = new Hashtable<JavaMethod,Count>();
		for (Iterator<JavaMethod> it =toBeRetained.iterator(); it.hasNext();){
			JavaMethod toRetain = it.next();
			if (methodcounts.containsKey(toRetain)) {
				newMethodCounts.put(toRetain,methodcounts.get(toRetain));
			}
		}	
		return new MethodCount(newMethodCounts);
	}
	
	public Set<JavaMethod> getSet (){ 
		return methodcounts.keySet();
	}
	
	private TreeMap<Count,TreeSet> getReverseForm() {
		TreeMap<Count,TreeSet> reverseMap = new TreeMap<Count,TreeSet>();
		for (Iterator<JavaMethod>it = methodcounts.keySet().iterator(); it.hasNext(); ){
			JavaMethod jm = it.next();
			Count value = methodcounts.get(jm);
			TreeSet set = null;
			if (reverseMap.get(value)==null){ 
				set = new TreeSet();
			}else {
				set = reverseMap.get(value);
			}
			set.add(jm);
			reverseMap.put(value,set);
		}
		return reverseMap;
	}
	public void printReverseStrategy (PrintStream printStream) {
		TreeMap<Count,TreeSet> reverseMap = getReverseForm();
		for (Iterator<Count> it = reverseMap.keySet().iterator(); it.hasNext();){ 
			Count value = it.next();
			printStream.println(value+":");
			TreeSet set = reverseMap.get(value);
			
			for (Iterator i = set.iterator(); i.hasNext();) {
				Object member = i.next();
				printStream.println("\t"+member);
			}
		}
	}
}
