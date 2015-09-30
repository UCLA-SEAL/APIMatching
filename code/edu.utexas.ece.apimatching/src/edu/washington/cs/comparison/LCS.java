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
package edu.washington.cs.comparison;

import java.util.ArrayList;
import java.util.List;


public class LCS<T> {

	public static void main(String args[]) {

		System.out.println(LCS.getRegularExpCommonPattern("Kim, Miryung", "Miryung Kim"));
		System.out.println(LCS.getNumSharedChar("Kim, Miryung", "Miryung Kim"));
		System.out.println(LCS.getNumSharedChar("com.jrefinery.chart", "*"));
		System.out.println(LCS.getRegularExpTokenPattern("createVerticalBarChart", "createHorizontalBarChart"));
		System.out.println(LCS.getRegularExpTokenPattern("create.*BarChart","b.*Chart"));
		System.out.println(LCS.getRegularExpTokenPattern("NumberTickUnit","Tick*"));
		System.out.println(LCS.getRegularExpTokenPattern("JFreeChart, int", "int"));
		System.out.println(LCS.getRegularExpTokenPattern("Graphics2D, CategoryPlot, .*Rectangle2D, Shape","Graphics2D, CategoryPlot, ValueAxis, Marker, Rectangle2D, Shape" ));
		
	}
	// These are "constants" which indicate a direction in the backtracking array.
	private static final int NEITHER = 0;

	private static final int UP = 1;

	private static final int LEFT = 2;

	private static final int UP_AND_LEFT = 3;

	public LCSElement[] LCSAlgorithm(List a, List b) {
		int n = a.size();
		int m = b.size();
		int S[][] = new int[n + 1][m + 1];
		int R[][] = new int[n + 1][m + 1];
		int ii, jj;

		// It is important to use <=, not <.  The next two for-loops are initialization
		for (ii = 0; ii <= n; ++ii) {
			S[ii][0] = 0;
			R[ii][0] = UP;
		}
		for (jj = 0; jj <= m; ++jj) {
			S[0][jj] = 0;
			R[0][jj] = LEFT;
		}

		// This is the main dynamic programming loop that computes the score and
		// backtracking arrays.
		for (ii = 1; ii <= n; ++ii) {
			for (jj = 1; jj <= m; ++jj) {
				Object ca = a.get(ii-1);
				Object cb = b.get(jj-1);
				if (ca.equals(cb)) {
					S[ii][jj] = S[ii - 1][jj - 1] + 1;
					R[ii][jj] = UP_AND_LEFT;
				}

				else {
					S[ii][jj] = S[ii - 1][jj - 1] + 0;
					R[ii][jj] = NEITHER;
				}

				if (S[ii - 1][jj] >= S[ii][jj]) {
					S[ii][jj] = S[ii - 1][jj];
					R[ii][jj] = UP;
				}

				if (S[ii][jj - 1] >= S[ii][jj]) {
					S[ii][jj] = S[ii][jj - 1];
					R[ii][jj] = LEFT;
				}
			}
		}

		// The length of the longest substring is S[n][m]
		ii = n;
		jj = m;
		int pos = S[ii][jj] - 1;
		LCSElement[] lcs = new LCSElement[pos+1];
		// Trace the backtracking matrix.
		while (ii > 0 || jj > 0) {
			if (R[ii][jj] == UP_AND_LEFT) {
				ii--;
				jj--;
				lcs[pos--] = new LCSElement(a.get(ii),ii,jj);
			}

			else if (R[ii][jj] == UP) {
				ii--;
			}

			else if (R[ii][jj] == LEFT) {
				jj--;
			}
		}
		return lcs;
	}
	public static int getNumSharedChar (String a, String b ){ 
		int n = a.length();
		int m = b.length();
		int S[][] = new int[n + 1][m + 1];
		int R[][] = new int[n + 1][m + 1];
		int ii, jj;

		// It is important to use <=, not <.  The next two for-loops are initialization
		for (ii = 0; ii <= n; ++ii) {
			S[ii][0] = 0;
			R[ii][0] = UP;
		}
		for (jj = 0; jj <= m; ++jj) {
			S[0][jj] = 0;
			R[0][jj] = LEFT;
		}

		// This is the main dynamic programming loop that computes the score and
		// backtracking arrays.
		for (ii = 1; ii <= n; ++ii) {
			for (jj = 1; jj <= m; ++jj) {
				char ca = a.charAt(ii-1);
				char cb = b.charAt(jj-1);
				if (ca==cb) {
					S[ii][jj] = S[ii - 1][jj - 1] + 1;
					R[ii][jj] = UP_AND_LEFT;
				}

				else {
					S[ii][jj] = S[ii - 1][jj - 1] + 0;
					R[ii][jj] = NEITHER;
				}

				if (S[ii - 1][jj] >= S[ii][jj]) {
					S[ii][jj] = S[ii - 1][jj];
					R[ii][jj] = UP;
				}

				if (S[ii][jj - 1] >= S[ii][jj]) {
					S[ii][jj] = S[ii][jj - 1];
					R[ii][jj] = LEFT;
				}
			}
		}

		// The length of the longest substring is S[n][m]
		
		return S[n][m];
	}
	public static String getRegularExpTokenPattern (String a, String b) {
		a = a.replace(".*","*");
		b = b.replace(".*","*");
		String[] tokensA = Tokenize.tokenizeCapitalLetter(a);
		String[] tokensB = Tokenize.tokenizeCapitalLetter(b);
		int n = tokensA.length;
		int m = tokensB.length;
		int S[][] = new int[n + 1][m + 1];
		int R[][] = new int[n + 1][m + 1];
		int ii, jj;

		// It is important to use <=, not <.  The next two for-loops are initialization
		for (ii = 0; ii <= n; ++ii) {
			S[ii][0] = 0;
			R[ii][0] = UP;
		}
		for (jj = 0; jj <= m; ++jj) {
			S[0][jj] = 0;
			R[0][jj] = LEFT;
		}

		// This is the main dynamic programming loop that computes the score and
		// backtracking arrays.
		for (ii = 1; ii <= n; ++ii) {
			for (jj = 1; jj <= m; ++jj) {
				String ca = tokensA[ii-1];
				String cb = tokensB[jj-1];
				if (ca.equals(cb)) {
					S[ii][jj] = S[ii - 1][jj - 1] + 1;
					R[ii][jj] = UP_AND_LEFT;
				}

				else {
					S[ii][jj] = S[ii - 1][jj - 1] + 0;
					R[ii][jj] = NEITHER;
				}

				if (S[ii - 1][jj] >= S[ii][jj]) {
					S[ii][jj] = S[ii - 1][jj];
					R[ii][jj] = UP;
				}

				if (S[ii][jj - 1] >= S[ii][jj]) {
					S[ii][jj] = S[ii][jj - 1];
					R[ii][jj] = LEFT;
				}
			}
		}

		// The length of the longest substring is S[n][m]
		ii = n;
		jj = m;
		int pos = S[ii][jj] - 1;
		
		String s1 = new String();
		String s2 = new String();
		// Trace the backtracking matrix.
		while (ii > 0 || jj > 0) {
			if (R[ii][jj] == UP_AND_LEFT) {
				ii--;
				jj--;
				s1= tokensA[ii]+s1;
				s2 =tokensA[ii]+s2;
			}

			else if (R[ii][jj] == UP) {
				ii--;
				s1 = "*"+s1;
			}

			else if (R[ii][jj] == LEFT) {
				jj--;
				s2 = "*"+s2;
				s1 = "*"+s1;
			}
		}
		String result = replaceStar(s1);
		
		result= result.replace("*", ".*");
		return result; 
		
	}
	public static String getRegularExpCommonPattern (String a, String b){
		int n = a.length();
		int m = b.length();
		int S[][] = new int[n + 1][m + 1];
		int R[][] = new int[n + 1][m + 1];
		int ii, jj;

		// It is important to use <=, not <.  The next two for-loops are initialization
		for (ii = 0; ii <= n; ++ii) {
			S[ii][0] = 0;
			R[ii][0] = UP;
		}
		for (jj = 0; jj <= m; ++jj) {
			S[0][jj] = 0;
			R[0][jj] = LEFT;
		}

		// This is the main dynamic programming loop that computes the score and
		// backtracking arrays.
		for (ii = 1; ii <= n; ++ii) {
			for (jj = 1; jj <= m; ++jj) {
				char ca = a.charAt(ii-1);
				char cb = b.charAt(jj-1);
				if (ca==cb) {
					S[ii][jj] = S[ii - 1][jj - 1] + 1;
					R[ii][jj] = UP_AND_LEFT;
				}

				else {
					S[ii][jj] = S[ii - 1][jj - 1] + 0;
					R[ii][jj] = NEITHER;
				}

				if (S[ii - 1][jj] >= S[ii][jj]) {
					S[ii][jj] = S[ii - 1][jj];
					R[ii][jj] = UP;
				}

				if (S[ii][jj - 1] >= S[ii][jj]) {
					S[ii][jj] = S[ii][jj - 1];
					R[ii][jj] = LEFT;
				}
			}
		}

		// The length of the longest substring is S[n][m]
		ii = n;
		jj = m;
		int pos = S[ii][jj] - 1;
		
		String s1 = new String();
		String s2 = new String();
		// Trace the backtracking matrix.
		while (ii > 0 || jj > 0) {
			if (R[ii][jj] == UP_AND_LEFT) {
				ii--;
				jj--;
				s1= a.substring(ii,ii+1)+s1;
				s2 =a.substring(ii,ii+1)+s2;
			}

			else if (R[ii][jj] == UP) {
				ii--;
				s1 = "*"+s1;
			}

			else if (R[ii][jj] == LEFT) {
				jj--;
				s2 = "*"+s2;
				s1 = "*"+s1;
			}
		}
		String result = replaceStar(s1);
		
		result= result.replace("*", ".*");
		return result; 
//		System.out.println(s1);
//		System.out.println(s2);
//		System.out.println(replaceStar(s1));
//		System.out.println(replaceStar(s2));
//		return new Pair(replaceStar(s1), replaceStar(s2));
	}		
	
	public static String replaceStar (String s){
		if (s.indexOf("**")>=0) {
			s = s.replace("**","*");
			return replaceStar(s);
		}
		return s;
	}
	public static int getNumSharedTokens (String[] a, String[] b){ 
		int n = a.length;
		int m = b.length;
		int S[][] = new int[n + 1][m + 1];
		int R[][] = new int[n + 1][m + 1];
		int ii, jj;

		// It is important to use <=, not <.  The next two for-loops are initialization
		for (ii = 0; ii <= n; ++ii) {
			S[ii][0] = 0;
			R[ii][0] = UP;
		}
		for (jj = 0; jj <= m; ++jj) {
			S[0][jj] = 0;
			R[0][jj] = LEFT;
		}

		// This is the main dynamic programming loop that computes the score and
		// backtracking arrays.
		for (ii = 1; ii <= n; ++ii) {
			for (jj = 1; jj <= m; ++jj) {
				Object ca = a[ii-1];
				Object cb = b[jj-1];
				if (ca.equals(cb)) {
					S[ii][jj] = S[ii - 1][jj - 1] + 1;
					R[ii][jj] = UP_AND_LEFT;
				}

				else {
					S[ii][jj] = S[ii - 1][jj - 1] + 0;
					R[ii][jj] = NEITHER;
				}

				if (S[ii - 1][jj] >= S[ii][jj]) {
					S[ii][jj] = S[ii - 1][jj];
					R[ii][jj] = UP;
				}

				if (S[ii][jj - 1] >= S[ii][jj]) {
					S[ii][jj] = S[ii][jj - 1];
					R[ii][jj] = LEFT;
				}
			}
		}
		return S[n][m];
	}

}
