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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import edu.washington.cs.comparison.LCS;
import edu.washington.cs.comparison.Tokenize;
import edu.washington.cs.induction.FileNameService;
import edu.washington.cs.rules.JavaMethod;
import edu.washington.cs.util.ListOfPairs;
import edu.washington.cs.util.Pair;
import edu.washington.cs.util.SetOfPairs;

public class SeedMatchGenerator {

	public static boolean earlyExit =false;
	public static boolean seedXS=false;
	
	private static long runningTime =0;
	public ListOfPairs generateSeed(ProgramSnapshot left, ProgramSnapshot right,  
						boolean refresh, double TH) {
		File seedFile = FileNameService.getSeedXMLFile(left, right, TH);
		System.out.println();
		if (refresh == true || !seedFile.exists()) {
			// write it to a file
			ArrayList<JavaMethod> domain = left.minus(right);
			ArrayList<JavaMethod> codomain = right.minus(left);
			System.out.println("left.size" + left.getMethods().size());
			System.out.println("right.size" + right.getMethods().size());
			System.out.println("domain size:" + domain.size());
			System.out.println("codomain size:" + codomain.size());
			// set the domain and co-domain
			System.out.println("Writing a seed file: "
					+ seedFile.getAbsolutePath());
			Date start = new Date();
			if (seedXS) {
				
				File xsSeeds = FileNameService.getXSMatchFile(
						left.getProject(), left.getVersion(), right
								.getVersion());

				if (xsSeeds.exists()) {
					SetOfPairs seeds = SetOfPairs.readXMLFile(xsSeeds.getAbsolutePath());
					ListOfPairs result = new ListOfPairs(); 
					result.addSetOfPairs(seeds);
					return result;
					
				}else {
					xsSeeds = FileNameService.getXSMatchFile(left.getProject(),
							right.getVersion(), left.getVersion());
					if (xsSeeds.exists()) {
						SetOfPairs seeds = SetOfPairs.readXMLFile(xsSeeds.getAbsolutePath());
						ListOfPairs result = new ListOfPairs(); 
						result.addSetOfPairs(seeds);
						result = result.inverse();
						return result;
						
					}	
				}
			}
			ListOfPairs results = findSeed(domain, codomain, TH);
			Date end = new Date();
			runningTime = end.getTime()-start.getTime();
			try {
				seedFile.createNewFile();
				// write it to a file
				results.writeXMLFile(seedFile.getAbsolutePath());
			}catch (IOException e ){ 
				e.printStackTrace();
			}
			System.out.println("Seed.left.size"+results.numDistinctLeft());
			
			return results;
		}else if (seedFile.exists()){
			System.out.println("Reading from a seed file"+seedFile.getAbsolutePath());
			// read from existing seed file
			ListOfPairs results = ListOfPairs.readXMLFile(seedFile.getAbsolutePath());
			System.out.println("Seed.left.size"+results.numDistinctLeft());
			return results;
		}
		return null;
	}

	private ListOfPairs findSeed(ArrayList<JavaMethod> domain,
			ArrayList<JavaMethod> codomain, double TH) {
		// delete this later only for skim comparison 
		int startMinute= (new java.util.Date().getMinutes());
		ListOfPairs results = new ListOfPairs();
		for (int i = 0; i < domain.size(); i++) {
			double best_token = 0;
			JavaMethod domainMethod = domain.get(i);	
			ArrayList<JavaMethod> bestCorresponding_token = new ArrayList<JavaMethod>();
			boolean inserted = false;
			JavaMethod bestIngoreSig = null;
			for (int j = 0; j < codomain.size(); j++) {
				JavaMethod codomainMethod = codomain.get(j);
				double simIgnoreSignature = similarityIgnoreSignature(
						domainMethod, codomainMethod);
			
				double simToken = similarityToken(domainMethod, codomainMethod);
				if (simIgnoreSignature == (double) (1.0)) {
					results.addPair(new Pair<JavaMethod>(domainMethod,
							codomainMethod));
					inserted = true; 
					bestIngoreSig = codomainMethod;
					break;
				}
				if (earlyExit && simToken > TH) {
					// if earlyExit is true and simToken is better than TH. 
					// just add the pair and exit the loop. 
					results.addPair(new Pair<JavaMethod>(domainMethod,
							codomainMethod));
					inserted = true; 
					break;
				}
 				if (best_token < simToken) {
					best_token = simToken;
					bestCorresponding_token.clear(); 
					bestCorresponding_token.add(codomainMethod);
				}else if (best_token==simToken) { 
					bestCorresponding_token.add(codomainMethod);
				}
			}
			if (inserted && earlyExit==false) { 
				codomain.remove(bestIngoreSig);
			}
			if (best_token > TH && inserted ==false) {
				JavaMethod best= null;
				double bestS =0;
				for (int k = 0; k < bestCorresponding_token.size(); k++) {
					Pair pair = new Pair<JavaMethod>(domainMethod,
							bestCorresponding_token.get(k));
					double score = similaritySeqToken(domainMethod,
							bestCorresponding_token.get(k));
					if (score >bestS) {
						best = bestCorresponding_token.get(k);
						bestS = score;
					}
				}

				if (best!=null && bestS>(TH)) {
					results.addPair(new Pair<JavaMethod> (domainMethod, best));
				}
			}
		}
		int endMinute = new java.util.Date().getMinutes();
		System.out.println("Time Performance\t"+(endMinute-startMinute));
		return results;

	}
	private double similarityIgnoreSignature(JavaMethod left, JavaMethod right) { 
		// must be the same package, class, procedure name 
		boolean result = left.getPackageName().equals(right.getPackageName()) &&
				left.getClassName().equals(right.getClassName()) &&
				left.getProcedureName().equals(right.getProcedureName());
		if (result) return (double)1.0;
		return (double)0.0;
	}
	private double similarityToken(JavaMethod left, JavaMethod right){
		String oldPackageNameTokens[] = Tokenize.breakCapitalLetter(left.getPackageName());
		String oldClassNameTokens[] = Tokenize.breakCapitalLetter(left.getClassName());
		String oldProcedureNameTokens[] = Tokenize.breakCapitalLetter(left.getProcedureName());
		String oldSigNameTokens[] = Tokenize.breakCapitalLetter(left.getParameters().toString());
		String oldReturnToken[] = Tokenize.breakCapitalLetter(left.getReturntype());
		
		String newPackageNameTokens[] = Tokenize.breakCapitalLetter(right.getPackageName());
		String newClassNameTokens[] = Tokenize.breakCapitalLetter(right.getClassName());
		String newProcedureNameTokens[] = Tokenize.breakCapitalLetter(right.getProcedureName());
		String newSigNameTokens[] = Tokenize.breakCapitalLetter(right.getParameters().toString());
		String newReturnToken[] = Tokenize.breakCapitalLetter(right.getReturntype());
		
		int sharedPackage = LCS.getNumSharedTokens(oldPackageNameTokens,newPackageNameTokens);
		int sharedClass = LCS.getNumSharedTokens(oldClassNameTokens, newClassNameTokens);
		int sharedProcedure = LCS.getNumSharedTokens(oldProcedureNameTokens, newProcedureNameTokens);
		int sharedSig = LCS.getNumSharedTokens(oldSigNameTokens, newSigNameTokens);
		int sharedReturn = LCS.getNumSharedTokens(oldReturnToken, newReturnToken);

		double pack = (double) (sharedPackage * 10)
				/ (double) Math.max(oldPackageNameTokens.length,
						newPackageNameTokens.length);
		double cls = (double) (sharedClass * 30)
				/ Math
						.max(oldClassNameTokens.length,
								newClassNameTokens.length);
		double pr = (double) (sharedProcedure * 30)
				/ Math.max(oldProcedureNameTokens.length,
						newProcedureNameTokens.length);
		double sig = (double) (sharedSig * 20)
				/ Math.max(oldSigNameTokens.length, newSigNameTokens.length);
		double ret = (double) (sharedReturn * 10)
				/ Math.max(oldReturnToken.length, newReturnToken.length);
		return ((double) pack + cls + pr + sig + ret) / (double) (100);
	}
	private double similaritySeqToken(JavaMethod left, JavaMethod right) {
		String leftS = left.getPackageName()+" "+left.getClassName()+" "+left.getProcedureName()+" "
		+left.getReturntype();
		for (int i= 0 ; i<left.getParameters().size(); i++) {
			leftS = leftS +" "+left.getParameters().get(i);
		}
		String rightS = right.getPackageName()+" "+right.getClassName()+" "+right.getProcedureName()+" "
		+right.getReturntype();
		for (int i= 0 ; i<right.getParameters().size(); i++) {
			rightS = rightS +" "+right.getParameters().get(i);
		}
		String[] leftTokens = Tokenize.breakCapitalLetter(leftS);
		String[] rightTokens = Tokenize.breakCapitalLetter(rightS);
		int numShared = LCS.getNumSharedTokens(leftTokens, rightTokens);
		int max = Math.max(leftTokens.length, rightTokens.length);
		
		return (double)(numShared)/(double)(max); 
		
	}
	private double similarityCharacter(JavaMethod left, JavaMethod right) {
		String leftChar = left.toString();
		String rightChar = right.toString();
		double score =(double) LCS.getNumSharedChar(leftChar, rightChar);
		double max = (double) Math.max(leftChar.length(), rightChar.length());
		return (score) / max;
	}
	public static void main (String args[]) { 
		batchPreparation("jfreechart_list","jfreechart");
//		batchPreparation("jhotdraw_list","jhotdraw");
//		batchPreparation("jedit_list","jedit");
	}
	public static void batchPreparation (String dirList, String project) { 
		File[] dirs = ReadDirectories.getDirectories(dirList);
		for (int i = 0; i < dirs.length-1; i++) {
			ProgramSnapshot oldP = new ProgramSnapshot(project,dirs[i]);
			ProgramSnapshot newP = new ProgramSnapshot(project,dirs[i+1]);
			SeedMatchGenerator generator = new SeedMatchGenerator();
			ListOfPairs seeds = generator.generateSeed(oldP, newP, false, 0.7);
			System.out.println("Seed.left.size"+seeds.numDistinctLeft());
			System.out.println("Seed.size" +seeds.size());
		}
	}

	private static void compare(String dirList, String project) {
		File[] dirs = ReadDirectories.getDirectories(dirList);

		for (int i = 0; i < dirs.length - 1; i++) {
			ProgramSnapshot oldP = new ProgramSnapshot(project, dirs[i]);
			ProgramSnapshot newP = new ProgramSnapshot(project, dirs[i + 1]);

			SeedMatchGenerator generator = new SeedMatchGenerator();

			// File oldSFile = new
			// File("c://MatchingResult/jfreechart/0.9.4-0.9.50.7.nameApproximate");
			// ListOfPairs oldSeeds = new ListOfPairs(oldSFile);
			File oldSFile = new File(
					"e://workspace//edu.washington.cs.likelychangerule.data//seeds//jfreechart//DifferentSeeds//Seed0.9.4-0.9.5_0.7.xml_commit_no0.05");
			ListOfPairs newSeeds = ListOfPairs.readXMLFile(oldSFile
					.getAbsolutePath());
			ListOfPairs oldSeeds = generator.generateSeed(oldP, newP, false,
					0.7);

			newSeeds.sort();
			System.out.println(newSeeds.size());
			System.out.println(newSeeds.numDistinctLeft());
			System.out.println(oldSeeds.size());
			int diff = 0;
			for (int j = 0; j < newSeeds.size(); j++) {
				Pair<JavaMethod> nS = newSeeds.get(j);
				Pair<JavaMethod> oS = oldSeeds.getByLeft(nS.getLeft());
				if (oS == null || !nS.getRight().equals(oS.getRight())) {
					diff++;
					System.out.println("\nnew\t"
							+ nS.getLeft()
							+ ""
							+ "\n\t"
							+ nS.getRight()
							+ "\n\t"
							+ generator.similarityToken(nS.getLeft(), nS
									.getRight())
							+ "\t"
							+ generator.similaritySeqToken(nS.getLeft(), nS
									.getRight()));

					System.out.print("old\t");
					if (oS != null) {
						System.out.println(oS.getRight()
								+ "\n\t"
								+ generator.similarityToken(oS.getLeft(), oS
										.getRight())
								+ "\t"
								+ generator.similaritySeqToken(oS.getLeft(), oS
										.getRight()));
					} else {
						System.out.println();
					}
				}
			}
			System.out.println("Difference" + diff);

			// generator.generateSeed(oldP,newP,true,false,0.7);
		}

	}

	public static long getRunningTime() {
		return runningTime;
	}

}
