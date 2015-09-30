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

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class MethodStackTraceFilter extends FileFilter{
	public static final String suffix = ".stacktrace";
	
	public boolean accept(File f) {
		// TODO Auto-generated method stub
		if (f.isDirectory())
			return true;
		return (f.getAbsolutePath().indexOf(suffix) >= 0) ;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return suffix;
	}

}