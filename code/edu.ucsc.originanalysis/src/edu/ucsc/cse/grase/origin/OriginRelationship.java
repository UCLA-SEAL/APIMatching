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
package edu.ucsc.cse.grase.origin;

import edu.ucsc.cse.grase.origin.entity.Method;
import edu.ucsc.cse.grase.origin.kimmy.LCS;
import edu.ucsc.cse.grase.origin.kimmy.Tokenize;

public class OriginRelationship implements Comparable {
	Method deleteddMethod;

	Method addedMethod;

	double similarity;

	boolean automaticOriginRelaion;

	public int compareTo(Object obj) {
		if (!(obj instanceof OriginRelationship)) {
			return 0;
		}

		OriginRelationship originRelationship = (OriginRelationship) obj;
		if (similarity > originRelationship.similarity)
			return 1;

		if (similarity < originRelationship.similarity)
			return -1;

		return 0;
	}

	public String toString() {
		return "Origin Relationship\t" + similarity + "\t"
				+ automaticOriginRelaion + "\n -" + deleteddMethod + "\n +"
				+ addedMethod;
	}
	
	public Method getDeletedMethod() { 
		return deleteddMethod;
		
	}

	public Method getAddedMethod() { 
		return addedMethod;
	}
}
