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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import edu.washington.cs.extractors.ProgramSnapshot;
import edu.washington.cs.extractors.SeedMatchGenerator;
import edu.washington.cs.rules.JavaMethod;
import edu.washington.cs.rules.RuleBase;
import edu.washington.cs.util.ListOfPairs;
import edu.washington.cs.util.Pair;

public class HillClimbingSearch {
	/*
	 * input: arg[0] project input: arg[1] seed_threshold input: arg[2]
	 * exception_threshold input: arg[3] project_list (dir_list)
	 */
	public static void main(String args[]) {
		commandLineExecution(args);
	}
	
	public static void commandLineExecution (String args[]) {
		if (args.length != 4) {
			System.exit(0);
		}
		// (1) set a project
		String project = args[0];
		// (2) set threshold for name matcher
		final double SEED_TH = new Double(args[1]).doubleValue();
		// (3) set a seed selection value
		final double EXCEPTION_TH = new Double(args[2]).doubleValue();
		String dirList = args[3];
		batchRuleGeneration(false, project, SEED_TH,EXCEPTION_TH,dirList );
	}
	public static void batchRuleGeneration(boolean onlyOne, String project,
			double SEED_TH, double EXCEPTION_TH, String dirList) {

		File[] dirs = ReadDirectories.getDirectories(dirList);
		int loopend = 1;
		if (onlyOne == false)
			loopend = dirs.length - 1;
		for (int i = 0; i < loopend; i++) {
			ProgramSnapshot oldP = new ProgramSnapshot(project, dirs[i]);
			ProgramSnapshot newP = new ProgramSnapshot(project, dirs[i + 1]);
			batchRuleGenerationTwoVersion(oldP, newP, SEED_TH, EXCEPTION_TH);
		}
	}
	private static void batchRuleGenerationTwoVersion(ProgramSnapshot oldP,
			ProgramSnapshot newP, double SEED_TH, double EXCEPTION_TH) {
		HillClimbingSearch algorithm = new HillClimbingSearch();

		boolean forward = true;
		algorithm.createRuleBase(true, oldP, newP, forward, SEED_TH, EXCEPTION_TH);
		// backward search
		forward = false;
		algorithm.createRuleBase(true, oldP, newP, forward, SEED_TH, EXCEPTION_TH);
	}
	/**
	 * 
	 * @param oldP
	 * @param newP
	 * @param forward
	 * @param SEED_TH
	 * @param EXCEPTION_TH
	 * @return
	 */
	public RuleBase createRuleBase(boolean refresh,ProgramSnapshot oldP, ProgramSnapshot newP,
			boolean forward, double SEED_TH, double EXCEPTION_TH) {
		
		// generate seeds
		SeedMatchGenerator generator = new SeedMatchGenerator();
		
		// set left and right
		ProgramSnapshot left =null;
		ProgramSnapshot right=null;
		if (forward) { 
			left = oldP;
			right = newP;
		}else {
			left = newP;
			right = oldP;
		}
		ListOfPairs seeds = generator.generateSeed(left, right, refresh, SEED_TH);
		File ruleOutputFile = FileNameService.getRuleXMLFile(left,right,SEED_TH,EXCEPTION_TH);
		ArrayList<JavaMethod> domain =left.minus(right);
		ArrayList<JavaMethod> codomain =right.getMethods();
		return hillClimbingSearch(domain,
				codomain, seeds, ruleOutputFile, EXCEPTION_TH);
	} 
	/**
	 * 
	 * @param domain
	 * @param codomain
	 * @param seedMatches
	 * @param rulebaseOutputFile
	 * @return
	 */
	public RuleBase hillClimbingSearch(ArrayList<JavaMethod> domain,
			ArrayList<JavaMethod> codomain, ListOfPairs seedMatches,
			File rulebaseOutputFile, double EXCEPTION_THRESHOLD) {
		
		System.out.println("RuleFile:\t"+rulebaseOutputFile.getAbsolutePath());
		Date start = new java.util.Date();
		System.out.println("Start"+start.toLocaleString());
		System.out.println("Initial Rulebase Construction");
		RuleBase rb = new RuleBase(domain, codomain, seedMatches, EXCEPTION_THRESHOLD);
		boolean cont = true;
		int matches = rb.getAcceptedMatches().size();
		int count = 1;
		while (cont) {
//			debugPrint(count + "Try");
			count++;
			rb.acceptTheBestRule(true);
	
			int newmatches = rb.getAcceptedMatches().size();
			if (newmatches > matches) {
				matches = newmatches;
			} else {
				cont = false;
			}
		}
		debugPrint("Rule:\t"+rb.numAcceptedRule()+"\tMatch:"+rb.getAcceptedMatches().size());
		
		int countAcceptedMinusSeed = 0;
		for (Iterator<Pair> pairIt = rb.getAcceptedMatches().iterator(); pairIt
				.hasNext();) {
			Pair accepted = pairIt.next();
			if (!seedMatches.contains(accepted)) {
				countAcceptedMinusSeed++;
			}
		}
		debugPrint("seed > accepted (accepted but not in the seed):\t"
				+ countAcceptedMinusSeed);
		int countSeedMinusAccepted = 0;
		for (Iterator<Pair> pairIt = seedMatches.iterator(); pairIt.hasNext();) {
			Pair seed = pairIt.next();
			if (!rb.getAcceptedMatches().contains(seed)) {
				if (!rb.getAcceptedMatches().getLeftDomain().contains(seed.getLeft()))
				System.out.println("seed left not accepted " + seed);
				countSeedMinusAccepted++;
			}
		}
		debugPrint("accepted > seed (seed but not accepted):\t" + countSeedMinusAccepted);
		Date end = new Date();
		System.out.println(end.toLocaleString());
		// remember stat information.
		rb.setRunningTimeInMillis(end.getTime()-start.getTime());
		rb.setSizeAcceptedMinusSeed(countAcceptedMinusSeed);
		rb.setSizeSeedMinusAccepted(countSeedMinusAccepted);
		rb.setSizeCodomain(codomain.size());
		rb.setSizeDomain(domain.size());
		rb.setSizeSeed(seedMatches.size());
		rb.setSizeSeedLeft(seedMatches.numDistinctLeft());
		if (rulebaseOutputFile!=null){ 
			rb.writeXMLFile(rulebaseOutputFile.getAbsolutePath());
		}
		return rb;
	}

	public static void debugPrint(String s) {
		System.out.println(s);
	}
	
}
