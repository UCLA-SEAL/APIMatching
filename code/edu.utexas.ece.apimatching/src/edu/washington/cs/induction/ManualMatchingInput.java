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
package edu.washington.cs.induction;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import edu.washington.cs.profile.CallStack;
import edu.washington.cs.profile.MethodStack;
import edu.washington.cs.profile.MethodStackTraceFilter;
import edu.washington.cs.rules.JavaMethod;
import edu.washington.cs.rules.JavaMethodComparator;
import edu.washington.cs.util.ListOfPairs;
import edu.washington.cs.util.Pair;
import edu.washington.cs.util.SetOfPairs;

public class ManualMatchingInput extends JFrame{
	private static String xmlTag = "manualInput";
	private RulebasedMatching matchingResult = null;

	private MethodStack leftStackInfo = null;
	private MethodStack rightStackInfo = null;
	public JLabel status = null;
	public JTree leftEntityTree = null;
	public JTree rightEntityTree = null;
	public JEditorPane leftSelection = new JEditorPane();
	public JEditorPane rightSelection = new JEditorPane();
	int leftExamined = 0; 
	int rightExamined =0; 
	int numInitialDeleted = 0; 
	int numInitialAdded = 0; 
	private final SetOfPairs manualOnetoOnePairs; 
	private int WIDTH = 1200;
	private int LENGTH = 300;
	private int EDITOR = 100;
	private int MARGIN = 50;
	
	class LeftTreeSelectionHandler implements TreeSelectionListener {
		public void valueChanged(TreeSelectionEvent event) {
			//retrieve selection
			TreePath path = event.getPath();
			MatchStatus l = getMatchStatus(path);
			if (l!=null){ 
				JavaMethod jm = l.jm;
				if (leftStackInfo==null) return;
				CallStack cs = leftStackInfo.getValue(jm);
				if (cs!=null) {
					leftSelection.setText(cs.info());
				}else { 
					leftSelection.setText("No Info");
				}
			}
		}
	}
	class RightTreeSelectionHandler implements TreeSelectionListener {
		public void valueChanged(TreeSelectionEvent event) {
			//retrieve selection
			TreePath path = event.getPath();
			MatchStatus l = getMatchStatus(path);
			if (l!=null){ 
				JavaMethod jm = l.jm;
				if (rightStackInfo==null) return;
				CallStack cs = rightStackInfo.getValue(jm);
				if (cs!=null) {
					rightSelection.setText(cs.info());
				}else
				{
					rightSelection.setText("No Info");
				}
			}
		}
	}
	public class MatchStatus {
		private String xmlTag = "matchStatus";
		JavaMethod jm =null;
		boolean examinedByUser = false;
		boolean markedUnmatched = false;
		boolean forwardRule = false;
		boolean backwardRule = false;
		boolean oneToOne = false;
		boolean split = false; 
		boolean merge = false;
		public String toString () {
			String s="";
			if (examinedByUser) {
				s= s+"  Checked";
			}
			if (markedUnmatched) {
				s = s+"  Unmatched";
			}
			if (oneToOne) {
				s = s+"  1:1";
			} else {
				if (forwardRule) {
					s = s + "  F";
				}
				if (backwardRule) {
					s = s + "  B";
				}
			}
			if (split) {
				s = s+"  Split";
			}
			if (merge) {
				s = s+"  Merge";
			}
			s = s+ "  "+jm.toString();
			return s;
		}
	}
	public void updateStatusMessage() {
		String s = "Left:   " + leftExamined + " out of " + numInitialDeleted
				+ "    Right:" + rightExamined + " out of " + numInitialAdded;
		status.setText(s);
	}
	public ManualMatchingInput(RulebasedMatching matchingResult){
		super("Manual Input");
		this.numInitialAdded = matchingResult.numUnMatched(false);
		this.numInitialDeleted = matchingResult.numUnMatched(true);
		this.matchingResult= matchingResult;
		this.manualOnetoOnePairs = new SetOfPairs();
		this.manualOnetoOnePairs.addSetOfPairs(matchingResult.getManualOneToOneMatches());
		setLocation(100,100);
		setSize(WIDTH+MARGIN, LENGTH*2+EDITOR*2+MARGIN);
		status = new JLabel();
		JPanel content = new JPanel();
//		content.setLayout(new GridLayout(4,1));
		content.add(createEntityPanel(true, matchingResult));
		leftSelection.setSize(WIDTH, EDITOR);
		content.add(leftSelection);
		content.add(createEntityPanel(false, matchingResult));
		content.add(rightSelection);
		rightSelection.setSize(WIDTH,EDITOR);
		leftEntityTree.addTreeSelectionListener(new LeftTreeSelectionHandler());
		rightEntityTree.addTreeSelectionListener(new RightTreeSelectionHandler());
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(createMenuPanel(), BorderLayout.NORTH);
		getContentPane().add(content, BorderLayout.CENTER);
		getContentPane().add(status, BorderLayout.SOUTH);
		setVisible(true);
		
	}
	
	public JPanel createMenuPanel() {
		JButton markDeleted = new JButton("Mark Deleted"); 
		markDeleted.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent ae) {
				markUnmatched(true);
				updateStatusMessage();
			}
		});
		JButton markAdded = new JButton("Mark Added");
		markAdded.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent ae) {
				markUnmatched(false);
				updateStatusMessage();
			}
		});
		JButton nextLeft = new JButton("Next Left");
		nextLeft.addActionListener(new ActionListener() { 
			public void actionPerformed (ActionEvent ae) { 
				navigateToNext(true);
			}
		});
		JButton nextRight = new JButton("Next Right"); 
		nextRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) { 
				navigateToNext(false);	
			}
		}); 
		
		JButton mapOneToOne = new JButton("1:1 Match"); 
		mapOneToOne.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent ae) {
				markOneToOne();
				updateStatusMessage();
			}
		});
		JButton save = new JButton("Save"); 
		save.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent ae) { 
				save();
			}
		});
		JButton reset = new JButton("Reset");
		reset.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent ae){ 
				reset();
			}
		});
		JButton rightfocus = new JButton("Right Focus");
		rightfocus.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent ae){ 
				rightFocus();
			}
		});
		JButton leftfocus = new JButton("Left Focus");
		leftfocus.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent ae){ 
				leftFocus();
			}
		});
		JButton lefthelp = new JButton("Left Stack");
		lefthelp.addActionListener(new ActionListener(){ 
			public void actionPerformed(ActionEvent ae){ 
				readLeftInfo();
			}
		});
		JButton righthelp = new JButton("Right Stack");
		righthelp.addActionListener(new ActionListener(){ 
			public void actionPerformed(ActionEvent ae){ 
				readRightInfo();
			}
		});
		JPanel menu = new JPanel();
		menu.setLayout(new GridLayout(1, 7));
		menu.add(nextLeft);
		menu.add(nextRight);
		menu.add(markDeleted);
		menu.add(markAdded);
		menu.add(mapOneToOne);
		menu.add(save);
		menu.add(reset);
		menu.add(rightfocus);
		menu.add(leftfocus);
		menu.add(lefthelp);
		menu.add(righthelp);
	
		return menu;
	}
	public void readLeftInfo() { 
		JFileChooser chooser = new JFileChooser(new File("jfreechart"));
		chooser.setFileFilter(new MethodStackTraceFilter());
		int returnVal = chooser.showOpenDialog(new JFrame());
		chooser.setName("Open Old Stack Trace Data File");
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File stackTrace = chooser.getSelectedFile();
			leftStackInfo = new MethodStack(stackTrace,2);
		}
		// iterate over all match status and mark deleted if there's no info 
		for (int i=0; i<leftEntityTree.getRowCount(); i++) { 
			TreePath path = leftEntityTree.getPathForRow(i);
			MatchStatus status = getMatchStatus(path);
			if (status!=null && status.examinedByUser == false && status.forwardRule == false
					&& status.backwardRule == false
					&& status.markedUnmatched == false
					&& status.oneToOne == false) {
				if (leftStackInfo.getValue(status.jm) == null) {
					leftEntityTree.setSelectionRow(i);
					markUnmatched(true);
				}
			}
		}
	}
	public void readRightInfo() {
		JFileChooser chooser = new JFileChooser(new File("jfreechart"));
		chooser.setFileFilter(new MethodStackTraceFilter());
		int returnVal = chooser.showOpenDialog(new JFrame());
		chooser.setName("Open New Stack Trace Data File");
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File stackTrace = chooser.getSelectedFile();
			rightStackInfo = new MethodStack(stackTrace,2);
		}
		// iterate over all match status and mark added if there's no info
		for (int i = 0; i < rightEntityTree.getRowCount(); i++) {
			TreePath path = rightEntityTree.getPathForRow(i);
			MatchStatus status = getMatchStatus(path);
			if (status !=null && status.examinedByUser == false && status.forwardRule == false
					&& status.backwardRule == false
					&& status.markedUnmatched == false
					&& status.oneToOne == false) {
				if (rightStackInfo.getValue(status.jm) == null) {
					rightEntityTree.setSelectionRow(i);
					markUnmatched(false);
				}
			}
		}
	}
	public void markUnmatched(boolean left) {
		TreePath[] paths = null;
		if (left) {
			paths = leftEntityTree.getSelectionPaths();
		}else {
			paths = rightEntityTree.getSelectionPaths();
		}
		for (int i=0; i< paths.length; i++ ){ 
			MatchStatus matchStatus = getMatchStatus(paths[i]);
			if (matchStatus.oneToOne == true
					&& matchStatus.examinedByUser == true
					&& matchStatus.forwardRule == false
					&& matchStatus.backwardRule == false) {
				// delete from the manualOneToOne match
				Pair oldLPair = this.manualOnetoOnePairs.getFirstMatchByLeft(matchStatus.jm);
				Pair oldRPair = this.manualOnetoOnePairs.getFirstMatchByRight(matchStatus.jm);
				this.manualOnetoOnePairs.removePair(oldLPair);
				this.manualOnetoOnePairs.removePair(oldRPair);
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[i].getLastPathComponent();
				System.out.println("Seleted To Unmatch"+node.getUserObject()); 
				if  (node.getUserObject() instanceof MatchStatus) {
					node.removeAllChildren();
				}
			}
			matchStatus.markedUnmatched=true;
			if (matchStatus.examinedByUser==false) {
				if (left) { 
					leftExamined++;
				}else rightExamined++;
				matchStatus.examinedByUser=true;
			}
		}
	}
	public void navigateToNext(boolean left){
		JTree tree=null; 
		if (left) {  
			tree = leftEntityTree;
		}else { 
			tree = rightEntityTree;
		}
		for (int i=0; i<tree.getRowCount(); i++) { 
			TreePath path = tree.getPathForRow(i);
			MatchStatus status = getMatchStatus(path);
			if (status !=null &&status.examinedByUser==false && 
				(status.forwardRule==false && 
				status.backwardRule==false && 
				status.markedUnmatched==false && status.oneToOne==false)){ 
				tree.scrollRowToVisible(i);
				tree.setSelectionRow(i);
				return;
			}
		}
	}
	public void rightFocus() { 
		TreePath[] leftpaths= leftEntityTree.getSelectionPaths();
		TreePath leftPath = leftpaths[0];
		DefaultMutableTreeNode leftleaf = (DefaultMutableTreeNode) leftPath
				.getLastPathComponent();
		DefaultMutableTreeNode matchNode = null;
		if (leftleaf.getChildCount()>0) { 
			matchNode =(DefaultMutableTreeNode) leftleaf.getFirstChild();
		}
		if (matchNode!=null && matchNode.getUserObject() instanceof JavaMethod) { 
			JavaMethod matched = (JavaMethod) matchNode.getUserObject();
			for (int i=0; i< rightEntityTree.getRowCount(); i++) {
				TreePath path = rightEntityTree.getPathForRow(i);
				MatchStatus status = getMatchStatus(path);
				if (status!=null&& status.jm.equals(matched))  {
					rightEntityTree.scrollRowToVisible(i-2);
					rightEntityTree.scrollRowToVisible(i+2);
					rightEntityTree.setSelectionPath(path);
				}
			}
		}
	}
	public void leftFocus() { 
		TreePath[] rightpaths= rightEntityTree.getSelectionPaths();
		TreePath rightpath = rightpaths[0];
		DefaultMutableTreeNode rightleaf = (DefaultMutableTreeNode) rightpath
				.getLastPathComponent();
		DefaultMutableTreeNode matchNode = null;
		if (rightleaf.getChildCount()>0) { 
			matchNode = (DefaultMutableTreeNode) rightleaf.getFirstChild();
		}
		if (matchNode!=null&& matchNode.getUserObject() instanceof JavaMethod) { 
			JavaMethod matched = (JavaMethod) matchNode.getUserObject();
			for (int i=0; i< leftEntityTree.getRowCount(); i++) {
				TreePath path = leftEntityTree.getPathForRow(i);
				MatchStatus status = getMatchStatus(path);
				if (status!=null&& status.jm.equals(matched))  { 
					leftEntityTree.scrollRowToVisible(i-2);
					leftEntityTree.scrollRowToVisible(i+2);
					leftEntityTree.setSelectionPath(path);
				}
			}
		}
			
	}
	public void markOneToOne() {
		TreePath[] leftpaths= leftEntityTree.getSelectionPaths();
		TreePath[] rightpaths = rightEntityTree.getSelectionPaths();
		if (leftpaths.length!=1 && rightpaths.length!=1) { 
			System.out.println("We need 1:1 match");
		}
		TreePath leftPath = leftpaths[0];
		TreePath rightPath = rightpaths[0];
		MatchStatus leftStatus = getMatchStatus(leftPath);
		MatchStatus rightStatus = getMatchStatus(rightPath);
		leftStatus.oneToOne= true;
		leftStatus.markedUnmatched = false;
		if (leftStatus.examinedByUser==false){
			leftStatus.examinedByUser= true;
			leftExamined++;
		}
		rightStatus.oneToOne= true;
		rightStatus.markedUnmatched = false;
		if (rightStatus.examinedByUser==false){ 
			rightStatus.examinedByUser=true;
			rightExamined++;
		}
		this.manualOnetoOnePairs.addPair(new Pair(leftStatus.jm, rightStatus.jm));
		// manipulate children in the tree
		DefaultMutableTreeNode leftNode = (DefaultMutableTreeNode)leftPath.getLastPathComponent();
		if (leftNode.getUserObject() instanceof MatchStatus) {
			leftNode.removeAllChildren();
			leftNode.add(new DefaultMutableTreeNode(rightStatus.jm));
		}
		DefaultMutableTreeNode rightNode = (DefaultMutableTreeNode) rightPath.getLastPathComponent();
		if (rightNode.getUserObject() instanceof MatchStatus) { 
			rightNode.removeAllChildren();
			rightNode.add(new DefaultMutableTreeNode(leftStatus.jm));
		}
	}
	
	public MatchStatus getMatchStatus (TreePath path) {
		for (int i = 0; i < path.getPathCount(); i++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
					.getPathComponent(i);
			if (node.getUserObject() instanceof MatchStatus) {
				MatchStatus matchStatus = (MatchStatus) node
						.getUserObject();
				return matchStatus;
			}
		}
		return null;
	}
	public JPanel createEntityPanel(boolean left, RulebasedMatching matching){
		JTree tree = new JTree(createEntityModel(left,matching));
		JScrollPane scrollpane = new JScrollPane();
		scrollpane.setMinimumSize(new Dimension(WIDTH, LENGTH));
		scrollpane.setPreferredSize(new Dimension(WIDTH, LENGTH));
		scrollpane.getViewport().setView(tree);
		// forward entity tree creation
		if (left) {
			this.leftEntityTree =tree;
		}
		else {
			this.rightEntityTree = tree;
		}
	
		JPanel backwardContent = new JPanel();
		backwardContent.add(scrollpane);
		return backwardContent;
	}
	public DefaultTreeModel createEntityModel (boolean left, RulebasedMatching matching){
		String s=null;
		if (left) {
			s ="Old";
		}else s= "New";
		DefaultMutableTreeNode entityRoot = new DefaultMutableTreeNode(s);
		DefaultTreeModel model = new DefaultTreeModel(entityRoot);
		Set<JavaMethod> domainSet = matching.getDomain(left);
		ArrayList<JavaMethod> domainList = new ArrayList<JavaMethod>(domainSet);
		Collections.sort(domainList, new JavaMethodComparator());
		SetOfPairs forwardMatches = matching.getMatches(true);
		SetOfPairs backwardMatches = matching.getMatches(false);
		// update one to one matches
		SetOfPairs manualMatches = this.matchingResult.getManualOneToOneMatches(); 
		
		for (Iterator<JavaMethod> it = domainList.iterator(); it.hasNext();) {
			JavaMethod domainJm = it.next();
			MatchStatus matchStatus = new MatchStatus();
			matchStatus.jm = domainJm;
			SetOfPairs set = new SetOfPairs();
			if (left) {
				ListOfPairs fMP= forwardMatches.getMatchesByLeft(domainJm);
				if (fMP.size()>0) {
					matchStatus.forwardRule=true;
				}
				ListOfPairs bMP = backwardMatches.getMatchesByLeft(domainJm);
				if (bMP.size()>0) {
					matchStatus.backwardRule=true;
				}
				if (fMP.size() == 1 && bMP.size()==1) {
					matchStatus.oneToOne=true;
				}
				set.addSetOfPairs(fMP); 
				set.addSetOfPairs(bMP);
				if (set.size()>1) {
					matchStatus.split=true;
				}
				Pair manualOneToOne = manualMatches.getFirstMatchByLeft(domainJm);
				if (manualOneToOne!=null) { 
					set.addPair(manualOneToOne);
					matchStatus.oneToOne= true;
					matchStatus.examinedByUser= true;
					matchStatus.markedUnmatched= false;
					leftExamined++;
				}
			} else {
				ListOfPairs fMP = forwardMatches.getMatchesByRight(domainJm);
				if (fMP.size() > 0) {
					matchStatus.forwardRule = true;
				}
				ListOfPairs bMP = backwardMatches.getMatchesByRight(domainJm);
				if (bMP.size() > 0) {
					matchStatus.backwardRule = true;
				}
				if (fMP.size() == 1 && bMP.size()==1) {
					matchStatus.oneToOne=true;
				}
				set.addSetOfPairs(fMP);
				set.addSetOfPairs(bMP);
				if (set.size() > 1) {
					matchStatus.merge = true;
				}
				Pair manualOneToOne = manualMatches.getFirstMatchByRight(domainJm);
				if (manualOneToOne!=null){ 
					set.addPair(manualOneToOne);
					matchStatus.oneToOne =true;
					matchStatus.examinedByUser= true;
					matchStatus.markedUnmatched = false;
					rightExamined++;
				}
			}
			DefaultMutableTreeNode matchStatusNode = new DefaultMutableTreeNode(
					matchStatus);
			entityRoot.add(matchStatusNode);

			for (Iterator<Pair> pIt = set.iterator(); pIt.hasNext();) {
				Pair p = pIt.next();
				DefaultMutableTreeNode child = null;
				if (left) {
					child = new DefaultMutableTreeNode(p.getRight());
				} else {
					child = new DefaultMutableTreeNode(p.getLeft());
				}
				matchStatusNode.add(child);
			}
		}
		return model;
	}
	public void save() { 
		// push the one to one matching results;
		if (this.matchingResult!=null) { 
			System.out.println("Current Manual Matches");
			System.out.println(this.manualOnetoOnePairs);
			this.matchingResult.setManualOneToOneMatches(this.manualOnetoOnePairs);
		}
	}
	public void reset() { 
		this.manualOnetoOnePairs.reset();
		this.matchingResult.setManualOneToOneMatches(this.manualOnetoOnePairs);
	}
//	public void writeElement() { 
//		
//	}
//	public static ManualMatchingInput readElement(Element manualInput) {
//		if (!manualInput.getTagName().equals(xmlTag)){ 
//			return null;
//		}
//		int leftSize = new Integer(manualInput.getAttribute("leftSize")).intValue();
//		int rightSize = new Integer(manualInput.getAttribute("rightSize")).intValue();
//		MatchStatus [] leftMatches = new MatchStatus[leftSize];
//		MatchStatus [] rightMatches = new MatchStatus[rightSize];
//		NodeList children = manualInput.getChildNodes();
//		SetOfPairs oneToOneMatches = null;
//		int left =0; 
//		int right =0; 
//		
//		for (int i=0; i< children.getLength(); i++) { 
//			if (children.item(i) instanceof Element) { 
//				Element child = (Element) children.item(i);
//				if (child.getTagName().endsWith("leftMatchStatus")){ 
//					MatchStatus leftMatchStatus = readMatchStatusElement((Element)child.getFirstChild());
//					leftMatches[left] = leftMatchStatus;
//					left++;
//				}
//				else if (child.getTagName().equals("rightMatchStatus")){ 
//					MatchStatus rightMatchStatus = readMatchStatusElement((Element)child.getFirstChild());
//					rightMatches[right] = rightMatchStatus;
//					right++;
//				}else if (child.getTagName().equals(SetOfPairs.getXMLTag())){ 
//					oneToOneMatches = SetOfPairs.readElement((Element) child.getFirstChild());
//				}
//			}
//		}
//		ManualMatchingInput manualMatchInput = new ManualMatchingInput(leftMatches, rightMatches, oneToOneMatches);
//		return manualMatchInput;
//	}
//	public void writeMatchStatusElement(Element parent){ 
//		
//	}
//	public static MatchStatus readMatchStatusElement(Element matchStatus){ 
//		return null;
//	}
}