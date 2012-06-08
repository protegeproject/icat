package de.uulm.ecs.ai.owlapi.krssrenderer;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.AbstractOWLOntologyStorer;

import java.io.Writer;

import de.uulm.ecs.ai.owlapi.krssparser.KRSS2OntologyFormat;
/*
 * Copyright (C) 2008, Ulm University
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
 * See {@link de.uulm.ecs.ai.owlapi.krssrenderer.KRSS2ObjectRenderer KRSS2ObjectRenderer}
 * for definition/explanation of the syntax.
 *
 * Author: Olaf Noppens<br>
 * Ulm University<br>
 * Institute of Artificial Intelligence<br>
 */
public class KRSS2SyntaxOntologyStorer extends AbstractOWLOntologyStorer {

    /**
     * @inheritDoc
     */
    public boolean canStoreOntology(OWLOntologyFormat ontologyFormat) {
        return ontologyFormat.equals(new KRSS2OntologyFormat());
    }

    // I changed this class to extend AbstractOWLOntologyStorer - Matthew Horridge

    protected void storeOntology(OWLOntologyManager manager, OWLOntology ontology, Writer writer, OWLOntologyFormat format) throws
            OWLOntologyStorageException {
        KRSS2SyntaxRenderer renderer = new KRSS2SyntaxRenderer(manager);
        renderer.render(ontology, writer);
    }
}
