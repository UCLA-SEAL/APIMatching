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

import java.util.Comparator;

import edu.washington.cs.rules.JavaMethod;
import edu.washington.cs.rules.JavaMethodComparator;

public class PairComparator implements Comparator<Pair>{

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(T, T)
	 */
	public int compare(Pair p1, Pair p2 ) {
		
		Pair<JavaMethod> o1 = (Pair<JavaMethod>)p1;
		Pair<JavaMethod> o2 = (Pair<JavaMethod>)p2;
		// TODO Auto-generated method stub
		if (o1.getLeft()==null || o2.getLeft()==null) return -1;
		
		return new JavaMethodComparator().compare(o1.getLeft(), o2.getLeft());
	}
	
}	
