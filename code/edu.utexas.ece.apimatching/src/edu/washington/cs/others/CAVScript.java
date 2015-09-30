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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFrame;

import edu.washington.cs.extractors.ProgramSnapshot;
import edu.washington.cs.induction.FileNameService;
import edu.washington.cs.induction.RulebasedMatching;
import edu.washington.cs.util.SetOfPairs;

public class CAVScript {

	public static void main(String args[]) {
	
//		skimScript ("jedit_skim", 0.65, 0.34, true);
		skimScript ("argouml_skim", 0.65, 0.34, false);
//		wdScript("jedit_wd", 0.65, 0.34, false);
//		wdScript("tomcat_wd", 0.65, 0.34,true);
	}

	public static void skimScript(String project, double SEED_TH,
			double EXCEPTION_TH, boolean evaluation) {

		int evalkind = ComparisonAnalysisViewer.SKIM;
		File sk_stt = FileNameService.getSKimStillToGoFile(project, SEED_TH,
				EXCEPTION_TH);
		ThreeWayComp skComp = new ThreeWayComp();
		try {
			FileReader r = new FileReader(sk_stt);
			BufferedReader reader = new BufferedReader(r);
			for (String s = reader.readLine(); s != null; s = reader.readLine()) {
				int trNum = new Integer(s).intValue();

				File input = FileNameService.getSKimTransactionFile(project,
						trNum);
				if (trNum == 0)
					continue;
				SKimTransaction transaction = SKimTransaction.readXMLFile(input
						.getAbsolutePath());
				if (transaction == null)
					continue;
				ProgramSnapshot oldP = transaction
						.getOldProgramSnapshot(project);
				ProgramSnapshot newP = transaction
						.getNewProgramSnapshot(project);

				File matchingXML = FileNameService.getMatchingXMLFile(oldP,
						newP, SEED_TH, EXCEPTION_TH);

				if (matchingXML.exists()) {
					RulebasedMatching matching = RulebasedMatching
							.readXMLFile(matchingXML.getAbsolutePath());

					File nToNMatchXML = FileNameService.getNtoNEvalFile(oldP,
							newP, SEED_TH, EXCEPTION_TH);
					if (!nToNMatchXML.exists()) {
						System.out.println("OldP.V" + oldP.getVersion());
						System.out.println("NewP.V" + newP.getVersion());

						System.out.println("trNum" + trNum);

						ComparisonAnalysisViewer cav = new ComparisonAnalysisViewer(
								matching, oldP, newP, evalkind, SEED_TH,
								EXCEPTION_TH);

						JDialog dialog = new JDialog(new JFrame(),
								"Inspection View" + project + ":" + trNum, true);

						dialog.setLocation(130, 10);
						dialog.setSize(1250, 820);

						dialog.setContentPane(cav);

						dialog.setVisible(true);
						if (nToNMatchXML.exists()) {

							dialog.dispose();

						}
					} else if (nToNMatchXML.exists()) {
						System.out.println("trNum" + trNum);
						File skMatchXML = FileNameService.getSKimMatchFile(oldP
								.getProject(), trNum);
						SetOfPairs skMatches = SetOfPairs
								.readXMLFile(skMatchXML.getAbsolutePath());
						SetOfPairs mine = matching.getMatches(true);
						SetOfPairs nToN = SetOfPairs.readXMLFile(nToNMatchXML
								.getAbsolutePath());
						skComp.update(mine, skMatches, nToN);

					}
					
				}
			}
		} catch (Exception e) {
		}
		PrintStream p = System.out;
		p.print(project+"\t");
		ThreeWayComp.printHeader(p);
		p.print("\t");skComp.print(p);
	}

	public static void wdScript(String project, double SEED_TH,
			double EXCEPTION_TH, boolean evaluate) {
		PrintStream out = System.out;
		File file = FileNameService.getWDRefactoringFile(project);
		WDRefactoringReconstruction reconstruction = WDRefactoringReconstruction
				.readXMLFile(file.getAbsolutePath());
		String RCBest = "RcBest";
		String RCAll = "RcAll";
		File wd_stt_best = FileNameService.getWDStillToGoFile(project, RCBest,
				SEED_TH, EXCEPTION_TH);
		File wd_stt_all = FileNameService.getWDStillToGoFile(project, RCAll,
				SEED_TH, EXCEPTION_TH);
		ArrayList<Integer> trNumbers = new ArrayList<Integer>();

		try {
			FileReader r = new FileReader(wd_stt_best);
			BufferedReader reader = new BufferedReader(r);
			for (String s = reader.readLine(); s != null; s = reader.readLine()) {
				Integer trNum = new Integer(s);
				trNumbers.add(trNum);
			}
		} catch (Exception e) {
		}
		try {
			FileReader r = new FileReader(wd_stt_all);
			BufferedReader reader = new BufferedReader(r);
			for (String s = reader.readLine(); s != null; s = reader.readLine()) {
				Integer trNum = new Integer(s);
				trNumbers.add(trNum);
			}
		} catch (Exception e) {
		}
		ThreeWayComp allComp = new ThreeWayComp();
		ThreeWayComp bestComp = new ThreeWayComp();
		ThreeWayComp.printHeader(out);
		for (int i = 0; i < trNumbers.size(); i++) {
			int trNum = trNumbers.get(i).intValue();
			WDTransaction t = reconstruction.getTransaction(trNum);
			ProgramSnapshot oldP = t.getOldProgramSnapshot(project);
			ProgramSnapshot newP = t.getNewProgramSnapshot(project);
			File matchingXML = FileNameService.getMatchingXMLFile(oldP, newP,
					SEED_TH, EXCEPTION_TH);
			File nToNMatchXML = FileNameService.getNtoNEvalFile(oldP,
					newP, SEED_TH, EXCEPTION_TH);
		
			if (evaluate == false && (!nToNMatchXML.exists())
					&& matchingXML.exists()) {
				// || trNum == 1236
				// || trNum == 1698 || trNum == 1327
				out.println("trNum" + trNum);
				
				RulebasedMatching matching = RulebasedMatching
						.readXMLFile(matchingXML.getAbsolutePath());

				ComparisonAnalysisViewer cav = new ComparisonAnalysisViewer(
						matching, oldP, newP, ComparisonAnalysisViewer.WD,
						SEED_TH, EXCEPTION_TH);
//				t.printSVNCommand();
//				t.createSourceCodeRepository();
				JDialog dialog = new JDialog(new JFrame(), "Inspection View"
						+ project + ":" + trNum, true);

				dialog.setLocation(1320, 20);
				dialog.setSize(1250, 1000);

				dialog.setContentPane(cav);

				dialog.setVisible(true);
			}else if (nToNMatchXML.exists()) {
				System.out.println("trNum" + trNum);
				RulebasedMatching matching = RulebasedMatching
				.readXMLFile(matchingXML.getAbsolutePath());
				File wd_allXML = FileNameService.getWDMatch_All_File(oldP
						.getProject(), oldP.getVersion());
				File wd_bestXML = FileNameService.getWDMatch_Best_File(oldP
						.getProject(), oldP.getVersion());

				SetOfPairs mine = matching.getMatches(true);
				SetOfPairs rcAll = SetOfPairs.readXMLFile(wd_allXML.getAbsolutePath());
				SetOfPairs rcBest = SetOfPairs.readXMLFile(wd_bestXML.getAbsolutePath());
				SetOfPairs nToN = SetOfPairs.readXMLFile(nToNMatchXML.getAbsolutePath());
				
				allComp.update(mine,rcAll, nToN);
				bestComp.update(mine,rcBest, nToN);
			}
		}
		out.print(project+"\t");ThreeWayComp.printHeader(out);
		out.print("RcAll\t");
		allComp.print(out);
		out.print("RcBest\t");
		bestComp.print(out);
	}
	
}
