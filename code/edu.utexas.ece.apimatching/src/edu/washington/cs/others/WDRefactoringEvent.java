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


import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.washington.cs.util.Pair;

public abstract class WDRefactoringEvent {
	public String type = null;
	public String codeSim = null;
	public int AmbCount = -1;
	public int AmbPos = -1;
	
	protected WDSymbol removed ; 
	protected WDSymbol added; 
	public Pair makePair (){ 
		if (removed.getJavaMethod()==null ){ 
			System.out.println();
		}
		return new Pair(removed.getJavaMethod(), added.getJavaMethod());
	}
	protected static String getXMLTag () { 
		return null;
	}
	public WDRefactoringEvent(Element m){ 
		NodeList children = m.getChildNodes(); 
		for (int i=0; i<children.getLength();i++){
			if (children.item(i) instanceof Element ) { 
				Element child = (Element) children.item(i);
				if (child.getTagName().equals("metric")){ 
					 String metricName =child.getAttribute("metricName");
					 String metricValue = child.getAttribute("metricValue");
					 if (metricName.equals("CodeSim")) { 
						 this.codeSim = metricValue;
					 }else if (metricName.equals("AmbPos")){ 
						 this.AmbPos= new Integer(metricValue).intValue();
					 }else if (metricName.equals("AmbCount")){ 
						 this.AmbCount = new Integer(metricValue).intValue();
					 }
 				}
				else if (child.getTagName().equals("symbol")) { 
					WDSymbol s = WDSymbol.readElement(child);
					if (s==null) { 
						System.out.println("Parsing Error" + child.toString()+child.getFirstChild().toString());; 
						System.exit(0);
					}
					if (s.isAdded()) { 
						this.added = s;
					}else { 
						this.removed =s;
					}
				}
				else { 
					System.exit(0);
				}
			}
		}
	}
	public String toString (){ 
		String s =type;
		if (AmbPos!=1) { 
			s = s +"\tAmbiguious\t"+AmbPos+"//"+ AmbCount+"\t";
		}else { 
			s = s +"\tClear\t"; 
		}
		s = s + codeSim+"\t";
		if (removed != null) {
			if (removed.isMethod()) {
				s = s + removed.getJavaMethod()+"\t";
			} else {
				s = s + removed.getJavaClass()+"\t";
			}
		}
		if (added!=null) {
			if ( added.isMethod()) { 
				s = s+ added.getJavaMethod()+"\t";
			}else { 
				s = s+ added.getJavaClass()+"\t";
			}
		}
		return s;
	}
}
