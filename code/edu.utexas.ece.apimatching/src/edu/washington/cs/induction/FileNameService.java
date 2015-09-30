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
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import edu.washington.cs.extractors.ProgramSnapshot;
import edu.washington.cs.util.SetOfPairs;

public class FileNameService {

	public static String specialSuffix = "";

	private static final boolean isWindowsSystem = false; 
	
	private static final boolean toWorkOnTSE = true; 
//	private static final String matchingResultRoot = "e:\\workspace\\edu.washington.cs.likelychangerule.data";

//	private static final String matchingResultRoot = "X:\\LikelyChangeRule\\ChangeRule\\";
//	public static final String METHOD_DECLARATION_SUFFIX = ".declartion";
//	public static final File DECL_OUTDIR = new File("X:\\LogicalStructuralDelta\\ChangeRule\\declaration");;

	
	private static String matchingResultRoot;
	public static String declarationRoot;  
	private static String tablePath; 
	private static String rulePath; 
	private static String seedPath; 
	private static String xsPath;
	private static String sKimPath;
	private static String wdPath; 
	private static String matchingPath; 
	private static String evalPath; 
	static {
		if (isWindowsSystem) { 
			matchingResultRoot = "X:\\LikelyChangeRule\\LCR-Data-CameraReadySubmit";
			declarationRoot = "X:\\LogicalStructuralDelta\\ChangeRule\\declaration";
			tablePath = matchingResultRoot + "\\tables";
			rulePath = matchingResultRoot + "\\rules";
			seedPath = matchingResultRoot + "\\seeds";
			xsPath = matchingResultRoot + "\\XS";
			sKimPath = matchingResultRoot + "\\SKim";
			wdPath = matchingResultRoot + "\\WD";
			matchingPath = matchingResultRoot + "\\matching";
			evalPath = matchingResultRoot + "\\evaluation";

		} else {
			if (toWorkOnTSE) {
				matchingResultRoot = "/Volumes/gorillaHD2/Xdrive/LikelyChangeRule/LCR-Data-CameraReadySubmit";
				declarationRoot = matchingResultRoot + "/declaration";
				tablePath = matchingResultRoot + "/tables";
				rulePath = matchingResultRoot + "/rules";
				seedPath = matchingResultRoot + "/seeds";
				xsPath = matchingResultRoot + "/XS";
				sKimPath = matchingResultRoot + "/SKim";
				wdPath = matchingResultRoot + "/WD";
				matchingPath = matchingResultRoot + "/matching";
				evalPath = matchingResultRoot + "/evaluation";

			} else {
				matchingResultRoot = "/Volumes/gorillaHD2/APIChangeRule/";
				declarationRoot = matchingResultRoot + "/declaration";
				tablePath = matchingResultRoot + "/tables";
				rulePath = matchingResultRoot + "/rules";
				seedPath = matchingResultRoot + "/seeds";
				xsPath = matchingResultRoot + "/XS";
				sKimPath = matchingResultRoot + "/SKim";
				wdPath = matchingResultRoot + "/WD";
				matchingPath = matchingResultRoot + "/matching";
				evalPath = matchingResultRoot +"/evaluation";
			}
		}
	}
	
	
	private static final String ruleFileXMLSuffix = "rule.xml";

	private static final String matchingFileXMLSuffix = "matching.xml";

	private static final String seedFilePrefix = "Seed";

	private static final String seedFileSuffix = ".xml";

	private static final String tableSuffix = ".table";

	private static final String threeWaySuffix = ".3way";
	
	private static final String cumulativeSuffix = ".cdf";
	
	private static final String refactoringFileSuffix = "-refactorings.xml";
	
	public static final String declarationFileSuffix = ".declaration";

	private static final String NtoNSuffix = ".NtoN";
	
	private static final String LABELLED = "Labeled";

	public static File getRuleXMLFile(ProgramSnapshot left,
			ProgramSnapshot right, double SEED_TH, double EXCEPTION_TH) {
		File RULE_DIR = new File(rulePath);
		if (!RULE_DIR.exists())
			RULE_DIR.mkdir();
		// set rule result directory
		File ruleResultDir = new File(RULE_DIR, left.getProject());
		if (!ruleResultDir.exists())
			ruleResultDir.mkdir();
		File ruleOutputFile = new File(ruleResultDir, left.getVersion() + "-"
				+ right.getVersion() + SEED_TH + "_" + EXCEPTION_TH
				+ specialSuffix+ ruleFileXMLSuffix);
		return ruleOutputFile;
	}

	public static File getMatchingXMLFile(ProgramSnapshot left,
			ProgramSnapshot right, double SEED_TH, double EXCEPTION_TH) {
		return getMatchingXMLFile(left.getProject(), left.getVersion(), right.getVersion(), SEED_TH, EXCEPTION_TH);
	}
	
	public static File getMatchingXMLFile(String project, String oldV, String newV, double SEED_TH, double EXCEPTION_TH) { 
		File MATCHING_DIR = new File(matchingPath);
		if (MATCHING_DIR.exists())
			MATCHING_DIR.mkdir();

		File matchingResultDir = new File(MATCHING_DIR, project);
		if (!matchingResultDir.exists())
			matchingResultDir.mkdir();
		File matchingOutputFile = new File(matchingResultDir, oldV
				+ "-" + newV + SEED_TH + "_" + EXCEPTION_TH
				+ specialSuffix + matchingFileXMLSuffix);
		return matchingOutputFile;

	}
	public static File getLabeledMatchingXMLFile(ProgramSnapshot left,
			ProgramSnapshot right, double SEED_TH, double EXCEPTION_TH) {
		File MATCHING_DIR = new File(matchingPath);
		if (MATCHING_DIR.exists())
			MATCHING_DIR.mkdir();

		File matchingResultDir = new File(MATCHING_DIR, left.getProject());
		if (!matchingResultDir.exists())
			matchingResultDir.mkdir();
		File matchingOutputFile = new File(matchingResultDir, left.getVersion()
				+ "-" + right.getVersion() + SEED_TH + "_" + EXCEPTION_TH
				+ LABELLED + matchingFileXMLSuffix);
		return matchingOutputFile;
	}

	public static File getMatchingGroundTruthFile(ProgramSnapshot left,
			ProgramSnapshot right) {
		return null;
	}

	public static File getSeedXMLFile(ProgramSnapshot left,
			ProgramSnapshot right, double SEED_TH) {
		File seed = new File(seedPath);
		if (!seed.exists()) seed.mkdir();
		File SEED_DIR = new File(seedPath,left.getProject());
		if (!SEED_DIR.exists())
			SEED_DIR.mkdir();
		// set the domain and co-domain
		File seedFile = new File(SEED_DIR.getAbsolutePath(), seedFilePrefix + left.getVersion()
				+ "-" + right.getVersion() + "_" + SEED_TH + specialSuffix+seedFileSuffix);
		return seedFile;
	}

	private class RuleMatchFileFilter extends FileFilter {

		public boolean accept(File f) {
			// TODO Auto-generated method stub
			if (f.isDirectory())
				return true;
			return (f.getAbsolutePath().indexOf(matchingFileXMLSuffix) >= 0);
		}

		@Override
		public String getDescription() {
			// TODO Auto-generated method stub
			return ".xml";
		}
	}
	private class NtoNFileFilter extends FileFilter {

		public boolean accept(File f) {
			// TODO Auto-generated method stub
			if (f.isDirectory())
				return true;
			return (f.getAbsolutePath().indexOf(NtoNSuffix) >= 0);
		}

		@Override
		public String getDescription() {
			// TODO Auto-generated method stub
			return ".xml";
		}
	}
	public static JFileChooser openMatchingFileDialog() {
		FileFilter filter = new FileNameService().new RuleMatchFileFilter();
		File MATCHING_DIR = new File(matchingPath);
		JFileChooser chooser = new JFileChooser(MATCHING_DIR);
		chooser.setFileFilter(filter);
		return chooser;
	}
	public static JFileChooser openNtoNLabelledDialog() {
		FileFilter filter = new FileNameService().new NtoNFileFilter();
		File EVAL_DIR = new File(evalPath);
		JFileChooser chooser = new JFileChooser(EVAL_DIR);
		chooser.setFileFilter(filter);
		return chooser;
	}

	

	public static File getEvalFile(ProgramSnapshot oldP, ProgramSnapshot newP,
			double SEED_TH, double EXCEPTION_TH) {
		File evalFile = new File("c:\\MatchingResult\\" + oldP.getProject()
				+ "\\" + oldP.getVersion() + "-" + newP.getVersion()
				+ "rulematch-eval.xml");
		return evalFile;
	}

	public static File getTableFile(String project, double SEED_TH,
			double EXCEPTION_TH) {
		File TABLE_DIR = new File(tablePath);
		if (!TABLE_DIR.exists())
			TABLE_DIR.mkdir();
		return new File(TABLE_DIR, project + "-" + SEED_TH + "_" + EXCEPTION_TH
				+ specialSuffix + tableSuffix);
	}

	public static File get3WayEvalFile(String project, double SEED_TH,
			double EXCEPTION_TH) {
		File TABLE_DIR = new File(tablePath);
		if (!TABLE_DIR.exists())
			TABLE_DIR.mkdir();
		return new File(TABLE_DIR, project + "-" + SEED_TH + "_" + EXCEPTION_TH
				+ specialSuffix + threeWaySuffix);
	}
	public static File getSeedVaryingTableFile(String project, String version,
			double SEED_TH, double EXCEPTION_TH) {
		File TABLE_DIR = new File(tablePath);
		if (!TABLE_DIR.exists())
			TABLE_DIR.mkdir();
		return new File(TABLE_DIR, project + "V" + version + "s"+ SEED_TH+ "e"+ EXCEPTION_TH
				+ tableSuffix);
	}
	public static File getSeedVarying3WayEvalFile(String project, String version, 
			double SEED_TH, double EXCEPTION_TH) { 
		File TABLE_DIR = new File(tablePath);
		if (!TABLE_DIR.exists())
			TABLE_DIR.mkdir();
		return new File(TABLE_DIR, project + "V" + version + "s"+ SEED_TH+"e"+EXCEPTION_TH
				+ threeWaySuffix);
	}
	
	public static File get(String project, String version,
			double EXCEPTION_TH) {
		File TABLE_DIR = new File(tablePath);
		if (!TABLE_DIR.exists())
			TABLE_DIR.mkdir();
		return new File(TABLE_DIR, project + "V" + version + EXCEPTION_TH
				+ tableSuffix);
	}
	public static File getSKimLogFile (double SEED_TH, double EXCEPTION_TH) { 	
		return new File(sKimPath, "skim" + "-" + SEED_TH + "_" + EXCEPTION_TH
				+ specialSuffix+".log");
	}
	public static File getSKimStillToGoFile(String project, double SEED_TH, double EXCEPTION_TH) {

		return new File(sKimPath, project + "-" + SEED_TH + "_" + EXCEPTION_TH
				+ specialSuffix + ".stillToGo");
	}
	public static File getWDLogFile (double SEED_TH, double EXCEPTION_TH) { 
		return new File(wdPath, "wd" + "-" + SEED_TH + "_" + EXCEPTION_TH
				+ specialSuffix+".log");
	}

	public static File getWDStillToGoFile(String project, String kind, double SEED_TH, double EXCEPTION_TH) {
		return new File(wdPath, project + kind+"-" + SEED_TH + "_" + EXCEPTION_TH
				+ specialSuffix+".stillToGo");
	}
	
	
	public static File getXingStrouliaComparison(String project, double SEED_TH, double EXCEPTION_TH) {
		File XING_DIR = new File (xsPath, project);
		if (!XING_DIR.exists()) {
			XING_DIR.mkdir();
		}
		return new File(XING_DIR, "comparison"+"-"+SEED_TH+"_"+EXCEPTION_TH +tableSuffix);
	}
	public static File getXSMatchFile(String project, String oldVersion, String newVersion) {
		File XING_DIR = new File (xsPath, project);
		if (!XING_DIR.exists()) {
			XING_DIR.mkdir();
		}
		return new File(XING_DIR, "XSmatch"+"-"+oldVersion+"-"+newVersion +".xml");
	}
	public static File getSKimMatchFile (String project, int transactionid) { 
		File skimDir = new File(sKimPath, project);
		if (!skimDir.exists()) 
			skimDir.mkdir();
		File skMatch = new File(skimDir, "SKmatch" + transactionid + ".xml");
		return skMatch;
	}
	public static File getWDMatch_All_File (String project, String transactionid) { 
		File wdDir = new File(wdPath, project);
		if (!wdDir.exists()) 
			wdDir.mkdir();
		File wdMatch = new File(wdDir, "WDmatchAll" + transactionid + ".xml");
		return wdMatch;
	}
	public static File getWDMatch_Best_File (String project, String transactionid) { 
		File wdDir = new File(wdPath,project);
		if (!wdDir.exists()) 
			wdDir.mkdir();
		File wdMatch = new File(wdDir, "WDmatchBest" + transactionid + ".xml");
		return wdMatch;
	}
	public static File getSKimTransactionFile (String project, int transactionid) {
		File skimDir = new File(sKimPath, project+"_transaction");
		if (!skimDir.exists()) 
			skimDir.mkdir();
		File transactionFile = new File(skimDir, "transaction" + transactionid + "-"
				+ (transactionid+1) + ".xml");
		return transactionFile;
	}
	public static File getWDRefactoringFile (String project) { 
		File wdRefactoringFile = new File(wdPath, project+refactoringFileSuffix);
		return wdRefactoringFile;
	}
	public static File getNtoNEvalFile(ProgramSnapshot oldP, ProgramSnapshot newP,
			double SEED_TH, double EXCEPTION_TH) {
		return getNtoNEvalFile(oldP.getProject(), oldP.getVersion(), newP.getVersion());
	}
	private static File getNtoNEvalFile(String project, String oldV, String newV) {
		File EVAL_DIR = new File(evalPath,project);
		if (!EVAL_DIR.exists()) {
			EVAL_DIR.mkdir();
		}
		File evalFile = new File(EVAL_DIR, oldV + "-" + newV + NtoNSuffix
				+ ".xml");
		return evalFile;
		
	}
	private static File getCumulativeFile(String project, String oldV, String newV) {
		File EVAL_DIR = new File(evalPath,  project);
		if (!EVAL_DIR.exists()) {
			EVAL_DIR.mkdir();
		}
		File evalFile = new File(EVAL_DIR, oldV + "-" + newV + cumulativeSuffix);
		return evalFile;
	}
	
	public static void main(String args[]) {
		cumulativeDistribution("jfreechart", "0.9.8", "0.9.9");
		cumulativeDistribution("jhotdraw", "5.3", "5.4b1");
		cumulativeDistribution("jedit", "4.0", "4.1");
	}

	private static void cumulativeDistribution(String project, String oldV,
			String newV) {
		PrintStream p =System.out;
		File cdf = FileNameService.getCumulativeFile(project, oldV, newV);
		try {
			if (!cdf.exists()) {
				cdf.createNewFile();
				FileOutputStream oStream = new FileOutputStream(cdf);
				p = new PrintStream(oStream);
			}
		} catch (IOException e) {
		}
		
		File nN = FileNameService.getNtoNEvalFile(project, oldV, newV);
		if (!nN.exists())
			return;
		SetOfPairs nToNMatches = SetOfPairs.readXMLFile(nN.getAbsolutePath());

		File RB = FileNameService.getMatchingXMLFile(project, oldV, newV, 0.7,
				0.34);
		if (!RB.exists())
			return;
		RulebasedMatching matching = RulebasedMatching.readXMLFile(RB
				.getAbsolutePath());
		int maxCeling = matching.getDomain(true).size();
		double[][] evalResult = matching.getPrecisionRecallRatio(true,
				nToNMatches, maxCeling);

		p.println("fRule\tfMatch\tfPrecision\tfYield\tfRecall\tfRuleRatio");
		for (int i = 0; i < evalResult.length; i++) {
			p.println(evalResult[i][0] + "\t" + evalResult[i][1] + "\t"
					+ evalResult[i][2] + "\t" + evalResult[i][3] + "\t"
					+ evalResult[i][4] + "\t" + evalResult[i][5]);
		}

	}
	public static String getSKimSourcePath (int oldRevisionId){ 
		File file = new File("c:\\OriginAnalysisData\\jedit_checkouts\\revision_"+oldRevisionId);
		if (file.exists()) return file.getAbsolutePath();
		return null;
	}
	
}
