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
package edu.washington.cs.others;

import java.io.PrintStream;

import edu.washington.cs.util.Comparison;
import edu.washington.cs.util.SetOfPairs;

public class ThreeWayComp {
	int P = 0; 
	int N = 0; 
	int E = 0; 
	int PP = 0;

	int PN = 0;

	int NP = 0;

	int C_PP = 0;

	int C_PN = 0;

	int C_NP = 0;

	int NC_PN = 0; 
	int NC_NP = 0; 
	public void update(SetOfPairs mine, SetOfPairs other, SetOfPairs nToN) {
		P = P + mine.size();
		N = N + other.size();
		E = E + nToN.size();
		SetOfPairs common = Comparison.common(mine, other);
		SetOfPairs correctPP = Comparison.common(common, nToN);
		SetOfPairs P_minus_N = Comparison.leftMinusRight(mine, other);
		SetOfPairs correctPN = Comparison.common(P_minus_N, nToN);
		SetOfPairs N_minus_P = Comparison.leftMinusRight(other, mine);
		SetOfPairs correctNP = Comparison.common(N_minus_P, nToN);
	
		SetOfPairs notCorrectNP = Comparison.leftMinusRight(N_minus_P, correctNP);
//		System.out.println(notCorrectNP);
		NC_NP = NC_NP + notCorrectNP.size();
		
		SetOfPairs notCorrectPN = Comparison.leftMinusRight(P_minus_N, correctPN);
//		System.out.println(notCorrectPN);
		NC_PN = NC_PN + notCorrectPN.size();
		
		
		PP = PP + common.size();
		PN = PN + mine.size() - common.size();
		NP = NP + other.size() - common.size();
		C_PP = C_PP + correctPP.size();
		C_PN = C_PN + correctPN.size();
		C_NP = C_NP + correctNP.size();
	}
	public static void printHeader (PrintStream p){
		p.print("P\tN\tE\t");
		p.print("PP\tPN\tNP\t");
		p.print("C_PP\tC_PN\tC_NP\t");
		p.print("Prec.PP\tPrec.PN\tPrec.NP\n");
	}
	public void print(PrintStream p) {
		p.print(P+"\t");
		p.print(N+"\t");
		p.print(E+"\t");
		
		p.print(PP+"\t");
		p.print(PN+"\t");
		p.print(NP+"\t");
		p.print(C_PP+"\t");
		p.print(C_PN+"\t");
		p.print(C_NP+"\t");	
		p.print((double)C_PP/(double)PP+"\t");
		p.print((double)C_PN/(double)PN+"\t");
		p.print((double)C_NP/(double)NP+"\n");

	}
}
