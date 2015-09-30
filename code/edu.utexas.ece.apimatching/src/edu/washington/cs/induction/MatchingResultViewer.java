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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import edu.washington.cs.rules.JavaMethod;
import edu.washington.cs.rules.JavaMethodComparator;
import edu.washington.cs.rules.Scope;
import edu.washington.cs.rules.ScopeDisjunction;
import edu.washington.cs.rules.TransformationRule;
import edu.washington.cs.util.ListOfPairs;
import edu.washington.cs.util.Pair;
import edu.washington.cs.util.SetOfPairs;

public class MatchingResultViewer extends JPanel {

	protected JTree fRuleTree;

	protected JTree bRuleTree;

	private JTree fEntityTree;

	protected JTree bEntityTree;

	protected JTabbedPane ruleTabbedPane;

	protected JTabbedPane matchTabbedPane;

	protected JList currentStatus = null;

	protected JList oldStatus = null;

	// private JLabel ruleMsg;
	// private JLabel matchMsg;
	protected final RulebasedMatching matchingResult;

	protected static RuleRenderer ruleHighlighter = null;

	private static EntityRenderer entityHighlighter = null;

	protected static ArrayList<TransformationRule.RuleID> highlightRules = null;

	protected static ArrayList<JavaMethod> highlightEntities = null;

	protected static int fCurrentRuleRow = -1;

	protected static int bCurrentRuleRow = -1;

	protected static int fCurrentEntityRow = -1;

	protected static int bCurrentEntityRow = -1;

	protected static int WIDTH = 1200;
 
	protected static int LENGTH = 320;

	protected static int MARGIN = 50;

	protected static String VIEWER_NAME = "Matching Result Viewer";
	/* getting data from tree */
	protected TransformationRule getRule(TreePath path) {
		for (int i = 0; i < path.getPathCount(); i++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
					.getPathComponent(i);
			if (node.getUserObject() instanceof TransformationRule) {
				TransformationRule rule = (TransformationRule) node
						.getUserObject();
				return rule;
			}
		}
		return null;
	}

	protected Scope getScope(TreePath path) {
		for (int i = 0; i < path.getPathCount(); i++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
					.getPathComponent(i);
			if (node.getUserObject() instanceof Scope) {
				Scope scope = (Scope) node.getUserObject();
				return scope;
			}
		}
		return null;
	}

	protected Pair getMatch(TreePath path) {
		for (int i = 0; i < path.getPathCount(); i++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
					.getPathComponent(i);
			if (node.getUserObject() instanceof Pair) {
				Pair pair = (Pair) node.getUserObject();
				return pair;
			}
		}
		return null;
	}

	protected JavaMethod getJavaMethod(TreePath path) {
		for (int i = 0; i < path.getPathCount(); i++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
					.getPathComponent(i);
			if (node.getUserObject() instanceof JavaMethod) {
				JavaMethod jm = (JavaMethod) node.getUserObject();
				return jm;
			}
		}
		return null;
	}

	protected JPanel createRuleViewer() {
		TransformationRule[] forwardRules = matchingResult.getSortedRules(true);
		TransformationRule[] backwardRules = matchingResult
				.getSortedRules(false);

		DefaultTreeModel fRuleTreeModel = createRuleModel(forwardRules, true);
		// forward rule tree creation
		fRuleTree = new JTree(fRuleTreeModel);
		fRuleTree.setCellRenderer(ruleHighlighter);
		JScrollPane fRuleTreePane = new JScrollPane();
		fRuleTreePane.setMinimumSize(new Dimension(WIDTH, LENGTH));
		fRuleTreePane.setPreferredSize(new Dimension(WIDTH, LENGTH));
		fRuleTreePane.getViewport().setView(fRuleTree);

		// backward rule tree creation
		DefaultTreeModel bRuleTreeModel = createRuleModel(backwardRules, false);
		bRuleTree = new JTree(bRuleTreeModel);
		bRuleTree.setCellRenderer(ruleHighlighter);
		JScrollPane bRuleTreePane = new JScrollPane();
		bRuleTreePane.setMinimumSize(new Dimension(WIDTH, LENGTH));
		bRuleTreePane.setPreferredSize(new Dimension(WIDTH, LENGTH));
		bRuleTreePane.getViewport().setView(bRuleTree);

		ruleTabbedPane = new JTabbedPane();
		ruleTabbedPane.add(fRuleTreePane, "Forward Rules");
		ruleTabbedPane.add(bRuleTreePane, "Backward Rules");

		JPanel ruleContent = new JPanel();
		ruleContent.add(ruleTabbedPane);
		ruleContent.setVisible(true);
		return ruleContent;
		
	}

	protected JPanel createEntityViewer() {
		// forward entity tree creation

		fEntityTree = new JTree(createEntityModel(true));
		JScrollPane fEntityPane = new JScrollPane();
		fEntityPane.setMinimumSize(new Dimension(WIDTH, LENGTH));
		fEntityPane.setPreferredSize(new Dimension(WIDTH, LENGTH));
		fEntityPane.getViewport().setView(fEntityTree);

		bEntityTree = new JTree(createEntityModel(false));

		JScrollPane bEntityPane = new JScrollPane();
		bEntityPane.setMinimumSize(new Dimension(WIDTH, LENGTH));
		bEntityPane.setPreferredSize(new Dimension(WIDTH, LENGTH));
		bEntityPane.getViewport().setView(bEntityTree);
	
		// create tabbed pane
		matchTabbedPane = new JTabbedPane();
		matchTabbedPane.add(fEntityPane, "Forward Entities");
		matchTabbedPane.add(bEntityPane, "Backward Entities");

		JPanel backwardContent = new JPanel();
		backwardContent.add(matchTabbedPane);
		
		fEntityTree.setCellRenderer(entityHighlighter);
		bEntityTree.setCellRenderer(entityHighlighter);
		backwardContent.setVisible(true);
		return backwardContent;

	}

	private void modifyAction(boolean keep) {
		TreePath[] paths = null;
		boolean forward = true;
		if (ruleTabbedPane.getSelectedIndex() == 0) {
			paths = fRuleTree.getSelectionPaths();
			;
			forward = true;
		} else if (ruleTabbedPane.getSelectedIndex() == 1) {
			paths = bRuleTree.getSelectionPaths();
			forward = false;
		}
		if (paths == null)
			return;
		for (int i = 0; i < paths.length; i++) {
			TransformationRule rule = getRule(paths[i]);
			Scope fScope = getScope(paths[i]);
			modify(forward, keep, rule, fScope);
			if (forward)
				fRuleTree.scrollPathToVisible(paths[i]);
			else
				bRuleTree.scrollPathToVisible(paths[i]);
		}
		// update the status
		updateStatusMessage();
	}

	protected void navigateEntityAction(boolean forward) {
		if (highlightEntities == null)
			return;
		if (forward) {
			for (int i = (1 + fCurrentEntityRow); i < fEntityTree.getRowCount(); i++) {
				TreePath path = fEntityTree.getPathForRow(i);
				if (getJavaMethod(path) != null
						&& highlightEntities.contains(getJavaMethod(path))) {
					System.out.println("scrolling to:" + getJavaMethod(path));
					fEntityTree.expandPath(path);
					fEntityTree.scrollPathToVisible(path);
					fEntityTree.setSelectionPath(path);
					fCurrentEntityRow = i;
					update();
					return;
				}
			}
			fCurrentEntityRow = -1;
		} else {
			for (int i = (1 + bCurrentEntityRow); i < bEntityTree.getRowCount(); i++) {
				TreePath path = bEntityTree.getPathForRow(i);
				if (getJavaMethod(path) != null
						&& highlightEntities.contains(getJavaMethod(path))) {
					System.out.println("scrolling to:" + getJavaMethod(path));
					bEntityTree.expandPath(path);
					bEntityTree.setSelectionPath(path);
					bEntityTree.scrollPathToVisible(path);
					bCurrentEntityRow = i;
					update();
					return;
				}
			}
			bCurrentEntityRow = -1;
		}
	}

	protected void navigateRuleAction(boolean forward) {
		if (highlightRules == null) {
			System.out.println("No Rule Selected");
			return;
		}
		if (forward) {
			for (int i = (1 + fCurrentRuleRow); i < fRuleTree.getRowCount(); i++) {
				TreePath path = fRuleTree.getPathForRow(i);
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
						.getLastPathComponent();
				if (node.getUserObject() instanceof TransformationRule
						&& getRule(path) != null
						&& highlightRules.contains(getRule(path).getRuleID())) {
					System.out.println("scrolling to:"
							+ getRule(path).getRuleID());
					fRuleTree.expandPath(path);
					fRuleTree.scrollPathToVisible(path);
					fRuleTree.setSelectionPath(path);
					fCurrentRuleRow = i;
					update();
					return;
				}
			}
			fCurrentRuleRow = -1;
		} else {
			for (int i = (1 + bCurrentRuleRow); i < bRuleTree.getRowCount(); i++) {
				TreePath path = bRuleTree.getPathForRow(i);
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
						.getLastPathComponent();
				if (node.getUserObject() instanceof TransformationRule
						&& getRule(path) != null
						&& highlightRules.contains(getRule(path).getRuleID())) {
					System.out.println("scrolling to:"
							+ getRule(path).getRuleID());
					bRuleTree.expandPath(path);
					bRuleTree.scrollPathToVisible(path);
					bRuleTree.setSelectionPath(path);
					bCurrentRuleRow = i;
					update();
					return;
				}
			}
			bCurrentRuleRow = -1;
		}
	}

	protected JPanel createMenuPanel() {
		JButton saveAction = new JButton("Save");
		saveAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				save();
			}
		});
		// button creation
		JButton keepAction = new JButton("Keep Rule");
		keepAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				// only works for rule
				modifyAction(true);
			}
		});

		JButton deleteAction = new JButton("Delete Rule");
		deleteAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				modifyAction(false);
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
		JButton nextEntityAction = new JButton("Next Entity");
		nextEntityAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (matchTabbedPane.getSelectedIndex() == 0) {
					navigateEntityAction(true);
				} else {
					navigateEntityAction(false);
				}
			}
		});
		JButton ruleToMatchesAction = new JButton("Rule->Matches");
		ruleToMatchesAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ruleToMatches();
			}
		});
		JButton depRuleAction = new JButton("Rule->Codep");
		depRuleAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ruleToCodependentRules();
			}
		});

		JButton matchToRulesAction = new JButton("Dmn->Rules");
		matchToRulesAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				domainToRules();
			}
		});
		JButton domainToCounterRules = new JButton("Dmn->CounterRules");
		domainToCounterRules.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				domainToCounterRules();
			}
		});
		JButton reset = new JButton("Reset");
		reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				highlightEntities(new TreeSet<JavaMethod>());
				highlightRelevantRules(new TreeSet<TransformationRule.RuleID>());
			}
		});
		JButton copy = new JButton("Copy");
		copy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				copyFrom();
			}
		});
		JButton eval = new JButton("Eval");
		eval.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				evaluate();
			}
		});
		JButton manualInput = new JButton("Input");
		manualInput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				input();
			}
		});
		JPanel menu = new JPanel();
		menu.setSize(WIDTH, LENGTH / 8);
		menu.setLayout(new GridLayout(1, 10));
		menu.add(saveAction);
		menu.add(keepAction);
		menu.add(deleteAction);
		menu.add(nextRuleAction);
		menu.add(nextEntityAction);
		menu.add(ruleToMatchesAction);
		menu.add(depRuleAction);
		menu.add(matchToRulesAction);
		menu.add(domainToCounterRules);
		menu.add(reset);
		menu.add(copy);
		menu.add(eval);
		menu.add(manualInput);
		return menu;
	}


	public MatchingResultViewer(RulebasedMatching matchingResult, String title) {
		// JFrame - JPanel
//		super();
		this.matchingResult = matchingResult;
		
	}
	
	protected void initialize () {
		setSize(WIDTH + MARGIN, LENGTH * 2 + MARGIN * 4);
		setLocation(100, 100);
//		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.ruleHighlighter = new RuleRenderer();
		this.entityHighlighter = new EntityRenderer();

		JPanel content = new JPanel();
		content.setLayout(new GridLayout(2, 1));
		content.add(createRuleViewer());
		content.add(createEntityViewer());

		
		updateStatusMessage();
		JPanel status = new JPanel();
		status.setLayout(new GridLayout(1, 2));
		status.add(currentStatus);
		status.add(oldStatus);
//		JFrame - JPanel
//		getContentPane().setLayout(new BorderLayout());
//		getContentPane().add(createMenuPanel(), BorderLayout.NORTH);
//		getContentPane().add(content, BorderLayout.CENTER);
//		getContentPane().add(status, BorderLayout.SOUTH);

		content.setVisible(true);
		this.setLayout(new BorderLayout());
		this.add(createMenuPanel(), BorderLayout.NORTH);
		this.add(content, BorderLayout.CENTER);
		this.add(status, BorderLayout.SOUTH);
		
		setVisible(true);

	}

	private DefaultTreeModel createRuleModel(TransformationRule[] rules,
			boolean forward) {
		int rulesWithDeleteExceptions =0; 
		int rulesWithConflictExceptions =0; 
		DefaultMutableTreeNode ruleRoot = null;
		if (forward) {
			ruleRoot = new DefaultMutableTreeNode("Forward Rules");
		} else {
			ruleRoot = new DefaultMutableTreeNode("Backward Rules");
		}
		DefaultTreeModel ruleTreeModel = new DefaultTreeModel(ruleRoot);
		for (int i = 0; i < rules.length; i++) {
			TransformationRule rule = rules[i];
			DefaultMutableTreeNode ruleNode = rule.getTreeNode();
			
			if (rule.getExceptionMatches().size()>0) { 
				if (rule.getNumConflictException()>0) { 
					rulesWithConflictExceptions++; 
					if (forward==true) { 
						System.out.println(rule.toString());
					}
				}else 
					rulesWithDeleteExceptions++; 
			}
			ruleRoot.add(ruleNode);
		}
		if (forward == true) {
			System.out.println("MK021811: # rules with Conflict Exceptions:\t"
					+ rulesWithConflictExceptions);
			System.out.println("MM021811: # rules with only Delete Exceptions:\t"
					+ rulesWithDeleteExceptions);
		}
		return ruleTreeModel;
		
	}
		
	protected DefaultTreeModel createEntityModel(boolean forward) {
		String s = null;
		if (forward) {
			s = "Forward Entities";
		} else
			s = "Backward Entities";
		// create entity model
		DefaultMutableTreeNode entityRoot = new DefaultMutableTreeNode(s);
		DefaultTreeModel model = new DefaultTreeModel(entityRoot);
		Set<JavaMethod> domainSet = this.matchingResult.getDomain(forward);
		ArrayList<JavaMethod> domainList = new ArrayList<JavaMethod>(domainSet);
		Collections.sort(domainList, new JavaMethodComparator());
		SetOfPairs domainMatches = this.matchingResult.getMatches(forward);
		for (Iterator<JavaMethod> it = domainList.iterator(); it.hasNext();) {
			JavaMethod domainJm = it.next();
			DefaultMutableTreeNode jmNode = new DefaultMutableTreeNode(domainJm);
			entityRoot.add(jmNode);
			ListOfPairs matchesFromDomainJM = null;
			if (forward) {
				matchesFromDomainJM = domainMatches.getMatchesByLeft(domainJm);
			} else {
				matchesFromDomainJM = domainMatches.getMatchesByRight(domainJm);
			}
			// ArrayList<TransformationRule> rules =
			// this.matchingResult.getExplainingRules(domainJm,forward);
			for (Iterator<Pair> matchIt = matchesFromDomainJM.iterator(); matchIt
					.hasNext();) {
				Pair p = matchIt.next();
				if (forward) {
					DefaultMutableTreeNode counterpartNode = new DefaultMutableTreeNode(
							p.getRight());
					jmNode.add(counterpartNode);
				} else {
					DefaultMutableTreeNode counterpart = new DefaultMutableTreeNode(
							p.getLeft());
					jmNode.add(counterpart);
				}
			}
		}
		return model;
	}

	public static void main(String args[]) {
		JFileChooser chooser = FileNameService.openMatchingFileDialog();
		int returnVal = chooser.showOpenDialog(new JFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File inputFile = chooser.getSelectedFile();
			System.out.println("You chose to open this file: "
					+ inputFile.getName());
			RulebasedMatching rb = RulebasedMatching.readXMLFile(inputFile
					.getAbsolutePath());
			MatchingResultViewer mv = new MatchingResultViewer(rb, "Matching Result Viewer");
			mv.initialize();
		}
	}

	protected void modify(boolean forward, boolean keep, TransformationRule rule,
			Scope scope) {
		this.matchingResult.modifyRule(forward, keep, rule, scope);
		update();
	}

	protected Set<JavaMethod> getExplainedMatches(boolean forward,
			TransformationRule rule, Scope scope, Pair match) {
		Set<JavaMethod> domain = new TreeSet<JavaMethod>();
		if (match != null) {
			if (forward) {
				domain.add((JavaMethod) match.getLeft());
			} else
				domain.add((JavaMethod) match.getRight());
			return domain;
		} else if (scope != null) {
			ListOfPairs list = rule.getPositiveMatches();
			list.addSetOfPairs(rule.getExceptionMatches());
			for (Iterator<Pair> it = list.iterator(); it.hasNext();) {
				Pair p = it.next();
				JavaMethod jm = null;
				if (forward)
					jm = (JavaMethod) p.getLeft();
				else
					jm = (JavaMethod) p.getRight();
				if (scope.match(jm)) {
					if (forward)
						domain.add((JavaMethod) p.getLeft());
					else
						domain.add((JavaMethod) p.getRight());
				}
			}
			return domain;
		}
		ListOfPairs list = rule.getPositiveMatches();
		list.addSetOfPairs(rule.getExceptionMatches());
		if (forward) {
			domain.addAll(list.getLeftDomain());
		} else {
			domain.addAll(list.getRightDomain());
		}
		return domain;
	}

	/* counter rules */
	protected Set<TransformationRule.RuleID> counterRules(boolean forward,
			JavaMethod jm) {
		Pair p = null;
		if (forward) {
			p = this.matchingResult.getMatches(forward).getFirstMatchByLeft(jm);
		} else {
			p = this.matchingResult.getMatches(forward)
					.getFirstMatchByRight(jm);
		}
		if (p==null) return new TreeSet<TransformationRule.RuleID> ();
		TreeSet<TransformationRule.RuleID> rules = new TreeSet<TransformationRule.RuleID>();
		if (forward) {
			rules.addAll(this.matchingResult.getCodependentRules((JavaMethod) p
					.getRight(), false));
			// find backward rules that start from p.right
		} else {
			rules.addAll(this.matchingResult.getCodependentRules((JavaMethod) p
					.getLeft(), true));
			// find forward rules that start from p.left
		}
		return rules;
	}

	/* all co-dependent rules */
	protected Set<TransformationRule.RuleID> codependentRule(boolean forward,
			JavaMethod jm) {
		TreeSet<TransformationRule.RuleID> relevantRules = new TreeSet<TransformationRule.RuleID>();
		relevantRules.addAll(this.matchingResult.getCodependentRules(jm,
				forward));
		return relevantRules;
	}

	protected Set<TransformationRule.RuleID> codependentRule(boolean forward,
			TransformationRule rule, Scope scope, Pair match) {
		Set<JavaMethod> domain = new TreeSet<JavaMethod>();
		if (match != null) {
			if (forward) {
				domain.add((JavaMethod) match.getLeft());
			} else
				domain.add((JavaMethod) match.getRight());
		} else if (scope != null) {
			Set<JavaMethod> domainToBeMatched = matchingResult
					.getDomain(forward);
			for (Iterator<JavaMethod> it = domainToBeMatched.iterator(); it
					.hasNext();) {
				JavaMethod jm = it.next();
				if (scope.match(jm)) {
					domain.add(jm);
				}
			}
		} else if (rule != null) {
			if (forward) {
				domain.addAll(rule.getPositiveMatches().getLeftDomain());
			} else
				domain.addAll(rule.getPositiveMatches().getRightDomain());
		}
		TreeSet<TransformationRule.RuleID> relevantRules = new TreeSet<TransformationRule.RuleID>();
		for (Iterator<JavaMethod> it = domain.iterator(); it.hasNext();) {
			JavaMethod jm = it.next();
			System.out.println(jm);
			relevantRules.addAll(this.matchingResult.getCodependentRules(jm,
					forward));
		}
		return relevantRules;
	}

	protected void updateRuleTree(boolean forward) {
		DefaultTreeModel ruleTreeModel = null;
		if (forward) {
			ruleTreeModel = (DefaultTreeModel) fRuleTree.getModel();
		} else {
			ruleTreeModel = (DefaultTreeModel) bRuleTree.getModel();
		}
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) ruleTreeModel
				.getRoot();
		for (int i = 0; i < root.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) root
					.getChildAt(i);
			if (child.getUserObject() instanceof TransformationRule) {
				TransformationRule tRule = (TransformationRule) child
						.getUserObject();
				if (tRule.isModified()) {
					System.out.println(tRule.getRuleID() + "is modified");
					root.remove(i);
					root.insert(tRule.getTreeNode(), i);
					tRule.resetModified();
					ruleTreeModel.reload();
				}
			}
		}
	}

	protected void update() {
		updateRuleTree(true);
		updateRuleTree(false);
	}

	class RuleRenderer extends DefaultTreeCellRenderer {
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			// TODO Auto-generated method stub
			super.getTreeCellRendererComponent(tree, value, sel, expanded,
					leaf, row, hasFocus);
			if (isRelevant(value)) {
				setBackground(Color.YELLOW);
				setForeground(Color.RED);
				// if (!tree.isExpanded(row)) tree.expandRow(row);
			}
			return this;
		}

		public boolean isRelevant(Object value) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			if (node.getUserObject() instanceof TransformationRule) {
				TransformationRule t = (TransformationRule) node
						.getUserObject();
				if (highlightRules != null
						&& highlightRules.contains(t.getRuleID())) {
					return true;
				}
			}
			if (node.getUserObject() instanceof ScopeDisjunction) {
				Object[] paths = node.getUserObjectPath();
				for (int i = 0; i < paths.length; i++) {
					Object obj = paths[i];
					if (obj instanceof TransformationRule) {
						TransformationRule t = (TransformationRule) obj;
						if (highlightRules != null
								&& highlightRules.contains(t.getRuleID()))
							return true;
					}
				}
			}
			return false;
		}
	}

	class EntityRenderer extends DefaultTreeCellRenderer implements TreeCellRenderer {
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			// TODO Auto-generated method stu
			if (isRelevant(value)) {
				setBackground(Color.YELLOW);
				setForeground(Color.BLUE);
				// if (!tree.isExpanded(row)) tree.expandRow(row);
			}
//			return new JCheckBox("Kimmy");
			return this;
		}

		public boolean isRelevant(Object value) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			if (node.getUserObject() instanceof JavaMethod) {
				JavaMethod t = (JavaMethod) node.getUserObject();
				if (highlightEntities != null && highlightEntities.contains(t)) {
					return true;
				}
			}
			return false;
		}
	}

	protected static void highlightRelevantRules(
			Set<TransformationRule.RuleID> rules) {
		if (ruleHighlighter != null) {
			System.out.println("Relevant Rules:\t" + rules);
			highlightRules = new ArrayList<TransformationRule.RuleID>();
			highlightRules.addAll(rules);
		}
	}

	private static void highlightEntities(Set<JavaMethod> entities) {
		if (entityHighlighter != null) {
			System.out.println("Relevant Entities:\t" + entities);
			highlightEntities = new ArrayList<JavaMethod>();
			highlightEntities.addAll(entities);
		}
	}

	
	protected void copyFrom() {
		JFileChooser chooser = FileNameService.openMatchingFileDialog();
		int returnVal = chooser.showOpenDialog(new JFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File inputFile = chooser.getSelectedFile();
			System.out.println("You chose to open this file to copy from: "
					+ inputFile.getName());
			RulebasedMatching toCopyFrom = RulebasedMatching
					.readXMLFile(inputFile.getAbsolutePath());
			this.matchingResult.copyFrom(toCopyFrom);
		}
	}

	private void save() {

		JFileChooser chooser = FileNameService.openMatchingFileDialog();
		int returnVal = chooser.showSaveDialog(new JFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File outputFile = chooser.getSelectedFile();
			System.out.println("You chose to save to this file: "
					+ outputFile.getName());
			this.matchingResult.writeXMLFile(outputFile.getAbsolutePath());
		}
	}

	protected void evaluate() {
		System.out.println("No Implementation");
	}

	protected void updateStatusMessage() {
		String s1 = "Left.unmatched:     "
				+ this.matchingResult.numUnMatched(true) + " /"
				+ this.matchingResult.getDomain(true).size();
		String s2 = "Right.unmatched:     "
				+ this.matchingResult.numUnMatched(false) + " /"
				+ this.matchingResult.getDomain(false).size();
		String s3 = "ForwardRule.check / mark / total:     "
				+ this.matchingResult.numCheckedRules(true) + " / "
				+ this.matchingResult.numMarkedRules(true) + " / "
				+ this.matchingResult.numRules(true);
		String s4 = "BackwardRule.check / mark / total:     "
				+ this.matchingResult.numCheckedRules(false) + " / "
				+ +this.matchingResult.numMarkedRules(false) + " / "
				+ this.matchingResult.numRules(false);
		String s5 = "Manual 1:1     "
				+ this.matchingResult.getManualOneToOneMatches().size();
		String s[] = { s1, s2, s3, s4, s5 };
		DefaultListModel currentModel = null;
		DefaultListModel oldModel = null;
		if (oldStatus == null) {
			oldStatus = new JList();
			oldModel = new DefaultListModel();
			oldStatus.setModel(oldModel);
		} else {
			oldModel = (DefaultListModel) oldStatus.getModel();
		}
		if (currentStatus == null) {
			currentStatus = new JList();
			currentModel = new DefaultListModel();
			currentStatus.setModel(currentModel);
		} else {
			currentModel = (DefaultListModel) currentStatus.getModel();
		}
		oldModel.removeAllElements();
		for (int i = 0; i < s.length; i++) {
			if (currentModel.size() == s.length)
				oldModel.insertElementAt("from    "
						+ currentModel.getElementAt(i), i);
		}
		currentModel.removeAllElements();
		for (int i = 0; i < s.length; i++) {
			currentModel.insertElementAt(s[i], i);
		}
	}

	protected void ruleToCodependentRules() {
		TreePath[] paths = null;
		boolean forward = true;
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
		if (paths != null) {
			for (int i = 0; i < paths.length; i++) {
				TransformationRule rule = getRule(paths[i]);
				Scope scope = getScope(paths[i]);
				Pair pair = getMatch(paths[i]);
				Set<TransformationRule.RuleID> rules = codependentRule(forward,
						rule, scope, pair);
				highlightRelevantRules(rules);
				navigateRuleAction(forward);
			}
		}
	}

	protected void domainToRules() {
		TreePath[] paths = null;
		boolean forward = true;
		if (matchTabbedPane.getSelectedIndex() == 0) {
			forward = true;
			paths = fEntityTree.getSelectionPaths();
		} else {
			forward = false;
			paths = bEntityTree.getSelectionPaths();
		}
		if (paths != null) {
			for (int i = 0; i < paths.length; i++) {
				JavaMethod jm = getJavaMethod(paths[i]);
				Set<TransformationRule.RuleID> rules = codependentRule(forward,
						jm);
				highlightRelevantRules(rules);
				navigateRuleAction(forward);
			}
		}
	}

	protected void domainToCounterRules() {
		TreePath[] paths = null;
		boolean forward = true;
		if (matchTabbedPane.getSelectedIndex() == 0) {
			forward = true;
			paths = fEntityTree.getSelectionPaths();
		} else {
			forward = false;
			paths = bEntityTree.getSelectionPaths();
		}
		if (paths != null) {
			for (int i = 0; i < paths.length; i++) {
				JavaMethod jm = getJavaMethod(paths[i]);
				Set<TransformationRule.RuleID> rules = counterRules(forward, jm);
				highlightRelevantRules(rules);
				if (forward) {
					// visible backward rules
					ruleTabbedPane.setSelectedIndex(1);
				} else {
					ruleTabbedPane.setSelectedIndex(0);
				}
				navigateRuleAction(!forward);
			}
		}
	}

	private void ruleToMatches() {
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
		if (paths != null) {
			for (int i = 0; i < paths.length; i++) {
				TransformationRule rule = getRule(paths[i]);
				Scope scope = getScope(paths[i]);
				Pair pair = getMatch(paths[i]);
				Set<JavaMethod> jms = getExplainedMatches(forward, rule, scope,
						pair);
				highlightEntities(jms);
				navigateEntityAction(forward);
			}
		}
	}
	protected void input() {
		new ManualMatchingInput(this.matchingResult);
	}
}
