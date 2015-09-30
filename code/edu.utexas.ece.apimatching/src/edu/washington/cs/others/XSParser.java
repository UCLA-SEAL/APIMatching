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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import edu.washington.cs.rules.JavaMethod;

public class XSParser {
	public static void main (String args[]) {
		
	}
	
	public static void testSingleFile () { 
		File dir =  new File("C:\\UMLDiffData");
		File inputFile = null; 
		JFileChooser chooser = new JFileChooser(dir);
		int returnVal = chooser.showOpenDialog(new JFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			inputFile = chooser.getSelectedFile();
		}
		parseFile(inputFile);
	}
	public static ArrayList<XSChange> parseFile(File inputFile) {
		ArrayList<XSChange> changes = new ArrayList<XSChange>();
		String fileInput = inputFile.getName();
		try {
			if (inputFile.exists()) {
				FileReader fread = new FileReader(inputFile);
				BufferedReader reader = new BufferedReader(fread);
				int cnt = 0;
				reader.readLine();
				for (String line = reader.readLine(); line != null; line = reader
						.readLine()) {
					// System.out.println(cnt++);
					// read a line by line
					line = line.replace("\"", "");
					XSChange changeEvent = null;
					if (fileInput.indexOf("_move") > 0) {
						changeEvent = new XSMove(line);
					} else if (fileInput.indexOf("_rename") > 0) {
						changeEvent = new XSRename(line);
					} else if (fileInput.indexOf("_paramlist") > 0) {
						changeEvent = new XSParamList(line);
					} else {
						changeEvent = new XSChange(line);
					}
					if (changeEvent.change.equals("ERROR")) {
						System.out.println("ERROR:" + line);
					}
					changes.add(changeEvent);
				}
			}
		} catch (FileNotFoundException e ){ 
			e.printStackTrace();
		}catch (IOException e){
			e.printStackTrace();
		}
		return changes;
	}
	public static ArrayList<XSChange> parseAllFiles() {
		ArrayList<XSChange> changes = new ArrayList<XSChange>();
		File dir =  new File("C:\\UMLDiffData");
		File renameFile = new File(dir,"jfreechart_rename.csv");
		File moveFile = new File(dir,"jfreechart_move.csv");
		File paramFile = new File(dir,"jfreechart_paramlist.csv");
		if (renameFile.exists()) changes.addAll(parseFile(renameFile));
		if (moveFile.exists()) changes.addAll(parseFile(moveFile));
		if (paramFile.exists()) changes.addAll(parseFile(paramFile));
		// count number of rename, move, paramlist. 
		
		return changes;
	}


}
