package org.coode.owlapi.rdfxml.parser;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.net.URI;

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
 * Bio-Health Informatics Group<br>
 * Date: 08-Dec-2006<br><br>
 */
public class DataMaxCardinalityTranslator extends AbstractDataCardinalityTranslator {

    public DataMaxCardinalityTranslator(OWLRDFConsumer consumer) {
        super(consumer);
    }


    protected OWLClassExpression createRestriction(OWLDataPropertyExpression prop, int cardi, OWLDataRange filler) {
        return getDataFactory().getOWLDataMaxCardinality(cardi, prop, filler);
    }


    protected IRI getCardinalityTriplePredicate() {
        return OWLRDFVocabulary.OWL_MAX_CARDINALITY.getIRI();
    }

    /**
     * Gets the predicate of the qualified cardinality triple.
     * @return The predicate IRI
     */
    protected IRI getQualifiedCardinalityTriplePredicate() {
        return OWLRDFVocabulary.OWL_MAX_QUALIFIED_CARDINALITY.getIRI();
    }
}
