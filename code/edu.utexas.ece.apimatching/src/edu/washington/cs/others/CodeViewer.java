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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import edu.ucsc.cse.grase.origin.entity.Method;
import edu.ucsc.cse.grase.origin.parser.IParser;
import edu.ucsc.cse.grase.origin.parser.Parser;
import edu.washington.cs.extractors.ProgramSnapshot;
import edu.washington.cs.induction.ReadDirectories;
import edu.washington.cs.rules.JavaMethod;
import edu.washington.cs.util.Pair;

public class CodeViewer extends JFrame{

	private static int WIDTH = 1200;
	public static void main (String args[]) {
		String dirList ="jfreechart_list";
		String project ="jfreechart";
		double SEED_TH = 0.7;
		double EXCEPTION_TH =0.34;
		File[] dirs = ReadDirectories.getDirectories(dirList);
		int loopend = 1;
		boolean onlyOne = true;
		if (onlyOne == false)
			loopend = dirs.length - 1;
		for (int i = 0; i < loopend; i++) {
			ProgramSnapshot oldP = new ProgramSnapshot(project, dirs[i]);
			ProgramSnapshot newP = new ProgramSnapshot(project, dirs[i + 1]);
			Pair <JavaMethod> pair = new Pair(oldP.getMethods().get(0), newP.getMethods().get(0));
			new CodeViewer(pair, oldP, newP);
		}
	}
	class GrayPainter extends DefaultHighlighter.DefaultHighlightPainter {
		public GrayPainter() {
			super(Color.LIGHT_GRAY);
		}
	}
	private GrayPainter painter = new GrayPainter();
	public class ItemLocation implements Comparable{
		JavaMethod jm;
		String version; 
		public ItemLocation(JavaMethod jm, String version) { 
			this.jm = jm;
			this.version = version;
		}

		public int compareTo(Object o) {
			ItemLocation ot = (ItemLocation) o;
			return (version + jm).compareTo((ot.version + ot.jm));
		}
		public String toString (){
			return version+":"+jm.toString();
		}
		@Override
		public int hashCode() {
			// TODO Auto-generated method stub
			return toString().hashCode();
		}
		public boolean equals(Object o) {
			return (this.toString().equals(o.toString()));
		}
		
	}
	public class PhysicalLocation implements Comparable{ 
		String absolutePath;
		int startLine;
		int endLine;
		int startColumn;
		int endColumn;
		public PhysicalLocation(String absolutePath, int startLine, int endLine, int startColumn, int endColumn) { 
			this.absolutePath = absolutePath; 
			this.startLine = startLine;
			this.endLine = endLine;
		}
		public int compareTo(Object o) {
			PhysicalLocation ot = (PhysicalLocation) o;
			return (absolutePath + startLine + "-" + endLine)
					.compareTo(ot.absolutePath + ot.startLine + "-"
							+ ot.endLine);
		}
		public String toString () {
			return absolutePath+"("+startLine+","+endLine+")";
		}
		public int hashCode() {
			// TODO Auto-generated method stub
			return toString().hashCode();
		}
		public boolean equals(Object o) {
			return (this.toString().equals(o.toString()));
		}
	}
	private static TreeSet<String> parsedPaths = new TreeSet<String>();
	private static HashMap<ItemLocation,PhysicalLocation> locationMap =new HashMap<ItemLocation,PhysicalLocation>();
	public CodeViewer(Pair<JavaMethod> pair, ProgramSnapshot oldP, ProgramSnapshot newP) {
		super("CodeViewer");
		setSize(WIDTH+20, 600);
		System.out.println("oldP"+oldP.getSrcPath());
		System.out.println("newP"+newP.getSrcPath());
		if (!parsedPaths.contains(oldP.getSrcPath())) { 
			createLocationMap(oldP);
		}
		if (!parsedPaths.contains(newP.getSrcPath())){ 
			createLocationMap(newP);
		}
		ItemLocation leftItem = new ItemLocation(pair.getLeft(), oldP.getVersion());
		ItemLocation rightItem = new ItemLocation(pair.getRight(), newP.getVersion());

		System.out.println(leftItem.version+":"+leftItem.jm.toString());
		System.out.println(rightItem.version+":"+rightItem.jm.toString());
		PhysicalLocation phyLeft = locationMap.get(leftItem);
		PhysicalLocation phyRight = locationMap.get(rightItem);
		
		if (phyLeft==null) {
			System.out.println("Left Does not ");
			return; 
		}else if (phyRight == null ) {
			System.out.println("Right does not");
			return; 
		}
		JScrollPane left = createEditor (phyLeft,leftItem );
		JScrollPane right = createEditor (phyRight, rightItem);
		JSplitPane editor = new JSplitPane();
		editor.setSize(WIDTH, 500);
		editor.setLeftComponent(left);
		editor.setRightComponent(right);
		editor.setDividerLocation(0.5);
		getContentPane().add(editor);
		setVisible(true);
		
	}
	
	private void createLocationMap (ProgramSnapshot snapshot) {
		//
		System.out.println("Location Extracting "+snapshot.getSrcPath());
		IParser parser = new Parser();
		File root = new File(snapshot.getSrcPath());
		if (!root.exists()) {
			System.out.println("Invalid Path");
			return;
		}
		List methodList = parser.parser(root);
		for (int i = 0; i < methodList.size(); i++) {
			Method m = (Method) methodList.get(i);
			String name = m.getName().replace(":", ".");
			String returnType = m.getSignature().getReturnType();
			if (returnType.equals(""))
				returnType = "void";
			String[] args = m.getSignature().getParameterTypes();
			JavaMethod jm = new JavaMethod(m);
			ItemLocation itemLocation = new ItemLocation(jm, snapshot
					.getVersion());
			PhysicalLocation phsLocation = new PhysicalLocation(m
					.getFullFileName(), m.getStartLine(), m.getEndLine(), m
					.getStartPos(), m.getEndPos());
			locationMap.put(itemLocation, phsLocation);
			if (!locationMap.containsKey(itemLocation)) {
				System.out.println("locationMap ");
			}
			if (!locationMap.containsValue(phsLocation)) {
				System.out.println("phsLocation ");
			}
			if (!locationMap.get(itemLocation).equals(phsLocation)) {
				System.out.println("Not Match");
			}
		}
		this.parsedPaths.add(snapshot.getSrcPath());		
	}
	public JScrollPane createEditor (PhysicalLocation p, ItemLocation loc) {
		JScrollPane pane = new JScrollPane();
		pane.setSize(WIDTH/2,500);
		JTextArea editor = new JTextArea();
		editor.setEditable(false);
		editor.setVisible(true);
		editor.setSize(WIDTH/2-20,450);
		readFileToEditor(p.absolutePath, editor);
		Highlighter highlighter = editor.getHighlighter();
		try {
			//set the focus
			editor.setCaretPosition(editor.getLineStartOffset((p.endLine)));
			highlighter.addHighlight(editor
					.getLineStartOffset(p.startLine - 1)
					+ p.startColumn-1, editor.getLineStartOffset(p.endLine - 1)
					+ p.endColumn-1, painter);
			
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		pane.getViewport().add(editor);

		JLabel title = new JLabel(loc.version + " "+loc.jm.getClassName() + "."
				+ loc.jm.getProcedureName() + loc.jm.getParameters().toString());
		pane.setColumnHeaderView(title);
		return pane;
	}
	private void readFileToEditor(String inputfile, JTextArea editor) {
		if (editor == null)
			return;
		BufferedReader buffer = null;
		try {
			FileReader reader = new FileReader(inputfile);
			buffer = new BufferedReader(reader);
			String s = buffer.readLine();
			while (s != null) {
				editor.append(s + "\n");
				s = buffer.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}