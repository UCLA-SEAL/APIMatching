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

import java.util.ArrayList;
import java.util.List;

public class Signature {
	Parameter returnType;

	List parameters = new ArrayList();

	public void setReturnType(String type) {
		returnType = new Parameter();
		returnType.setType(type);
	}

	public String getReturnType (){
		return returnType.getType();
	}
	
	public String [] getParameterTypes () {
		String [] params = new String[parameters.size()];
		for (int i = 0; i < parameters.size(); i++) {
			Parameter p = (Parameter)parameters.get(i);
			params[i] = p.getType();
		}
		return params;
	}
	public void addParameter(String type, String name) {
		Parameter parameter = new Parameter();
		parameter.setType(type);
		parameter.setName(name);
		parameters.add(parameter);
	}

	public List getAllParameters() {
		List allParamater = new ArrayList();
		allParamater.add(returnType);
		allParamater.addAll(parameters);

		return allParamater;
	}

	public String toString() {
		String ret = returnType.toString() + "(";
		for (int i = 0; i < parameters.size(); i++) {
			if (i != 0) {
				ret += ",";
			}
			ret += parameters.get(i);
		}

		return ret + ")";
	}
}
