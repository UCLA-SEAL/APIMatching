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
import java.util.ArrayList;
import java.util.Properties;

import edu.ucsc.cse.grase.origin.entity.Method;

/**
 * @author Sung Kim
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class CCFinder extends Factor {

	String[] previousTextSplit = null;

	int cloneLineCount = 0;

	ArrayList lineCountedList = new ArrayList();

	int maxFileLineSize = 0;

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
		 * (similarity != -1f) return similarity;
		 */
		String oldBody = oldMethod.getBody();
		String newBody = newMethod.getBody();
		double similarity = 0;

		File oldSrc = null;
		File newSrc = null;
		try {

			oldSrc = stringToCFile(oldBody);
			newSrc = stringToCFile(newBody);

			System.out.print(oldMethod.getName() + "<->" + newMethod.getName()
					+ "... ");

			similarity = getCCFinderSimilarity(oldSrc, newSrc);

			oldSrc.delete();
			newSrc.delete();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (oldSrc != null)
			oldSrc.delete();

		if (newSrc != null)
			newSrc.delete();

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
	 * @param oldBody
	 * @return
	 */
	private File stringToCFile(String body) throws Exception {
		// Geterate randon C file
		File cSrc = File.createTempFile("ccfind", ".c");
		BufferedWriter bw = new BufferedWriter(new FileWriter(cSrc));
		bw.write(body);
		bw.close();
		return cSrc;
	}

	/**
	 * Most of the global variables need to be initialized
	 */
	private void initVariables() {
		previousTextSplit = null;
		cloneLineCount = 0;
		lineCountedList = new ArrayList();
		maxFileLineSize = 0;
	}

	public double getCCFinderSimilarity(File oldSrc, File newSrc)
			throws Exception {
		// Initialize variables
		initVariables();

		File outFile = File.createTempFile("ccfind", ".out");

		int minTokenLength = Math.min(getMinTokenLength(oldSrc, newSrc), 30);

		/* execute ccfinder to compare 2 files. */
		// Need to install ccfinder in the right path
		Process ccfinderProc = Runtime.getRuntime().exec(
				"/tmp/ccfinder/bin/ccfinder c " + "-b " + minTokenLength
						+ " -o " + outFile.getAbsolutePath() + " "
						+ oldSrc.getAbsolutePath() + " "
						+ newSrc.getAbsolutePath());
		int status = ccfinderProc.waitFor();
		ccfinderProc.destroy();

		/* Read results from the out file */
		BufferedReader br = new BufferedReader(new FileReader(outFile));
		String textLine = br.readLine();
		boolean inCloneSection = false;
		boolean inFileDescSection = false;

		while (textLine != null) {
			if (textLine.startsWith("#begin{clone}"))
				inCloneSection = true;
			else if (textLine.startsWith("#end{clone}"))
				inCloneSection = false;
			else if (textLine.startsWith("#begin{file"))
				inFileDescSection = true;
			else if (textLine.startsWith("#end{file"))
				inFileDescSection = false;
			else if (inCloneSection) {
				/* Read clone data */
				readCloneData(textLine);
			} else if (inFileDescSection) {
				String[] textSplit = textLine.split("[ \t]+");
				int fileLineSize = Integer.parseInt(textSplit[1]);
				if (fileLineSize > maxFileLineSize)
					maxFileLineSize = fileLineSize;
			}

			textLine = br.readLine();
		}

		double similarity = (double) cloneLineCount / maxFileLineSize;
		System.out.println("Similarity is " + similarity);
		br.close();
		outFile.delete();

		return similarity;
	}

	public int getMinTokenLength(File oldSrc, File newSrc) throws Exception {
		// Initialize variables
		initVariables();

		File outFile = File.createTempFile("ccfind", ".out");
		outFile.deleteOnExit();

		/* execute ccfinder to compare 2 files. */
		// Need to install ccfinder in the right path
		Process ccfinderProc = Runtime.getRuntime().exec(
				"/tmp/ccfinder/bin/ccfinder c -o " + outFile.getAbsolutePath()
						+ " " + oldSrc.getAbsolutePath() + " "
						+ newSrc.getAbsolutePath());
		int status = ccfinderProc.waitFor();
		ccfinderProc.destroy();

		/* Read results from the out file */
		BufferedReader br = new BufferedReader(new FileReader(outFile));
		String textLine = br.readLine();
		boolean inFileDescSection = false;

		int minTokenSize = Integer.MAX_VALUE;
		while (textLine != null) {
			if (textLine.startsWith("#begin{file"))
				inFileDescSection = true;
			else if (textLine.startsWith("#end{file")) {
				inFileDescSection = false;
				break;
			} else if (inFileDescSection) {
				String[] textSplit = textLine.split("[ \t]+");
				int tokenSize = Integer.parseInt(textSplit[2]);

				minTokenSize = Math.min(minTokenSize, tokenSize);
			}

			textLine = br.readLine();
		}

		br.close();
		outFile.delete();

		System.out.print(".. Token length: " + minTokenSize + " ");

		return minTokenSize;
	}

	/**
	 * Get token length from Stringed function Body
	 * 
	 * @param oldBody
	 * @return
	 */
	private int getTokenLength(String body) {
		// TODO Auto-generated method stub
		return 0;
	}

	private void readCloneData(String textLine) {
		String[] textSplit = textLine.split("[ \t]+");

		/*
		 * The length of textSplit should be 8. Recover the duplicated data
		 * item.
		 */
		for (int i = 0; i < textSplit.length; i++) {
			if (textSplit[i].equals("="))
				textSplit[i] = previousTextSplit[i];
		}

		previousTextSplit = textSplit;

		if (textSplit[0].equals(textSplit[4]))
			return; /* Clone in the same file. Ignore it. */

		String startSectionPosition = textSplit[1];
		String endSectionPosition = textSplit[2];
		String[] dataSplit;

		/* Go fine the start line of the clone section */
		dataSplit = startSectionPosition.split(",");
		int startLineNumber = Integer.parseInt(dataSplit[0]);

		/* Go fine the start line of the clone section */
		dataSplit = endSectionPosition.split(",");
		int endLineNumber = Integer.parseInt(dataSplit[0]);

		for (int i = startLineNumber; i <= endLineNumber; i++) {
			if (lineCountedList.indexOf(new Integer(i)) == -1) {
				cloneLineCount++; /* Find one clone line */
				lineCountedList.add(new Integer(i));
			}
		}
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