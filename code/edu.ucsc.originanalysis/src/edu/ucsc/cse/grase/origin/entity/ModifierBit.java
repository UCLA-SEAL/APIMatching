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
package edu.ucsc.cse.grase.origin.entity;

import java.lang.reflect.Modifier;

public class ModifierBit {

	boolean 	is_Abstract=false; 
    boolean 	is_Final=false;
    boolean 	is_Private=false; 
    boolean 	is_Protected=false; 
    boolean 	is_Public=false; 
    boolean 	is_Static=false;
    boolean 	is_Strict=false; 
    boolean 	is_Synchronized=false; 
    boolean 	is_Transient=false;
    boolean 	is_Volatile=false;

    public ModifierBit(int modifiers) { 
    	if (Modifier.isAbstract(modifiers)) { 
			is_Abstract=true; 
		}
		if (Modifier.isFinal(modifiers)) { 
			is_Final =true; 			
		}
		if (Modifier.isPrivate(modifiers)) { 
			is_Private=true;
		}
		if (Modifier.isProtected(modifiers)) { 
			is_Protected=true;
		}
		if (Modifier.isPublic(modifiers)) { 
			is_Public=true;
		}
		if (Modifier.isStatic(modifiers)){ 
			is_Static=true;
		}
		if (Modifier.isStrict(modifiers)){ 
			is_Strict=true;
		}
		if (Modifier.isSynchronized(modifiers)) { 
			is_Synchronized=true;
		}
		if (Modifier.isTransient(modifiers)) { 
			is_Transient=true;
		}
		if (Modifier.isVolatile(modifiers)) { 
			is_Volatile=true;
		}
    }
    public String toString () {
    	String str="";
    	if (is_Abstract) { 
			str=str+("abstract"); 
		}
		if (is_Final) { 
			str=str+("final"); 			
		}
		if (is_Private) { 
			str=str+("private");
		}
		if (is_Protected) { 
			str=str+("protected");
		}
		if (is_Public) { 
			str=str+("public");
		}
		if (is_Static){ 
			str=str+("static");
		}
		if (is_Strict){ 
			str=str+("strict");
		}
		if (is_Synchronized) { 
			str=str+("synchronized");
		}
		if (is_Transient) { 
			str=str+("transient");
		}
		if (is_Volatile) { 
			str=str+("volatile");
		}
		return str;
    }
}
