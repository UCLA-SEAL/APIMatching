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

import java.util.List;
import java.util.Properties;

import edu.ucsc.cse.grase.origin.entity.Method;
import edu.ucsc.cse.grase.origin.factors.metrics.LCSC;
import edu.ucsc.cse.grase.origin.factors.metrics.Similarity;

/**
 * @author Sung Kim
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public abstract class Factor {
	String name;

	private double weight = 1.0f;

	public static final int SIMILARITY_LCSC_METHOD = 1;

	public static final int SIMILARITY_COMMON_METHOD = 2;

	public static final int SIMILARITY_SMITH = 2;

	private Similarity similarityClass = new LCSC();

	private boolean exclude = false;

	private int oldRev;

	/**
	 * All subclass must implement this
	 * 
	 * @param oldMethod
	 * @param newMethod
	 * @return
	 */
	abstract public double getSimilarity(Method oldMethod, Method newMethod);

	/**
	 * @param weight
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}

	/**
	 * @param weight
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * Return similarity class based on the similarity method input
	 * 
	 * @return
	 */
	double getSimilarity(List oldList, List newList) {
		return weight * similarityClass.getSimilarity(oldList, newList);
	}

	/**
	 * Return similarity class based on the similarity method input
	 * 
	 * @return
	 */
	double getSimilarity(String oldString, String newString) {
		return weight * similarityClass.getSimilarity(oldString, newString);
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param b
	 */
	public void setExclude(boolean exclude) {
		this.exclude = true;
	}

	/**
	 * @param opts
	 */
	public void setOptions(Properties opts) {
		// TODO Auto-generated method stub
	}

	/**
	 * @return Returns the oldRev.
	 */
	public int getOldRev() {
		return oldRev;
	}

	/**
	 * @param oldRev
	 *            The oldRev to set.
	 */
	public void setOldRev(int oldRev) {
		this.oldRev = oldRev;
	}

	/**
	 * @return Returns the exclude.
	 */
	public boolean isExclude() {
		return exclude;
	}

}
