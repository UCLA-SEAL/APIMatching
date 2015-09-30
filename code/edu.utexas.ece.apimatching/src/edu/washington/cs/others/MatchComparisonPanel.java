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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.washington.cs.extractors.ProgramSnapshot;
import edu.washington.cs.induction.FileNameService;
import edu.washington.cs.induction.ReadDirectories;
import edu.washington.cs.induction.RulebasedMatching;
import edu.washington.cs.rules.JavaMethod;
import edu.washington.cs.util.Pair;
import edu.washington.cs.util.SetOfPairs;
import edu.washington.cs.util.SetUtil;

public class MatchComparisonPanel extends JPanel{

	private JCheckBox[] myMatchCheck; 
	private JCheckBox[] otherMatchCheck;
	
	private JList methodList; 
	
	public static void main (String args[]) { 
			compare (true, "jfreechart_list", "jfreechart", 0.7, 0.34);
		}
	public static void compare(boolean onlyOne, String dirList, String project,
			double SEED_TH, double EXCEPTION_TH) {
		File[] dirs = ReadDirectories.getDirectories(dirList);
		int loopend = 1;
		if (onlyOne == false)
			loopend = dirs.length - 1;
		for (int i = 0; i < loopend; i++) {
			ProgramSnapshot oldP = new ProgramSnapshot(project, dirs[i]);
			ProgramSnapshot newP = new ProgramSnapshot(project, dirs[i + 1]);
			File matchingFile = FileNameService.getMatchingXMLFile(oldP, newP,
					SEED_TH, EXCEPTION_TH);
			RulebasedMatching rb = RulebasedMatching.readXMLFile(matchingFile
					.getAbsolutePath());
			File xingMatch = FileNameService.getXSMatchFile(oldP.getProject(),
					oldP.getVersion(), newP.getVersion());
			SetOfPairs evalData = SetOfPairs.readXMLFile(xingMatch
					.getAbsolutePath());
			JPanel panel = new MatchComparisonPanel(oldP, newP, rb
					.getMatches(true), evalData);
			panel.setVisible(true);
			JFrame frame = new JFrame("Comparison Viewer");
			frame.setSize(1000,800);
			frame.getContentPane().setLayout(new BorderLayout()); 
			JScrollPane pane = new JScrollPane();
			pane.getViewport().setView(panel);
			frame.getContentPane().add(pane, BorderLayout.CENTER);
			frame.getContentPane().add(new JLabel("Comparison" +oldP.getProject()+oldP.getVersion()+newP.getVersion()), BorderLayout.NORTH);
			frame.setVisible(true);
		}
	}
	public MatchComparisonPanel (ProgramSnapshot oldP, ProgramSnapshot newP,SetOfPairs myMatches, SetOfPairs otherMatches) {
		super();
		myMatchCheck = new JCheckBox[myMatches.size()];
		otherMatchCheck  = new JCheckBox[otherMatches.size()];		
		this.setLayout(new GridLayout(oldP.getMethods().size()*3,1));
		this.setBackground(Color.WHITE);
		SetUtil<JavaMethod> exactMatcher = new SetUtil<JavaMethod>();
		Set<JavaMethod> common = oldP.common(newP);
		int indexMyCheck = 0; 
		int indexOtherCheck = 0;
		this.setVisible(true);
//		if (true) return;
		for (int i =0 ; i<oldP.getMethods().size(); i++){
			JavaMethod method =oldP.getMethods().get(i);
			if (common.contains(method)) { 
				JLabel label= new JLabel("1:1   "+method.toString());
				this.add(label);
			}else { 
				Pair myMatch = myMatches.getFirstMatchByLeft(method);
				Pair otherMatch = otherMatches.getFirstMatchByLeft(method);
				String flag="";
				if (myMatch != null) {
					flag = "+";
				} else {
					flag = "-";
				}
				if (otherMatch != null) {
					flag = flag + "+";
				} else {
					flag = flag + "-";
				}
				if ((myMatch == null && otherMatch == null)
						|| (myMatch != null && otherMatch != null && myMatch
								.getRight().equals(otherMatch.getRight()))) {
					flag = "S" + flag;
				} else {
					flag = "C" + flag;
				}
				JLabel label = new JLabel(flag+"   "+method.toString());
				this.add(label); 
				if (myMatch==null && otherMatch ==null) continue ;
				if (myMatch!=null) {				
					myMatchCheck[indexMyCheck] = new JCheckBox(myMatch.getRight().toString());
					myMatchCheck[indexMyCheck].setSelected(true);
					this.add(myMatchCheck[indexMyCheck]);
					if (otherMatch!=null &&myMatch.getRight().equals(otherMatch.getRight())) {
						myMatchCheck[indexMyCheck].setBackground(Color.white);
					}else myMatchCheck[indexMyCheck].setForeground(Color.green);
					indexMyCheck++;
				}
				if (otherMatch!=null) {
					otherMatchCheck[indexOtherCheck] = new JCheckBox(otherMatch.getRight().toString());
					otherMatchCheck[indexOtherCheck].setSelected(true);
					this.add(otherMatchCheck[indexOtherCheck]);
					if (myMatch!=null && myMatch.getRight().equals(otherMatch.getRight())){ 
						otherMatchCheck[indexOtherCheck].setBackground(Color.white);
					}
					else otherMatchCheck[indexOtherCheck].setForeground(Color.RED);
					indexOtherCheck++;
				}
			}
		}
	}
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		Object source = e.getItemSelectable();
		System.out.println(source.toString()+"got selected");
		for (int i = 0; i < myMatchCheck.length; i++) {
			if (source == myMatchCheck[i]) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					myMatchCheck[i].setSelected(false);
				} else {
					myMatchCheck[i].setSelected(true);
				
				}
			}
		}
	}
	
	 
}	
