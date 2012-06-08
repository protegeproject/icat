package uk.ac.manchester.cs.owl.owlapi;

import clojure.lang.LockingTransaction;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.semanticweb.owlapi.model.*;

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

/*
 *  based on OWLOntologyImpl svn revision 1294
 */ 


/**
 * 	Generic class for Ref.
 * 	It can be a public class, but for the time being
 * 	will remain here.
 */
class Ref<T> extends clojure.lang.Ref{

	public Ref(Object initVal) throws Exception {
		super(initVal);
	}

	@SuppressWarnings("unchecked")
	public T deref() {
		return (T) super.deref();
	}
	
	@SuppressWarnings("unchecked")
	public T set(Object val){
		return (T) super.set(val);
	}

    @SuppressWarnings("unchecked")
	static public <T> T transaction(Callable c) {
    	try {
			return (T) LockingTransaction.runInTransaction(c);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }    

    static public <T> Ref<T> create(T obj) {
    	try {
			return new Ref<T>(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}

/**
 * Author: Matthew Horridge<br> The University Of Manchester<br> Bio-Health Informatics Group<br> Date:
 * 26-Oct-2006<br><br>
 */
public class OWLMutableOntologyImpl implements OWLMutableOntology {
	
	/**
	 * It can be implemented with closure.language.Atom, but
	 * I'm not sure how atoms and refs interact in a transaction
	 * (other transactions may call applyChanges).
	 */
	final Ref<OWLOntologyImpl> ref;
	
	/**
	 *    Should be reimplemented.
	 */
	public String toString() {
		return "OWLMutableOntology: " + ref.deref();
	}
	
	 public OWLMutableOntologyImpl(OWLOntologyManager manager, OWLOntologyID ontologyID) {
		 ref= Ref.create(new OWLOntologyImpl(manager, ontologyID));
	}
	    
    public List<OWLOntologyChange> applyChange(final OWLOntologyChange change) {
    	return applyChanges(Collections.singletonList(change));
    }

    public List<OWLOntologyChange> applyChanges(final List<OWLOntologyChange> changes) {
    	
    	return Ref.transaction(new Callable<List<OWLOntologyChange>>(){

			public List<OWLOntologyChange> call() throws Exception {
				List<OWLOntologyChange> appliedChanges = new ArrayList<OWLOntologyChange>(changes.size());
//				OWLOntology onto= ref.deref().immutableCopytWithChanges(changes, appliedChanges);
				OWLOntology onto=  new OWLOntologyImpl(ref.deref(), OWLMutableOntologyImpl.this, changes, appliedChanges);
				ref.set(onto);
				return appliedChanges;
			}});
    }
    
    public void accept(OWLObjectVisitor visitor) {
        visitor.visit(this);
    }

//	Method isn' part of the interfce or is called by anyone
//
//    public void accept(OWLNamedObjectVisitor visitor) {
//        visitor.visit(this);
//    }

    public <O> O accept(OWLObjectVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

	public boolean containsAnnotationPropertyInSignature(
			IRI owlAnnotationPropertyIRI) {
		return ref.deref().containsAnnotationPropertyInSignature(owlAnnotationPropertyIRI);
	}

	public boolean containsAnnotationPropertyInSignature(
			IRI owlAnnotationPropertyIRI, boolean includeImportsClosure) {
		return ref.deref().containsAnnotationPropertyInSignature(owlAnnotationPropertyIRI, includeImportsClosure);
	}

	public boolean containsAxiom(OWLAxiom axiom) {
		return ref.deref().containsAxiom(axiom);
	}

	public boolean containsAxiom(OWLAxiom axiom, boolean includeImportsClosure) {
		return ref.deref().containsAxiom(axiom, includeImportsClosure);
	}

	public boolean containsAxiomIgnoreAnnotations(OWLAxiom axiom) {
		return ref.deref().containsAxiomIgnoreAnnotations(axiom);
	}

	public boolean containsAxiomIgnoreAnnotations(OWLAxiom axiom,
			boolean includeImportsClosure) {
		return ref.deref().containsAxiomIgnoreAnnotations(axiom, includeImportsClosure);
	}

	public boolean containsClassInSignature(IRI owlClassIRI) {
		return ref.deref().containsClassInSignature(owlClassIRI);
	}

	public boolean containsClassInSignature(IRI owlClassIRI,
			boolean includeImportsClosure) {
		return ref.deref().containsClassInSignature(owlClassIRI, includeImportsClosure);
	}

	public boolean containsDataPropertyInSignature(IRI owlDataPropertyIRI) {
		return ref.deref().containsDataPropertyInSignature(owlDataPropertyIRI);
	}

	public boolean containsDataPropertyInSignature(IRI owlDataPropertyIRI,
			boolean includeImportsClosure) {
		return ref.deref().containsDataPropertyInSignature(owlDataPropertyIRI, includeImportsClosure);
	}

	public boolean containsDatatypeInSignature(IRI owlDatatypeIRI) {
		return ref.deref().containsDatatypeInSignature(owlDatatypeIRI);
	}

	public boolean containsDatatypeInSignature(IRI owlDatatypeIRI,
			boolean includeImportsClosure) {
		return ref.deref().containsDatatypeInSignature(owlDatatypeIRI, includeImportsClosure);
	}

	public boolean containsEntityInSignature(OWLEntity owlEntity) {
		return ref.deref().containsEntityInSignature(owlEntity);
	}

	public boolean containsEntityInSignature(OWLEntity owlEntity,
			boolean includeImportsClosure) {
		return ref.deref().containsEntityInSignature(owlEntity, includeImportsClosure);
	}

	public boolean containsEntityInSignature(IRI entityIRI) {
		return ref.deref().containsEntityInSignature(entityIRI);
	}

	public boolean containsEntityInSignature(IRI entityIRI,
			boolean includeImportsClosure) {
		return ref.deref().containsEntityInSignature(entityIRI, includeImportsClosure);
	}

	public boolean containsIndividualInSignature(IRI owlIndividualIRI) {
		return ref.deref().containsIndividualInSignature(owlIndividualIRI);
	}

	public boolean containsIndividualInSignature(IRI owlIndividualIRI,
			boolean includeImportsClosure) {
		return ref.deref().containsIndividualInSignature(owlIndividualIRI, includeImportsClosure);
	}

	public boolean containsObjectPropertyInSignature(IRI owlObjectPropertyIRI) {
		return ref.deref().containsObjectPropertyInSignature(owlObjectPropertyIRI);
	}

	public boolean containsObjectPropertyInSignature(IRI owlObjectPropertyIRI,
			boolean includeImportsClosure) {
		return ref.deref().containsObjectPropertyInSignature(owlObjectPropertyIRI, includeImportsClosure);
	}
	
    public OWLOntology getImmutableCopy(){
    	return ref.deref();
    }

//	public OWLOntology immutableCopyWithChanges(List<? extends OWLOntologyChange> changes) {
//		return ref.deref().immutableCopyWithChanges(changes);
//	}
//
//    public OWLOntology immutableCopytWithChanges(List<? extends OWLOntologyChange> changes, List<OWLOntologyChange> changed){
//		return ref.deref().immutableCopytWithChanges(changes, changed);
//	}

	public Set<OWLAnnotationAssertionAxiom> getAnnotationAssertionAxioms(
			OWLAnnotationSubject entity) {
		return ref.deref().getAnnotationAssertionAxioms(entity);
	}

	public Set<OWLAnnotationProperty> getAnnotationPropertiesInSignature() {
		return ref.deref().getAnnotationPropertiesInSignature();
	}

	public Set<OWLAnnotationPropertyDomainAxiom> getAnnotationPropertyDomainAxioms(
			OWLAnnotationProperty property) {
		return ref.deref().getAnnotationPropertyDomainAxioms(property);
	}

	public Set<OWLAnnotationPropertyRangeAxiom> getAnnotationPropertyRangeAxioms(
			OWLAnnotationProperty property) {
		return ref.deref().getAnnotationPropertyRangeAxioms(property);
	}

	public Set<OWLAnnotation> getAnnotations() {

		return ref.deref().getAnnotations();
	}

	public Set<OWLAsymmetricObjectPropertyAxiom> getAsymmetricObjectPropertyAxioms(
			OWLObjectPropertyExpression property) {
		return ref.deref().getAsymmetricObjectPropertyAxioms(property);
	}

	public int getAxiomCount() {
		return ref.deref().getAxiomCount();
	}

	public <T extends OWLAxiom> int getAxiomCount(AxiomType<T> axiomType) {
		return ref.deref().getAxiomCount(axiomType);
	}

	public <T extends OWLAxiom> int getAxiomCount(AxiomType<T> axiomType,
			boolean includeImportsClosure) {
		return ref.deref().getAxiomCount(axiomType, includeImportsClosure);
	}

	public Set<OWLAxiom> getAxioms() {
		return ref.deref().getAxioms();
	}

	public <T extends OWLAxiom> Set<T> getAxioms(AxiomType<T> axiomType) {
		return ref.deref().getAxioms(axiomType) ;
	}

	public <T extends OWLAxiom> Set<T> getAxioms(AxiomType<T> axiomType,
			boolean includeImportsClosure) {
		return ref.deref().getAxioms(axiomType, includeImportsClosure);
	}

	public Set<OWLClassAxiom> getAxioms(OWLClass cls) {
		return ref.deref().getAxioms(cls);
	}

	public Set<OWLObjectPropertyAxiom> getAxioms(
			OWLObjectPropertyExpression prop) {
		return ref.deref().getAxioms(prop);
	}

	public Set<OWLDataPropertyAxiom> getAxioms(OWLDataProperty prop) {
		return ref.deref().getAxioms(prop);
	}

	public Set<OWLIndividualAxiom> getAxioms(OWLIndividual individual) {
		return ref.deref().getAxioms(individual);
	}

	public Set<OWLAnnotationAxiom> getAxioms(OWLAnnotationProperty property) {
		return ref.deref().getAxioms(property);
	}

	public Set<OWLDatatypeDefinitionAxiom> getAxioms(OWLDatatype datatype) {
		return ref.deref().getAxioms(datatype);
	}

	public Set<OWLAxiom> getAxiomsIgnoreAnnotations(OWLAxiom axiom) {
		return ref.deref().getAxiomsIgnoreAnnotations(axiom);
	}

	public Set<OWLAxiom> getAxiomsIgnoreAnnotations(OWLAxiom axiom,
			boolean includeImportsClosure) {
		return ref.deref().getAxiomsIgnoreAnnotations(axiom, includeImportsClosure);
	}

	public Set<OWLClassAssertionAxiom> getClassAssertionAxioms(
			OWLIndividual individual) {
		return ref.deref().getClassAssertionAxioms(individual);
	}

	public Set<OWLClassAssertionAxiom> getClassAssertionAxioms(OWLClass ce) {
		return ref.deref().getClassAssertionAxioms(ce);
	}

	public Set<OWLClass> getClassesInSignature() {
		return ref.deref().getClassesInSignature();
	}

	public Set<OWLClass> getClassesInSignature(boolean includeImportsClosure) {
		return ref.deref().getClassesInSignature(includeImportsClosure);
	}

	public Set<OWLDataProperty> getDataPropertiesInSignature() {
		return ref.deref().getDataPropertiesInSignature();
	}

	public Set<OWLDataProperty> getDataPropertiesInSignature(
			boolean includeImportsClosure) {
		return ref.deref().getDataPropertiesInSignature(includeImportsClosure);
	}

	public Set<OWLDataPropertyAssertionAxiom> getDataPropertyAssertionAxioms(
			OWLIndividual individual) {
		return ref.deref().getDataPropertyAssertionAxioms(individual);
	}

	public Set<OWLDataPropertyDomainAxiom> getDataPropertyDomainAxioms(
			OWLDataProperty property) {
		return ref.deref().getDataPropertyDomainAxioms(property);
	}

	public Set<OWLDataPropertyRangeAxiom> getDataPropertyRangeAxioms(
			OWLDataProperty property) {
		return ref.deref().getDataPropertyRangeAxioms(property);
	}

	public Set<OWLSubDataPropertyOfAxiom> getDataSubPropertyAxiomsForSubProperty(
			OWLDataProperty subProperty) {
		return ref.deref().getDataSubPropertyAxiomsForSubProperty(subProperty);
	}

	public Set<OWLSubDataPropertyOfAxiom> getDataSubPropertyAxiomsForSuperProperty(
			OWLDataPropertyExpression superProperty) {
		return ref.deref().getDataSubPropertyAxiomsForSuperProperty(superProperty);
	}

	public Set<OWLDatatypeDefinitionAxiom> getDatatypeDefinitions(
			OWLDatatype datatype) {
		return ref.deref().getDatatypeDefinitions(datatype);
	}

	public Set<OWLDatatype> getDatatypesInSignature() {
		return ref.deref().getDatatypesInSignature();
	}

	public Set<OWLDatatype> getDatatypesInSignature(
			boolean includeImportsClosure) {
		return ref.deref().getDatatypesInSignature(includeImportsClosure);
	}

	public Set<OWLDeclarationAxiom> getDeclarationAxioms(OWLEntity subject) {
		return ref.deref().getDeclarationAxioms(subject);
	}

	public Set<OWLDifferentIndividualsAxiom> getDifferentIndividualAxioms(
			OWLIndividual individual) {
		return ref.deref().getDifferentIndividualAxioms(individual);
	}

	public Set<IRI> getDirectImportsDocuments()
			throws UnknownOWLOntologyException {
		return ref.deref().getDirectImportsDocuments();
	}

	public Set<OWLDisjointClassesAxiom> getDisjointClassesAxioms(OWLClass cls) {
		return ref.deref().getDisjointClassesAxioms(cls);
	}

	public Set<OWLDisjointDataPropertiesAxiom> getDisjointDataPropertiesAxioms(
			OWLDataProperty property) {
		return ref.deref().getDisjointDataPropertiesAxioms(property);
	}

	public Set<OWLDisjointObjectPropertiesAxiom> getDisjointObjectPropertiesAxioms(
			OWLObjectPropertyExpression property) {
		return ref.deref().getDisjointObjectPropertiesAxioms(property);
	}

	public Set<OWLDisjointUnionAxiom> getDisjointUnionAxioms(OWLClass owlClass) {
		return ref.deref().getDisjointUnionAxioms(owlClass);
	}

	public Set<OWLEntity> getEntitiesInSignature(IRI iri) {
		return ref.deref().getEntitiesInSignature(iri);
	}

	public Set<OWLEntity> getEntitiesInSignature(IRI iri,
			boolean includeImportsClosure) {
		return ref.deref().getEntitiesInSignature(iri, includeImportsClosure);
	}

	public Set<OWLEquivalentClassesAxiom> getEquivalentClassesAxioms(
			OWLClass cls) {
		return ref.deref().getEquivalentClassesAxioms(cls);
	}

	public Set<OWLEquivalentDataPropertiesAxiom> getEquivalentDataPropertiesAxioms(
			OWLDataProperty property) {
		return ref.deref().getEquivalentDataPropertiesAxioms(property);
	}

	public Set<OWLEquivalentObjectPropertiesAxiom> getEquivalentObjectPropertiesAxioms(
			OWLObjectPropertyExpression property) {
		return ref.deref().getEquivalentObjectPropertiesAxioms(property);
	}

	public Set<OWLFunctionalDataPropertyAxiom> getFunctionalDataPropertyAxioms(
			OWLDataPropertyExpression property) {
		return ref.deref().getFunctionalDataPropertyAxioms(property);
	}

	public Set<OWLFunctionalObjectPropertyAxiom> getFunctionalObjectPropertyAxioms(
			OWLObjectPropertyExpression property) {
		return ref.deref().getFunctionalObjectPropertyAxioms(property);
	}

	public Set<OWLClassAxiom> getGeneralClassAxioms() {
		return ref.deref().getGeneralClassAxioms();
	}

	public Set<OWLHasKeyAxiom> getHasKeyAxioms(OWLClass cls) {
		return ref.deref().getHasKeyAxioms(cls);
	}

	/*
	 * WARNING: The manager saves pointers to all OWLOntology objects in the
	 * import closure list. It assumes that there is only one object representing
	 * each loaded ontology.
	 * 
	 * It's important to call these 3 methods from here so they will pass
	 * the correct this pointer to the manager. Failure to do that, will
	 * cause problem as the manager will cache the pointers to the no mutable
	 * OWLOntology objects
	 */
    public Set<OWLOntology> getImports() throws UnknownOWLOntologyException {
        return ref.deref().getOWLOntologyManager().getImports(this);
    }

    public Set<OWLOntology> getDirectImports() throws UnknownOWLOntologyException {
        return ref.deref().getOWLOntologyManager().getDirectImports(this);
    }

    public Set<OWLOntology> getImportsClosure() throws UnknownOWLOntologyException {
        return ref.deref().getOWLOntologyManager().getImportsClosure(this);
    }
    
    /****************************************************************/

	public Set<OWLImportsDeclaration> getImportsDeclarations() {
		return ref.deref().getImportsDeclarations();
	}

	public Set<OWLNamedIndividual> getIndividualsInSignature() {
		return ref.deref().getIndividualsInSignature();
	}

	public Set<OWLNamedIndividual> getIndividualsInSignature(
			boolean includeImportsClosure) {
		return ref.deref().getIndividualsInSignature(includeImportsClosure);
	}

	public Set<OWLInverseFunctionalObjectPropertyAxiom> getInverseFunctionalObjectPropertyAxioms(
			OWLObjectPropertyExpression property) {
		return ref.deref().getInverseFunctionalObjectPropertyAxioms(property);
	}

	public Set<OWLInverseObjectPropertiesAxiom> getInverseObjectPropertyAxioms(
			OWLObjectPropertyExpression property) {
		return ref.deref().getInverseObjectPropertyAxioms(property);
	}

	public Set<OWLIrreflexiveObjectPropertyAxiom> getIrreflexiveObjectPropertyAxioms(
			OWLObjectPropertyExpression property) {
		return ref.deref().getIrreflexiveObjectPropertyAxioms(property);
	}

	public int getLogicalAxiomCount() {
		return ref.deref().getAxiomCount();
	}

	public Set<OWLLogicalAxiom> getLogicalAxioms() {
		return ref.deref().getLogicalAxioms();
	}

	public Set<OWLNegativeDataPropertyAssertionAxiom> getNegativeDataPropertyAssertionAxioms(
			OWLIndividual individual) {
		return ref.deref().getNegativeDataPropertyAssertionAxioms(individual);
	}

	public Set<OWLNegativeObjectPropertyAssertionAxiom> getNegativeObjectPropertyAssertionAxioms(
			OWLIndividual individual) {
		return ref.deref().getNegativeObjectPropertyAssertionAxioms(individual);
	}

	public OWLOntologyManager getOWLOntologyManager() {
		return ref.deref().getOWLOntologyManager();
	}

	public Set<OWLObjectProperty> getObjectPropertiesInSignature() {
		return ref.deref().getObjectPropertiesInSignature();
	}

	public Set<OWLObjectProperty> getObjectPropertiesInSignature(
			boolean includeImportsClosure) {
		return ref.deref().getObjectPropertiesInSignature(includeImportsClosure);
	}

	public Set<OWLObjectPropertyAssertionAxiom> getObjectPropertyAssertionAxioms(
			OWLIndividual individual) {
		return ref.deref().getObjectPropertyAssertionAxioms(individual);
	}

	public Set<OWLObjectPropertyDomainAxiom> getObjectPropertyDomainAxioms(
			OWLObjectPropertyExpression property) {
		return ref.deref().getObjectPropertyDomainAxioms(property);
	}

	public Set<OWLObjectPropertyRangeAxiom> getObjectPropertyRangeAxioms(
			OWLObjectPropertyExpression property) {
		return ref.deref().getObjectPropertyRangeAxioms(property);
	}

	public Set<OWLSubObjectPropertyOfAxiom> getObjectSubPropertyAxiomsForSubProperty(
			OWLObjectPropertyExpression subProperty) {
		return ref.deref().getObjectSubPropertyAxiomsForSubProperty(subProperty);
	}

	public Set<OWLSubObjectPropertyOfAxiom> getObjectSubPropertyAxiomsForSuperProperty(
			OWLObjectPropertyExpression superProperty) {
		return ref.deref().getObjectSubPropertyAxiomsForSuperProperty(superProperty);
	}

	public OWLOntologyID getOntologyID() {
		return ref.deref().getOntologyID();
	}

	public Set<OWLAnonymousIndividual> getReferencedAnonymousIndividuals() {
		return ref.deref().getReferencedAnonymousIndividuals();
	}

	public Set<OWLAxiom> getReferencingAxioms(OWLEntity owlEntity) {
		return ref.deref().getReferencingAxioms(owlEntity);
	}

	public Set<OWLAxiom> getReferencingAxioms(OWLEntity owlEntity,
			boolean includeImportsClosure) {
		return ref.deref(). getReferencingAxioms(owlEntity, includeImportsClosure);
	}

	public Set<OWLAxiom> getReferencingAxioms(OWLAnonymousIndividual individual) {
		return ref.deref().getReferencingAxioms(individual);
	}

	public Set<OWLReflexiveObjectPropertyAxiom> getReflexiveObjectPropertyAxioms(
			OWLObjectPropertyExpression property) {
		return ref.deref().getReflexiveObjectPropertyAxioms(property);
	}

	public Set<OWLSameIndividualAxiom> getSameIndividualAxioms(
			OWLIndividual individual) {
		return ref.deref().getSameIndividualAxioms(individual);
	}

	public Set<OWLEntity> getSignature() {
		return ref.deref().getSignature();
	}

	public Set<OWLEntity> getSignature(boolean includeImportsClosure) {
		return ref.deref().getSignature(includeImportsClosure);
	}

	public Set<OWLSubAnnotationPropertyOfAxiom> getSubAnnotationPropertyOfAxioms(
			OWLAnnotationProperty subProperty) {
		return ref.deref().getSubAnnotationPropertyOfAxioms(subProperty);
	}

	public Set<OWLSubClassOfAxiom> getSubClassAxiomsForSubClass(OWLClass cls) {
		return ref.deref().getSubClassAxiomsForSubClass(cls);
	}

	public Set<OWLSubClassOfAxiom> getSubClassAxiomsForSuperClass(OWLClass cls) {
		return ref.deref().getSubClassAxiomsForSuperClass(cls);
	}

	public Set<OWLSymmetricObjectPropertyAxiom> getSymmetricObjectPropertyAxioms(
			OWLObjectPropertyExpression property) {
		return ref.deref().getSymmetricObjectPropertyAxioms(property);
	}

	public Set<OWLTransitiveObjectPropertyAxiom> getTransitiveObjectPropertyAxioms(
			OWLObjectPropertyExpression property) {
		return ref.deref().getTransitiveObjectPropertyAxioms(property);
	}

	public boolean isAnonymous() {
		return ref.deref().isAnonymous();
	}

	public boolean isDeclared(OWLEntity owlEntity) {
		return ref.deref().isDeclared(owlEntity);
	}

	public boolean isDeclared(OWLEntity owlEntity, boolean includeImportsClosure) {
		return ref.deref().isDeclared(owlEntity, includeImportsClosure);
	}

	public boolean isEmpty() {
		return ref.deref().isEmpty();
	}

	public int compareTo(OWLObject o) {
		return ref.deref().compareTo(o);
	}
}
