package uk.ac.manchester.owl.owlapi.tutorial.examples;

//import gnu.getopt.Getopt;
//import gnu.getopt.LongOpt;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import uk.ac.manchester.owl.owlapi.tutorial.io.OWLTutorialSyntaxOntologyFormat;
import uk.ac.manchester.owl.owlapi.tutorial.io.OWLTutorialSyntaxOntologyStorer;

import java.net.URI;

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
 * <p>Simple Rendering Example. Reads an ontology and then renders it.</p>
 * <p/>
 * Author: Sean Bechhofer<br>
 * The University Of Manchester<br>
 * Information Management Group<br>
 * Date: 24-April-2007<br>
 * <br>
 */
public class RenderingExample {

    public static void main(String[] args) {
        // A simple example of how to load and save an ontology
//        try {
//
//            /* Command line arguments */
//            LongOpt[] longopts = new LongOpt[11];
//            String inputOntology = null;
//            String outputOntology = null;
//
//            longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, '?');
//            longopts[1] = new LongOpt("input", LongOpt.REQUIRED_ARGUMENT, null,
//                    'i');
//            longopts[2] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT,
//                    null, 'o');
//
//            Getopt g = new Getopt("", args, "?:i:o", longopts);
//            int c;
//
//            while ((c = g.getopt()) != -1) {
//                switch (c) {
//                    case '?':
//                        System.out.println("RenderingExample --input=URL --output=URL");
//                        System.exit(0);
//                    case 'i':
//                        /* input */
//                        inputOntology = g.getOptarg();
//                        break;
//                    case 'o':
//                        /* input */
//                        outputOntology = g.getOptarg();
//                        break;
//                }
//            }
//
//            /* Get an Ontology Manager */
//            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
//            if (inputOntology == null || outputOntology == null) {
//                System.out.println("RenderingExample --input=URL --output=URL");
//
//                System.exit(1);
//            }
//
//            IRI inputDocumentIRI = IRI.create(inputOntology);
//            IRI outputDocumentIRI = IRI.create(outputOntology);
//
//            /* Load an ontology from a document IRI */
//
//            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(inputDocumentIRI);
//            /* Report information about the ontology */
//            System.out.println("Ontology Loaded...");
//            System.out.println("Document IRI: " + inputDocumentIRI);
//            System.out.println("Logical IRI : " + ontology.getOntologyID());
//            System.out.println("Format      : "
//                    + manager.getOntologyFormat(ontology));
//
//            /* Register the ontology storer with the manager */
//            manager.addOntologyStorer(new OWLTutorialSyntaxOntologyStorer());
//
//            /* Save using a different format */
//
//            System.out.println("Storing     : " + outputDocumentIRI);
//            manager.saveOntology(ontology, new OWLTutorialSyntaxOntologyFormat(), outputDocumentIRI);
//            /* Remove the ontology from the manager */
//            manager.removeOntology(ontology);
//            System.out.println("Done");
//
//        } catch (OWLException e) {
//            e.printStackTrace();
//        }
    }
}
