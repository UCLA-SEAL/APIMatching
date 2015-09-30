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
import java.util.Set;

import edu.washington.cs.extractors.ExtractMethods;
import edu.washington.cs.extractors.ReadDirectories;
import edu.washington.cs.rules.JavaMethod;
import edu.washington.cs.util.SetUtil;

public class DomainSizeMistake {
	public static void main(String args[]) {
		String dirList = "jfreechart_list";
		String project = "jfreechart";
		File[] dirs = ReadDirectories.getDirectories(dirList);

		for (int i = 0; i < dirs.length - 1; i++) {
			File oldProgramDir = dirs[i];
			File newProgramDir = dirs[i + 1];
			String leftPath = oldProgramDir.getAbsolutePath();
			String leftVersion = leftPath
					.substring(leftPath.lastIndexOf("-") + 1);
			String rightPath = newProgramDir.getAbsolutePath();
			String rightVersion = rightPath.substring(rightPath
					.lastIndexOf("-") + 1);

			// get oldEntities
			Set<JavaMethod> oldInitMethods = new ExtractMethods(new File(
					leftPath), leftVersion, project, false).getMatcableItems();
			// get newEntities
			Set<JavaMethod> newInitMethods = new ExtractMethods(new File(
					rightPath), rightVersion, project, false).getMatcableItems();
			
			SetUtil<JavaMethod> exactNameMatcher = new SetUtil<JavaMethod>();
			Set<JavaMethod> remainingOld = exactNameMatcher.diff(
					oldInitMethods, newInitMethods);
			Set<JavaMethod> remainingNew = exactNameMatcher.diff(
					newInitMethods, oldInitMethods);

			// (1) matching from left to right
			ArrayList<JavaMethod> OMinusN = new ArrayList<JavaMethod>();
			OMinusN.addAll(remainingOld);
			// (2) matching from right to left
			ArrayList<JavaMethod> NMinusO = new ArrayList<JavaMethod>();
			NMinusO.addAll(remainingNew);

			String curVerPair = leftVersion + " - " + rightVersion;

			RulebasedMatching rb = RulebasedMatching
					.readXMLFile("c:\\MatchingResult\\jfreechart\\"
							+ leftVersion + "-" + rightVersion
							+ "rulematch100.xml");

			Set<JavaMethod> left = rb.getDomain(true);

			// get newEntities
			Set<JavaMethod> right = rb.getDomain(false);
			System.out.println(curVerPair);
			if (left.size() != OMinusN.size()) {
				System.out.println("domain:" + OMinusN.size() + "->"
						+ left.size());
			}
			if (right.size() != NMinusO.size()) {
				System.out.println("codomain:" + NMinusO.size() + "->"
						+ right.size());
			}

			System.out.println("Next");
		}
	}
}
