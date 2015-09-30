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
import java.util.Set;

import edu.washington.cs.profile.MatcherComparable;


public interface MatchingStrategy<T extends Comparable, V extends MatcherComparable> {
	// T: is the type of MatchableItem
	
	// V: is the value of MatchableItem
	
	// remove already matched ones from matching strategy set 
	public MatchingStrategy removeMatchedOnes(Set<T> toBeRemoved);
	
	// retain only matchableItems
	public MatchingStrategy retainMatchableOnes (Set<T> toBeRetained);
	
	// return the set of matchable items for a value V  
	public Set<T> match(V value);
	
	// return the value of each matcable item 
	
	public V getValue(T t1);
	
	// return a set of <T> s
	
	public Set<T> getSet();
	
	// return the total size of matching strategy. 
	public int size();
	
	// persist the current strategy  
	public void printStrategy(PrintStream printStream);
	
	public void printReverseStrategy (PrintStream printStream);
	
}
