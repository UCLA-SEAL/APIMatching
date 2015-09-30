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
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import edu.washington.cs.rules.JavaMethod;
import edu.washington.cs.util.Bin;
import edu.washington.cs.util.MatchingStrategy;

public class MethodStack implements MatchingStrategy<JavaMethod, CallStack> {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	private Hashtable<JavaMethod, CallStack> methodToCallStack;

	private final int stackDepth;

	public MethodStack(String project, String version, int depth) {
		methodToCallStack = new Hashtable<JavaMethod, CallStack>();
		stackDepth = depth;
		File fProject = new File(project);
		assert (fProject.exists());
		File fStackTrace = new File(fProject, version
				+ MethodStackTraceFile.STACKTRACE_SUFFIX);
		assert (fStackTrace.exists());
		if (fStackTrace.exists()) readFromStackTrace(fStackTrace);
	}
	public MethodStack(File f, int d) { 
		methodToCallStack =new Hashtable<JavaMethod, CallStack>();
		stackDepth = d; 
		readFromStackTrace(f);
	}
	
	private MethodStack(Hashtable<JavaMethod, CallStack> h, int d) {
		methodToCallStack = h;
		stackDepth = d;
	}

	public void readFromStackTrace(File fStackTrace) {
		try {
			FileReader fread = new FileReader(fStackTrace);
			BufferedReader reader = new BufferedReader(fread);
			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {
				assert (line.indexOf("\t") < 0);
				JavaMethod jm = new JavaMethod(line);
				CallStack stack = null;
				if (methodToCallStack.containsKey(jm)) {
					stack = methodToCallStack.get(jm);
				} else {
					stack = new CallStack();
				}
				line = reader.readLine();
				assert (line.indexOf(MethodStackTraceFile.DEPTH_TAG) >= 0);
				Integer depth = new Integer(line
						.substring(MethodStackTraceFile.DEPTH_TAG.length()));
				String boundedTrace[] = new String[stackDepth];
				for (int i = 0; i < stackDepth; i++) {
					line = reader.readLine();
					String methodName = line.substring("\t".length(), line
							.lastIndexOf("("));
					boundedTrace[i] = methodName;
				}
				stack.addStack(depth, boundedTrace);
				methodToCallStack.put(jm, stack);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.washington.cs.compare.MatchingStrategy#createBin(int)
	 */
	public Bin createBin(int binsize) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.washington.cs.compare.MatchingStrategy#getSet()
	 */
	public Set<JavaMethod> getSet() {
		return methodToCallStack.keySet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.washington.cs.compare.MatchingStrategy#getValue(T)
	 */
	public CallStack getValue(JavaMethod t1) {
		// TODO Auto-generated method stub
		return methodToCallStack.get(t1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.washington.cs.compare.MatchingStrategy#match(V)
	 */
	public Set<JavaMethod> match(CallStack value) {
		// TODO Auto-generated method stub
		Set<JavaMethod> s = methodToCallStack.keySet();
		Set<JavaMethod> result = new TreeSet<JavaMethod>();
		Iterator<JavaMethod> sit = s.iterator();
		while (sit.hasNext()) {
			JavaMethod jm = sit.next();
			CallStack v = methodToCallStack.get(jm);
			if (v.equals(value)) {
				result.add(jm);
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.washington.cs.compare.MatchingStrategy#removeMatchedOnes(java.util.Set)
	 */
	public MatchingStrategy removeMatchedOnes(Set<JavaMethod> toBeRemoved) {
		// TODO Auto-generated method stub
		Hashtable<JavaMethod, CallStack> newMethodStack = new Hashtable<JavaMethod, CallStack>();
		for (Iterator<JavaMethod> it = methodToCallStack.keySet().iterator(); it
				.hasNext();) {
			JavaMethod jm = it.next();
			if (!toBeRemoved.contains(jm)) {
				newMethodStack.put(jm, methodToCallStack.get(jm));
			}
		}
		return new MethodStack(newMethodStack, stackDepth);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.washington.cs.compare.MatchingStrategy#retainMatchableOnes(java.util.Set)
	 */
	public MatchingStrategy retainMatchableOnes(Set<JavaMethod> toBeRetained) {
		Hashtable<JavaMethod, CallStack> newMethodStack = new Hashtable<JavaMethod, CallStack>();
		for (Iterator<JavaMethod> it = toBeRetained.iterator(); it.hasNext();) {
			JavaMethod toRetain = it.next();
			if (methodToCallStack.containsKey(toRetain)) {
				newMethodStack.put(toRetain, methodToCallStack.get(toRetain));
			}
		}
		return new MethodStack(newMethodStack, stackDepth);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.washington.cs.compare.MatchingStrategy#size()
	 */
	public int size() {
		// TODO Auto-generated method stub
		return methodToCallStack.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.washington.cs.compare.MatchingStrategy#writeCurrentStrategy(java.io.File)
	 */
	public void printStrategy(PrintStream printStream) {
		// just for each method print out call stack 
		for (Iterator<JavaMethod> jmIt = methodToCallStack.keySet().iterator(); jmIt.hasNext(); ) {
			JavaMethod jm = jmIt.next();
			CallStack s = methodToCallStack.get(jm);
			printStream.println(jm);
			printStream.println("\t"+s);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.washington.cs.compare.MatchingStrategy#printReverseStrategy(java.io.PrintStream)
	 */
	public void printReverseStrategy(PrintStream printStream) {
		// the problem is that they are not exactly same.
		// it's (group) - (group) matching.
		// just sort by CallerStack and print java methods.

		TreeSet set = new TreeSet(methodToCallStack.values());
		printStream.println("SET: " + set.size());
		int index = 0;
		for (Iterator<CallStack> valueIt = set.iterator(); valueIt.hasNext();) {
			CallStack stack = valueIt.next();
			index++;
			printStream.println("STACK " + index + ":" + stack);
			for (Iterator<JavaMethod> jmIt = methodToCallStack.keySet()
					.iterator(); jmIt.hasNext();) {
				JavaMethod jm = jmIt.next();
				CallStack s = methodToCallStack.get(jm);
				if (stack.equals(s)) {
					printStream.println("\t" + jm + ":" + s);
				}
			}
		}
	}
}
