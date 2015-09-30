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

public class StringUtil {

	/**
	 * Compute relative path
	 * 
	 * @param absolutePath
	 * @return
	 */
	public static String toRelativePath(String absolutePath,
			File checkedOutLocation) {
		String relativePath = new File(absolutePath).getAbsolutePath();

		// Remove workspace part
		if (relativePath.startsWith(checkedOutLocation.getAbsolutePath())) {
			relativePath = absolutePath.substring(checkedOutLocation
					.getAbsolutePath().length());
		}

		// Remove the leading slash
		if (relativePath.startsWith(File.separator)) {
			relativePath = relativePath.substring(File.separator.length());
		}

		return relativePath;
	}

	/**
	 * Two way trim
	 * 
	 * @param line
	 * @return
	 */
	public static String twoWayTrim(String line) {
		StringBuffer lineBuffer = new StringBuffer(line.trim());
		String reversedTrimString = lineBuffer.reverse().toString().trim();
		String trimedLine = new StringBuffer(reversedTrimString).reverse()
				.toString();
		return trimedLine;
	}

	public static boolean isAllDigits(String string) {
		if (string == null)
			return false;

		for (int i = 0; i < string.length(); i++) {
			char ch = string.charAt(i);
			if (!Character.isDigit(ch))
				return false;
		}

		return true;
	}
}
