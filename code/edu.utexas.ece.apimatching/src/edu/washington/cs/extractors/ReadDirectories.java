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
package edu.washington.cs.extractors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ReadDirectories {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length!=1) System.exit(0);
		File[] fs= getDirectories(args[0]);
		for (int i=0; i< fs.length; i++){ 
			System.out.println(fs[i].getAbsolutePath());
		}
		
	}
	public static File[] getDirectories (String dir_list){

		ArrayList<File> list = new ArrayList();
		
		File f = new File (dir_list);
		try {
			if (!f.exists()) {
				System.out.println("Directory list file does not exist");
				System.exit(0);
			}
			FileReader freader = new FileReader(f);
			BufferedReader reader = new BufferedReader(freader);
			for (String s = reader.readLine(); s != null; s = reader.readLine()) {
				File dir = new File (s); 
				if (!dir.exists()) {
					System.out.println("Error in reading directory list"+ dir.getName());
				}
				else { 
					list.add(dir);
				}
			}
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}
		File [] files = new File[list.size()];
		for (int i=0; i<files.length; i++){ 
			files[i] = list.get(i);
		}
		return files;
	}
	

}
