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
//	 Arup Guha
//4/14/03
//COP 3503 Spring 2003 assignment #6
//The program takes in two input files and then computes the Longest
//Common Subsequence between the files ignoring whitespace. Given two
//files where a common secret message is hidden, determining the LCS
//of the two files should compromise most of the meaning of the secret
//message.
package edu.ucsc.cse.grase.origin.factors.metrics;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hunkim
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class SmithWaterman extends Similarity {
	List a, b;

	public double getSimilarity(List a, List b) {
		this.a = a;
		this.b = b;

		int aSize = a.size();
		int bSize = b.size();

		// Both zero they are same
		if (aSize == 0 && bSize == 0)
			return 1;

		//If only one of them are null, return 0
		if (aSize == 0 || bSize == 0)
			return 0;

		int cSize = getCommonCount();
		// No common
		if (cSize == 0)
			return 0;

		return ((double) cSize / aSize + (double) cSize / bSize) / 2;
	}

	/**
	 * 
	 * @return
	 */
	public int getCommonCount() {
		int commonCount = 0;
		List bTmp = new ArrayList();
		bTmp.addAll(b);

		for (int i = 0; i < a.size(); i++) {
			Object obj = a.get(i);
			if (bTmp.contains(obj)) {
				commonCount++;
				bTmp.remove(obj);
			}
		}

		return commonCount;
	}

}