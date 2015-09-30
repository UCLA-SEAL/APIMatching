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
import java.util.Random;

import edu.washington.cs.induction.RulebasedMatching;
import edu.washington.cs.util.Comparison;
import edu.washington.cs.util.SetOfPairs;

public class ComparisonStat {
	int numSampledTransaction =0;
	boolean stillToGo  =false;
	private static int MINIMUM_MATCH_TH = 100;
	private static int TRANS_TH = 50;
	
	int numMineRule = 0; 
	int numMineMatch = 0;
	double mineRatio = 0;	
	int numOtherRule = 0; 
	int numOtherMatch =0; 
	double otherRatio =0; 
	int numPP =0;
	int numPN = 0;
	int numNP =0;
	
	int manualPP = 0; 
	int manualPN = 0; 
	int manualNP = 0; 
	
	int sampledPP =0; 
	int sampledPN =0; 
	int sampledNP =0; 
	
	double precisionPP; 
	double precisionPN; 
	double precisionNP;
	
	int numTransaction = 0;
	public ComparisonStat(int numTransaction) { 
		this.numTransaction= numTransaction;
	}
	public void update(RulebasedMatching rb, SetOfPairs others, int otherRule) {
		SetOfPairs ruleMatches =null;
		int rule= 0;
		if (rb!=null) { 
			ruleMatches = rb.getMatches(true);
			rule = rb.numRules(true);
		}else {
			ruleMatches = new SetOfPairs();
		}
		
		numMineRule = numMineRule + rule;
		numMineMatch = numMineMatch + ruleMatches.size();
		if (numMineRule!=0) mineRatio = (double)numMineMatch / (double)numMineRule;
		
		numOtherRule = numOtherRule + otherRule;
		numOtherMatch = numOtherMatch + others.size();
		if (otherRule != 0)
			otherRatio = (double) numOtherMatch / (double) numOtherRule;
		
		int common = Comparison.common(ruleMatches, others).size();
		numPP = numPP+ common;
		numPN = numPN + (ruleMatches.size()) - common;
		numNP = numNP + others.size()-common;
		
		// update still to go 
		
		int randomNumber = new Random().nextInt(6);
		boolean random = (randomNumber % 5 == 0);
		this.stillToGo = (ruleMatches.size()) > 0 && others.size() > 0;
		boolean forPP = common > 0;
		boolean forPN = (ruleMatches.size() - common) > 0;
		boolean forNP = (others.size() - common) > 0;
		this.stillToGo = random && this.stillToGo && (forNP || forPP || forPN)
				&& numSampledTransaction < TRANS_TH;
		if (stillToGo) { 
			numSampledTransaction++;
			sampledPP = sampledPP + common;
			sampledPN = sampledPN + ruleMatches.size() - common;
			sampledNP = sampledNP + others.size()-common;
		}
	}

	public boolean stillToGo() {
		boolean result= stillToGo;
		stillToGo =false;
		return result;
	}

	public static void printHeader(PrintStream p) { 
		p.print("Project\t");
		p.print("O. Kind\t");
		p.print("O. Matches\t");
		p.print("O. Refactorings\t");
		p.print("O. Ratio\t");
		
		p.print("M. Matches\t");
		p.print("M. Rules\t");
		p.print("M. Ratio\t");
		
		p.print("% Increase Matches\t");
		p.print("% Decrease Size\t");
		
		p.print("Num. Trans\t");
		p.print("Num Sample Trans\t");
		
		p.print("Other ^ Mine\t");
		p.print("Mine - Other\t");
		p.print("Other - Mine\t");
		
		
		p.print("Sample(O^M)\t");
		p.print("Sample(M-O)\t");
		p.print("Sample(O-M)\t");
		
		p.print("Correct(O^M)\t");
		p.print("Correct(M-O)\t");
		p.print("Correct(O-M)\t");
		
		p.print("Prec(O^M)\t");
		p.print("Prec(M-O)\t");
		p.print("Prec(O-M)\t");
		p.print("\n");
		
	}
	public void print(PrintStream p, String project, String kind) {
		p.print(project + "\t");
		p.print(kind + "\t");
		p.print(numOtherMatch + "\t");
		p.print(numOtherRule + "\t");
		p.print(otherRatio + "\t");

		p.print(numMineMatch + "\t");
		p.print(numMineRule + "\t");
		p.print(mineRatio + "\t");
		double increaseMatches = (double) (numMineMatch - numOtherMatch)
				/ (double) (numOtherMatch);
		double decreaseRules = (double) (numOtherRule - numMineRule)
				/ (double) (numOtherRule);

		p.print(increaseMatches + "\t");
		p.print(decreaseRules + "\t");

		p.print(numTransaction + "\t");
		p.print(numSampledTransaction + "\t");

		p.print(numPP + "\t");
		p.print(numPN + "\t");
		p.print(numNP + "\t");
		p.print(sampledPP + "\t");
		p.print(sampledPN + "\t");
		p.print(sampledNP + "\t");
		p.print(manualPP + "\t");
		p.print(manualPN + "\t");
		p.print(manualNP + "\t");
		p.print(precisionPP + "\t");
		p.print(precisionPN + "\t");
		p.print(precisionNP + "\t");
		p.print("\n");
	}
}
