package org.coode.owlapi.rdfxml.parser;

import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.UnloadableImportException;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
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
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 13-Feb-2007<br><br>
 */
public class TypeSelfRestrictionHandler extends BuiltInTypeHandler {

    public TypeSelfRestrictionHandler(OWLRDFConsumer consumer) {
        super(consumer, OWLRDFVocabulary.OWL_SELF_RESTRICTION.getIRI());
    }


    public void handleTriple(IRI subject, IRI predicate, IRI object) throws UnloadableImportException {
        consumeTriple(subject, predicate, object);
        getConsumer().addRestriction(subject);
        // Patch to new OWL syntax
        getConsumer().addTriple(subject, OWLRDFVocabulary.OWL_HAS_SELF.getIRI(), getDataFactory().getOWLTypedLiteral(true));
    }
}
