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
package edu.ucsc.cse.grase.origin.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.ucsc.cse.grase.origin.entity.Method;
import edu.ucsc.cse.grase.origin.util.StringUtil;



public class Parser implements IParser {
	static final int C = 1;

	static final int CPP = 2;

	static final int JAVA = 3;

	public static final String sourceExtType[] = { ".c", "C", ".h", "C",
			".cpp", "CPP", ".hpp", "CPP", ".cc", "CPP", ".java", "JAVA" };

	
	class SourceFile {

		String fileName;

		int type;
	}

	/**
	 * return list of FileEntity
	 * 
	 * @param checkedOutLocation
	 * @return
	 */
	public List parser(File checkedOutLocation) {
		List sourceFiles = getSourceFiles(checkedOutLocation.getAbsolutePath());
		List allEntityList = new ArrayList();
		for (int i = 0; i < sourceFiles.size(); i++) {
			SourceFile sourceFile = (SourceFile) sourceFiles.get(i);
			try {
				List entityList = getEntityList(sourceFile);
				
				// No entity to add
				if (entityList == null) {
					continue;
				}
				
				String relativePath = StringUtil.toRelativePath(sourceFile.fileName,
						checkedOutLocation);
				
				// Set file name to the relative path
				// Add each entity to the allEntityList
				for (int j=0; j<entityList.size(); j++) {
					Method method = (Method)entityList.get(j);
					method.setFileName(relativePath);
					allEntityList.add(method);
				}				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return allEntityList;
	}

	private List getEntityList(SourceFile sourceFile) throws Exception {
		List entityList = null;

		// FIXME: How much can we trust the file extension. Espacially c/cpp
		// stuff
		switch (sourceFile.type) {
		case C:
			// Not implemented
			break;
		case CPP:
			// Not implemented
			break;

		case JAVA:
			JavaParser jParser = new JavaParser();
			entityList = jParser.parser(sourceFile.fileName);
			break;
		}

		return entityList;
	}

	/**
	 * @param jarDir
	 * @param string
	 * @return
	 */
	public List getSourceFiles(String path) {
		List sourceFiles = new ArrayList();
		File file = new File(path);

		File subFiles[] = file.listFiles();

		if (subFiles == null)
			return sourceFiles;

		for (int i = 0; i < subFiles.length; i++) {
			if (subFiles[i].isDirectory()) {
				List list = getSourceFiles(subFiles[i].getAbsolutePath());
				sourceFiles.addAll(list);
			} else {
				String lowerFileName = subFiles[i].getName().toLowerCase();
				int type = isSource(lowerFileName);
				if (type != -1) {
					SourceFile sourceFile = new SourceFile();
					sourceFile.fileName = subFiles[i].getAbsolutePath();
					sourceFile.type = type;

					sourceFiles.add(sourceFile);
				}
			}
		}

		return sourceFiles;
	}

	/**
	 * Check if a given file name is source code
	 * 
	 * @param lowerFileName
	 * @return
	 */
	public static int isSource(String lowerFileName) {
		String type = null;
		for (int i = 0; i < sourceExtType.length; i += 2) {
			if (lowerFileName.endsWith(sourceExtType[i])) {
				type = sourceExtType[i + 1];
				break;
			}
		}

		if (type == null) {
			return -1;
		} else if (type.equals("C")) {
			return C;
		} else if (type.equals("CPP")) {
			return CPP;
		} else if (type.equals("JAVA")) {
			return JAVA;
		}

		return -1;
	}
}
