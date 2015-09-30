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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import edu.washington.cs.rules.JavaMethod;
import edu.washington.cs.rules.Transformation;
import edu.washington.cs.rules.TransformationRule;

public class APILevelRuleExaminer {

	public static void main (String args[]) { 
		File rbmFile = FileNameService.getMatchingXMLFile("jfreechart", "1.0.12", "1.0.13", 0.7, 0.34); 
		RulebasedMatching rbm = RulebasedMatching.readXMLFile(rbmFile.getAbsolutePath());
		APILevelRuleExaminer apiRuleExaminer = new APILevelRuleExaminer(); 
		apiRuleExaminer.compute_API_RULE_CHANGETYPE(rbm); 
		apiRuleExaminer.print_API_RULE_CHANGETYPE(System.out);
	}
	// create a hash map where key is a Transformation.Type and value is a list of transformation rules; 
	private HashMap<Integer, ArrayList<TransformationRule>> changeTypeToRules = new HashMap<Integer, ArrayList<TransformationRule>>();

	private Set<JavaMethod> deletedAPIs = new TreeSet<JavaMethod>(); 
	private Set<JavaMethod> addedAPIs = new TreeSet<JavaMethod>(); 
	
	public void compute_API_RULE_CHANGETYPE(RulebasedMatching rbm){ 
		// get forward transformation rules
		TransformationRule[] rules = rbm.getSortedRules(true); 
		
		for (TransformationRule ftr: rules) { 
			Transformation t = ftr.getTransformation(); 
			
			Integer keyInteger = new Integer(t.getType()); 
			ArrayList<TransformationRule> rarray = changeTypeToRules.get(keyInteger); 
			
			if (rarray==null) {
				rarray= new ArrayList<TransformationRule>(); 
				changeTypeToRules.put(keyInteger, rarray);
			}
			rarray.add(ftr); 
		}
		deletedAPIs = get_API_DELETED(rbm);
		addedAPIs = get_API_ADDED(rbm); 
		System.out.println("Num Rules:\t"+rules.length);
	}

	public Set<JavaMethod> get_API_DELETED (RulebasedMatching rbm) { 
		Set<JavaMethod> oldMethods= rbm.getDomain(true); 
		System.out.println("Domain Size:\t"+oldMethods.size());
		Set<JavaMethod> matchedLeft = rbm.getMatches(true).getLeftDomain();
		System.out.println("MatchedLeft Size:\t"+matchedLeft.size());
		oldMethods.removeAll(matchedLeft);
		return oldMethods;
	}
	public Set<JavaMethod> get_API_ADDED(RulebasedMatching rbm)  { 
		Set<JavaMethod> newMethods= rbm.getDomain(false);
		System.out.println("Codomain Size:\t"+newMethods.size());	
		Set<JavaMethod> matchedRight = rbm.getMatches(true).getRightDomain();
		System.out.println("MatchedRight Size:\t"+matchedRight.size());
		
		newMethods.removeAll(matchedRight);
		return newMethods;
	}

	public void print_API_MATCH_CHANGETYPE(PrintStream p){ 
		// print out a change type and matches for each change type. 
		for (Integer key: changeTypeToRules.keySet()) { 
			String changeTypeString = Transformation.getTypeString(key.intValue());
			ArrayList<TransformationRule> rules = changeTypeToRules.get(key);
			int nummatch=0; 
			for (TransformationRule r :rules) { 
				nummatch = nummatch + r.getPositiveMatches().size();
			}
			p.println("Number of matches with "+changeTypeString+":\t"+ nummatch); 
		}
		
	}
	
	public void print_API_RULE_CHANGETYPE(PrintStream p){ 
		// print out a change type and rules for each change type 	
		int index[] ={  1, 2, 9, 3, 4, 8, 5, 7, 6} ;  
		for (int i: index) { 
			String changeTypeString = Transformation.getTypeString(i);
			ArrayList<TransformationRule> rules = changeTypeToRules.get(new Integer(i));
			p.println(changeTypeString+":"); 
			if (rules==null) continue;
			
			p.println("# of rules:"+rules.size()); 
			int nummatch=0; 
			for (TransformationRule r :rules) { 
				nummatch = nummatch + r.getPositiveMatches().size();
			}
			p.println("# of matches:"+nummatch);
			for (TransformationRule r :rules) { 
				p.println(r.toRuleExaminePrint()); 
			}
			p.println();
		}
		// print out deleted APIS
		p.println("deletedMethod:"+deletedAPIs.size()); 
		for (JavaMethod jm:deletedAPIs){ 
			p.println(jm); 
		}
		p.println("");
		// print out added APIS
		p.println("addedMethod:"+addedAPIs.size()); 
		for (JavaMethod jm:addedAPIs){ 
			p.println(jm); 
		}
	}
	
	
}