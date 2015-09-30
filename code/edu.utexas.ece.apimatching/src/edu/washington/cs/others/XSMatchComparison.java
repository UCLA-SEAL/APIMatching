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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import edu.washington.cs.extractors.ProgramSnapshot;
import edu.washington.cs.extractors.ReadDirectories;
import edu.washington.cs.induction.FileNameService;
import edu.washington.cs.induction.RulebasedMatching;
import edu.washington.cs.util.Comparison;
import edu.washington.cs.util.ListOfPairs;
import edu.washington.cs.util.SetOfPairs;

public class XSMatchComparison {

	public static void main(String args[]) {

		compare(false, "jfreechart_list_CGE", "jfreechart", 0.7, 0.34);
		// compare (false, "jfreechart_list", "jfreechart", 0.7, 0.34);
		// compare (false, "jfreechart_list", "jfreechart", 0.7, 0.5);
		// compare (false, "jfreechart_list", "jfreechart", 0.8, 0.25);
		// compare (false, "jfreechart_list", "jfreechart", 0.8, 0.34);
		// compare (false, "jfreechart_list", "jfreechart", 0.8, 0.5);
	}

	public static void compare(boolean onlyOne, String dirList, String project,
			double SEED_TH, double EXCEPTION_TH) {
		File[] dirs = ReadDirectories.getDirectories(dirList);
		compareProject(onlyOne, dirs, project, SEED_TH, EXCEPTION_TH);
	}

	public static void compareProject(boolean onlyOne, File[] dirs,
			String project, double SEED_TH, double EXCEPTION_TH) {
		int end = 1;
		if (!onlyOne) {
			end = dirs.length - 1;
		}
		for (int i = 0; i < end; i++) {
			ProgramSnapshot oldP = new ProgramSnapshot(project, dirs[i]);
			ProgramSnapshot newP = new ProgramSnapshot(project, dirs[i + 1]);
			File frb = FileNameService.getMatchingXMLFile(oldP, newP, SEED_TH,
					EXCEPTION_TH);
			RulebasedMatching rb = RulebasedMatching.readXMLFile(frb
					.getAbsolutePath());

			File xingMatch = FileNameService.getXSMatchFile(project, oldP.getVersion(), newP.getVersion());
			ListOfPairs xingmatches = ListOfPairs.readXMLFile(xingMatch.getAbsolutePath());
			SetOfPairs xsMatches = new SetOfPairs();
			xsMatches.addSetOfPairs(xingmatches);
			SetOfPairs ruleMatches = rb.getMatches(true);
			
			// compare xsMatches and ruleMatches 
			SetOfPairs mine_theirs = Comparison.leftMinusRight(ruleMatches, xsMatches);
			SetOfPairs theirs_mine = Comparison.leftMinusRight(xsMatches, ruleMatches);
			// update mine_theirs
		}
		
	}
}
