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
import edu.washington.cs.util.Comparison;
import edu.washington.cs.util.SetOfPairs;

public class MultiVersionComposition {

	public static void main(String args[]){ 
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
		SetOfPairs composedResult = new SetOfPairs();
		for (int i = 0; i < loopend; i++) {
			ProgramSnapshot oldP = new ProgramSnapshot(project, dirs[i]);
			ProgramSnapshot newP = new ProgramSnapshot(project, dirs[i + 1]);
			File forwardRule = FileNameService.getRuleXMLFile(oldP, newP,
					SEED_TH, EXCEPTION_TH);
			File backwardRule = FileNameService.getRuleXMLFile(newP, oldP,
					SEED_TH, EXCEPTION_TH);
			File matchingResult = FileNameService.getMatchingXMLFile(oldP,
					newP, SEED_TH, EXCEPTION_TH);
			RulebasedMatching twoVersionM = RulebasedMatching.readXMLFile(matchingResult
					.getAbsolutePath());
			if (i==0) { 
				composedResult.addSetOfPairs(twoVersionM.getMatches(true));
				composedResult.addSetOfPairs(twoVersionM.getUnchangedMatches());
			} else {
				ProgramSnapshot startP = new ProgramSnapshot(project, dirs[0]);
				File skippedFile = FileNameService.getMatchingXMLFile(startP,newP, SEED_TH, EXCEPTION_TH);
				RulebasedMatching skipped = RulebasedMatching
						.readXMLFile(skippedFile.getAbsolutePath());
				SetOfPairs twoVersionMatches = new SetOfPairs();
				twoVersionMatches.addSetOfPairs(twoVersionM.getMatches(true));
				twoVersionMatches.addSetOfPairs(twoVersionM.getUnchangedMatches());
				composedResult = Comparison.forwardJoin(composedResult, twoVersionMatches);
				SetOfPairs skippedMatches =  new SetOfPairs(); 
				skippedMatches.addSetOfPairs(skipped.getMatches(true));
				skippedMatches.addSetOfPairs(skipped.getUnchangedMatches());
				Comparison.comparison(composedResult, skippedMatches);
			}
		}
	}
}
