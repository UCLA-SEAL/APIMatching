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
public class LCSC extends Similarity {
	List a, b;

	public double getSimilarity(List a, List b) {
		if (a==null) {
			a=new ArrayList();
		}
		
		if (b==null) {
			b=new ArrayList();
		}		
		
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

		int cSize = getLCSCount();
		// No common
		if (cSize == 0)
			return 0;

		return ((double) cSize / aSize + (double) cSize / bSize) / 2;
	}

	/**
	 * 
	 * @return
	 */
	public int getLCSCount() {
		// Initialize 2D array to store lengths of all LCS's.
		int n = a.size();
		int m = b.size();
		int[][] seq = new int[n + 1][m + 1];

		for (int i = 0; i <= n; i++)
			seq[i][0] = 0;

		for (int j = 0; j <= m; j++)
			seq[0][j] = 0;

		// Implements dynamic programming algorithm in text.
		for (int i = 1; i < n + 1; i++) {
			for (int j = 1; j < m + 1; j++) {

				if (a.get(i - 1).equals(b.get(j - 1)))
					seq[i][j] = seq[i - 1][j - 1] + 1;
				else
					seq[i][j] = Math.max(seq[i - 1][j], seq[i][j - 1]);
			}
		}

		return seq[n][m];
	}

	/**
	 * 
	 * @return
	 */
	public List getLCS() {
		// Initialize 2D array to store lengths of all LCS's.
		int n = a.size();
		int m = b.size();
		int[][] seq = new int[n + 1][m + 1];

		for (int i = 0; i <= n; i++)
			seq[i][0] = 0;

		for (int j = 0; j <= m; j++)
			seq[0][j] = 0;

		// Implements dynamic programming algorithm in text.
		for (int i = 1; i < n + 1; i++) {
			for (int j = 1; j < m + 1; j++) {

				if (a.get(i - 1).equals(b.get(j - 1)))
					seq[i][j] = seq[i - 1][j - 1] + 1;
				else
					seq[i][j] = Math.max(seq[i - 1][j], seq[i][j - 1]);
			}
		}

		int i = n;
		int j = m;
		int back = seq[n][m] - 1;
		Object common[] = new Object[back + 1];

		// Prints out LCS to screen by using computed LCS's of all
		// pairs of prefixes of original strings.
		while (i > 0 && j > 0) {

			// Current letter in either string is not used in LCS.
			if (seq[i][j - 1] == seq[i][j])
				j--;
			else if (seq[i - 1][j] == seq[i][j])
				i--;

			// Current letter must be used in LCS.
			else {
				common[back] = a.get(i - 1);
				back--;
				i--;
				j--;
			}
		}

		List ret = new ArrayList();

		for (i = 0; i < common.length; i++) {
			ret.add(common[i]);
		}

		return ret;
	}
}