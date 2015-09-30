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



public abstract class MatcherComparable implements Comparable{
	
 	public static CustomComparator comparator = null; 
	
	public static void setComparator(CustomComparator c){
		comparator = c;
	}
	public abstract CustomComparator getDefaultComparator ();
	public boolean equals (Object obj) {
		if (comparator==null) {
			System.err.println("Comparator is not set.");
		} 
		return comparator.equals(this, obj);
	}
	public int compareTo (Object obj){
		if (comparator==null) {
			System.err.println("Comparator is not set.");
		}
		MatcherComparable m = (MatcherComparable) obj;
		return comparator.compareTo(this, obj);
	}
	public int hashCode(){
		if (comparator==null) { 
			System.err.println("Comparator is not set.");
		}
		return comparator.hashCode(this);
	}
	public abstract String toString ();
	
}