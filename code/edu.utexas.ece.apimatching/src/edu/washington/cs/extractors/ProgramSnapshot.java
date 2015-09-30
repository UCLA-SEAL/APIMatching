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
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import edu.washington.cs.rules.JavaMethod;
import edu.washington.cs.util.SetUtil;

public class ProgramSnapshot {
	private final String srcPath; 
	private final String version;
	private final String project;
	private final ArrayList<JavaMethod> methods;
	public ProgramSnapshot (String project, File srcDirectory) {
		this.project = project;
		this.srcPath = srcDirectory.getAbsolutePath();
		String versionX = null; 
		if (srcPath.indexOf("-") > 0) {
			 versionX = srcPath.substring(srcPath.lastIndexOf("-") + 1);
		}

		else if (srcPath.indexOf("_") > 0) {
			versionX = srcPath.substring(srcPath.lastIndexOf("_") + 1);

		}
		this.version = versionX.substring(versionX.lastIndexOf("\\") + 1);
		// get oldEntities
		Set<JavaMethod> initMethods = new ExtractMethods(new File(
				this.srcPath), this.version, this.project, false).getMatcableItems();
		this.methods = new ArrayList<JavaMethod> ();
		this.methods.addAll(initMethods);
	}
	public ArrayList<JavaMethod> getMethods() {
		return methods;
	}
	public String getProject() {
		return project;
	}
	public String getVersion() {
		return version;
	}
	public String getSrcPath() {
		return srcPath;
	}

	public ProgramSnapshot (String project, int transaction_id, ArrayList<JavaMethod> methods, String srcPath) { 
		this.project = project;
		this.version = new Integer(transaction_id).toString();
		this.methods = methods;
		this.srcPath = srcPath;	
	}
	public ProgramSnapshot (String project, String transaction_id, ArrayList<JavaMethod> methods, String srcPath) { 
		this.project = project;
		this.version = transaction_id;
		this.methods = methods;
		this.srcPath = srcPath;	
	}
	public static void main (String args[]) { 
		ExtractMethods em =new ExtractMethods(new File("e:\\jfreechart_archive\\jfreechart-0.9.17"),"0.9.17","jfreechart",true);
		System.out.println(em.getMatcableItems().size());
//		batchPreparation("jfreechart_list","jfreechart");
//		batchPreparation("jhotdraw_list","jhotdraw");
//		batchPreparation("jedit_list","jedit");
	}
	public static void batchPreparation (String dirList, String project) { 
		File[] dirs = ReadDirectories.getDirectories(dirList);
//		for (int i = 0; i < dirs.length - 1; i++) {
//			File srcDir = dirs[i];
//			new ProgramSnapshot(project, srcDir);
//		}
	}
	public ArrayList<JavaMethod> minus (ProgramSnapshot subtract) { 
		ArrayList<JavaMethod> domain = new ArrayList<JavaMethod>();
		TreeSet<JavaMethod> leftSet = new TreeSet<JavaMethod>();
		leftSet.addAll(this.getMethods());
		TreeSet<JavaMethod> rightSet = new TreeSet<JavaMethod>();
		rightSet.addAll(subtract.getMethods());
		SetUtil<JavaMethod> exactMatcher = new SetUtil<JavaMethod>();
		
		domain.addAll(exactMatcher.diff(leftSet, rightSet));
//		System.out.println("Intersection:"+(exactMatcher.intersect(leftSet,rightSet).size()));
		return domain; 
	}
	public Set<JavaMethod> common (ProgramSnapshot subtract) { 
		TreeSet<JavaMethod> leftSet = new TreeSet<JavaMethod>();
		leftSet.addAll(this.getMethods());
		TreeSet<JavaMethod> rightSet = new TreeSet<JavaMethod>();
		rightSet.addAll(subtract.getMethods());
		SetUtil<JavaMethod> exactMatcher = new SetUtil<JavaMethod>();
		return (exactMatcher.intersect(leftSet, rightSet));
	}
}
