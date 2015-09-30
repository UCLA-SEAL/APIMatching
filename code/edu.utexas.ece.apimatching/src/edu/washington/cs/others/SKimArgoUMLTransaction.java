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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;

public class SKimArgoUMLTransaction {

	public static void main(String[] args) {
//		splitBigFileToSmallFiles(1527);
////		splitBigFileToSmallFiles(40);
////		splitBigFileToSmallFiles(104);
////		splitBigFileToSmallFiles(177);
////		splitBigFileToSmallFiles(4036);
//		splitBigFileToSmallFiles(1122);
//		splitBigFileToSmallFiles(27);
//		splitBigFileToSmallFiles(4239);
//		splitBigFileToSmallFiles(113);
//		splitBigFileToSmallFiles(2763);
//		splitBigFileToSmallFiles(4551);
//		splitBigFileToSmallFiles(1241);
//		splitBigFileToSmallFiles(3509);
//		splitBigFileToSmallFiles(4620);
//		splitBigFileToSmallFiles(1289);
//		splitBigFileToSmallFiles(3907);
//		splitBigFileToSmallFiles(672);
//		splitBigFileToSmallFiles(143);
//		splitBigFileToSmallFiles(3977);
		splitBigFileToSmallFiles("jedit");
	}
	public static void splitBigFileToSmallFiles (String project) { 
		String path = "c:\\OriginAnalysisData\\"+project+".xml";
		File inputFile = new File(path);
		File outputDir = new File("c:\\OriginAnalysisData\\"+project+"_transaction");
		try {
			FileReader fread = new FileReader(inputFile);
			BufferedReader reader = new BufferedReader(fread);
			int transaction =0;
			PrintStream pStream = null;
			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {
				if (line.indexOf("<transaction>") > 0) {
					transaction++;
					File output = new File(outputDir,"transaction"+(transaction-1)+"-"+(transaction)+".xml");
					if (output.exists()) { 
						output.createNewFile();
					}
					if (pStream != null) {
						pStream.flush();
						pStream.close();
					}
					FileOutputStream outs = new FileOutputStream(output);
					pStream = new PrintStream(outs);
					pStream.println(line);
				} else {
					if (pStream!=null) pStream.println(line);
				}
			}
		} catch (Exception e) {		
		}
	}
	public static void splitBigFileToSmallFiles (int i) { 
		String path = "c:\\OriginAnalysisData\\argouml_transaction\\transaction"+i+"-"+(i+1)+".xml";
		File inputFile = new File(path);
		File outputDir = new File("e:\\argouml_temp"+i+"-"+(i+1));
		if (!outputDir.exists()) outputDir.mkdir();
		else return;
		try {
			FileReader fread = new FileReader(inputFile);
			BufferedReader reader = new BufferedReader(fread);
			int result =0;
			PrintStream pStream = null;
			FileOutputStream outs = null;
			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {
				if (line.indexOf("<result>") > 0) {
					result++;
					File output = new File(outputDir,"result"+(result-1)+"-"+(result)+".xml");
					
					if (output.exists()) { 
						output.createNewFile();
					}
					if (pStream != null) {
						pStream.println("</group>");
						pStream.flush();
						pStream.close();
						outs.close();
					}
					outs = new FileOutputStream(output);
					pStream = new PrintStream(outs);
					pStream.println("<group>");
					pStream.println(line);
				} else {
					if (pStream!=null) pStream.println(line);
				}
			}
		} catch (Exception e) {		
		}
	}
}
