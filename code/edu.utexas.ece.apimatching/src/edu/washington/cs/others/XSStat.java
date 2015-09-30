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
import java.util.ArrayList;

import edu.washington.cs.extractors.ProgramSnapshot;
import edu.washington.cs.extractors.ReadDirectories;
import edu.washington.cs.induction.FileNameService;
import edu.washington.cs.induction.RulebasedMatching;
import edu.washington.cs.rules.JavaMethod;
import edu.washington.cs.util.Comparison;
import edu.washington.cs.util.Pair;
import edu.washington.cs.util.SetOfPairs;

public class XSStat {
	
	// total number of lines: 133625 (match.csv)
	// total number of rename: 1656 
	// total number of param: 2399
	// total number of move : 775
	// total number of rename, param, move : 4830
	
	
	public int renameCount;

	public int moveCount;

	public int paramCount;

	public int rename_package;

	public int rename_class;

	public int rename_interface;

	public int rename_method;

	public int rename_constructor;

	public int rename_field;

	public int move_class;

	public int move_interface;

	public int move_method;

	public int move_field;

	public int param_change_identifier;

	public int param_renameormove_paramtype;

	public int param_remove_parameter;

	public int param_add_parameter;

	public int param_change_paramtype;

	private static ArrayList<XSChange> changes = XSParser.parseAllFiles();
	public static void sanityCheck () { 
		
	}
	public static void printRefactoringTableRow(PrintStream p) {
		p.print("rename\t");
		p.print("move\t");
		p.print("param\t");

		p.print("rename_package\t");
		p.print("rename_class\t");
		p.print("rename_interface\t");
		p.print("rename_method\t");
		p.print("rename_constructor\t");
		p.print("rename_field\t");

		p.print("move_class\t");
		p.print("move_interface\t" );
		p.print("move_method\t" );
		p.print("move_field\t");

		p.print("param_change_identifier\t" );
		p.print("param_renameormove_paramtype\t" );
		p.print("param_remove_parameter\t" );
		p.print("param_add_parameter\t" );
		p.print("param_change_paramtype\t");
	}
	public void printRefactoring(PrintStream p) {
		p.print(renameCount +"\t");
		p.print(moveCount+"\t");
		p.print(paramCount+"\t");

		p.print(rename_package+"\t");
		p.print(rename_class+"\t");
		p.print(rename_interface+"\t");
		p.print(rename_method+"\t");
		p.print(rename_constructor+"\t");
		p.print(rename_field+"\t");

		p.print(move_class+"\t");
		p.print(move_interface+"\t");
		p.print(move_method+"\t");
		p.print(move_field+"\t");

		p.print(param_change_identifier+"\t");
		p.print(param_renameormove_paramtype+"\t");
		p.print(param_remove_parameter+"\t");
		p.print(param_add_parameter+"\t");
		p.print(param_change_paramtype+"\t");
	}
	public static void printTableRow (PrintStream p) {
		p.print("Version\t");
		p.print("Domain\t");
		p.print("Codomain\t");
		p.print("ruleMatch\t");
		p.print("xsMatch\t");
		p.print("rules\t");
		p.print("xsTrans\t");
		p.print("xsContTrans\t");
		p.print("ruleM^xsM\t");
		p.print("ruleM-xsM\t");
		p.print("xsM-ruleM\t");
		p.print("(ruleM^xsM)^nToN\t");
		p.print("(ruleM-xsM)^nToN\t");
		p.print("(xsM-ruleM)^nToN\t");
		printRefactoringTableRow(p);
		p.print("\n");
	}

	public static void main(String args[]) {	
		compare (false, true, "jfreechart_list_CGE", "jfreechart", 0.7, 0.34);
	}
	public static void compare(boolean refresh, boolean onlyOne, String dirList, String project,
			double SEED_TH, double EXCEPTION_TH) {

		File[] dirs = ReadDirectories.getDirectories(dirList);
		// parse files
		changes = XSParser.parseAllFiles();
		// comparison file
		PrintStream tableStream = null;
		File comparisonTable = FileNameService.getXingStrouliaComparison(
				project, SEED_TH, EXCEPTION_TH);
		if (comparisonTable.exists()) { 
			return;
		}
		if (refresh) {
			try {
				comparisonTable.createNewFile();
				FileOutputStream os = new FileOutputStream(comparisonTable);
				tableStream = new PrintStream(os);
			} catch (Exception e) {
			}
		}else { 
			tableStream = System.out;
		}
		printTableRow(tableStream);
		compareProject(tableStream,onlyOne, dirs, project, SEED_TH, EXCEPTION_TH);
	}
	public static void compareProject(PrintStream tableStream, boolean onlyOne, File[] dirs,
			String project, double SEED_TH, double EXCEPTION_TH) {
		int end = 1;
		if (!onlyOne) {
			end = dirs.length - 1;
		}
		for (int i = 0; i < end; i++) {
			ProgramSnapshot oldP = new ProgramSnapshot(project, dirs[i]);
			ProgramSnapshot newP = new ProgramSnapshot(project, dirs[i + 1]);
			XSStat stat = new XSStat();
			String curVerPair = oldP.getVersion() + " - " + newP.getVersion();
			File frb = FileNameService.getMatchingXMLFile(oldP, newP, SEED_TH,
					EXCEPTION_TH);
			SetOfPairs ruleMatch = null;

			RulebasedMatching rb = null;
			if (frb.exists()){
				rb= RulebasedMatching.readXMLFile(frb
					.getAbsolutePath());
				ruleMatch = rb.getMatches(true);
				
			}
			
			ArrayList<JavaMethod> domain = oldP.getMethods();
			ArrayList<JavaMethod> codomain = newP.getMethods();
			SetOfPairs xsMatch = new SetOfPairs();
			// add only-return type changes to xsMatch
			for (int kd=0; kd<domain.size(); kd++ ){ 
				JavaMethod d = domain.get(kd);
				for (int kc = 0; kc<codomain.size(); kc++) { 
					JavaMethod c = codomain.get(kc);
					if (d.onlyDifferByReturn(c)
							&& !d.equals(c)) { 
						xsMatch.addPair(new Pair(d,c));
					}
				}
			}
			int xsTrans =0; 
			int xsContTrans=0;
			// for each change event convert domain to codomain
			for (int j = 0; j < changes.size(); j++) {
				XSChange change = changes.get(j);
				if (change.version.equals(curVerPair)) {
					xsTrans++; // only the same as version 
					// check validity of codomain
					boolean codomainvalid = change
							.checkCodomainValidity(codomain);
					boolean domainvalid = change.checkDomainValidity(domain);
					SetOfPairs result = change.applyTransformation(domain,
							codomain);
					
					xsMatch.addSetOfPairs(result);
					if (codomainvalid && domainvalid && result.size() > 0) {
						change.tally(stat);
						xsContTrans++;
					}
				}
			}
			
			File xingMatch = FileNameService.getXSMatchFile(project, oldP.getVersion(), newP.getVersion());
			File nToNMatch = FileNameService.getNtoNEvalFile(oldP,newP,SEED_TH, EXCEPTION_TH);
			SetOfPairs nToNMatches = new SetOfPairs();
			if (nToNMatch.exists()) { 
				nToNMatches= SetOfPairs.readXMLFile(nToNMatch.getAbsolutePath());
			}
			if (!xingMatch.exists())
			{
				xsMatch.writeXMLFile(xingMatch.getAbsolutePath());
				System.out.println("Wrote to XS Match Files"+xingMatch.getAbsolutePath());
			}else { 
				SetOfPairs xF = SetOfPairs.readXMLFile(xingMatch.getAbsolutePath());
			
				if (xsMatch.size() != xF.size()) {
					System.out.println("Xing Match File not correct");
				}
			}
			
			if (ruleMatch != null && xsMatch != null) {
				SetOfPairs X_and_M = Comparison.common(ruleMatch, xsMatch);
				tableStream.print(curVerPair + "\t");
				tableStream.print(domain.size() + "\t");
				tableStream.print(codomain.size() + "\t");
				tableStream.print(ruleMatch.size() + "\t");
				tableStream.print(xsMatch.size() + "\t");
				tableStream.print(rb.numRules(true) + "\t");
				tableStream.print(xsTrans + "\t");
				tableStream.print(xsContTrans +"\t");
				
				SetOfPairs X_minus_M = Comparison.leftMinusRight(xsMatch,
						ruleMatch);
				SetOfPairs M_minus_X = Comparison.leftMinusRight(ruleMatch,
						xsMatch);
				SetOfPairs NN_and_X_and_M = Comparison.common(X_and_M, nToNMatches);
				SetOfPairs NN_and_X_minus_M = Comparison.common(X_minus_M, nToNMatches);
				SetOfPairs NN_and_M_minus_X = Comparison.common(M_minus_X, nToNMatches);
				
				tableStream.print(X_and_M.size() + "\t");
				tableStream.print(M_minus_X.size() + "\t");
				tableStream.print(X_minus_M.size() + "\t");
				
				tableStream.print(NN_and_X_and_M.size()+"\t");
				tableStream.print(NN_and_M_minus_X.size()+"\t");
				tableStream.print(NN_and_X_minus_M.size()+"\t");
				stat.printRefactoring(tableStream);
				tableStream.print("\n");
			}
		}
		
	}
}
