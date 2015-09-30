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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.ucsc.cse.grase.origin.entity.Method;
import edu.ucsc.cse.grase.origin.parser.IParser;
import edu.ucsc.cse.grase.origin.parser.Parser;
import edu.washington.cs.induction.FileNameService;
import edu.washington.cs.rules.JavaMethod;
import edu.washington.cs.util.Matchable;

public class ExtractMethods implements Matchable<JavaMethod>{

	public static final boolean RECODER_USE = false;
	
	public static PrintStream stream = null;
	private TreeSet<JavaMethod> methods = null;

	public static void main (String[] args){
		if (args.length == 0) {
			System.out.println("Please check the root directory");
		}
		File f = new File(".");
		try {
			new ExtractMethods(f, "ver", "temp_decl", true );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public ExtractMethods(File root, String version, String project, boolean refresh){
		File DECL_OUTDIR = new File (FileNameService.declarationRoot);
		if (!DECL_OUTDIR.exists()) DECL_OUTDIR.mkdir();
		File projectDeclDir = new File(DECL_OUTDIR, project);
		if (!projectDeclDir.exists()) projectDeclDir.mkdir();
		File decl_file = new File (projectDeclDir,version+FileNameService.declarationFileSuffix); 
		if (refresh ==false && decl_file.exists()) { 
			methods = new TreeSet<JavaMethod>();
			parseDeclaration(decl_file);
		} else {
			try {
			System.out.println ("File path "+decl_file.getAbsolutePath());
			decl_file.createNewFile();
			FileOutputStream outstream = new FileOutputStream(decl_file);
			stream = new PrintStream(outstream);
			methods = new TreeSet<JavaMethod>();
			String locations[] = { root.getAbsolutePath() };
			if (RECODER_USE){
//				retrieveMethodDeclarations(locations);
			}else {
				System.out.print("Parsing "+root.getAbsolutePath());
				parseUsingOriginAnalysis(root);
			}
			} catch (IOException e){ 
				e.printStackTrace();
			}
			catch (Exception e1) {
				e1.printStackTrace();
			} 
		}
	}
	
	private void parseUsingOriginAnalysis (File root){
		assert (root.exists());
		IParser parser = new Parser();
		List methodList = parser.parser(root);
		for (int i=0; i<methodList.size();i++){
			Method m = (Method)methodList.get(i);
			String name = m.getName().replace(":",".");
			String returnType = m.getSignature().getReturnType();
			if (returnType.equals("")) returnType= "void";
			String [] args = m.getSignature().getParameterTypes();
			JavaMethod jm = new JavaMethod(m);
			if (methods.contains(jm)) {
				System.out.println(jm);  
			}
			methods.add(jm);
			if (stream!=null) {
				stream.println(jm.toString());
			}
		}
	}

	/**
	 * Soft error handler from
	 * http://sourceforge.net/forum/forum.php?thread_id=1247697&forum_id=88905
	 * 
	 * @author hunkim
	 * 
	 * TODO To change the template for this generated type comment go to Window -
	 * Preferences - Java - Code Style - Code Templates
	 */
//	public class SoftErrorHandler extends DefaultErrorHandler {
//
//		public SoftErrorHandler() {
//			super();
//		}
//
//		protected boolean isReferingUnavailableCode(ModelElement me) {
//			return true;
//		}
//
//		protected boolean isIgnorable(Exception e) {
//			return true;
//		}
//
//	}

	public Set<JavaMethod> getMatcableItems() {
		return methods;
	}

	public void parseDeclaration(File decl) {
		try {
			FileReader freader = new FileReader(decl);
			BufferedReader reader = new BufferedReader(freader);
			for (String s = reader.readLine(); s != null; s = reader.readLine()) {
				JavaMethod jm = new JavaMethod(s);
				methods.add(jm);
			}
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}
	}
	
}