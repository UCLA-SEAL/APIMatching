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
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Date;

import edu.washington.cs.extractors.ProgramSnapshot;
import edu.washington.cs.extractors.SeedMatchGenerator;
import edu.washington.cs.others.ThreeWayComparison;
import edu.washington.cs.rules.RuleBase;
import edu.washington.cs.util.SetOfPairs;

public class OnePipeLineScript {
		public static void main(String args[]) {
		// varying seed threshold
		// running on the Jan 16th, 2007 9PM
		// seedVaryingPipeLineScript(false, "jfreechart", "jfreechart_list_CGE",
		// true);
		// varying seed quality
		// seedQualityVaryingPipeLineScript();
		// exceptionThresholdVaryingScript();
		// projectVaryingScript();
//		 xingStrouliaComparison();
		
			// change commented out on Dec, 21st, 2009 
//		runCarolCheckin();
//		runDnsjava();
//		runCarol();
//		runJunit();
//        runDerby();
//		runColumba();	
//		runJdtCore();
//		runHadoop();
		runJfreechartSeedthreshold();
	}
		
	private static void runJfreechartSeedthreshold() { 
        seedThresholdVaryingPipeLineScript(false,
				"jfreechart", "jfreechart_list_CGE", false);
	}
	private static void runUwar_io() { 
		pipeLineScript(true, false, "uwar.io", 0.7, 0.34, "uwar.io_list", false, ThreeWayComparison.XING);
	}

	private static void runDnsjava() { 
		pipeLineScript(false, false, "dnsjava", 0.7, 0.34, "dnsjava_list", false, ThreeWayComparison.XING);
	}
	private static void runCarolCheckin() { 
		pipeLineScript(true, false, "carol", 0.7, 0.34, "carol_checkin_list", false, ThreeWayComparison.XING);
	}
	private static void runCarol() { 
		pipeLineScript(true, false, "carol", 0.7, 0.34, "carol_list", false, ThreeWayComparison.XING);
	}
	private static void runJunit() { 
		pipeLineScript(true, false, "junit", 0.7, 0.34, "junit_list", false, ThreeWayComparison.XING);
	}
	
	private static void runHadoop() { 
		pipeLineScript(true, false, "hadoop", 0.7, 0.34, "hadoop_list_sample", false, ThreeWayComparison.NO_COMPARISON);
	}
	
	
	private static void runDerby() { 
		pipeLineScript(true, false, "derby", 0.7, 0.34, "derby_list", false, ThreeWayComparison.NO_COMPARISON);
		
	}
	private static void runColumba() { 
		pipeLineScript(true, false, "columba", 0.7, 0.34, "columba_list", false, ThreeWayComparison.NO_COMPARISON);
	}
	
	private static void runJdtCore() { 
		boolean refresh = true; boolean onlyOne =false;
		String project="jdt"; double SEED_TH=0.7; double EXCEPTION_TH=0.34;
		int kind = ThreeWayComparison.NO_COMPARISON;
		boolean threeWayEval=false; 
		PrintStream printStream = null;try {
				if (!onlyOne) {
					File tableFile = new File ("jdt.table.5000to10000");
					tableFile.createNewFile();
					FileOutputStream o = new FileOutputStream(tableFile);
					printStream = new PrintStream(o);
				} else {
					printStream = System.out;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		if (threeWayEval) { 
			RulebasedMatching.printComparisonChartRow(printStream);
		}else { 
			pipeLineHeader(printStream);
		}
		// version checked out until 15365. 
		
		for (int i = 5000; i < 10000; i++) {
			File oldD = new File ("/Volumes/gorillaHD2/JdtCore_Source/jdt_core"+i);
			File newD = new File ("/Volumes/gorillaHD2/JdtCore_Source/jdt_core"+(i+1));
		
			ProgramSnapshot oldP = new ProgramSnapshot(project, oldD);
			ProgramSnapshot newP = new ProgramSnapshot(project, newD);

			if (!threeWayEval) {
				// regular running pipe line
				pipeLineTwoVersion(refresh, project, SEED_TH, EXCEPTION_TH,
						oldP, newP, true, printStream);
				pipeLineTwoVersion(refresh, project, SEED_TH, EXCEPTION_TH,
						oldP, newP, false, printStream);
			} else {
				// threeway evaluation 
				File matchingFile = FileNameService.getMatchingXMLFile(oldP,
						newP, SEED_TH, EXCEPTION_TH);
				if (!matchingFile.exists())
					continue;
				RulebasedMatching rb = RulebasedMatching
						.readXMLFile(matchingFile.getAbsolutePath());
				ThreeWayComparison.compHelper(printStream, oldP, newP, SEED_TH,
						EXCEPTION_TH, kind, rb, SEED_TH);
			}
		}
	}
	private static void projectVaryingScript() { 
		// varying projects
		pipeLineScript(false, false, "jhotdraw", 0.7, 0.34, "jhotdraw_list",
				false, ThreeWayComparison.XING);
		pipeLineScript(false, false, "jedit", 0.7, 0.34, "jedit_list",
				false, ThreeWayComparison.XING);
	}
	private static void xingStrouliaComparison() {
		pipeLineScript(false, false, "jfreechart", 0.7, 0.34,
				"jfreechart_list_CGE", true, ThreeWayComparison.XING);
		
		
	}
	private static void exceptionThresholdVaryingScript() { 
		pipeLineScript(false, false, "jfreechart", 0.7, 0.25,
		"jfreechart_list_CGE", false, ThreeWayComparison.XING);
		
		pipeLineScript(false, false, "jfreechart", 0.7, 0.34,
				"jfreechart_list_CGE", false, ThreeWayComparison.XING);
		
		pipeLineScript(false, false, "jfreechart", 0.7, 0.5,
		"jfreechart_list_CGE", false, ThreeWayComparison.XING);
	}
	private static void seedQualityVaryingPipeLineScript() {
		pipeLineScript(false, false, "jfreechart", 0.7, 0.34,
				"jfreechart_list_CGE", true, ThreeWayComparison.SEED);
		// currently evaluated based on "XS matches"
		// early exit seed strategy

		FileNameService.specialSuffix = "earlyExit";
		SeedMatchGenerator.earlyExit = true;
		pipeLineScript(false, false, "jfreechart", 0.7, 0.34,
				"jfreechart_list_CGE", true, ThreeWayComparison.SEED);

		System.out.println("End of early exit strategy");
		FileNameService.specialSuffix = "seedXs";
		SeedMatchGenerator.seedXS = true;
		pipeLineScript(false, false, "jfreechart", 0.7, 0.34,
				"jfreechart_list_CGE", true, ThreeWayComparison.SEED);
	}

	private static void pipeLineScript(boolean refresh, boolean onlyOne,
			String project, double SEED_TH, double EXCEPTION_TH,
			String dirList, boolean threeWayEval, int kind) {
		File tableFile = null;
		if (threeWayEval) {
			// three way evaluation
			tableFile = FileNameService.get3WayEvalFile(project, SEED_TH,
					EXCEPTION_TH);
		} else {
			// regular table file 
			tableFile = FileNameService.getTableFile(project, SEED_TH,
					EXCEPTION_TH);
		}
		PrintStream printStream = null;
		if (!tableFile.exists() || onlyOne) {
			try {
				if (!onlyOne) {
					tableFile.createNewFile();
					FileOutputStream o = new FileOutputStream(tableFile);
					printStream = new PrintStream(o);
				} else {
					printStream = System.out;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (threeWayEval) { 
			RulebasedMatching.printComparisonChartRow(printStream);
		}else { 
			pipeLineHeader(printStream);
		}
		File[] dirs = ReadDirectories.getDirectories(dirList);
		int loopend = 1;
		if (onlyOne == false)
			loopend = dirs.length - 1;
		for (int i = 0; i < loopend; i++) {
			ProgramSnapshot oldP = new ProgramSnapshot(project, dirs[i]);
			ProgramSnapshot newP = new ProgramSnapshot(project, dirs[i + 1]);

			if (!threeWayEval) {
				// regular running pipe line
				pipeLineTwoVersion(refresh, project, SEED_TH, EXCEPTION_TH,
						oldP, newP, true, printStream);
				pipeLineTwoVersion(refresh, project, SEED_TH, EXCEPTION_TH,
						oldP, newP, false, printStream);
			} else {
				// threeway evaluation 
				File matchingFile = FileNameService.getMatchingXMLFile(oldP,
						newP, SEED_TH, EXCEPTION_TH);
				if (!matchingFile.exists())
					continue;
				RulebasedMatching rb = RulebasedMatching
						.readXMLFile(matchingFile.getAbsolutePath());
				ThreeWayComparison.compHelper(printStream, oldP, newP, SEED_TH,
						EXCEPTION_TH, kind, rb, SEED_TH);
			}
		}

	}
	

	private static void seedThresholdVaryingPipeLineScript(boolean refresh,
			String project, String dirList, boolean threeWayEval) {
		
		 double seed_th[] = { 0.60, 0.70, 0.80, 0.82, 0.84, 0.86, 0.88,
		 0.90, 0.92, 0.94, 0.96, 0.98 };
		
		// double seed_th[] = { 0.50, 0.60, 0.70, 0.80, 0.82, 0.84, 0.86, 0.88,
		// 0.90,
		// 0.92, 0.94, 0.96, 0.98 };
		double EXCEPTION_TH = 0.34;
		double EV_EXCEPTION_TH = EXCEPTION_TH;
		double EV_SEED_TH = 0.7;
		int kind = ThreeWayComparison.SEED;
		// String version = "0.9.4";
		boolean forward = true;

		File[] dirs = ReadDirectories.getDirectories(dirList);
		int loopend = dirs.length - 1;

		// String path = dirs[i].getAbsolutePath();
		// if (path.indexOf(version) > 0) {
		for (int s = 0; s < seed_th.length; s++) {
			double SEED_TH = seed_th[s];

			PrintStream printStream = null;

			File tableFile = null;
			if (!threeWayEval) {
				// regular running pipeLineTwoVersion Table
				tableFile = FileNameService.getSeedVaryingTableFile(project,
						"ALL", SEED_TH, EXCEPTION_TH);
			} else {
				tableFile = FileNameService.getSeedVarying3WayEvalFile(project,
						"ALL", SEED_TH, EXCEPTION_TH);
			}
			if (!tableFile.exists()) {
				try {
					tableFile.createNewFile();
					FileOutputStream o = new FileOutputStream(tableFile);
					printStream = new PrintStream(o);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				printStream = System.out;
			}

			if (threeWayEval) {
				RulebasedMatching.printComparisonChartRow(printStream);
			} else {
				pipeLineHeader(printStream);
			}
			for (int i = 0; i < loopend; i++) {

				ProgramSnapshot oldP = new ProgramSnapshot(project, dirs[i]);
				ProgramSnapshot newP = new ProgramSnapshot(project, dirs[i + 1]);

				if (!threeWayEval) {
					// regular running pipeLineTwoVersion
					pipeLineTwoVersion(refresh, project, SEED_TH, EXCEPTION_TH,
							oldP, newP, forward, printStream);
				} else {
					// evaluation
					File matchingFile = FileNameService.getMatchingXMLFile(
							oldP, newP, SEED_TH, EXCEPTION_TH);
					if (!matchingFile.exists())
						continue;
					RulebasedMatching rb = RulebasedMatching
							.readXMLFile(matchingFile.getAbsolutePath());
					ThreeWayComparison.compHelper(printStream, oldP, newP,
							EV_SEED_TH, EV_EXCEPTION_TH, kind, rb, SEED_TH);
				}
			}// for
			// }//if
		}// for
	}
	
	public static void getMatchingForLSD(String project, String oldVersion,File oldSourceDir, String newSource, File newSourceDir){
		try { 
		PrintStream p = new PrintStream("temp_table");
		ProgramSnapshot oldP = new ProgramSnapshot(project, oldSourceDir);
		ProgramSnapshot newP = new ProgramSnapshot(project, newSourceDir); 
		
		pipeLineTwoVersion(true, project, 0.7, 0.34, oldP, newP, true, p);
		}catch (Exception e){  
			e.printStackTrace();
		}
	}
	private static void pipeLineTwoVersion(boolean refresh, String project,
			double SEED_TH, double EXCEPTION_TH, ProgramSnapshot oldP,
			ProgramSnapshot newP, boolean forward, PrintStream tableOutput) {
		// Version->Version, Left size, Right Size, Domain Size, CoDomain Size,
		// Seed Size, SeedLeft Size, Seed-Accepted, Accepted-Seed
		//
		// Rule, Match, TranRule, Match, DomainUnmatched, Last Precision, Last
		// Yield, M/R Ratio
		
		// Running Time,
		ProgramSnapshot left=null, right=null;
		if (forward) {
			left = oldP; right = newP;
		}else {
			left = newP; right = oldP;
		}
		tableOutput.print(left.getVersion() + "->" + right.getVersion() + "\t");
		tableOutput.print(left.getMethods().size() + "\t");
		tableOutput.print(right.getMethods().size() + "\t");

		// matching generation
		RulebasedMatching matchingResult = null;
		if (forward) {
			matchingResult = RulebasedMatching
					.batchMatchingGenerationTwoVersion(refresh, oldP, newP,
							SEED_TH, EXCEPTION_TH);
		} else {
			// the reverse direction do make up time 
			matchingResult = RulebasedMatching
					.batchMatchingGenerationTwoVersion(false, oldP, newP,
							SEED_TH, EXCEPTION_TH);
		}
		File rule = FileNameService.getRuleXMLFile(left, right, SEED_TH,
					EXCEPTION_TH);
		RuleBase rb = RuleBase.readXMLFile(rule.getAbsolutePath());
		// matching evaluation
		File nTonMatchFile = FileNameService.getNtoNEvalFile(oldP, newP, 0.7,
				0.34);

		if (nTonMatchFile.exists()) {
			SetOfPairs nTonMatches = SetOfPairs.readXMLFile(nTonMatchFile
					.getAbsolutePath());
			if (nTonMatches != null)
				matchingResult.setPrecYieldRecallRatioUsingSetOfPairs(
						nTonMatches, forward);
		}
		tableOutputHelper(rb, matchingResult, forward, tableOutput);
		tableOutput.print("\n");
	}
	private static void pipeLineHeader(PrintStream tableOutput) {
		tableOutput.print("Version->Version\t");
		tableOutput.print("LeftSize\t");
		tableOutput.print("RightSize\t");
		tableOutput.print("Domain\t");
		tableOutput.print("Codomain\t");
		tableOutput.print("Seed\t");
		tableOutput.print("SeedLeft\t");
		tableOutput.print("Rule\t");
		tableOutput.print("Match\t");
		tableOutput.print("Seed-Accepted\t");
		tableOutput.print("Accepted-Seed\t");
		tableOutput.print("postRule\t");
		tableOutput.print("postMatch\t");
		tableOutput.print("postUnmatched\t");
		tableOutput.print("Precision\t");
		tableOutput.print("Yield\t");
		tableOutput.print("Recall\t");
		tableOutput.print("MRratio\t");
		
		tableOutput.print("SeedTime\t");
		tableOutput.print("InferenceTime\t");
		tableOutput.print("PostTime\t");
		
		tableOutput.print("\n");
	}
	private static void tableOutputHelper (RuleBase rb, RulebasedMatching matching, boolean forward, PrintStream tableOutput) {
		// Rule, Match, TranRule, Match, DomainUnmatched, Last Precision, Last Yield, M/R Ratio
		// rule generation
	
		tableOutput.print(rb.getSizeDomain()+"\t");
		tableOutput.print(rb.getSizeCodomain()+"\t");
		tableOutput.print(rb.getSizeSeed()+"\t");
		tableOutput.print(rb.getSizeSeedLeft()+"\t");
		tableOutput.print(rb.getAcceptedRules().size()+"\t");
		tableOutput.print(rb.getAcceptedMatches().size()+"\t");
		tableOutput.print(rb.getSizeSeedMinusAccepted()+"\t");
		tableOutput.print(rb.getSizeAcceptedMinusSeed()+"\t");
		
		tableOutput.print(matching.numRules(forward)+"\t");
		tableOutput.print(matching.getMatches(forward).size()+"\t");
		tableOutput.print(matching.numUnMatched(forward)+"\t");
		tableOutput.print(matching.getPrecision()+"\t");
		tableOutput.print(matching.getYield()+"\t");
		tableOutput.print(matching.getRecall()+"\t");
		tableOutput.print(matching.getMRratio() + "\t");
		
		Date runningTime = new Date();
		runningTime.setTime(SeedMatchGenerator.getRunningTime());
		tableOutput.print(runningTime.getTime()+"\t");
		runningTime.setTime(rb.getRunningTimeInMillis());
		tableOutput.print(runningTime.getTime()+"\t");
		if (forward) {
			runningTime.setTime(matching.getForwardTimeInMillis());
		} else {
			runningTime.setTime(matching.getBackwardTimeInMillis());
		}
		tableOutput.print(runningTime.getTime() + "\t");
	}
}