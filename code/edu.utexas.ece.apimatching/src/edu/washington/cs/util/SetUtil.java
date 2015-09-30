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

import java.util.Set;
import java.util.TreeSet;

public class SetUtil<T extends Comparable> {

	// return difference between two sets as a new object set
	public Set<T> diff(Set<T> a1, Set<T> b1) {
		Set<T> result = new TreeSet<T>();
		result.addAll(a1);
		result.removeAll(b1);
		return result;
	}

	// return interesect between two sets as a new object set
	public Set<T> intersect(Set<T> a1, Set<T> b1) {
		Set<T> result = new TreeSet<T>();
		result.addAll(a1);
		result.retainAll(b1);
		return result;
	}
	

}
