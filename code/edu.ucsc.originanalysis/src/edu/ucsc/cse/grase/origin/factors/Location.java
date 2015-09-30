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

import edu.ucsc.cse.grase.origin.entity.Method;

/**
 * @author Sung Kim
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class Location extends Factor {

	int LOC = 300;

	public Location() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.ucsc.cse.grase.origin.factors.Factor#getSimilarity(edu.ucsc.cse.grase.origin.method.Method,
	 *      edu.ucsc.cse.grase.origin.method.Method)
	 */
	public double getSimilarity(Method oldMethod, Method newMethod) {
		if (!oldMethod.getFileName().equals(newMethod.getFileName())) {
			return 0;
		}

		int delta = Math.abs(newMethod.getStartLine() - oldMethod.getEndLine());
		return 1 - Math.min(1, ((float) delta / LOC));
	}
}