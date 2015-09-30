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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.washington.cs.rules.RuleBase;
import edu.washington.cs.util.Comparison;
import edu.washington.cs.util.Pair;
import edu.washington.cs.util.SetOfPairs;

public class ManualCheck extends JPanel implements ItemListener{
	private final String project; 
	private final int oldRevId;
	private final int newRevId;
	final int sampledPP; 
	final int sampledPN; 
	final int sampledNP;
	int truePP=0;
	int truePN=0;
	int trueNP=0;
	final boolean readFromXML;
	private JCheckBox[] checkboxes = null;
	private JButton close = null;
	
	public ManualCheck(RuleBase rb, SetOfPairs otherMatches, String project,
			int oldrevision, int newrevision) {
		
		SetOfPairs ruleMatches = new SetOfPairs();
		if (rb!=null) ruleMatches = rb.getAcceptedMatches();
		this.project = project;
		this.oldRevId = oldrevision;
		this.newRevId = newrevision;
		Element element =readXMLFile();
		if (element!=null) {
			readFromXML=true;
			System.out.println("Reading from a file");
			NodeList children = element.getChildNodes();
			int sPP=0; int sPN=0; int sNP=0;
			int tPP=0; int tPN=0; int tNP=0;
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i) instanceof Element) {
					Element child = (Element) children.item(i);

					if (child.getTagName().equals("result")) {
						String kind = child.getAttribute("kind");
						String manual = child.getAttribute("manual");
						if (kind.equals("PP")) {
							sPP++;
							if (manual.equals("true"))  tPP++;
						}else if (kind.equals("PN")){
							sPN++;
							if (manual.equals("true")) tPN++;
						}else if (kind.equals("NP")) {
							sNP++;
							if (manual.equals("true")) tNP++;
						}
					}
				}
			}
			this.sampledPP = sPP;
			this.sampledPN = sPN;
			this.sampledNP = sNP;
			this.truePP =tPP;
			this.truePN =tPN;
			this.trueNP =tNP; 
			System.out.println("PP:"+truePP+"/"+sampledPP+"PN:"+truePN+"/"+sampledPN
					+trueNP+"/"+sampledNP);
			return;
		}
		readFromXML=false;
		this.setLayout(new GridLayout(0,1));
		// compare rb with othermatches
		
		SetOfPairs PP = Comparison.common(ruleMatches, otherMatches);
		SetOfPairs PN= Comparison.leftMinusRight(ruleMatches, otherMatches);
		SetOfPairs NP= Comparison.rightMinusLeft(ruleMatches, otherMatches);
		this.sampledPP = PP.size();
		this.sampledPN = PN.size();
		this.sampledNP = NP.size();
		close = new JButton("Close");
		close.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent ae) {
				closeThisWindow();
			}
		}); 
		this.add(close);
		
		JLabel label = new JLabel("     PP:"+PP.size()+"     PN:"+PN.size()+"     NP:"+NP.size());
		this.add(label);
		
		int size = PP.size()+PN.size()+ NP.size();

		setSize(1000, size*100);
		checkboxes  = new JCheckBox[size];
		int index =0; 
		for (Iterator<Pair> it = PP.iterator(); it.hasNext(); ) { 
			checkboxes[index] = new JCheckBox("PP~"+it.next().toString());
			checkboxes[index].setSelected(true);
			checkboxes[index].addItemListener(this);
			checkboxes[index].setVisible(true);
			this.add(checkboxes[index]);
			index++;
		}
		for (Iterator<Pair> it = PN.iterator(); it.hasNext(); ) { 
			checkboxes[index] = new JCheckBox("PN~"+it.next().toString());
			checkboxes[index].setSelected(true);
			checkboxes[index].addItemListener(this);
			checkboxes[index].setVisible(true);
			this.add(checkboxes[index]);
			index++;
		}
		for (Iterator<Pair> it = NP.iterator(); it.hasNext(); ) { 
			checkboxes[index] = new JCheckBox("NP~"+it.next().toString());
			checkboxes[index].setSelected(true);
			checkboxes[index].addItemListener(this);
			checkboxes[index].setVisible(true);
			this.add(checkboxes[index]);
			index++;
		}
		this.setVisible(true);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		Object source = e.getItemSelectable(); 
		for (int i=0 ; i< checkboxes.length; i++) { 
			if (source == checkboxes[i]) { 
				if (e.getStateChange()==ItemEvent.DESELECTED) { 
					checkboxes[i].setSelected(false);
				}else{ 
					checkboxes[i].setSelected(true);
				}
			}
		}
	}
	
	public void writeXMLFile () {

		Document doc = DOMImplementationImpl.getDOMImplementation()
				.createDocument("namespaceURI", "manualcheck", null);
		// update document
		writeElement(doc.getDocumentElement());
		File file = new File("c:\\OriginAnalysisData\\manualcheck\\"+project+oldRevId+"-"+newRevId+".xml");
		try {
			if (!file.exists()) {
				file.createNewFile();
				// serialize DOM document to outputfile
				XMLSerializer serializer = new XMLSerializer();
				OutputFormat format = new OutputFormat();
				format.setPreserveSpace(true);
				format.setPreserveEmptyAttributes(true);
				format.setIndenting(true);
				format.setIndent(4);
				format.setLineWidth(80);
				String[] nonEscapingElements = { "\n", "\t" };
				format.setNonEscapingElements(nonEscapingElements);
				serializer = new XMLSerializer(format);
				FileOutputStream outstream = new FileOutputStream(file);
				serializer.setOutputByteStream(outstream);
				assert (doc != null);
				serializer.serialize(doc);
				outstream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void writeElement(Element parent) {
		for (int i=0; i< checkboxes.length; i++) { 
			JCheckBox box = checkboxes[i];
			String boxName = box.getActionCommand();
			String kind = boxName.substring(0,2);
			String pair = boxName.substring(3);
			Element e = parent.getOwnerDocument().createElement("result");
			e.setAttribute("pair",pair);
			e.setAttribute("kind",kind);
			e.setAttribute("manual",new Boolean(box.isSelected()).toString());
			parent.appendChild(e);
			System.out.println("pair:"+pair);
			System.out.println("kind:"+kind);
			System.out.println("manual:"+box.isSelected());
			if (kind.equals("PP") && box.isSelected()){ 
				truePP++;
			}else if (kind.equals("PN") && box.isSelected()){ 
				truePN++;
			}else if (kind.equals("NP") && box.isSelected()){ 
				trueNP++;
			}	
		}
	}
	public Element readXMLFile() {
		String filename = "c:\\OriginAnalysisData\\manualcheck\\" + project
				+ oldRevId + "-" + newRevId + ".xml";
		File file = new File(filename); 
		if (!file.exists()) return null;
		System.out.println(filename);
		Document doc = null;
		DOMParser domparser = new DOMParser();
		try {
			domparser.parse(filename);
			doc = domparser.getDocument();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException s) {
			s.printStackTrace();
		}
		return doc.getDocumentElement();
	}
	public int getTruePP () {
		return truePP;
	}
	public int getTruePN () { 
		return truePN;
	}
	public int getTrueNP () { 
		return trueNP;
	}
	
	public void closeThisWindow() { 
		SKimTransaction.killDialog();
		WDRefactoringReconstruction.killDialog();
	}
	public boolean isReadFromXML() {
		return readFromXML;
	}
}
