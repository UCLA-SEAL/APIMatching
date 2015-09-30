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
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JDialog;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.washington.cs.extractors.ProgramSnapshot;
import edu.washington.cs.induction.FileNameService;
import edu.washington.cs.induction.RulebasedMatching;
import edu.washington.cs.rules.JavaMethod;
import edu.washington.cs.util.SetOfPairs;

public class SKimTransaction{
	static JDialog dialog = null;
	final ArrayList<JavaMethod> domain = new ArrayList<JavaMethod>(); 
	final ArrayList<JavaMethod> codomain = new ArrayList<JavaMethod>();	
	final int oldRevisionId;
	final int newRevisionId;
	final int numOriginCandidates;
	ArrayList<SKimOriginRelation> originCandidates = new ArrayList<SKimOriginRelation>();
	
	public SKimTransaction(Element tElement) {
		int oldRevisionId = 0;
		int newRevisionId = 0;
		int numOriginCandidates = 0;
		NodeList children = tElement.getChildNodes();
		Set<JavaMethod> dm= new TreeSet<JavaMethod>();
		Set<JavaMethod> cdm = new TreeSet<JavaMethod>();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i) instanceof Element) {
				Element child = (Element) children.item(i);

				if (child.getTagName().equals("oldrevision")) {
					oldRevisionId = new Integer(child.getTextContent())
							.intValue();
				}
				if (child.getTagName().equals("newrevision")) {
					newRevisionId = new Integer(child.getTextContent())
							.intValue();
				}
				if (child.getTagName().equals("origin_candidate")) {
					numOriginCandidates = new Integer(child.getTextContent())
							.intValue();
				}
				if (child.getTagName().equals("result")) {
					SKimOriginRelation origin = new SKimOriginRelation(child);
					if (origin.isOrigin) { 
						this.originCandidates.add(origin);
					}
					dm.add(origin.oldMethod);
					cdm.add(origin.newMethod);
					
				}
			}
		}
		this.oldRevisionId = oldRevisionId;
		this.newRevisionId = newRevisionId;
		this.numOriginCandidates = numOriginCandidates;
		this.domain.addAll(dm);
		this.codomain.addAll(cdm);
	}

	public SetOfPairs getSKimOriginMatches () { 
		SetOfPairs origins = new SetOfPairs();
		for (int i= 0; i< originCandidates.size(); i++) {	
			SKimOriginRelation or = originCandidates.get(i);
			if (or.isOrigin ) origins.addPair(or.getOriginPair());
		}
		return origins;
	}
	public void printOriginMatches () { 
		for (int i= 0; i< originCandidates.size(); i++) {
			SKimOriginRelation or = originCandidates.get(i);
			if (or.isOrigin) System.out.println(or.toString());
		}
	}

	public ProgramSnapshot getOldProgramSnapshot (String project) { 

		String srcPath = FileNameService.getSKimSourcePath (oldRevisionId);
		ProgramSnapshot oldP = new ProgramSnapshot(project, oldRevisionId, domain, srcPath) ; 
		return oldP;
	}
	public ProgramSnapshot getNewProgramSnapshot (String project) { 
		String srcPath = FileNameService.getSKimSourcePath (newRevisionId);
		ProgramSnapshot newP = new ProgramSnapshot(project, newRevisionId, codomain, srcPath); 	
		return newP;
	}
	

	public static SKimTransaction readXMLFile(String filename) {
		File file = new File(filename);
		if (file.exists()) {
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
		return new SKimTransaction(doc.getDocumentElement());
		}
		return null;
	}

	public static void generateMatchings(int numTransaction, String project,
			boolean refresh, double SEED_TH, double EXCEPTION_TH, PrintStream p) {
		ComparisonStat compStat = new ComparisonStat(numTransaction);
		
		File stt = FileNameService.getSKimStillToGoFile(project, SEED_TH, EXCEPTION_TH);
		PrintStream stillToGo = System.err;
		try { 
			if (!stt.exists()) {
				stt.createNewFile();
			}
			FileOutputStream o = new FileOutputStream(stt);
			stillToGo = new PrintStream(o);
		}catch (Exception e){ 
			e.printStackTrace();
		}
		for (int transNum = 0; transNum < numTransaction; transNum++) {
			File input = FileNameService.getSKimTransactionFile(project, transNum);
			SKimTransaction transaction = readXMLFile(input.getAbsolutePath());
			if (transaction == null)
				continue;
			ProgramSnapshot oldP = transaction.getOldProgramSnapshot(project);
			ProgramSnapshot newP = transaction.getNewProgramSnapshot(project);
			if (oldP.getMethods().size()<=0 || newP.getMethods().size()<=0)
				continue;
			
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
			// write SKim match file
			File skimMatchXML = FileNameService.getSKimMatchFile(project,
					transNum);
			SetOfPairs skMatches = transaction.getSKimOriginMatches();
			if (!skimMatchXML.exists() || refresh)
				skMatches.writeXMLFile(skimMatchXML.getAbsolutePath());
			compStat.update(matching, skMatches, skMatches.size());
			if (compStat.stillToGo()) { 
				stillToGo.print(transNum+"\n");
			}
		}
		compStat.print(p,project, "NOTHING");
	}

	public static void main(String[] args) {
//		double [] thresholds = {0.65, 0.68, 0.70, 0.72, 0.75};
		double [] thresholds = {0.65}; 
		for (int i= 0; i<thresholds.length; i++ ) {
			double s = thresholds[i];
//			SKimTransaction.batchRun(s);
//			WDRefactoringReconstruction.batchRun(s);
		}
	}
	
	public static void batchRun(double SEED_TH) {
		File logFile = FileNameService.getSKimLogFile(SEED_TH, 0.34);
		try {
			logFile.createNewFile();
			FileOutputStream outStream = new FileOutputStream(logFile);
			PrintStream pStream = new PrintStream(outStream);
			ComparisonStat.printHeader(pStream);

			generateMatchings(1189, "jedit_skim", true, SEED_TH, 0.34, pStream);
			generateMatchings(4683, "argouml_skim", true, SEED_TH, 0.34,
					pStream);
		} catch (Exception e) {

		}
	}
	public static void killDialog() { 
		if (dialog!=null) dialog.dispose();
	}
	
}