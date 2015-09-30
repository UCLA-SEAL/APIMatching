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
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.swing.JDialog;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.washington.cs.extractors.ProgramSnapshot;
import edu.washington.cs.induction.FileNameService;
import edu.washington.cs.induction.RulebasedMatching;
import edu.washington.cs.util.SetOfPairs;

public class WDRefactoringReconstruction {

	static JDialog dialog = null;
	private ArrayList<WDTransaction> transactions = new ArrayList<WDTransaction>();
	
	public static WDRefactoringReconstruction readXMLFile(String filename) {
		WDRefactoringReconstruction reconstruction = new WDRefactoringReconstruction();
		Document doc = null;
		DOMParser domparser = new DOMParser();
		try {
			domparser.parse(filename);
			doc = domparser.getDocument();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException s) {
			s.printStackTrace();
		}
		// parse doc and return GroupMap
		NodeList children = doc.getDocumentElement().getChildNodes();
		for (int i=0 ; i< children.getLength(); i++) { 
			Node child = children.item(i);
			if (child instanceof Element) { 
				WDTransaction t = WDTransaction.readElement((Element)child);
				reconstruction.transactions.add(t);
			}
		}
		return reconstruction;
		
	}
	public static void main(String args[]){
		batchRun(0.7);
	}
	public static void batchRun (double SEED_TH) { 
		File logFile = FileNameService.getWDLogFile(SEED_TH, 0.34);
		try {
			logFile.createNewFile();
			FileOutputStream outStream = new FileOutputStream(logFile);
			PrintStream pStream = new PrintStream(outStream);
			ComparisonStat.printHeader(pStream);
			run(true, "jedit_wd", SEED_TH, 0.34, pStream);
			run(true, "tomcat_wd", SEED_TH, 0.34, pStream);
		}catch (Exception e){ 
			
		}
	}
	public static void run (boolean refresh, String project, double SEED_TH, double EXCEPTION_TH, PrintStream p ) {
		
		File file = FileNameService.getWDRefactoringFile(project);
		WDRefactoringReconstruction reconstruction = readXMLFile(file.getAbsolutePath());
		WDStatRefactoringReconstruction ambiguityStat = new WDStatRefactoringReconstruction(
				true);
		WDStatRefactoringReconstruction ambPos1Stat = new WDStatRefactoringReconstruction(
				false);
		ComparisonStat rcAllStat = new ComparisonStat(reconstruction.transactions.size());
		ComparisonStat rcBestStat = new ComparisonStat(reconstruction.transactions.size());

//		File rcAll_stt = FileNameService.getWDStillToGoFile(project, "RcAll",SEED_TH, EXCEPTION_TH);
//		File rcBest_stt = FileNameService.getWDStillToGoFile(project, "RcBest", SEED_TH, EXCEPTION_TH);
//		PrintStream rcAll_stillToGo = System.err;
//		PrintStream rcBest_stillToGo = System.err;
//		try { 
//			if (!rcAll_stt.exists()) {
//				rcAll_stt.createNewFile();
//			}
//			if (!rcBest_stt.exists()) { 
//				rcBest_stt.createNewFile();
//			}
//			FileOutputStream o = new FileOutputStream(rcAll_stt);
//			rcAll_stillToGo = new PrintStream(o);
//			FileOutputStream o1 = new FileOutputStream(rcBest_stt);
//			rcBest_stillToGo = new PrintStream(o1);
//		}catch (Exception e){ 
//			e.printStackTrace();
//		}
	
		for (int i=0 ; i<reconstruction.transactions.size(); i++) { 
			WDTransaction t = reconstruction.transactions.get(i);	
			ProgramSnapshot oldP = t.getOldProgramSnapshot(project);
			ProgramSnapshot newP = t.getNewProgramSnapshot(project);
			System.out.println("OLD: "+ oldP.getMethods().size() + "NEW: "+ newP.getMethods().size());
			if (oldP.getMethods().size()>0 && newP.getMethods().size()>0) {
				// run hill climbing search
				ambiguityStat.numTransCount++;
				ambPos1Stat.numTransCount++;
				File matchingXML = FileNameService.getMatchingXMLFile(oldP, newP,
						SEED_TH, EXCEPTION_TH);
				
				RulebasedMatching matching = null;
				if (!matchingXML.exists() || refresh) {
					matching = RulebasedMatching.batchMatchingGenerationTwoVersion(
							refresh, oldP, newP, SEED_TH, EXCEPTION_TH);
				} else {
					matching = RulebasedMatching.readXMLFile(matchingXML
							.getAbsolutePath());
				}
//				t.printInferredRefactoring();
				SetOfPairs rcAll = t.getMatchesExplainedByRefactoring(ambiguityStat);
				SetOfPairs rcBest = t.getMatchesExplainedByRefactoring(ambPos1Stat);
				File rcAllFile = FileNameService.getWDMatch_All_File(project, t.id);
				File rcBestFile = FileNameService.getWDMatch_Best_File(project, t.id);
				if (!rcAllFile.exists() || refresh) { 
					rcAll.writeXMLFile(rcAllFile.getAbsolutePath());
				}
				if (!rcBestFile.exists() || refresh) { 
					rcBest.writeXMLFile(rcBestFile.getAbsolutePath());
				}
				rcAllStat.update(matching, rcAll,0);
//				if (rcAllStat.stillToGo()) { 
//					rcAll_stillToGo.print(i+"\n");
//				}
				rcBestStat.update(matching, rcBest, 0);
//				if (rcBestStat.stillToGo()) { 
//					rcBest_stillToGo.print(i+"\n");
//				}
			} 
		}
		
		rcAllStat.print(p, project, "RCAll");
		rcBestStat.print(p, project, "RCBest");
		ambiguityStat.print(p);
		ambPos1Stat.print(p);
	}
	public static void killDialog() { 
		System.out.println("Kill Dialog");
		if (dialog!=null) dialog.dispose();
	}
	public WDTransaction getTransaction (int index){ 
		return this.transactions.get(index);
	}
}
