package org.semanticweb.owlapi.api.test;

import junit.framework.TestCase;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.profiles.*;
/*
 * Copyright (C) 2009, University of Manchester
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
 * The University of Manchester<br>
 * Information Management Group<br>
 * Date: 18-Aug-2009
 */
public class ProfileValidationTestCase extends TestCase {


    private static final String TEST_NAMESPACE = "http://www.w3.org/2007/OWL/testOntology#";

    private static final IRI TEST_CASE_IRI = IRI.create(TEST_NAMESPACE + "TestCase");

    private static final IRI PROFILE_IDENTIFICATION_TEST_IRI = IRI.create(TEST_NAMESPACE + "ProfileIdentificationTest");

    private static final IRI PROFILE_IRI = IRI.create(TEST_NAMESPACE + "profile");

    private static final IRI SPECIES_IRI = IRI.create(TEST_NAMESPACE + "species");

    private static final IRI FULL_IRI = IRI.create(TEST_NAMESPACE + "FULL");

    private static final IRI DL_IRI = IRI.create(TEST_NAMESPACE + "DL");

    private static final IRI EL_IRI = IRI.create(TEST_NAMESPACE + "EL");

    private static final IRI QL_IRI = IRI.create(TEST_NAMESPACE + "QL");

    private static final IRI RL_IRI = IRI.create(TEST_NAMESPACE + "RL");

    private static final IRI RDF_XML_PREMISE_ONTOLOGY_IRI = IRI.create(TEST_NAMESPACE + "rdfXmlPremiseOntology");


    public void testProfiles() throws Exception {
//        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
//        URL resourceURL = ProfileValidationTestCase.class.getResource("/all.rdf");
//        URI allTestURI = resourceURL.toURI();
//        OWLOntology testCasesOntology = man.loadOntologyFromOntologyDocument(allTestURI);
//
//        OWLDataFactory df = man.getOWLDataFactory();
//        OWLClass profileIdentificationTestClass = df.getOWLClass(PROFILE_IDENTIFICATION_TEST_IRI);
//        OWLObjectProperty profileProperty = df.getOWLObjectProperty(PROFILE_IRI);
//        OWLNamedIndividual EL = df.getOWLNamedIndividual(EL_IRI);
//        OWLNamedIndividual QL = df.getOWLNamedIndividual(QL_IRI);
//        OWLNamedIndividual RL = df.getOWLNamedIndividual(RL_IRI);
//        OWLObjectProperty speciesProperty = df.getOWLObjectProperty(SPECIES_IRI);
//        OWLNamedIndividual FULL = df.getOWLNamedIndividual(FULL_IRI);
//        OWLNamedIndividual DL = df.getOWLNamedIndividual(DL_IRI);
//
//        OWLDataProperty rdfXMLPremiseOntologyProperty = df.getOWLDataProperty(RDF_XML_PREMISE_ONTOLOGY_IRI);
//
//        man.addOntologyLoaderListener(new OWLOntologyLoaderListener() {
//                /**
//                 * Called when the process of attempting to load an ontology starts.
//                 * @param event The loading started event that describes the ontologt that
//                 *              is being loaded.
//                 */
//                public void startedLoadingOntology(LoadingStartedEvent event) {
//                    System.out.println("STARTING: " + event.getOntologyID());
//                }
//
//                /**
//                 * Called when the process of loading an ontology has
//                 * finished.  This method will be called regardless of whether the
//                 * ontology could be loaded or not - it merely indicates that the process
//                 * of attempting to load an ontology has finished.
//                 * @param event The loading finished event that describes the ontology that was
//                 *              loaded.
//                 */
//                public void finishedLoadingOntology(LoadingFinishedEvent event) {
//                    System.out.println("FINISHING: " + event.getOntologyID());
//                }
//            });
//
//
//        for (OWLClassAssertionAxiom ax : testCasesOntology.getClassAssertionAxioms(profileIdentificationTestClass)) {
//            System.out.println("---------------------------------------------------------------------------------------------------");
//            OWLIndividual ind = ax.getIndividual();
//            System.out.println(ind);
//            Set<OWLLiteral> vals = ind.getDataPropertyValues(rdfXMLPremiseOntologyProperty, testCasesOntology);
//            if (vals.size() != 1) {
//                continue;
//            }
//            String ontologySerialisation = vals.iterator().next().getLiteral();
//            System.out.println(ontologySerialisation);
//            System.out.println("Loading...");
//            OWLOntology ontology = man.loadOntologyFromOntologyDocument(new StringInputSource(ontologySerialisation, ind.asOWLNamedIndividual().getIRI().toURI()));
//
//            System.out.println("   ... loaded ont");
//            // FULL?
//            if (ind.hasObjectPropertyValue(speciesProperty, FULL, testCasesOntology)) {
//                checkProfile(ontology, new OWL2Profile(), true);
//            }
//
//            if (ind.hasNegativeObjectPropertyValue(speciesProperty, FULL, testCasesOntology)) {
//                checkProfile(ontology, new OWL2Profile(), false);
//            }
//
//            // DL?
//            if (ind.hasObjectPropertyValue(speciesProperty, DL, testCasesOntology)) {
//                checkProfile(ontology, new OWL2DLProfile(), true);
//            }
//
//            if (ind.hasNegativeObjectPropertyValue(speciesProperty, DL, testCasesOntology)) {
//                checkProfile(ontology, new OWL2DLProfile(), false);
//            }
//
//            // EL?
//            if (ind.hasObjectPropertyValue(speciesProperty, EL, testCasesOntology)) {
//                checkProfile(ontology, new OWL2ELProfile(), true);
//            }
//
//            if (ind.hasNegativeObjectPropertyValue(speciesProperty, EL, testCasesOntology)) {
//                checkProfile(ontology, new OWL2ELProfile(), false);
//            }
//            // QL?
//            if (ind.hasObjectPropertyValue(speciesProperty, QL, testCasesOntology)) {
//                checkProfile(ontology, new OWL2QLProfile(), true);
//            }
//
//            if (ind.hasNegativeObjectPropertyValue(speciesProperty, QL, testCasesOntology)) {
//                checkProfile(ontology, new OWL2QLProfile(), false);
//            }
//            // RL?
//            if (ind.hasObjectPropertyValue(speciesProperty, RL, testCasesOntology)) {
//                checkProfile(ontology, new OWL2RLProfile(), true);
//            }
//
//            if (ind.hasNegativeObjectPropertyValue(speciesProperty, RL, testCasesOntology)) {
//                checkProfile(ontology, new OWL2RLProfile(), false);
//            }
//            man.removeOntology(ontology);
//
//        }
    }

    private void checkProfile(OWLOntology ontology, OWLProfile profile, boolean shouldBeInProfile) {
        OWLProfileReport report = profile.checkOntology(ontology);
        if (report.isInProfile() != shouldBeInProfile) {
            if (shouldBeInProfile) {
                System.out.println("FAIL: " + ontology.getOntologyID() + " should be in the " + profile.getName() + " profile. Report: " + report);
            }
            else {
                System.out.println("FAIL: " + ontology.getOntologyID() + " should not be in the " + profile.getName() + " profile. Report: " + report);
            }
        }
    }

}
