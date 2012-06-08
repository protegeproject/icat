package org.coode.owlapi.obo.renderer;

import org.semanticweb.owlapi.io.OWLRendererException;

import java.util.List;
import java.util.ArrayList;
/*
* Copyright (C) 2007, University of Manchester
*
* Modifications to the initial code base are copyright of their
* respective authors, or their employers as appropriate.  Authorship
* of the modifications may be determined from the ChangeLog placed at
* the end of this file.
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.

* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.

* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Apr 9, 2009<br><br>
 */
public class OBOStorageIncompleteException extends OWLRendererException {

    private List<OBOStorageException> exceptions;


    public OBOStorageIncompleteException(List<OBOStorageException> exceptions) {
        super("Warning: OBO storage incomplete (" + exceptions.size() + " errors)");
        this.exceptions = exceptions;
    }


    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
            for (OBOStorageException e : exceptions){
                sb.append("\n");
                sb.append(e.getMessage());
            }
        return sb.toString();
    }


    public List<OBOStorageException> getCauses(){
        return new ArrayList<OBOStorageException>(exceptions);
    }
}
