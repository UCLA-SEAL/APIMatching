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

import java.io.PrintStream;

import edu.washington.cs.util.SetOfPairs;

public class WDStatRefactoringReconstruction {
	public WDStatRefactoringReconstruction(boolean ambiguity) { 
		this.ambiguity = ambiguity;
	}
	final boolean ambiguity ;
	int numTransCount =0;
	int numMoveClass =0;
	int numRenameClass = 0; 
	int numRenameInterface =0;
	int numMoveInterface = 0; 
	int numMoveMethod =0; 
	int numRenameMethod = 0;
	int numAddParameter = 0;
	int numRemoveParameter =0;
	int numContributingRefactorings =0; 
	int numTotalRefactoring =0;

	public void print (PrintStream p) { 
		p.print("Ambiguity\t"+ambiguity +"\n");
		p.print("WDTransaction\t"+ numTransCount +"\n");
		p.print("WDMove Class\t"+ numMoveClass +"\n");
		p.print("WDRename Class\t"+ numRenameClass+ "\n");
		p.print("WDMove Interface\t"+numMoveInterface+ "\n");
		p.print("WDMove Method\t"+numMoveMethod +"\n");
		p.print("WDRename Method\t"+numRenameMethod +"\n");
		p.print("Add Parameter\t"+ numAddParameter +"\n");
		p.print("Remove Parameter\t" + numRemoveParameter +"\n");
		p.print("Contribution Refactorings\t" + numContributingRefactorings+ "\n");
		p.print("Total Refactorings\t" + numTotalRefactoring +"\n");
	}
	
}