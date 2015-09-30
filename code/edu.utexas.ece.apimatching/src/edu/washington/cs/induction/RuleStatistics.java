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
package edu.washington.cs.induction;

import java.io.File;

import edu.washington.cs.extractors.ProgramSnapshot;
import edu.washington.cs.util.SetOfPairs;

public class RuleStatistics {

	public static void main(String args[]) {
		int maxRule = 500;
		boolean test = false;
		// forward search
		if (args.length != 4) {
			System.exit(0);
		}
		String project = args[0];
		// (2) set threshold for name matcher
		final double SEED_TH = new Double(args[1]).doubleValue();
		// (3) set a seed selection value
		final double EXCEPTION_TH = new Double(args[2]).doubleValue();
		String dirList = args[3];
		File[] dirs = ReadDirectories.getDirectories(dirList);
		int loopend = 1;
		if (test == false)
			loopend = dirs.length - 1;
		double yields[][] = new double[maxRule][dirs.length];
		double precision[][] = new double[maxRule][dirs.length];
		double matchratio [] [] = new double[maxRule][dirs.length];
		// version, old, new, rules, matches, ratio, precision, yield
		double table [][] = new double[dirs.length][8];
		for (int i = 0; i < loopend; i++) {
			ProgramSnapshot oldP = new ProgramSnapshot(project, dirs[i]);
			ProgramSnapshot newP = new ProgramSnapshot(project, dirs[i+1]);
			File input = FileNameService.getMatchingXMLFile(oldP,newP,SEED_TH, EXCEPTION_TH);
			File eval = FileNameService.getLabeledMatchingXMLFile(oldP,newP, SEED_TH, EXCEPTION_TH); 
			RulebasedMatching inputRulebase = RulebasedMatching
					.readXMLFile(input.getAbsolutePath());
			
			RulebasedMatching evalRulebase = RulebasedMatching.readXMLFile(eval
					.getAbsolutePath());
			SetOfPairs evalMatches = evalRulebase.getMatches(true);
			double[][] evalResult = inputRulebase.getPrecisionRecallRatio(true,
					evalMatches, Math.min(inputRulebase.getDomain(true).size(),
							inputRulebase.getDomain(false).size()));
			// System.out.println("fRule\tfMatch\tfPrecision\tfYield\tfRecall\tfRuleRatio");
			for (int j = 0; j < evalResult.length; j++) {
				matchratio [j][i] = evalResult[j][6];
				yields[j][i] = evalResult[j][3];
				precision[j][i] = evalResult[j][2];
				if (j== evalResult.length-1) {

					// version, old, new, rules, matches, ratio, precision, yield
					table[i][0] = inputRulebase.getDomain(true).size();
					table[i][1] = inputRulebase.getDomain(false).size();
					table[i][2] = evalResult[j][0];//rule
					table[i][3] = evalResult[j][1];//matches
					table[i][4] = evalResult[j][5]; //ratio
					table[i][5] = evalResult[j][2];//precision
					table[i][6] = evalResult[j][3];// yield
					table[i][7] = evalResult[j][4];//recall
				}
			}
		}
		//precision 
		System.out.println("Precision");
		for (int i =0; i<maxRule; i++) {
			System.out.print(i+1+"\t");
			for (int j=0 ; j<dirs.length; j++) {
				if (precision[i][j]>0) {
					System.out.print(precision[i][j]+"\t");
				}else {
					System.out.print("\t");
						
				}
			}

			System.out.println();
		}
		
		//yield
		System.out.println("Yield");
		for (int i =0; i<maxRule; i++) {
			System.out.print(i+1+"\t");
			for (int j=0 ; j<dirs.length; j++) {
				if (yields[i][j]>0) {
					System.out.print(yields[i][j]+"\t");
				}else System.out.print("\t");
			}
			System.out.println();
		}
		// found rules 
		System.out.println("Rule Founds");
		for (int i =0; i<maxRule; i++) {
			System.out.print(i+1+"\t");
			for (int j=0 ; j<dirs.length; j++) {
				if (yields[i][j]>0) {
					double maxNumRule = (int)table[j][2];
					double used = ((double)(i+1))/maxNumRule;
					System.out.print(used +"\t"+matchratio[i][j]+"\t");
				}else System.out.print("\t\t");
			}
			System.out.println();
		}
		// table
		for (int i =0; i<dirs.length; i++ ){
			for (int j=0; j<8; j++) {
				System.out.print(table[i][j]+"\t");
			}
			System.out.println();
			
		}
 	}
}