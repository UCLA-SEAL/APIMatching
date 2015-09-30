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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;

import edu.ucsc.cse.grase.origin.entity.Method;

/**
 * @author Sung Kim
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class Moss extends Factor {
	private String cacheName;

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.ucsc.cse.grase.origin.factors.Factor#getSimilarity(edu.ucsc.cse.grase.origin.method.Method,
	 *      edu.ucsc.cse.grase.origin.method.Method)
	 */
	public double getSimilarity(Method oldMethod, Method newMethod) {

		// Read similarity from cache
		/*
		 * double similarity = readCache(oldMethod, newMethod, cacheName); if
		 * (similarity!=-1f) return similarity;
		 */
		String oldBody = oldMethod.getBody();
		String newBody = newMethod.getBody();

		File oldSrc = null;
		File newSrc = null;

		double similarity = 0;

		try {
			oldSrc = stringToCFile(oldBody);
			newSrc = stringToCFile(newBody);

			similarity = getMossSimilarity(oldSrc, newSrc);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}

		if (oldSrc != null) {
			oldSrc.delete();
		}

		if (newSrc != null) {
			newSrc.delete();
		}

		try {
			// Write similarity to Cache
			// writeCache(similarity, oldMethod, newMethod, cacheName);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return getWeight() * similarity;
	}

	/**
	 * @param oldSrc
	 * @param newSrc
	 * @return
	 */
	private double getMossSimilarity(File oldSrc, File newSrc) throws Exception {
		File outFile = File.createTempFile("moss", ".out",
				new File("/tmp/moss"));
		outFile.deleteOnExit();

		String args[] = { oldSrc.getAbsolutePath(), newSrc.getAbsolutePath(),
				outFile.getAbsolutePath() };
		String strArgs = "";
		for (int i = 0; i < args.length; i++) {
			strArgs += " " + args[i];
		}

		Process mossProc = Runtime.getRuntime().exec(
				"/usr/local/bin/mosscomp" + strArgs);
		System.out.print("/usr/local/bin/mosscomp" + strArgs + " ...");
		int status = mossProc.waitFor();
		mossProc.destroy();

		// Read results from the out file
		BufferedReader br = new BufferedReader(new FileReader(outFile));
		String strSimilarity = br.readLine();
		br.close();

		// Remove the tmp out File
		outFile.delete();

		int intSimilarity = new Integer(strSimilarity).intValue();

		System.out.println(intSimilarity);
		return (double) intSimilarity / 100f;
	}

	/**
	 * @param oldBody
	 * @return
	 */
	private File stringToCFile(String body) throws Exception {
		// Geterate randon C file
		File cSrc = File.createTempFile("moss", ".c", new File("/tmp/moss"));
		BufferedWriter bw = new BufferedWriter(new FileWriter(cSrc));
		bw.write(body);
		bw.close();
		return cSrc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.se.evolution.kenyon.FactExtractor#setOptions(java.util.Properties)
	 */
	public void setOptions(Properties options) {
		cacheName = options.getProperty("cache");
	}
}