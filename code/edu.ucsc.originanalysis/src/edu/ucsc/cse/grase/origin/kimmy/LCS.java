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
package edu.ucsc.cse.grase.origin.kimmy;

import java.util.ArrayList;
import java.util.List;


public class LCS<T> {

	public static void main(String args[]) {
		try {
			List<String> a= new ArrayList<String>();
			List<String> b= new ArrayList<String>();
			for (int i=0; i<args[0].length(); i++){
				String ac = args[0].substring(i,i+1);
				a.add(i,ac);
			}
			for (int i=0; i<args[1].length(); i++){
				String bc = args[1].substring(i,i+1);
				b.add(i,bc);
			}
			LCSElement[] s = new LCS().LCSAlgorithm(a,b);
			
			for (int i=0; i<s.length;i++){
				System.out.println(s[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(LCS.getRegularExpCommonPattern("Kim, Miryung", "Miryung Kim"));
		System.out.println(LCS.getNumSharedChar("Kim, Miryung", "Miryung Kim"));
		System.out.println(LCS.getNumSharedChar("com.jrefinery.chart", "*"));
	}
//	public static void main(String args[]) {
//		try {
//			File fa = new File("e:\\mandarax_archive\\mandarax3.2\\methods.trace"); 
//			assert(fa.exists());
//			File fb = new File("e:\\mandarax_archive\\mandarax3.3\\methods.trace");
//			assert(fb.exists());
//			MethodTrace ma = new MethodTrace();
//			ma.parse(fa);
//			MethodTrace mb = new MethodTrace();
//			mb.parse(fb);
//			List a= ma.getList();
//			List b= mb.getList();
////			for (int i=0; i<args[0].length(); i++){
////				String ac = args[0].substring(i,i+1);
////				a.add(i,ac);
////			}
////			for (int i=0; i<args[1].length(); i++){
////				String bc = args[1].substring(i,i+1);
////				b.add(i,bc);
////			}
//			LCSElement[] s = LCSAlgorithm(a,b);
//			
//			for (int i=0; i<s.length;i++){
//				System.out.println(s[i]);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	
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
	private static int getNumSharedTokens (String[] a, String[] b){ 
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
	private static int getNumSharedChar (String a, String b ){ 
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
		private static String getRegularExpCommonPattern (String a, String b){
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
}
