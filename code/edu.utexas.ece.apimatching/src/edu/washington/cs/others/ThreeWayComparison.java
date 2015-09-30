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
import java.io.PrintStream;

import edu.washington.cs.extractors.ProgramSnapshot;
import edu.washington.cs.induction.FileNameService;
import edu.washington.cs.induction.RulebasedMatching;
import edu.washington.cs.util.ListOfPairs;
import edu.washington.cs.util.SetOfPairs;

public class ThreeWayComparison {
	public final static int XING = 0; 
	public final static int SKIM = 1; 
	public final static int WD = 2; 
	public final static int SEED = 3;
	public final static int NO_COMPARISON= 4;
	
	public static void compHelper(PrintStream pStream, ProgramSnapshot oldP, ProgramSnapshot newP,
			double EV_SEED_TH, double EV_EXCEPTION_TH, int kind, RulebasedMatching rb, double SEED_TH) {

		SetOfPairs otherMatches = null;
		if (kind == XING) {
			File xingMatchFile = FileNameService.getXSMatchFile(oldP
					.getProject(), oldP.getVersion(), newP.getVersion());
			otherMatches = SetOfPairs.readXMLFile(xingMatchFile
					.getAbsolutePath());
		} else if (kind == SKIM) {
			otherMatches = null;
		} else if (kind == WD) {
			otherMatches = null;
		} else if (kind == SEED) {
			otherMatches = new SetOfPairs();
			File seedFile = FileNameService.getSeedXMLFile(oldP, newP, SEED_TH);
			if (seedFile.exists()) {
				ListOfPairs seedMatches = ListOfPairs.readXMLFile(seedFile
						.getAbsolutePath());
				otherMatches.addSetOfPairs(seedMatches);
			} else {
				File xsFile = FileNameService.getXSMatchFile(oldP.getProject(), oldP
						.getVersion(), newP.getVersion());
				SetOfPairs xsMatches = SetOfPairs.readXMLFile(xsFile
						.getAbsolutePath());
				otherMatches.addSetOfPairs(xsMatches);
			}
		}
		File nToNMatchFile = FileNameService.getNtoNEvalFile(oldP, newP,
				EV_SEED_TH, EV_EXCEPTION_TH);
		if (nToNMatchFile.exists()) {
			SetOfPairs nToNLabeledMatches = SetOfPairs
					.readXMLFile(nToNMatchFile.getAbsolutePath());
			if (nToNLabeledMatches != null && otherMatches != null) {
				// evaluate using this matches.
				if (rb != null) {
					pStream.print(oldP.getVersion() + "-" + newP.getVersion());
					rb.print3WayComparisonChart(pStream, otherMatches,
							nToNLabeledMatches);
				}
			}
		}
	}
}

