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

import java.util.Hashtable;
import java.util.Iterator;

import edu.washington.cs.rules.JavaMethod;
import edu.washington.cs.util.Pair;
import edu.washington.cs.util.SetOfPairs;

public class Refactoring {

	
	class JavaClass implements Comparable{
		public JavaClass(String p, String c) { 
			this.packageName= p;
			this.className = c;
		}
		public JavaClass(boolean conflict) {
			assert conflict;
			this.conflict=true;
		}
		String packageName;
		String className;
		boolean conflict =false;
		public int compareTo(Object o) {
			// TODO Auto-generated method stub
			return this.toString().compareTo(o.toString());
		}
		@Override
		public boolean equals(Object obj) {
			// TODO Auto-generated method stub
			return this.toString().equals(obj.toString());
		}
		@Override
		public int hashCode() {
			// TODO Auto-generated method stub
			return this.toString().hashCode();
		}
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return packageName+"%"+className;
		}
		
		
	}
	class JavaPackage implements Comparable{
		public JavaPackage(String p) { 
			this.packageName = p; 
		}
		public JavaPackage(boolean conflict) { 
			assert conflict;
			this.conflict = true;
		}
		String packageName;
		boolean conflict =false;
		public int compareTo(Object o) {
			// TODO Auto-generated method stub
			return this.toString().compareTo(o.toString());
		}
		@Override
		public boolean equals(Object obj) {
			// TODO Auto-generated method stub
			return this.toString().equals(obj.toString());
		}
		@Override
		public int hashCode() {
			// TODO Auto-generated method stub
			return this.toString().hashCode();
		}
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return packageName;
		}
	}
	// all renamings are only allowed in the same container.  
	Hashtable<JavaClass, JavaClass> classLevelMapping = new Hashtable<JavaClass, JavaClass>();

	Hashtable<JavaPackage, JavaPackage> packageLevelMapping = new Hashtable<JavaPackage, JavaPackage>();

	Hashtable<JavaMethod, Integer> methodLevelMapping = new Hashtable<JavaMethod, Integer>();
	
	SetOfPairs methodLevelMatches;
	
	public static final int PACKAGE_LEVEL=0;
	public static final int CLASS_LEVEL=1; 
	public static final int METHOD_LEVEL=2; 
	public static final int SIGNATURE_LEVEL =3;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}
	
	public Refactoring(RulebasedMatching matching) { 
		methodLevelMatches = matching.getMatches(true);
			
		// infer package level renaming by taking prefixes.
		
		for (Iterator<Pair> it = methodLevelMatches.iterator(); it.hasNext();) {
			Pair<JavaMethod> match = (Pair<JavaMethod>) it.next();
			JavaMethod left = match.getLeft();
			JavaMethod right = match.getRight();
			// the problem is that a few moves would invalidate a package level
			// renaming.
			JavaPackage pleft = new JavaPackage(left.getPackageName());
			JavaPackage pright = new JavaPackage(right.getPackageName());
			if (!pleft.equals(pright)) {
				JavaPackage matched = packageLevelMapping.get(pleft);
				if (matched != null) {
					// there exists a mapping already
					if (!matched.equals(pright)) {
						// if this conflicts with the previous matching because
						// the previous one is actually move not renaming
						// so we have to mark that this is conflict.
						packageLevelMapping.put(pleft, new JavaPackage(true));
					} else {
						// it infers the same package renaming as other match
					}
				} else {
					// no mapping exists yet, so put it in.
					packageLevelMapping.put(pleft, pright);
				}
			}	
		}
		
		// infer class level renaming by taking prefixes 
		
		for (Iterator<Pair> it = methodLevelMatches.iterator(); it.hasNext();) {
			Pair<JavaMethod> match = (Pair<JavaMethod>) it.next();
			JavaMethod left = match.getLeft();
			JavaMethod right = match.getRight();
			JavaClass cleft = new JavaClass(left.getPackageName(), left
					.getClassName());
			JavaClass cright = new JavaClass(right.getPackageName(), right
					.getClassName());

			if (!cleft.equals(cright)) {
				JavaClass matched = classLevelMapping.get(cleft);
				if (matched!=null) { 
					// there exists a mapping already
					if (!matched.equals(cright)) { 
						//mark conflict 
//						System.out.println("Class Conflict:L "+ cleft);
//						System.out.println("Class Conflict:R "+ cright);
//						System.out.println("Class Conflict:M "+ matched+"\n");
						
						classLevelMapping.put(cleft, new JavaClass(true));
					}else { 
						// it infers the same class level mapping. 
					}
				}else {
					// no mapping exist yet, so put it in
					classLevelMapping.put(cleft, cright);
				}
			
			}
		}
		
		// infer method level renanming
		for (Iterator<Pair> it = methodLevelMatches.iterator(); it.hasNext();) {
			Pair<JavaMethod> match = (Pair<JavaMethod>) it.next();
			JavaMethod left = match.getLeft();
			JavaMethod right = match.getRight();
		
			if (!left.equals(right)) {
				Integer num = methodLevelMapping.get(left);
				if (num==null) { 
					methodLevelMapping.put(left, new Integer(1));
				}else {
					// no mapping exist yet, so put it in
					num = new Integer(num.intValue()+1);
					methodLevelMapping.put(left, num);
				}
			
			}
		}		
	}
	private void printPackageMap() { 
		int cnt=0;
		for (JavaPackage packLeft: packageLevelMapping.keySet()){ 
			JavaPackage packRight = packageLevelMapping.get(packLeft);
			if (packRight==null || packRight.conflict==true) { 
				continue;
			}else {
				cnt++;
				System.out.println("["+packLeft+","+packRight+"]");	
			}
		}
		System.out.println("PACKAGE. "+cnt);
	}
	private void printClassMap() {
		int cnt=0;
		for (JavaClass classLeft : classLevelMapping.keySet()) {
			JavaClass classRight = classLevelMapping.get(classLeft);
			if (classRight == null || classRight.conflict == true) {
				continue;
			} else {
				cnt++;
				System.out.println("[" + classLeft + "," + classRight + "]");
			}
		}
		System.out.println("CLASS. "+cnt);
	}
	private void printMethodMap() { 
		int cnt=0;
		for (JavaMethod methodLeft : methodLevelMapping.keySet()) { 
			Integer num = methodLevelMapping.get(methodLeft);
			if (num.intValue()==1) { 
				cnt++;
				System.out.println("[" + methodLeft +"," + methodLevelMatches.getFirstMatchByLeft(methodLeft)+"]"); 
			}
		}
		System.out.println("METHOD. "+cnt);
	}
	public void print() {
		System.out.println("Package Level" + packageLevelMapping.size());
		printPackageMap();
		System.out.println("Class Level" + classLevelMapping.size());
		printClassMap();
		System.out.println("Method Level" + methodLevelMapping.size());
		printMethodMap();
	}
}
