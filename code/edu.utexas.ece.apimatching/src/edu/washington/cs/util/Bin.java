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
package edu.washington.cs.util;

import java.util.HashMap;
import java.util.Set;

public class Bin {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		int [] a = { 3,3,4,5,1,2,3,4,5,10,99,13,12,33,5,5,5,5,6};
		Bin bin = new Bin(5);
		for (int i=0; i<a.length; i++) { 
			bin.add(a[i]);	
		}
		bin.printStat();
		bin.printStat(3,20);
	}
	private int min = Integer.MAX_VALUE;
	private int max = Integer.MIN_VALUE;
	private int binsize = 1;
	private HashMap<Integer,Integer> map = null;
	public Bin (int binsize) { 
		this.binsize=binsize;
		map = new HashMap<Integer,Integer>(); 
	}
	public void add (int value) {
		// update min and max
		if (value<min) { 
			min = value;
		}
		if (value>max) {
			max = value;
		}
		
		// update the map
		Integer index = new Integer(value/binsize);
		Integer count = new Integer(1);
		if (map.containsKey(index)) { 
			count = map.get(index);
			count = new Integer(count.intValue()+1);
		}
		map.put(index,count);
	}
	public void printStat() {
		System.out.println("bin\tcount");
		for (int i=min; i<=max; i=i+binsize) { 
			Integer index = new Integer(i/binsize); 
			Integer count = map.get(index);
			int c = 0; 
			if (count!=null) { 
				c = count.intValue();
			}
			int start = (i/binsize)*binsize;
			int end = start+binsize-1;
			System.out.println( (start)+"~"+(end)+"\t"+c); 
		}
	}
	public void printStat(int start,int end) { 
		System.out.println("bin\tcount");
		for (int i=start; i<=end; i=i+binsize) { 
			Integer index = new Integer(i/binsize); 
			Integer count = map.get(index);
			int c = 0; 
			if (count!=null) { 
				c = count.intValue();
			}
			int s = (i/binsize)*binsize;
			int e= s+binsize-1;
			System.out.println( (s)+"~"+(e)+"\t"+c); 
		}
	}
	public void add (Set<Integer> values){ 
		
	}
}
