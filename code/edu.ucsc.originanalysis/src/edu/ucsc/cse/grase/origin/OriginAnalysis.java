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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.ucsc.cse.grase.origin.entity.Method;
import edu.ucsc.cse.grase.origin.parser.IParser;
import edu.ucsc.cse.grase.origin.parser.Parser;

/**
 * Origin Analysis runner
 * 
 * @author hunkim
 * 
 */
// 1. Parse each path and get entity sets
// 2. find out added entity set and deleted entitySet
// 3. Find automatic origin relationship such as no method overiding and
// only signature change
// 4. make origin candidate set
// 5. get similarity of all candidate set
// 6. order them and find the origin relationship
public class OriginAnalysis {
	List automaticOriginRelationList = new ArrayList();

	List originRelationList = new ArrayList();

	boolean verbose = true;

	private PrintStream debugOriginAnalysisStream = null;

	public OriginAnalysis(String output) {
		if (output==null) return;
		try {
			File file = new File(output);
			if (file.exists())
				file.delete();
			file.createNewFile();
			FileOutputStream fileOut = new FileOutputStream(file);
			debugOriginAnalysisStream = new PrintStream(fileOut);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* 
	 * Between two versions of a program, 
	 * match by the exact same name.
	 * matching critera: the exact same procedure name  
	 * input: oldRevisionPath, newRevisionPath, 
	 * output: a list of matches. 
	 */
	private List getExactSameProcedureNameMatches(String oldRevisionPath,
			String newRevisionPath) {
		
		List automaticOriginRelations = new ArrayList();
		IParser parser = new Parser();
		// 1. Parse each path and get entity sets
		List oldRevisionEntityList = parser.parser(new File(oldRevisionPath));
		List newRevisionEntityList = parser.parser(new File(newRevisionPath));

		if (verbose) {
			System.err.println("We've got " + oldRevisionEntityList.size()
					+ " entities in old revision");
			System.err.println("We've got " + newRevisionEntityList.size()
					+ " entities in new revision");
		}

		// 2. find out added entity set and deleted entitySet
		HashSet newRevisionSet = makeHashSet(newRevisionEntityList);
		List deletedEntityList = new ArrayList();
		for (int i = 0; i < oldRevisionEntityList.size(); i++) {
			Method method = (Method) oldRevisionEntityList.get(i);
			if (!newRevisionSet.contains(method.getId())) {
				deletedEntityList.add(method);
			}
		}

		HashSet oldRevisionSet = makeHashSet(oldRevisionEntityList);
		List addedEntityList = new ArrayList();
		for (int i = 0; i < newRevisionEntityList.size(); i++) {
			Method method = (Method) newRevisionEntityList.get(i);
			if (!oldRevisionSet.contains(method.getId())) {
				addedEntityList.add(method);
			}
		}

		if (verbose) {
			System.err.println("We've got " + deletedEntityList.size()
					+ " deleted entities.");
			System.err.println("We've got " + addedEntityList.size()
					+ " added entities.");
		}

		// If any of list is null (zero size), no origin relationship
		if (deletedEntityList.size() == 0 || addedEntityList.size() == 0)
			return null;
		// 3. Find automatic origin relationship such as no method overiding and
		// only signature change
		int count2 = 0;
		Hashtable deletedNoOverrideEntityTable = makeNoOverrideEntityTable(deletedEntityList);
		Hashtable addedNoOverrideEntityTable = makeNoOverrideEntityTable(addedEntityList);
		for (Iterator it = deletedNoOverrideEntityTable.values().iterator(); it
				.hasNext();) {
			Method deletedMethod = (Method) it.next();

			// If the same method exist in the added method, it's automatic
			// origin relationship
			Method addedMethod = (Method) addedNoOverrideEntityTable
					.get(deletedMethod.getIdNoSignature());

			// We found it
			if (addedMethod != null) {
				OriginRelationship originRelationship = new OriginRelationship();
				originRelationship.deleteddMethod = deletedMethod;
				originRelationship.addedMethod = addedMethod;
				originRelationship.automaticOriginRelaion = true;
				automaticOriginRelations.add(originRelationship);

				if (verbose) {
					count2++;
					System.err.println("Found only signature change:\n" + "\t"
							+ addedMethod.toString() + "\n\t"
							+ deletedMethod.toString());
				}
				// remove each from the added/deleted list
				deletedEntityList.remove(deletedMethod);
				addedEntityList.remove(addedMethod);
			}
		}
		if (verbose && automaticOriginRelations.size() > 0) {
			System.err.println("Now, we've got " + deletedEntityList.size()
					+ " deleted entities.");
			System.err.println("Now, we've got " + addedEntityList.size()
					+ " added entities.");
		}

		debugPrint("Total Count 2:" + count2);
		return automaticOriginRelations;

	}

	private List getApproximateSignatureNameMatches(String oldRevisionPath,
			String newRevisionPath) {

		IParser parser = new Parser();
		// 1. Parse each path and get entity sets
		List oldRevisionEntityList = parser.parser(new File(oldRevisionPath));
		List newRevisionEntityList = parser.parser(new File(newRevisionPath));

		if (verbose) {

			System.err.println("We've got " + oldRevisionEntityList.size()
					+ " entities in old revision");
			System.err.println("We've got " + newRevisionEntityList.size()
					+ " entities in new revision");

			debugPrint("1. Signature Exact Match ");
			debugPrint("We've got " + oldRevisionEntityList.size()
					+ " entities in old revision");
			debugPrint("We've got " + newRevisionEntityList.size()
					+ " entities in new revision");
			debugPrint("Total Count 1:" + oldRevisionEntityList.size());
		}

		// 2. find out added entity set and deleted entitySet
		HashSet newRevisionSet = makeHashSet(newRevisionEntityList);
		List deletedEntityList = new ArrayList();
		for (int i = 0; i < oldRevisionEntityList.size(); i++) {
			Method method = (Method) oldRevisionEntityList.get(i);
			if (!newRevisionSet.contains(method.getId())) {
				deletedEntityList.add(method);
			}
		}

		HashSet oldRevisionSet = makeHashSet(oldRevisionEntityList);
		List addedEntityList = new ArrayList();
		for (int i = 0; i < newRevisionEntityList.size(); i++) {
			Method method = (Method) newRevisionEntityList.get(i);
			if (!oldRevisionSet.contains(method.getId())) {
				addedEntityList.add(method);
			}
		}

		if (verbose) {
			System.err.println("We've got " + deletedEntityList.size()
					+ " deleted entities.");
			System.err.println("We've got " + addedEntityList.size()
					+ " added entities.");
			debugPrint("We've got " + deletedEntityList.size()
					+ " deleted entities.");
			debugPrint("We've got " + addedEntityList.size() + " added entities.");
		}

		// If any of list is null (zero size), no origin relationship
		if (deletedEntityList.size() == 0 || addedEntityList.size() == 0)
			return null;

		// 3. Find automatic origin relationship such as no method overiding and
		// only signature change

		debugPrint("2. Simple Signature Change ");

		int count2 = 0;
		Hashtable deletedNoOverrideEntityTable = makeNoOverrideEntityTable(deletedEntityList);
		Hashtable addedNoOverrideEntityTable = makeNoOverrideEntityTable(addedEntityList);
		for (Iterator it = deletedNoOverrideEntityTable.values().iterator(); it
				.hasNext();) {
			Method deletedMethod = (Method) it.next();

			// If the same method exist in the added method, it's automatic
			// origin relationship
			Method addedMethod = (Method) addedNoOverrideEntityTable
					.get(deletedMethod.getIdNoSignature());

			// We found it
			if (addedMethod != null) {
				OriginRelationship originRelationship = new OriginRelationship();
				originRelationship.deleteddMethod = deletedMethod;
				originRelationship.addedMethod = addedMethod;
				originRelationship.automaticOriginRelaion = true;
				automaticOriginRelationList.add(originRelationship);

				if (verbose) {
					count2++;
					System.err.println("Found only signature change:\n" + "\t"
							+ addedMethod.toString() + "\n\t"
							+ deletedMethod.toString());
					debugPrint("Found only signature change:\n" + "\t"
							+ addedMethod.toString() + "\n\t"
							+ deletedMethod.toString());

				}

				// remove each from the added/deleted list
				deletedEntityList.remove(deletedMethod);
				addedEntityList.remove(addedMethod);
			}
		}
		if (verbose && automaticOriginRelationList.size() > 0) {
			System.err.println("Now, we've got " + deletedEntityList.size()
					+ " deleted entities.");
			System.err.println("Now, we've got " + addedEntityList.size()
					+ " added entities.");
			debugPrint("Now, we've got " + deletedEntityList.size()
					+ " deleted entities.");
			debugPrint("Now, we've got " + addedEntityList.size()
					+ " added entities.");
		}

		debugPrint("Total Count 2:" + count2);
		// If any of list is null (zero size), no origin relationship
		if (deletedEntityList.size() == 0 || addedEntityList.size() == 0)
			return null;

		// 4. make origin candidate set
		for (int i = 0; i < deletedEntityList.size(); i++) {
			Method deletedMethod = (Method) deletedEntityList.get(i);
			for (int j = 0; j < addedEntityList.size(); j++) {
				Method addedMethod = (Method) addedEntityList.get(j);

				OriginRelationship originRelationship = new OriginRelationship();
				originRelationship.deleteddMethod = deletedMethod;
				originRelationship.addedMethod = addedMethod;

				// 5. get similarity of all candidate set
				originRelationship.similarity = Similarity
						.getSimilarity(originRelationship);
				originRelationList.add(originRelationship);
			}
		}

		// printout only signature changes: automatic origin relatonship
		for (int i = 0; i < automaticOriginRelationList.size(); i++) {
			OriginRelationship originRelationship = (OriginRelationship) automaticOriginRelationList
					.get(i);
			if (verbose) {
				debugPrint(originRelationship.toString());
			}
		}

		// 6. order them and find out the origin relationship
		Collections.sort(originRelationList);
		debugPrint("3. Origin Relationship Using Body Diff, Function Name Diff, Location Diff, Signature Diff");
		// Need to build Set to check already mapped entity
		HashSet mappedDeletedEntitySet = new HashSet();
		HashSet mappedAddedEntitySet = new HashSet();
		
		/// Now create results 
		ArrayList originResults = new ArrayList();

		int count3 = 0;
		for (int i = 0; i < originRelationList.size(); i++) {
			OriginRelationship originRelationship = (OriginRelationship) 
			originRelationList.get(i);

			String deletedMethodId = originRelationship.deleteddMethod.getId();
			String addedMethodId = originRelationship.addedMethod.getId();

			// It is already mapped
			if (mappedDeletedEntitySet.contains(deletedMethodId))
				continue;

			if (mappedAddedEntitySet.contains(addedMethodId))
				continue;

			// Save mapped methods
			mappedDeletedEntitySet.add(deletedMethodId);
			mappedAddedEntitySet.add(addedMethodId);
			if (verbose) {
				debugPrint(originRelationship.toString());
				originResults.add(originRelationship);
				count3++;
			}
		}
		debugPrint("Total Count 3.:" + count3);
		return originResults;
	}

	/**
	 * Make entity table using the name: noSigId, value: method
	 * 
	 * @param deletedEntityList
	 * @return
	 */
	private Hashtable makeNoOverrideEntityTable(List list) {
		Hashtable table = new Hashtable();
		Set seenSet = new HashSet();
		for (int i = 0; i < list.size(); i++) {
			Method method = (Method) list.get(i);
			String idNoSig = method.getIdNoSignature();
			// Already saw the method, then remove it from the table
			if (seenSet.contains(idNoSig)) {
				table.remove(idNoSig);
			} else { // Maybe it's the only method. Let add it to the table
				seenSet.add(idNoSig);
				table.put(idNoSig, method);
			}
		}
		return table;
	}

	private HashSet makeHashSet(List list) {
		HashSet set = new HashSet();
		if (list == null)
			return set;

		for (int i = 0; i < list.size(); i++) {
			Method method = (Method) list.get(i);
			set.add(method.getId());
		}

		return set;
	}
	private HashSet makeKimmyHashSet(List list) {
		HashSet set = new HashSet();
		if (list == null)
			return set;

		for (int i = 0; i < list.size(); i++) {
			Method method = (Method) list.get(i);
			set.add(method.getKimmyId());
		}

		return set;
	}
	public static void main(String args[]) {
		if (args.length < 2) {
			System.err
					.println("OriginAnaysls <old_revision_dir> <new_revision_dir>");
			return;
		}

		OriginAnalysis originAnalysis = new OriginAnalysis("output");
		originAnalysis.getApproximateSignatureNameMatches(args[0], args[1]);
	}

	private void debugPrint(String s) {
		if (debugOriginAnalysisStream != null) {
			debugOriginAnalysisStream.println(s);
		}
		System.out.println(s);
	}

//	public List getApproximateNameSignatureMatchOverThreshold(String oldRevisionPath,
//			String newRevisionPath, double THRESHOLD ) {
//
//		IParser parser = new Parser();
//		// 1. Parse each path and get entity sets
//		List oldRevisionEntityList = parser.parser(new File(oldRevisionPath));
//		List newRevisionEntityList = parser.parser(new File(newRevisionPath));
//
//		// 2. find out added entity set and deleted entitySet
//		HashSet newRevisionSet = makeKimmyHashSet(newRevisionEntityList);
//		List deletedEntityList = new ArrayList();
//		for (int i = 0; i < oldRevisionEntityList.size(); i++) {
//			Method method = (Method) oldRevisionEntityList.get(i);
//			if (!newRevisionSet.contains(method.getKimmyId())) {
//				deletedEntityList.add(method);
//			}
//		}
//
//		HashSet oldRevisionSet = makeKimmyHashSet(oldRevisionEntityList);
//		List addedEntityList = new ArrayList();
//		for (int i = 0; i < newRevisionEntityList.size(); i++) {
//			Method method = (Method) newRevisionEntityList.get(i);
//			if (!oldRevisionSet.contains(method.getKimmyId())) {
//				addedEntityList.add(method);
//			}
//		}
//
//		// If any of list is null (zero size), no origin relationship
//		if (deletedEntityList.size() == 0 || addedEntityList.size() == 0)
//			return null;
//
//		// 3. Find automatic origin relationship such as no method overiding and
//		// only signature change
//
//		List originRelationList = new ArrayList();
//		int count2 = 0;
//		Hashtable deletedNoOverrideEntityTable = makeNoOverrideEntityTable(deletedEntityList);
//		Hashtable addedNoOverrideEntityTable = makeNoOverrideEntityTable(addedEntityList);
//		for (Iterator it = deletedNoOverrideEntityTable.values().iterator(); it
//				.hasNext();) {
//			Method deletedMethod = (Method) it.next();
//
//			// If the same method exist in the added method, it's automatic
//			// origin relationship
//			Method addedMethod = (Method) addedNoOverrideEntityTable
//					.get(deletedMethod.getIdNoSignature());
//
//			// We found it
//			if (addedMethod != null) {
//				OriginRelationship originRelationship = new OriginRelationship();
//				originRelationship.deleteddMethod = deletedMethod;
//				originRelationship.addedMethod = addedMethod;
//				originRelationship.automaticOriginRelaion = true;
//				automaticOriginRelationList.add(originRelationship);
//				// remove each from the added/deleted list
//				deletedEntityList.remove(deletedMethod);
//				addedEntityList.remove(addedMethod);
//				originRelationship.similarity = Similarity
//				.getSimilarity(originRelationship);
////				debugPrint("automatic\t"+originRelationship);
//				originRelationList.add(originRelationship);
//				debugPrint(originRelationship.toString());
//			}
//		}
//		// If any of list is null (zero size), no origin relationship
//		if (deletedEntityList.size() == 0 || addedEntityList.size() == 0)
//			return null;
//		// 4. make origin candidate set
//		for (int i = 0; i < deletedEntityList.size(); i++) {
//			Method deletedMethod = (Method) deletedEntityList.get(i);
//			double bestSimilarity = -0.1;
//			OriginRelationship bestRelationship = null;
//			for (int j = 0; j < addedEntityList.size(); j++) {
//				Method addedMethod = (Method) addedEntityList.get(j);
//				OriginRelationship originRelationship = new OriginRelationship();
//				originRelationship.deleteddMethod = deletedMethod;
//				originRelationship.addedMethod = addedMethod;
//				// 5. get similarity of all candidate set
//				originRelationship.similarity = originRelationship.getNameTokenDistance();
//				if (originRelationship.similarity > bestSimilarity) {
//					bestRelationship = originRelationship;
//					bestSimilarity = originRelationship.similarity;
//				}
//			}
//			// printout this maxRelationship.
//			if (bestSimilarity > THRESHOLD) {
//				originRelationList.add(bestRelationship);
//				debugPrint(bestRelationship.toString());
//				addedEntityList.remove(bestRelationship.getAddedMethod());
//			}
//		}
//		debugPrint("S.Kim Analysis: Approximate Name Match"+originRelationList.size());
//		debugPrint("DELETED\t"+(deletedEntityList.size()- originRelationList.size()));
//		debugPrint("ADDED\t"+addedEntityList.size());
//		return originRelationList;
//	 }
}
