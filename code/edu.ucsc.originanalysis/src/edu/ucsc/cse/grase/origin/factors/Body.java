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
/*
 * Created on 2005. 4. 8.
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.ucsc.cse.grase.origin.factors;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import edu.ucsc.cse.grase.origin.entity.Method;

/**
 * @author Sung Kim
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class Body extends Factor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.ucsc.cse.grase.origin.factors.Factor#getSimilarity(edu.ucsc.cse.grase.origin.method.Method,
	 *      edu.ucsc.cse.grase.origin.method.Method)
	 */
	public double getSimilarity(Method oldMethod, Method newMethod) {
		String oldBody = oldMethod.getBody();
		String newBody = newMethod.getBody();
		List oldList = stringToList(oldBody);
		List newList = stringToList(newBody);

		return getSimilarity(oldList, newList);
	}

	/**
	 * Make Each line as an element
	 * 
	 * @param oldBody
	 * @return
	 */
	private List stringToList(String body) {
		List list = new ArrayList();

		StringTokenizer tok = new StringTokenizer(body, "\n\r");

		// Ignore the firstline with signature and function name
		if (tok.hasMoreTokens()) {
			tok.nextToken();
		}

		while (tok.hasMoreTokens()) {
			String line = normalizeWhitespace(tok.nextToken());

			// Ignore blankline
			if (line != null) {
				list.add(line);
				// System.out.println(line);
			}
		}

		return list;
	}

	/**
	 * Normalize all whitespace
	 * 
	 * @param string
	 * @return
	 */
	private String normalizeWhitespace(String string) {
		String returnString = "";
		String splits[] = string.split("\\s+");

		// Blank line
		if (splits.length == 0)
			return null;

		for (int i = 0; i < splits.length; i++) {
			returnString += " " + splits[i];
		}

		return returnString;
	}
}