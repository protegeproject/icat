package org.semanticweb.owlapi.model;
/*
 * Copyright (C) 2006, University of Manchester
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
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group
 * Date: 24-Oct-2006
 *
 * Represents a restriction (<a href="http://www.w3.org/TR/2009/REC-owl2-syntax-20091027/#Object_Property_Restrictions">Object Property Restriction</a> or
 * <a href="http://www.w3.org/TR/2009/REC-owl2-syntax-20091027/#Data_Property_Restrictions">Data Property Restriction</a>) in the OWL 2 specification.
 */
public interface OWLRestriction<P extends OWLPropertyExpression> extends OWLAnonymousClassExpression {

    /**
     * Gets the property that the restriction acts along.
     * @return The property
     */
    public P getProperty();

    /**
     * Determines if this is an object restriction.
     * @return <code>true</code> if this is an object restriction, otherwise <code>false</code>
     */
    boolean isObjectRestriction();

    /**
     * Determines if this is a data restriction.
     * @return <code>true</code> if this is a data restriction, otherwise <code>false</code>
     */
    boolean isDataRestriction();
}
