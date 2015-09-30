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
package edu.ucsc.cse.grase.origin.util;

import java.io.File;
import java.io.RandomAccessFile;

import org.eclipse.cdt.core.parser.CodeReader;

public class ReadContent {
	public static String getContent(String fileName, int startPos, int endPos) {

		//System.out.println(fileName + ":" + startPos + ":" + endPos);

		try {
			CodeReader codeReader = new CodeReader(fileName);
			String allContent = new String(codeReader.buffer);
			String conent = allContent.substring(startPos, endPos);
			
			//System.out.println(conent);
			
			return conent;

			/*
			 * FIXME: randmom access is not working well 
			 * byte data[] = new
			 * byte[endPos - startPos + 1]; RandomAccessFile accessFile = new
			 * RandomAccessFile(fileName, "r"); System.out.println("Length: " +
			 * accessFile.length()); accessFile.(data, startPos, (endPos -
			 * startPos + 1)); accessFile.close(); return new String(data);
			 */
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
