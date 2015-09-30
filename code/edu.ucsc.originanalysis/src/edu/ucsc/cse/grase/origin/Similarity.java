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

import edu.ucsc.cse.grase.origin.factors.Body;
import edu.ucsc.cse.grase.origin.factors.Factor;
import edu.ucsc.cse.grase.origin.factors.FunctionName;

public class Similarity {
	static Factor factors[] = { new Body(), new FunctionName()};
	// new Location(),
//			new Signature() };

	public static double getSimilarity(OriginRelationship originRelationship) {
		double totalWeight = 0;
		double totalSimilarity = 0;

		for (int i = 0; i < factors.length; i++) {
			totalWeight += factors[i].getWeight();
			
			double sim = factors[i].getSimilarity(
					originRelationship.deleteddMethod,
					originRelationship.addedMethod);
//			System.out.println(i+"\t"+sim);
			totalSimilarity += sim;
		}

		return totalSimilarity / totalWeight;
	}
}
