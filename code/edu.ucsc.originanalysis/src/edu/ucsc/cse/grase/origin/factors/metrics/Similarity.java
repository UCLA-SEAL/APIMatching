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
/*
 * Created on 2005. 4. 8.
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.ucsc.cse.grase.origin.factors.metrics;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sung Kim
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public abstract class Similarity {
	abstract public double getSimilarity(List a, List b);

	/**
	 * Simple convert String to List and get Similarity
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public double getSimilarity(String a, String b) {
		List l1 = new ArrayList();
		List l2 = new ArrayList();

		for (int i = 0; i < a.length(); i++) {
			l1.add("" + a.charAt(i));
		}

		for (int i = 0; i < b.length(); i++) {
			l2.add("" + b.charAt(i));
		}

		return getSimilarity(l1, l2);
	}
}
