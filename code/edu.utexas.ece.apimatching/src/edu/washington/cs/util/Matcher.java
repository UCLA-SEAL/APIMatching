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

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import edu.washington.cs.profile.CustomComparator;
import edu.washington.cs.profile.MatcherComparable;

public class Matcher<T extends Comparable,V extends MatcherComparable > {
	
	public Matcher (CustomComparator c){ 
		V.setComparator (c);
	}
	public Set<Pair> cartesianProduct(Set<T> before, Set<T> after,
			MatchingStrategy<T, V> before_strategy,
			MatchingStrategy<T, V> after_strategy) {
		Set<Pair> result = null;
		if (before_strategy == null && after_strategy == null) {
			result = new TreeSet<Pair>();
			Iterator<T> bit = before.iterator();
			while (bit.hasNext()) {
				T b_item = bit.next();
				Iterator<T> ait = after.iterator();
				while (ait.hasNext()) {
					T a_item = ait.next();
					Pair<T> p = new Pair<T>(b_item, a_item);
					result.add(p);
				}
			}
		} else {
			Set<Pair> s1 = this.join(before, before_strategy, after_strategy);
			Set<Pair> s2 = this.join(after, before_strategy, after_strategy);
			s1.addAll(s2);
			result = s1;
		}
		return result;
	}

	public Set<Pair> join(Set<T> before_set, MatchingStrategy<T, V> before,
			MatchingStrategy<T, V> after) {
		Set<Pair> pairs = new TreeSet<Pair>();
		if (before_set == null) {
			before_set = before.getSet();
		}
		if (before != null && after != null) {
			Iterator<T> bit = before_set.iterator();
			while (bit.hasNext()) {
				T b_item = bit.next();
				V b_value = before.getValue(b_item);
				Set<T> matched_for_bitem = after.match(b_value);
				if (matched_for_bitem.size() == 1) {
					Iterator<T> matched = matched_for_bitem.iterator();
					while (matched.hasNext()) {
						Pair<T> p = new Pair<T>(b_item, matched.next());
						pairs.add(p);
					}
				} else {
					if (matched_for_bitem.size() == 0) {
						// System.out.println(b_item+":none matched with the
						// method count"+b_value);
					}
				}
			}
		} else {
			return null;
		}
		return pairs;
	}

}
