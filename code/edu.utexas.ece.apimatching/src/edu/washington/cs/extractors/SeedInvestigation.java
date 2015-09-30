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
package edu.washington.cs.extractors;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.washington.cs.induction.FileNameService;
import edu.washington.cs.induction.HillClimbingSearch;
import edu.washington.cs.induction.ReadDirectories;
import edu.washington.cs.rules.Change;
import edu.washington.cs.rules.JavaMethod;
import edu.washington.cs.rules.RuleBase;
import edu.washington.cs.rules.Scope;
import edu.washington.cs.util.ListOfPairs;
import edu.washington.cs.util.Pair;
import edu.washington.cs.util.SetOfPairs;

public class SeedInvestigation {

	// why when I am rejecting seeds, it does not replace with other seeds. 
	// 0.9.5 - 0.9.4 
	// go over all left in seeds that does not show up in the left seed in 0.9.5-0.9.4 matches
	// make rules based on this seeds. 
	// check whether these rules contain such seeds. 
	
	public static void main (String args[]) {
		
		File[] dirs = ReadDirectories.getDirectories("jfreechart_list");
		double EXCEPTION_TH = 0.25;
		for (int i = 1; i < dirs.length-1; i++) {
			ProgramSnapshot oldP = new ProgramSnapshot("jfreechart", dirs[i]);
			ProgramSnapshot newP = new ProgramSnapshot("jfreechart",
					dirs[i + 1]);

			File seedMatch = FileNameService.getSeedXMLFile(newP, oldP, 0.7);
			ListOfPairs seeds = ListOfPairs.readXMLFile(seedMatch
					.getAbsolutePath());
//			System.out.print("Version"+oldP.getVersion()+"-"+newP.getVersion());
//			System.out.print("\tSeed Size:" + seeds.size());
			File rule = FileNameService.getRuleXMLFile(newP, oldP, 0.7, EXCEPTION_TH);
			RuleBase rb = RuleBase.readXMLFile(rule.getAbsolutePath());
			SetOfPairs matches = rb.getAcceptedMatches();
//			System.out.println("\tMatch Size:" + matches.size());

			SetOfPairs seedMinusMatch = new SetOfPairs();
			
			if (seeds.size() > matches.size()) {
				for (Iterator<Pair> sit = seeds.iterator(); sit.hasNext();) {
					Pair seed = sit.next();
					// System.out.println(seed);
					Pair match = matches.getFirstMatchByLeft(seed.getLeft());
					if (match == null) {
						seedMinusMatch.addPair(seed);
						// why is this seed rejected but not replaced. 
					}
				}
				System.out.println(seedMinusMatch.size());
				for (Iterator<Pair> rit = seedMinusMatch.iterator(); rit
						.hasNext();) {
					Pair smm = rit.next();
					JavaMethod left = (JavaMethod)smm.getLeft();
					
					List<Change> change = Change.createChange((JavaMethod) smm
							.getLeft(), (JavaMethod) smm.getRight());
					SetOfPairs explainedByChange = new SetOfPairs();
					// (1) is it because rules do not explain the seed itself 
					for (int j = 0; j < change.size(); j++) {
						Change c = change.get(j);
						Scope s = new Scope(left, c);						
						if (s.match(left) && c.isApplicable(left)) {
							JavaMethod cor = c.applyTransformation(left);
							explainedByChange.addPair(new Pair(left, cor));
						}// if
					}// changes
						
					// (2) is it because we remove domain more than necessary. 
				}
						
				ArrayList<JavaMethod> domain =newP.minus(oldP);
				ArrayList<JavaMethod> codomain =oldP.minus(newP);
				// trying out with seedMinusMatch seeds 
				ListOfPairs rejectedSeeds = new ListOfPairs(); 
				rejectedSeeds.addSetOfPairs(seedMinusMatch);
				new HillClimbingSearch().hillClimbingSearch(domain,
						codomain, rejectedSeeds, new File("SeedInvestigation.xml"), EXCEPTION_TH);
				
			}
			
		}
		
	}
}
