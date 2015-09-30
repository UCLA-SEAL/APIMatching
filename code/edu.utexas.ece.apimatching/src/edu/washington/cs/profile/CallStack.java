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
package  edu.washington.cs.profile;

import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import edu.washington.cs.rules.JavaMethod;
import edu.washington.cs.util.SetUtil;

public class CallStack extends MatcherComparable{ 

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CallStack aCStk = new CallStack();

		CallStack.setComparator(aCStk.getDefaultComparator());
		Stack aC = aCStk.new Stack(3); 
		aC.appendToStack("M1", 0);
		aC.appendToStack("M2", 1);
		aC.appendToStack("M2", 2);
		aCStk.addStack(aC);
		Stack a2C = aCStk.new Stack(3); 
		a2C.appendToStack("M1", 0);
		a2C.appendToStack("M2", 1);
		a2C.appendToStack("M2", 2);
		aCStk.addStack(a2C);
		boolean b1 = aC.equals(a2C);
		System.out.println(aCStk.toString());
		System.out.println(aCStk.getImmediateCallers());
		
		CallStack bCStk = new CallStack();
		Stack bC = bCStk.new Stack(3);
		bC.appendToStack("M1", 0);
		bC.appendToStack("M2", 1);
		bC.appendToStack("M2", 2);
		bCStk.addStack(bC);
		bC = bCStk.new Stack(3);
		bC.appendToStack("M2", 0);
		bC.appendToStack("M2", 1);
		bC.appendToStack("M2", 2);
		bCStk.addStack(bC);
		
		System.out.println(bCStk.toString());
		System.out.println(bCStk.getImmediateCallers());
		boolean b = aCStk.equals(bCStk);
		
		if (b) System.out.println("SUCCESS");
	} 
	
	private class Stack implements Comparable{
		// methodNames[0] top
		// methodNames[n] bottom 
		private String [] methodNames;
		private final int realDepth;
		private Stack (int realDepth){
			this.realDepth= realDepth;
			methodNames = new String[realDepth];
		}
		private Stack (int realDepth, String[] boundedTrace){
			this.realDepth= realDepth;
			methodNames = boundedTrace;
		}
		private void appendToStack (String methodName, int depth) {
			methodNames[depth]= methodName;	
		}	
		private void setBoundedTrace (String []btrace){ 
			methodNames= btrace;
		}
		public int compareTo (Object o) {
			if (this.equals(o)) return 0; 
			return -1;
		}
		public boolean equals (Object o){
			Stack st = (Stack) o;
			if (st.realDepth != this.realDepth) return false;
			if (st.methodNames.length != this.methodNames.length) return false;
			for (int i=0; i< (st.methodNames.length);i++){
				if (!st.methodNames[i].equals(this.methodNames[i])) return false; 
			}
			return true;
		}
		public String toString() {
			String s ="";
			s = s+realDepth;
			for (int i=0; i<methodNames.length; i++){
				s = s+methodNames[i];
			}
			return s;
		}
	}
	
 	private Set<Stack> stacks ; 
	private Set<Integer> depths;
	private Set<String> callers;
	
 	public CallStack() {
 		stacks = new TreeSet<Stack>();
 	}
 	private Set<String> getImmediateCallers() {
 		if (callers == null) {
			callers = new TreeSet<String>();
			for (Iterator<Stack> it = stacks.iterator(); it.hasNext();) {
				Stack st = it.next();
				callers.add(st.methodNames[0]);
				
			}
		}
 		return callers;
 	}
 	private Set<Integer> getStackDepths() {
 		if (depths == null) {
			depths = new TreeSet<Integer>();
			for (Iterator<Stack> it = stacks.iterator(); it.hasNext();) {
				Stack st = it.next();
				depths.add(st.realDepth);
			}
		}
 		return depths;
 	}
 	private Integer getNumImmediateCallers() {
 		if (callers == null) {
			callers = new TreeSet<String>();
			for (Iterator<Stack> it = stacks.iterator(); it.hasNext();) {
				Stack st = it.next();
				callers.add(st.methodNames[0]);
			}
 		}
 		return callers.size();
 	}
 	
	public void addStack(int depth, String[] boundTrace){
		Stack s = new Stack(boundTrace.length);
		s.setBoundedTrace(boundTrace);
		stacks.add(s);
	}
	public void addStack(Stack s){
		stacks.add(s);
	}
	
	/* (non-Javadoc)
	 * @see edu.washington.cs.profile.MatcherComparable#getDefaultComparator()
	 */
	@Override
	public CustomComparator getDefaultComparator() {
		return new DefaultCallStackComparator();
	}
	/* (non-Javadoc)
	 * @see edu.washington.cs.profile.MatcherComparable#toString()
	 */
	@Override
	public String toString() {
		String s ="";
		s = s+"NumStacks:"+stacks.size()+"\n";
		s = s+stacks.toString();
		return s;
	}
	public String info(){ 
		String s ="";
		s = s+"NumStacks:"+stacks.size()+"\n";
		for (Iterator<CallStack.Stack> it = stacks.iterator(); it.hasNext(); ) { 
			CallStack.Stack st = it.next();
			s = s+"\t"+st.toString()+"\n";
		}
		return s;
	}
	public class DefaultCallStackComparator implements CustomComparator{

		/* (non-Javadoc)
		 * @see edu.washington.cs.profile.CustomComparator#compareTo(java.lang.Object, java.lang.Object)
		 */
		public int compareTo(Object a, Object b) {
			if (equals(a,b)) return 0;
			return -1;
		}

		/* (non-Javadoc)
		 * @see edu.washington.cs.profile.CustomComparator#equals(java.lang.Object, java.lang.Object)
		 */
		public boolean equals(Object a, Object b) {
			CallStack aCStack = (CallStack) a;
			CallStack bCStack = (CallStack) b; 
			Set<Stack> aStacks = aCStack.stacks;
			Set<Stack> bStacks = bCStack.stacks;
			if (aStacks.size()!= bStacks.size()) return false;
			if (!aStacks.equals(bStacks)) return false; 
			return true;
		}

		/* (non-Javadoc)
		 * @see edu.washington.cs.profile.CustomComparator#hashCode(java.lang.Object)
		 */
		public int hashCode(Object a) {
			CallStack aCStack = (CallStack)a;
			return aCStack.toString().hashCode();
		}	
	}
	public class SetImmediateCallersComparator implements CustomComparator{

		/* (non-Javadoc)
		 * @see edu.washington.cs.profile.CustomComparator#compareTo(java.lang.Object, java.lang.Object)
		 */
		public int compareTo(Object a, Object b) {
			if (equals(a,b)) return 0; 
			return -1;
		}

		/* (non-Javadoc)
		 * @see edu.washington.cs.profile.CustomComparator#equals(java.lang.Object, java.lang.Object)
		 */
		public boolean equals(Object a, Object b) {
			CallStack aCStack = (CallStack) a;
			CallStack bCStack = (CallStack) b; 
			Set<String> aImmediateCallers = aCStack.getImmediateCallers();
			Set<String> bImmediateCallers = bCStack.getImmediateCallers();
			if (aImmediateCallers.equals(bImmediateCallers)) return true;
			return false;
		}

		/* (non-Javadoc)
		 * @see edu.washington.cs.profile.CustomComparator#hashCode(java.lang.Object)
		 */
		public int hashCode(Object a) {
			CallStack aCStack = (CallStack) a;
			Set<String> aImmediateCallers = aCStack.getImmediateCallers();
			return aImmediateCallers.toString().hashCode();
		} 
	}
	public class NumImmediateCallersComparator implements CustomComparator{

		/* (non-Javadoc)
		 * @see edu.washington.cs.profile.CustomComparator#compareTo(java.lang.Object, java.lang.Object)
		 */
		public int compareTo(Object a, Object b) {
			CallStack aCStack = (CallStack) a;
			CallStack bCStack = (CallStack) b; 
			Set<String> aImmediateCallers = aCStack.getImmediateCallers();
			Set<String> bImmediateCallers = bCStack.getImmediateCallers();
			Integer aNumCaller = aImmediateCallers.size();
			Integer bNumCaller = bImmediateCallers.size();
			return aNumCaller.compareTo(bNumCaller);
		
		}

		/* (non-Javadoc)
		 * @see edu.washington.cs.profile.CustomComparator#equals(java.lang.Object, java.lang.Object)
		 */
		public boolean equals(Object a, Object b) {
			CallStack aCStack = (CallStack) a;
			CallStack bCStack = (CallStack) b; 
			Set<String> aImmediateCallers = aCStack.getImmediateCallers();
			Set<String> bImmediateCallers = bCStack.getImmediateCallers();
			int aNumCaller = aImmediateCallers.size();
			int bNumCaller = bImmediateCallers.size();
			
			if (aNumCaller==bNumCaller) return true;
			return false;
		}

		/* (non-Javadoc)
		 * @see edu.washington.cs.profile.CustomComparator#hashCode(java.lang.Object)
		 */
		public int hashCode(Object a) {
			CallStack aCStack = (CallStack) a;
			return aCStack.getNumImmediateCallers();
		} 	
	}
	public class SetDepthComparator implements CustomComparator {

		/* (non-Javadoc)
		 * @see edu.washington.cs.profile.CustomComparator#compareTo(java.lang.Object, java.lang.Object)
		 */
		public int compareTo(Object a, Object b) {
			if (equals(a,b)) return 0;
			return -1;
		}

		/* (non-Javadoc)
		 * @see edu.washington.cs.profile.CustomComparator#equals(java.lang.Object, java.lang.Object)
		 */
		public boolean equals(Object a, Object b) {
			CallStack aCStack = (CallStack) a;
			CallStack bCStack = (CallStack) b; 
			Set<Integer> aDepths = aCStack.getStackDepths();
			Set<Integer> bDepths = bCStack.getStackDepths();
			return aDepths.equals(bDepths);
		}

		/* (non-Javadoc)
		 * @see edu.washington.cs.profile.CustomComparator#hashCode(java.lang.Object)
		 */
		public int hashCode(Object a) {
			CallStack aCStack = (CallStack)a;
			Set<Integer> aDepths = aCStack.getStackDepths();
			return aDepths.toString().hashCode();
		} 
		
	}
	public class AtLeastOneStackSameComparator implements CustomComparator{

		/* (non-Javadoc)
		 * @see edu.washington.cs.profile.CustomComparator#compareTo(java.lang.Object, java.lang.Object)
		 */
		public int compareTo(Object a, Object b) {
			if (equals(a,b)) {
				return 0;
			}
			return -1;
		}

		/* (non-Javadoc)
		 * @see edu.washington.cs.profile.CustomComparator#equals(java.lang.Object, java.lang.Object)
		 */
		public boolean equals(Object a, Object b) {
			CallStack aCStack = (CallStack) a;
			CallStack bCStack = (CallStack) b; 
			Set<Stack> aStacks = aCStack.stacks;
			Set<Stack> bStacks = bCStack.stacks;

			SetUtil<Stack> util = new SetUtil<Stack> ();
			int intersect = util.intersect(aStacks, bStacks).size();
			if (intersect>0) return true;
			return false;
		}

		/* (non-Javadoc)
		 * @see edu.washington.cs.profile.CustomComparator#hashCode(java.lang.Object)
		 */
		public int hashCode(Object a) {
			CallStack aCStack = (CallStack) a;
			return a.hashCode();
		}
		
	}
}
