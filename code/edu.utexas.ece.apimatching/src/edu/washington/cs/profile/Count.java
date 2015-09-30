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
package edu.washington.cs.profile;

public class Count extends MatcherComparable{
	
	private Integer data;
	
	public Count () { 
		comparator = new DefaultCountComparator();
	}
	public Count(String s){	
		this.data = new Integer(s);
	}
	public Count(int cnt){ 
		this.data = new Integer(cnt);  
	}
	public void incrementCount() { 
		data = new Integer(data.intValue()+1);
	}
	public int getCount() { 
		return this.data.intValue();
	}
	public String toString() { 
		return data.toString();
	}
	public CustomComparator getDefaultComparator() { 
		return comparator;
	}
	
	public class DefaultCountComparator implements CustomComparator{

		/* (non-Javadoc)
		 * @see edu.washington.cs.profile.CustomComparator#compareTo(java.lang.Object, java.lang.Object)
		 */
		public int compareTo(Object a, Object b) {
			// TODO Auto-generated method stub
			Count ia = (Count) a;
			Count ib = (Count) b;
			return ia.data.compareTo(ib.data);
		}

		/* (non-Javadoc)
		 * @see edu.washington.cs.profile.CustomComparator#equals(java.lang.Object, java.lang.Object)
		 */
		public boolean equals(Object a, Object b) {
			// TODO Auto-generated method stub
			Count ia = (Count) a;
			Count ib = (Count) b;
			return ia.data.equals(ib.data);
		
		}

		/* (non-Javadoc)
		 * @see edu.washington.cs.profile.CustomComparator#hashCode(java.lang.Object)
		 */
		public int hashCode(Object a) {
			// TODO Auto-generated method stub
			Count ia = (Count)a;
			return ia.data.hashCode();
		} 
	}

}
