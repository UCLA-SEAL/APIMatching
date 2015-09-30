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
package edu.washington.cs.util;

import java.io.PrintStream;
import java.util.Iterator;

import edu.washington.cs.rules.JavaMethod;

public class Comparison {

	public static SetOfPairs common (SetOfPairs s1, SetOfPairs s2) {
		SetOfPairs result = new SetOfPairs();
		result.addSetOfPairs(s1);
		result.retainOnly(s2);
		return result;
	}
	public static SetOfPairs leftMinusRight (SetOfPairs left, SetOfPairs right){
		SetOfPairs result = new SetOfPairs();
		result.addSetOfPairs(left);
		result.removeSetOfPairs(right);
		return result;
		
	}
	public static SetOfPairs rightMinusLeft (SetOfPairs left, SetOfPairs right) { 
		SetOfPairs result = new SetOfPairs();
		result.addSetOfPairs(right);
		result.removeSetOfPairs(left);
		return result;
	}
	public static SetOfPairs forwardJoin (SetOfPairs oldS, SetOfPairs newS) {
		SetOfPairs result = new SetOfPairs();
		for (Iterator<Pair> it = oldS.iterator(); it.hasNext(); ){ 
			Pair p = it.next();
			JavaMethod jmLeft = (JavaMethod) p.getLeft();
			JavaMethod jmRight =(JavaMethod) p.getRight();
			// find pairs in s2 that starts with jmRight;
			ListOfPairs matches = newS.getMatchesByLeft(jmRight);
			if (matches.size()>0) { 
				for (Iterator mIt = matches.iterator(); mIt.hasNext(); ){ 
					Pair mP = (Pair)mIt.next();
					Pair join = new Pair(jmLeft,mP.getRight());
					result.addPair(join);
				}
			}
		}
		System.out.println("L:"+oldS.size()+"\tR:"+newS.size()+"\tFJoin:"+result.size());
		return result;
	}
	public static SetOfPairs backwardJoin (SetOfPairs oldS, SetOfPairs newS){
		SetOfPairs result = new SetOfPairs();
		for (Iterator<Pair> it = newS.iterator(); it.hasNext(); ){ 
			Pair p = it.next();
			JavaMethod jmLeft = (JavaMethod) p.getLeft();
			JavaMethod jmRight = (JavaMethod) p.getRight();
			// find pairs in s1 that ends with jmLeft; 
			ListOfPairs matches = oldS.getMatchesByRight(jmLeft);
			if (matches.size() > 0) {
				for (Iterator mIt = matches.iterator(); mIt.hasNext();) {
					Pair mP = (Pair) mIt.next();
					Pair join = new Pair(mP.getLeft(), jmRight);
					result.addPair(join);
				}
			} 
		}
		return null;
	}
	public static void comparison(SetOfPairs left, SetOfPairs right) {
		if (left.size()==0 || right.size()==0) return;
		int L = left.size();
		int R = right.size();
		int common = common(left, right).size();
		int LminusR = leftMinusRight(left, right).size();
		int RminusL = rightMinusLeft(left, right).size();
		System.out.println("++:" + common + "\t%L" + (common * 100 / L)
				+ "\t%R" + (common * 100 / R));
		System.out.println("+-:" + LminusR + "\t%L" + (LminusR * 100 / L));
		System.out.println("-+:" + RminusL + "\t%R" + (RminusL * 100 / R));
	}
	public static void comparison(SetOfPairs left, SetOfPairs right, PrintStream p) {
		if (left.size()==0 || right.size()==0) return;
		int L = left.size();
		int R = right.size();
		int common = common(left, right).size();
		int LminusR = leftMinusRight(left, right).size();
		int RminusL = rightMinusLeft(left, right).size();
		p.println("++:" + common + "\t%L" + (common * 100 / L)
				+ "\t%R" + (common * 100 / R));
		p.println("+-:" + LminusR + "\t%L" + (LminusR * 100 / L));
		p.println("-+:" + RminusL + "\t%R" + (RminusL * 100 / R));
	}
	
}
