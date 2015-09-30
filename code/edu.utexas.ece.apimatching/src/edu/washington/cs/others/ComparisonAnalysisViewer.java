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
package edu.washington.cs.others;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import edu.washington.cs.extractors.ProgramSnapshot;
import edu.washington.cs.induction.FileNameService;
import edu.washington.cs.induction.MatchingResultViewer;
import edu.washington.cs.induction.ReadDirectories;
import edu.washington.cs.induction.Refactoring;
import edu.washington.cs.induction.RulebasedMatching;
import edu.washington.cs.rules.JavaMethod;
import edu.washington.cs.rules.JavaMethodComparator;
import edu.washington.cs.rules.TransformationRule;
import edu.washington.cs.util.Comparison;
import edu.washington.cs.util.ListOfPairs;
import edu.washington.cs.util.Pair;
import edu.washington.cs.util.SetOfPairs;

public class ComparisonAnalysisViewer extends MatchingResultViewer implements
		KeyListener {

	public final static int XING = 0;

	public final static int SKIM = 1;

	public final static int WD = 2;
	
	public final static int NOTHING = 3;
	
	private final ProgramSnapshot oldP;

	private final ProgramSnapshot newP;

	private double SEED_TH;

	private double EXCEPTION_TH;

	private final SetOfPairs originalOtherMatches;

	private static Set<JavaMethod> highlightDomainItems = null;

	protected String VIEWER_NAME = "Comparison Analysis";

	private JTree matchTree;

	private static int domainItemRow = 0;

	private static int conflictItemRow = 0;

	private static CodeViewer codeViewer;

	private static EntityViewItemRenderer itemRenderer = null;

	private class DomainItem {
		boolean conflict = false;

		boolean examined = false;

		String flag;

		final JavaMethod item;

		final Pair myFMatch;

		final ListOfPairs myBMatches;

		final ListOfPairs otherMatches;

		public DomainItem(JavaMethod jm, Pair myFMatch, ListOfPairs myBMatches,
				ListOfPairs otherMatches, boolean Nto1, boolean unchanged) {
			if (myFMatch != null || unchanged) {
				flag = "f1:b";
			} else if (Nto1) {
				flag = "fN:b";
			} else {
				flag = "f0:b";
			}
			if (myBMatches != null) {
				flag = flag + myBMatches.size();
			} else if (unchanged) {
				flag = flag + "1";
			} else {
				flag = flag + "0";
			}

			if (otherMatches != null) {
				flag = flag + "+"+otherMatches.size();
			} else {
				flag = flag + "-";
			}
			SetOfPairs FBMatches = new SetOfPairs();
			if (myFMatch != null)
				FBMatches.addPair(myFMatch);
			if (myBMatches != null)
				FBMatches.addSetOfPairs(myBMatches);
			if (unchanged)
				FBMatches.addPair(new Pair(jm, jm));
			SetOfPairs otherSet = new SetOfPairs();
			otherSet.addSetOfPairs(otherMatches);
			if (otherMatches != null
					&& Comparison.leftMinusRight(otherSet, FBMatches).size() > 0)
			// false negative
			{
				flag = "FN  " + flag;
				conflict = true;
			} else {
				FBMatches.removeListOfPairs(otherMatches);
				if (unchanged == false && FBMatches.size() > 0) {
					flag = "FP  " + flag;
					conflict = true;
				}
			}
			this.myFMatch = myFMatch;
			this.myBMatches = myBMatches;
			this.otherMatches = otherMatches;
			this.item = jm;
		}

		public String toString() {
			String s = "";
			if (examined)
				s = s + "LABEL";
			return s + flag + "   " + item.toString();
		}

		public void setExamined() {
			this.examined = true;
		}
	}

	private class MatchItem {
		final Pair match;

		boolean deleted = false;

		public MatchItem(Pair p) {
			this.match = p;
		}

		public String toString() {
			String s = "";
			if (deleted)
				s = s + "DEL ";
			s = s + match.getRight().toString();
			return s;
		}

		public void setDeleted(boolean b) {
			deleted = b;
		}
	}

	private class MyForwardMatchItem extends MatchItem {
		public MyForwardMatchItem(Pair myMatch) {
			super(myMatch);
		}
	}

	private class MyBackwardMatchItem extends MatchItem {
		public MyBackwardMatchItem(Pair myMatch) {
			super(myMatch);
		}
	}

	private class OtherMatchItem extends MatchItem {
		public OtherMatchItem(Pair otherMatch) {
			super(otherMatch);
		}
	}

	public JPanel createMenuPanel() {

		JButton nextconflictAction = new JButton("Conflict");
		nextconflictAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				navigateConflictDomainItemAction();
			}
		});
		JButton saveAction = new JButton("Save");
		saveAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				save();
			}
		});
		// button creation
		JButton keepAction = new JButton("Keep");
		keepAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				keepMatches();
			}
		});
		JButton deleteAction = new JButton("Delete");
		deleteAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				deleteMatches();
			}
		});

		JButton codeAction = new JButton("Code View");
		codeAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				codeView();
			}
		});
		JButton nextRuleAction = new JButton("Next Rule");
		nextRuleAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (ruleTabbedPane.getSelectedIndex() == 0) {
					navigateRuleAction(true);
				} else {
					navigateRuleAction(false);
				}
			}
		});
		JButton nextEntityAction = new JButton("Next Item");
		nextEntityAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				navigateDomainItemAction();
			}
		});

		JButton ruleToMatchesAction = new JButton("Matches");
		ruleToMatchesAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ruleToMatchItem();
			}
		});

		JButton matchToRulesAction = new JButton("Rules");
		matchToRulesAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				matchItemToRules();
			}
		});

		JButton evaluate = new JButton("Evaluate");
		evaluate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				evaluate();
			}
		});
		JPanel menu = new JPanel();
		menu.setSize(WIDTH, LENGTH / 8);
		menu.setLayout(new GridLayout(1, 10));
		menu.add(keepAction);
		menu.add(deleteAction);
		menu.add(nextconflictAction);
		menu.add(nextRuleAction);
		menu.add(nextEntityAction);
		menu.add(ruleToMatchesAction);
		menu.add(matchToRulesAction);
		menu.add(codeAction);
		menu.add(saveAction);
		menu.add(evaluate);
		return menu;
	}

	protected JPanel createEntityViewer() {
		this.matchTree = new JTree(createEntityModel(true));
		JScrollPane matchPane = new JScrollPane();
		matchPane.setMinimumSize(new Dimension(WIDTH, LENGTH));
		matchPane.setPreferredSize(new Dimension(WIDTH, LENGTH));
		matchPane.getViewport().setView(matchTree);
		this.itemRenderer = new EntityViewItemRenderer();
		this.matchTree.setCellRenderer(this.itemRenderer);
		// forward entity tree creation
		matchTabbedPane = new JTabbedPane();
		matchTabbedPane.add(matchPane, "Forward/Backward/ Other Entities");
		matchTree.addKeyListener(this);
		JPanel content = new JPanel();
		content.add(matchTabbedPane);
		return content;

	}

	public ComparisonAnalysisViewer(RulebasedMatching matchingResult,
			ProgramSnapshot oldP, ProgramSnapshot newP, int kind,
			double SEED_TH, double EX_TH) {

		super(matchingResult, "Comparison Analysis");
		this.oldP = oldP;
		this.newP = newP;
		this.SEED_TH = SEED_TH;
		this.EXCEPTION_TH = EX_TH;
		SetOfPairs evalData = null;
		if (kind == XING) {
			File xingMatch = FileNameService.getXSMatchFile(oldP.getProject(),
					oldP.getVersion(), newP.getVersion());
			evalData = SetOfPairs.readXMLFile(xingMatch.getAbsolutePath());
		} else if (kind == SKIM) {
			int oldRevId = new Integer(oldP.getVersion()).intValue();
			// remember transaction number is one less than oldRevId. 
			File skimMatchXML = FileNameService.getSKimMatchFile(oldP.getProject(),
					oldRevId-1);
			System.out.println("Reading "+ skimMatchXML.getName());
			evalData = SetOfPairs.readXMLFile(skimMatchXML.getAbsolutePath());
			System.out.println("SKim Match "+ evalData.size());
		} else if (kind == WD) {
			File wd_allXML = FileNameService.getWDMatch_All_File(oldP
					.getProject(), oldP.getVersion());
			SetOfPairs rcAll = SetOfPairs.readXMLFile(wd_allXML.getAbsolutePath());
			File wd_bestXML = FileNameService.getWDMatch_Best_File(oldP
					.getProject(), oldP.getVersion());
			SetOfPairs rcBest = SetOfPairs.readXMLFile(wd_bestXML.getAbsolutePath());
			evalData = new SetOfPairs();
			evalData.addSetOfPairs(rcAll);
			evalData.addSetOfPairs(rcBest);
		} else { 
			evalData = new SetOfPairs();
		}
		this.originalOtherMatches = evalData;

		super.initialize();
	}

	protected DefaultTreeModel createEntityModel(boolean ignore) {
		
	
		Set<JavaMethod> common = oldP.common(newP);
		String s = null;
		ArrayList<JavaMethod> domainList;
		s = "Forward/Backward/ Evaluation Entities";
		domainList = oldP.getMethods();
		// create entity model
		DefaultMutableTreeNode entityRoot = new DefaultMutableTreeNode(s);
		DefaultTreeModel model = new DefaultTreeModel(entityRoot);
		Collections.sort(domainList, new JavaMethodComparator());

		SetOfPairs myforwardMatches = this.matchingResult.getMatches(true);
		SetOfPairs mybackwardMatches = this.matchingResult.getMatches(false);
		System.out.println("MyFowardMatches " + myforwardMatches.size());
		System.out.println("MyBackwardMatches " + mybackwardMatches.size());

		for (Iterator<JavaMethod> it = domainList.iterator(); it.hasNext();) {
			JavaMethod domainJm = it.next();

			Pair<JavaMethod> myFMatch = null;
			ListOfPairs myBMatches = null;
			
			myFMatch = myforwardMatches.getFirstMatchByLeft(domainJm);
			myBMatches = mybackwardMatches.getMatchesByLeft(domainJm);

			ListOfPairs otherMatches = null;
			if (originalOtherMatches != null)
				otherMatches = originalOtherMatches.getMatchesByLeft(domainJm);
			
			boolean unchanged = (common.contains(domainJm));
			boolean NTO1 = myFMatch != null
					&& (common.contains(myFMatch.getRight()) || myforwardMatches
							.includeRight(myFMatch.getRight()));
			DomainItem domainItem = new DomainItem(domainJm, myFMatch,
					myBMatches, otherMatches, NTO1, unchanged);
			DefaultMutableTreeNode domainItemNode = new DefaultMutableTreeNode(
					domainItem);
			entityRoot.add(domainItemNode);

			if (myFMatch == null && otherMatches == null)
				continue;
			if (myFMatch != null) {
				MyForwardMatchItem mine = new MyForwardMatchItem(myFMatch);
				DefaultMutableTreeNode mineNode = new DefaultMutableTreeNode(
						mine);
				domainItemNode.add(mineNode);
			}

			if (myBMatches != null) {
				// iterate all backward Matches
				for (Iterator<Pair> bit = myBMatches.iterator(); bit.hasNext();) {
					Pair bMatch = bit.next();
					MyBackwardMatchItem mine = new MyBackwardMatchItem(bMatch);
					DefaultMutableTreeNode mineNode = new DefaultMutableTreeNode(
							mine);
					domainItemNode.add(mineNode);
				}
			}
			if (otherMatches != null) {
				for (int om = 0; om < otherMatches.size(); om++) {
					OtherMatchItem other = new OtherMatchItem(otherMatches.get(om));
					DefaultMutableTreeNode otherNode = new DefaultMutableTreeNode(
							other);
					domainItemNode.add(otherNode);
				}
			}

		}
		return model;
	}

	public static void main(String args[]) {
		compare("jfreechart_list_CGE", "jfreechart", 0.7, 0.34,"0.9.8", XING);
		
//		compare ("carol_checkin_list", "carol", 0.7, 0.34, "429", NOTHING);
//		compare ("dnsjava_list", "dnsjava", 0.7, 0.34, "1.0.2", NOTHING);
//		compare ("carol_checkin_list", "carol", 0.7, 0.34, "429", NOTHING);
//		compare ("jhotdraw_list", "jhotdraw", 0.7, 0.34, "5.2", NOTHING);
//		
	}

	public static void compare(String dirList, String project, double SEED_TH,
			double EXCEPTION_TH, String oldVersion, int evalkind) {
		File[] dirs = ReadDirectories.getDirectories(dirList);
		int loopend = dirs.length - 1;
		for (int i = 0; i < loopend; i++) {
			if (dirs[i].getAbsolutePath().indexOf(oldVersion) > 0) {
				ProgramSnapshot oldP = new ProgramSnapshot(project, dirs[i]);
				ProgramSnapshot newP = new ProgramSnapshot(project, dirs[i + 1]);

				if (oldP.getVersion().equals(oldVersion)) {
					File matchingFile = FileNameService.getMatchingXMLFile(
							oldP, newP, SEED_TH, EXCEPTION_TH);
					System.out.println(matchingFile.getAbsolutePath());
					RulebasedMatching rb = RulebasedMatching
							.readXMLFile(matchingFile.getAbsolutePath());
				//	new Refactoring(rb).print();
					JPanel panel = new ComparisonAnalysisViewer(rb, oldP, newP,
							evalkind, SEED_TH, EXCEPTION_TH);
					JFrame frame = new JFrame();
					frame.setSize(1280, 800);
					frame.getContentPane().add(panel);
					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					frame.setVisible(true);
					return;
				}
			}
		}
	}

	public void codeView() {
		
		TreePath[] paths = matchTree.getSelectionPaths();
		if (paths.length > 1 || paths.length == 0) {
			System.out.println("Select only one path in the matchTree");
			return;
		}
		TreePath path = matchTree.getSelectionPath();
		DefaultMutableTreeNode leaf = (DefaultMutableTreeNode) path
				.getLastPathComponent();
		Object value = leaf.getUserObject();
		if (value instanceof DomainItem) {
			// nothing
			System.out.println("Select only MatchItem");
			return;
		} else if (value instanceof MatchItem) {
			MatchItem item = (MatchItem) value;
			codeViewer = new CodeViewer(item.match, oldP, newP);
		}

	}

	public void keepMatches() {
		// domainItem -> Nothing
		// MyForwardMatchItem -> myForwardRule
		// myBackwardMatchItem -> myBackwardRule
		// otherMatchItem -> otherMatchItem
		TreePath[] paths = matchTree.getSelectionPaths();
		for (int i = 0; i < paths.length; i++) {
			TreePath path = paths[i];
			DefaultMutableTreeNode leaf = (DefaultMutableTreeNode) path
					.getLastPathComponent();
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) leaf
					.getParent();
			Object value = leaf.getUserObject();
			if (value instanceof DomainItem) {
				// nothing
				System.out.println("Select only Match Items");
				return;
			} else if (value instanceof MatchItem) {
				MatchItem item = (MatchItem) value;
				item.setDeleted(false);
				DomainItem dm = (DomainItem) parent.getUserObject();
				dm.setExamined();
			}
		}
		updateStatusMessage();
	}

	public void deleteMatches() {
		// domainItem -> Nothing
		// MyForwardMatchItem -> myForwardRule
		// myBackwardMatchItem -> myBackwardRule
		// otherMatchItem -> otherMatchItem
		TreePath[] paths = matchTree.getSelectionPaths();
		for (int i = 0; i < paths.length; i++) {
			TreePath path = paths[i];
			DefaultMutableTreeNode leaf = (DefaultMutableTreeNode) path
					.getLastPathComponent();
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) leaf
					.getParent();
			Object value = leaf.getUserObject();
			if (value instanceof DomainItem) {
				// nothing
				System.out.println("Select only Match Items");
				return;
			} else if (value instanceof MatchItem) {
				MatchItem item = (MatchItem) value;
				item.setDeleted(true);
				DomainItem dm = (DomainItem) parent.getUserObject();
				dm.setExamined();
			}
		}
		updateStatusMessage();
	}

	public void matchItemToRules() {
		// domainItem -> Nothing
		// MyForwardMatchItem -> myForwardRule
		// myBackwardMatchItem -> myBackwardRule
		// otherMatchItem -> disabled
		TreePath[] paths = matchTree.getSelectionPaths();
		if (paths.length > 1 || paths.length == 0) {
			System.out.println("Select only one path in the matchTree");
			return;
		}
		TreePath path = matchTree.getSelectionPath();
		DefaultMutableTreeNode leaf = (DefaultMutableTreeNode) path
				.getLastPathComponent();
		Object value = leaf.getUserObject();
		if (value instanceof DomainItem || value instanceof OtherMatchItem) {
			// nothing
			System.out
					.println("Select only MyForwardMatchItem or myBackwardMatchItem");
			return;
		} else if (value instanceof MyForwardMatchItem) {
			MyForwardMatchItem myFItem = (MyForwardMatchItem) value;
			// pick up forward rule
			JavaMethod left = (JavaMethod) myFItem.match.getLeft();
			Set<TransformationRule.RuleID> rules = codependentRule(true, left);
			highlightRelevantRules(rules);
			navigateRuleAction(true);
			ruleTabbedPane.setSelectedIndex(0);
		} else if (value instanceof MyBackwardMatchItem) {
			MyBackwardMatchItem myBItem = (MyBackwardMatchItem) value;
			JavaMethod right = (JavaMethod) myBItem.match.getRight();
			Set<TransformationRule.RuleID> rules = codependentRule(false, right);
			highlightRelevantRules(rules);
			navigateRuleAction(true);
			ruleTabbedPane.setSelectedIndex(1);
		}
	}

	protected void ruleToMatchItem() {
		boolean forward = true;
		TreePath[] paths = null;
		if (ruleTabbedPane.getSelectedIndex() == 0) {
			// forward
			forward = true;
			paths = fRuleTree.getSelectionPaths();
		}
		if (ruleTabbedPane.getSelectedIndex() == 1) {
			// backward
			forward = false;
			paths = bRuleTree.getSelectionPaths();
		}

		if (paths != null && paths.length == 1) {
			TreePath path = paths[0];
			TransformationRule rule = getRule(path);
			ListOfPairs pairs = rule.getPositiveMatches();
			// highlight DomainItem
			highlightDomainItems((Set<JavaMethod>) pairs.getLeftDomain());
			System.out.println("Highlight " + pairs.size() + "Domain Items");
		} else {
			System.out.println("Select only one rule");
		}
	}

	private static void highlightDomainItems(Set<JavaMethod> domainItems) {
		// highlightDomainItems
		if (highlightDomainItems == null) {
			highlightDomainItems = new TreeSet<JavaMethod>();
		} else {
			highlightDomainItems.clear();
		}
		highlightDomainItems.addAll(domainItems);
	}

	private void navigateConflictDomainItemAction() {
		for (int i = (1 + conflictItemRow); i < matchTree.getRowCount(); i++) {
			TreePath path = matchTree.getPathForRow(i);
			if (path.getPath().length == 2) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
						.getPath()[1];
				if (node.getUserObject() instanceof DomainItem) {

					DomainItem dm = (DomainItem) node.getUserObject();
					if (dm.conflict == true && dm.examined == false) {
						System.out.println("scrolling to:" + dm.toString());
						matchTree.expandPath(path);
						matchTree.scrollRowToVisible(i + 4);
						matchTree.setSelectionPath(path);
						conflictItemRow = i;
						return;
					}
				}
			}
		}
		conflictItemRow = -1;
	}

	private void navigateDomainItemAction() {
		if (highlightDomainItems == null || highlightDomainItems.size() == 0)
			return;
		for (int i = (1 + domainItemRow); i < matchTree.getRowCount(); i++) {
			TreePath path = matchTree.getPathForRow(i);
			if (path.getPath().length == 2) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
						.getPath()[1];
				if (node.getUserObject() instanceof DomainItem) {
					DomainItem dm = (DomainItem) node.getUserObject();
					if (highlightDomainItems.contains(dm.item)) {
						System.out.println("scrolling to:" + dm.toString());
						matchTree.expandPath(path);
						matchTree.scrollRowToVisible(i + 4);
						matchTree.setSelectionPath(path);
						domainItemRow = i;
						return;
					}
				}
			}
		}
		domainItemRow = -1;
	}

	protected JavaMethod getJavaMethod(TreePath path) {
		// alway return the left hand side
		for (int i = 0; i < path.getPathCount(); i++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
					.getPathComponent(i);
			if (node.getUserObject() instanceof JavaMethod) {
				JavaMethod jm = (JavaMethod) node.getUserObject();
				return jm;
			}
			if (node.getUserObject() instanceof DomainItem) {
				DomainItem dm = (DomainItem) node.getUserObject();
				return dm.item;
			}
			if (node.getUserObject() instanceof MyForwardMatchItem) {
				MyForwardMatchItem mm = (MyForwardMatchItem) node
						.getUserObject();
				return (JavaMethod) mm.match.getLeft();
			}
			if (node.getUserObject() instanceof OtherMatchItem) {
				OtherMatchItem om = (OtherMatchItem) node.getUserObject();
				return (JavaMethod) om.match.getLeft();
			}
		}
		return null;
	}

	public void save() {
		// going over all matches and create an evaluation data set.
		// n:n matches
		SetOfPairs evalMatches = new SetOfPairs();
		DefaultTreeModel model = (DefaultTreeModel) matchTree.getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

		for (int i = 0; i < root.getChildCount(); i++) {
			// get the leaf
			DefaultMutableTreeNode domain = (DefaultMutableTreeNode) root
					.getChildAt(i);
			if (domain.getUserObject() instanceof DomainItem) {
				DomainItem item = (DomainItem) domain.getUserObject();
				for (int j = 0; j < domain.getChildCount(); j++) {
					DefaultMutableTreeNode match = (DefaultMutableTreeNode) domain
							.getChildAt(j);
					if (match.getUserObject() instanceof MatchItem) {
						MatchItem mi = (MatchItem) match.getUserObject();
						Pair pair = mi.match;
						if (mi.deleted == false
//								&& !pair.getLeft().equals(pair.getRight())) {
								){
							// add to the data set
							evalMatches.addPair(pair);
						}
					}
				}

			}
		}
		File nToN = FileNameService.getNtoNEvalFile(oldP, newP, SEED_TH,
				EXCEPTION_TH);
		if (!nToN.exists()) {
			System.out.println("Writing to " + nToN.getAbsolutePath());
			evalMatches.writeXMLFile(nToN.getAbsolutePath());
		} else {
			System.out.println("File already exists, cannot write to "
					+ nToN.getAbsolutePath());
		}
		
	}

	public class EntityViewItemRenderer extends DefaultTreeCellRenderer {
		public Component getTreeCellRendererComponent(JTree tree, Object node,
				boolean selected, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			DefaultMutableTreeNode treenode = (DefaultMutableTreeNode) node;
			Object value = treenode.getUserObject();
			super.getTreeCellRendererComponent(tree, value, selected, expanded,
					leaf, row, hasFocus);
			if (value instanceof DomainItem) {
				DomainItem dm = (DomainItem) value;
				if ((dm.conflict && dm.examined)
						|| (!dm.conflict && !dm.examined)) {
					setBackground(Color.WHITE);
				} else
					setForeground(Color.BLUE);
				if (highlightDomainItems != null
						&& highlightDomainItems.contains(dm.item)) {
					setForeground(Color.DARK_GRAY);
				}
			}
			if (value instanceof MyForwardMatchItem) {
				setForeground(Color.MAGENTA);
				// return myMatchChecks.get(value);
			} else if (value instanceof OtherMatchItem) {
				setForeground(Color.RED);
				// return otherMatchCheck.get(value);
			}
			return this;
		}
	}

	protected void updateStatusMessage() {
		System.out
				.println("Updating Status Message in Comparison Analysis Viewer");
		int numDomainConflict = 0;
		int numDomainExamined = 0;
		int numFDelete = 0;
		int numFTotal = 0;
		int numBDelete = 0;
		int numBTotal = 0;
		int numODelete = 0;
		int numOTotal = 0;
		DefaultTreeModel model = (DefaultTreeModel) matchTree.getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
		for (int i = 0; i < root.getChildCount(); i++) {
			// get the leaf
			DefaultMutableTreeNode domain = (DefaultMutableTreeNode) root
					.getChildAt(i);
			if (domain.getUserObject() instanceof DomainItem) {
				DomainItem dItem = (DomainItem) domain.getUserObject();
				if (dItem.conflict) {
					numDomainConflict++;
					if (dItem.examined)
						numDomainExamined++;

				}
				for (int j = 0; j < domain.getChildCount(); j++) {
					DefaultMutableTreeNode match = (DefaultMutableTreeNode) domain
							.getChildAt(j);
					Object value = match.getUserObject();
					if (value instanceof MyForwardMatchItem) {
						MyForwardMatchItem fItem = (MyForwardMatchItem) (value);
						if (fItem.deleted)
							numFDelete++;
						numFTotal++;
					} else if (value instanceof MyBackwardMatchItem) {
						MyBackwardMatchItem bItem = (MyBackwardMatchItem) (value);
						if (bItem.deleted)
							numBDelete++;
						numBTotal++;
					} else if (value instanceof OtherMatchItem) {
						OtherMatchItem oItem = (OtherMatchItem) (value);
						if (oItem.deleted)
							numODelete++;
						numOTotal++;
					}
				}
			}
		}
		String d = "DomainItem.Examined/ conflict  " + numDomainExamined + "/"
				+ numDomainConflict;
		String f = "ForwardMatch.delete / total:     " + numFDelete + "/"
				+ numFTotal;
		String b = "BackwardMatch.delete / total:     " + numBDelete + "/"
				+ numBTotal;
		String o = "OtherMatch.delete / total:     " + numODelete + "/"
				+ numOTotal;

		String s[] = { d, f, b, o };
//		System.out.println("Stat");
//		System.out.println(d);
//		System.out.println(f);
//		System.out.println(b);
//		System.out.println(o);

		DefaultListModel currentModel = null;
		if (currentStatus == null) {
			// beginnning
			currentStatus = new JList();
			currentModel = new DefaultListModel();
			currentStatus.setModel(currentModel);
		} else {
			currentModel = (DefaultListModel) currentStatus.getModel();
		}
		oldStatus = new JList();
		currentModel.clear();
		for (int i = 0; i < s.length; i++) {
			currentModel.insertElementAt(s[i], i);
		}
		currentStatus.setVisible(true);
	}

	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
//		System.out.println(e.getKeyChar() + " is typed");

	}

	public void keyReleased(KeyEvent e) {
		char c = e.getKeyChar();
		if (c == 'c') {
			navigateConflictDomainItemAction();
		} else if (c == 'r') {
			deleteMatches();
		} else if (c == 'k') {
			keepMatches();
		} else if (c == 'v') {
			if (codeViewer != null)
				codeViewer.dispose();
			codeView();
		} else if (c == 's') {
			if (codeViewer != null)
				codeViewer.dispose();
		}
	}

	protected void evaluate() {
		JFileChooser chooser = FileNameService.openNtoNLabelledDialog();
		int returnVal = chooser.showOpenDialog(new JFrame());
		chooser.setName("Open Evaluation Data File");
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File inputEvalData = chooser.getSelectedFile();
			System.out
					.println("You chose to open to this evaluation data file: "
							+ inputEvalData.getName());
			if (inputEvalData.exists()) {
				SetOfPairs nToNLabeledMatches = SetOfPairs
						.readXMLFile(inputEvalData.getAbsolutePath());
				if (nToNLabeledMatches != null) {
					// evaluate using this matches.
					if (this.matchingResult != null) {
						this.matchingResult.print3WayComparisonChart(
								System.out, originalOtherMatches,
								nToNLabeledMatches);
					}
				}
			}
		}
	}

}