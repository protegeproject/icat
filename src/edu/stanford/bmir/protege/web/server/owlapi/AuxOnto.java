/**
 * 
 */
package edu.stanford.bmir.protege.web.server.owlapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

//import org.protege.editor.owl.model.hierarchy.AssertedClassHierarchyProvider;
//import org.protege.editor.owl.model.hierarchy.OWLAnnotationPropertyHierarchyProvider;
//import org.protege.editor.owl.model.hierarchy.OWLDataPropertyHierarchyProvider;
//import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
//import org.protege.editor.owl.model.hierarchy.OWLObjectPropertyHierarchyProvider;
import org.protege.editor.owl.model.hierarchy.AssertedClassHierarchyProvider;
import org.protege.editor.owl.model.hierarchy.OWLAnnotationPropertyHierarchyProvider;
import org.protege.editor.owl.model.hierarchy.OWLDataPropertyHierarchyProvider;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.protege.editor.owl.model.hierarchy.OWLObjectPropertyHierarchyProvider;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/**
 * This class should have used the Decorator pattern as defined in
 * http://billharlan.com/pub/papers/Avoid_extending_classes.html
 * 
 * But that results in a lot of duck taping to make the correct methods
 * call their implementations using the interface.
 * 
 * The problem is how to extend a class when you don't have assess to this
 * class, but just to objects (from a factory) that implement a certain interface.
 * 
 * @author dilvan
 *
 */
public final class AuxOnto {
	
	private final OWLClass thing;
	private final OWLOntology onto;
	private final OWLObjectHierarchyProvider<OWLClass> hierarchy;
	private final OWLObjectHierarchyProvider<OWLObjectProperty> objProps;
	private final OWLObjectHierarchyProvider<OWLDataProperty> dataProps;
	private final OWLObjectHierarchyProvider<OWLAnnotationProperty> annotationProps;
	
	private final OWLObjectProperty topObjectProperty;
	private final OWLDataProperty topDataProperty;
	
	public AuxOnto(OWLOntology onto1){
		
		onto= onto1;
		OWLOntologyManager manager= onto.getOWLOntologyManager();
		
		thing= manager.getOWLDataFactory().getOWLThing();
		hierarchy= new AssertedClassHierarchyProvider(manager);
		hierarchy.setOntologies(Collections.singleton(onto));
		
		objProps= new OWLObjectPropertyHierarchyProvider(manager);
		objProps.setOntologies(onto.getImportsClosure());
		
		dataProps= new OWLDataPropertyHierarchyProvider(manager);
		dataProps.setOntologies(onto.getImportsClosure());
		
		annotationProps= new OWLAnnotationPropertyHierarchyProvider(manager);
		annotationProps.setOntologies(onto.getImportsClosure());

		topObjectProperty= manager.getOWLDataFactory().getOWLObjectProperty(OWLRDFVocabulary.OWL_TOP_OBJECT_PROPERTY.getIRI());
		topDataProperty= manager.getOWLDataFactory().getOWLDataProperty(OWLRDFVocabulary.OWL_TOP_DATA_PROPERTY.getIRI());
	}
	
	public OWLOntology getOntology(){
		return onto;
	}
	
	public OWLClass getOWLThing() {
		return thing;
	}
	
	public Set<OWLEntity> getSubproperties(String propertyName) {

		Set<OWLEntity> set= new HashSet<OWLEntity>();

		//	Null pointer indicates root properties
		if (propertyName==null) {

			set.addAll( objProps.getChildren(topObjectProperty));
			set.addAll( dataProps.getChildren(topDataProperty));

			for (OWLAnnotationProperty prop : annotationProps.getRoots()){
				if (prop.isBuiltIn()) continue;
				set.add(prop);
			}
			//				lst.addAll( Util.createEntityList(onto.annotationProps.getRoots()));
			return Collections.unmodifiableSet(set);
		}

		OWLDataFactory factory = onto.getOWLOntologyManager().getOWLDataFactory();
		IRI iri= IRI.create(propertyName);

//		Set<OWLEntity> entities= 
//			onto.getEntitiesWithIRI(IRI.create(propertyName), true);
//
//		if (entities.isEmpty())
//			return Collections.EMPTY_SET;
//
//		OWLEntity prop= entities.iterator().next();

		//			OWLProperty prop= onto.getOWLProperty(propertyName);

		OWLEntity prop= factory.getOWLObjectProperty(iri);
		if (prop!=null)
			set.addAll(objProps.getChildren(prop.asOWLObjectProperty()));
		prop= factory.getOWLDataProperty(iri);
		if (prop!=null)
			set.addAll(dataProps.getChildren(prop.asOWLDataProperty()));
		prop= factory.getOWLAnnotationProperty(iri);
		if (prop!=null)
			set.addAll(annotationProps.getChildren(prop.asOWLAnnotationProperty()));

		return Collections.unmodifiableSet(set);

		//			return (ArrayList) Util.createEntityList(onto.getSubProperties(prop));
	}
	
	public Set<OWLClass> getSubclasses(OWLClass cls) {

		return hierarchy.getChildren(cls);
	}
	
	public Set<OWLClass> getParents(String className, boolean direct) {
		
		OWLClass cls= getOWLClass(className);
		
		return (direct?
			hierarchy.getParents(cls):
			hierarchy.getAncestors(cls));
	}
	
	public Set<List<OWLClass>> getPathToRoot(OWLClass cls) {

		return hierarchy.getPathsToRoot(cls);
	}

	
//	public OWLEntity getOWLEntity(String entityName) {
//		// TODO There should be a better way of doing this
		
//		IRI iri= IRI.create(entityName);
//		OWLClass cls= onto.getEntitiesWithIRI(iri, true);
//		.getOWLOntologyManager().getOWLDataFactory().get.getOWLClass(iri);
//		if (cls.isBuiltIn() ||
//		    cls.isDefined(onto.getImportsClosure())) return cls;;
//		return null;

	
	//		if (entityName.equals(thing.toStringID())) return thing;
//		
//		OWLEntity entity= null;
//		for (OWLEntity entity1 : onto.getReferencedEntities()){
//			if (entity1.getURI().toString().equals(entityName)) {
//				entity= entity1;
//				break;
//			}
//		}
//		return entity;
//	}
	
	public OWLClass getOWLClass(String className) {
		// TODO There should be a better way of doing this
		
		return onto.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IRI.create(className));
//		for (OWLEntity entity : onto.getEntitiesWithIRI(IRI.create(className), true)) {
//			if (entity.isOWLClass()) cls= entity.asOWLClass();
//		}
//		return cls;

//		OWLClass cls= onto.getOWLOntologyManager().getOWLDataFactory().getOWLClass(iri);
//		if (cls.isBuiltIn() ||
//		    onto.containsClassReference(iri, true)) return cls;;
//		return null;
	}	
		
//		OWLClass cls= null;
//		for (OWLClass cls1 : onto.getReferencedClasses()){
//			if (cls1.toStringID().equals(className)) {
//				cls= cls1;
//				break;
//			}
//		}
//		return cls;
//	}
	
//	public OWLProperty getOWLProperty(String className) {
//		// TODO There should be a better way of doing this
//				
//		OWLProperty prop= null;
//		for (OWLObjectProperty prop1 : onto.getReferencedObjectProperties()){
//			if (prop1.toStringID().equals(className)) {
//				prop= prop1;
//				break;
//			}
//		}
//
//		if (prop!=null)
//			return prop;
//		
//		for (OWLDataProperty prop1 : onto.getReferencedDataProperties()){
//			if (prop1.toStringID().equals(className)) {
//				prop= prop1;
//				break;
//			}
//		}
//		return prop;
//	}
	
//	public List<OWLClass> getPathToRoot(List<OWLClass> path) {
//		
//		OWLClass firstElement = path.get(0);
//		
//		//if(firstElement == null || firstElement.isOWLThing()) 
//		//	return path;
//		
//		OWLClass firstParent = getFirstParent(firstElement);
//		
//		if (firstParent==null) return path;
//
//		path.add(0, firstParent);
//		return getPathToRoot(path);
//	}
	
//	public OWLClass getFirstParent(OWLClass child) {
//		Extractor extractor= new Extractor();
//		
//		for(OWLClassExpression sup : child.getSuperClasses(onto)) {
//			sup.accept(extractor);
//			if (extractor.getClses().size()>0) break;
//		}
//		
//		if (extractor.getClses().size()==0)
//			for(OWLClassExpression equ : child.getEquivalentClasses(onto)) {
//				equ.accept(extractor);
//				if (extractor.getClses().size()>0) break;
//			}
//		
//		return (extractor.getClses().iterator().hasNext()?extractor.getClses().iterator().next():null);
//	}
	
//	public Set<OWLClass> getSubClasses(OWLClass cls) {
//		
//		
//		
//		//	TODO There should be a better way of doing this
//		//	for owl:Thing
////		if (cls.isOWLThing()) {
////			return hierarchy..getRoots();
////		}
//		return hierarchy.getChildren(cls);
///*
//			Set<OWLClass> subs= new HashSet<OWLClass>();
//			
//			for (OWLClass clsRef : onto.getReferencedClasses()) {
//				if (clsRef.getSuperClasses(onto).size() == 0)
//					subs.add(clsRef);
//			}
//			return subs;
//		}
//		ClassExtractor extractor= new ClassExtractor();
//		//	Look for direct subclasses
//		for(OWLClassExpression sub : cls.getSubClasses(onto)) { //onto.getSubClassAxiomsForSuperClass(cls)) {
//			sub.accept(extractor);
//		}
//		
//		for(OWLSubClassOfAxiom sub : onto.getSubClassAxiomsForSuperClass(cls)) {
//			sub.getSubClass().accept(extractor);
//		}
//		
//		
////		//	Look for equivalents as subclasses
////		for(OWLClassExpression equi : cls.getEquivalentClasses(onto)) { //onto.getEquivalentClassesAxioms(cls)) {
////			//	asSubClassAxioms return A equiTo B, as two axioms: A subClassOf B AND B subClassOf A 
////			for(OWLClassExpression sub : equi.asConjunctSet()) {
////				if (sub.equals(cls)) continue;
////				sub.accept(extractor);
////			}
////		}
//		
//		//	Cannot make owlapi read equivalent axiom as subclass
//		//	Look for equivalents as subclasses
//		for(OWLEquivalentClassesAxiom equi : onto.getEquivalentClassesAxioms(cls)) {
//			//	asSubClassAxioms return A equiTo B, as two axioms: A subClassOf B AND B subClassOf A 
//			for(OWLSubClassOfAxiom sub : equi.asSubClassAxioms()) {
//				if (sub.getSubClass().equals(cls)) continue;
//				sub.getSubClass().accept(extractor);
//			}
//		}
//		
//		return extractor.getClasses();
//		*/
//	}
	
//	/**
//	 * Return a list of subproperties
//	 * @param prop
//	 * @return
//	 */
//	public Set<OWLEntity> getRootProperties() {
//
//		//	TODO There should be a better way of doing this
//		//	for root properties (prop == null)
//		Set<OWLEntity> subs= new HashSet<OWLEntity>();
//
//		//	Add all AnnotationProperties, as they do not have sub or super properties
//		//	relationships
//		for (OWLAnnotationProperty refProp : onto.getReferencedAnnotationProperties()) {
//			//	TODO Hack to make it compatible with protege 3 behavior
//			if (refProp.toStringID().startsWith(Util.OWL) ||
//			    refProp.toStringID().startsWith(Util.RDFS)	) continue;
//			subs.add(refProp);
//		}
//		
//		//	Add object properties
//		for (OWLObjectProperty refProp : onto.getReferencedObjectProperties()) {
//			if (refProp.getSuperProperties(onto).size() == 0)
//				subs.add(refProp);
//		}
//		
//		for (OWLDataProperty refProp : onto.getReferencedDataProperties()) {
//			if (refProp.getSuperProperties(onto).size() == 0)
//				subs.add(refProp);
//		}
//		return subs;
//	}
//
//	/**
//	 * Return a list of subproperties
//	 * @param prop
//	 * @return
//	 */
//	public Set<OWLProperty> getSubProperties(OWLProperty prop) {
//		
//		PropertyExtractor extractor= new PropertyExtractor();
//		//	Look for direct subclasses
//		for(Object sup : prop.getSubProperties(onto)) {
//			((OWLPropertyExpression) sup).accept(extractor);
//		}
//			
//		return extractor.getProps();
//		
//	}

//	/**
//	 * TODO We should test more than just Named classes
//	 * @author dilvan
//	 *
//	 */
//	 private class ClassExtractor extends OWLObjectVisitorAdapter {
//
//	    private Set<OWLClass> classses= new HashSet<OWLClass>();
//	    
//	    public Set<OWLClass> getClasses() {
//	        return classses;
//	    }
//
//	    public void visit(OWLClass cls){
//	    	classses.add(cls);
//	    }
//	    
//	    //	Probaly wrong adapted from OWLAnd
//	    public void visit(OWLObjectUnionOf owlAnd) {
//	        for(Iterator it = owlAnd.getOperands().iterator(); it.hasNext(); ) {
//	            ((OWLClassExpression) it.next()).accept(this);
//	        }
//	    }
//	}

	public StringBuffer getEquivalentClassesHtml(OWLClass cls) {
		StringBuffer buffer = new StringBuffer();		
		buffer.append("<table width=\"100%\" border=\"0\" cellspacing=\"3\"  class=\"restriction_table\">");		

		List<OWLClassExpression> equivClasses = new ArrayList<OWLClassExpression>( cls.getEquivalentClasses(onto));
		if (equivClasses.size() > 0) {
			buffer.append("<div class=\"restiction_title\">Equivalent classes (Necessary and Sufficient conditions)</div>");
		}
		Collections.sort(equivClasses, new RestrictionComparator());

		for (OWLClassExpression equivClass : equivClasses) {

			if (equivClass instanceof OWLObjectIntersectionOf) {
				List<OWLClassExpression> operands = new ArrayList<OWLClassExpression>(((OWLObjectIntersectionOf) equivClass).getOperands());
				Collections.sort(operands, new RestrictionComparator());
				
				for (OWLClassExpression operand : operands) {
					buffer.append("<tr><td>");
					buffer.append(getConditionHtmlString(operand));
					buffer.append("</td></tr>");
				}
			} else {
				buffer.append("<tr><td>");
				buffer.append(getConditionHtmlString(equivClass));
				buffer.append("</td></tr>");				
			}			
		}

		buffer.append("</table>");
		return buffer;		
	}


	public StringBuffer getSuperClassesHtml(OWLClass cls) {
		StringBuffer buffer = new StringBuffer();		
		buffer.append("<table width=\"100%\" border=\"0\" cellspacing=\"3\"  class=\"restriction_table\">");		

		List<OWLClassExpression> superClasses= new ArrayList<OWLClassExpression>(cls.getSuperClasses(onto));
		//List<RDFSClass> superClasses = new ArrayList<RDFSClass>(cls.getPropertyValues(cls.getOWLModel().getRDFSSubClassOfProperty()));
		if (superClasses.size() > 0) {
			buffer.append("<hr>");
			buffer.append("<div class=\"restiction_title\">Superclasses (Necessary conditions)</div>");
		}
	
		Collections.sort(superClasses, new RestrictionComparator());

		for (OWLClassExpression superCls : superClasses) {			
			buffer.append("<tr><td>");
			buffer.append(getConditionHtmlString(superCls));
			buffer.append("</td></tr>");
		}

		buffer.append("</table>");
		return buffer;		
	}


	private static final String delimsStrs[] = {"and", "or", "not", "some", "only", "has", "min", "exactly", "max"};
	private static List<String> delims = Arrays.asList(delimsStrs);

	private String getConditionHtmlString(OWLClassExpression cls) {
		StringBuffer buffer = new StringBuffer();
		StringTokenizer st = new StringTokenizer(Util.getBrowserText(cls), " \t\n\r\f", true);
		while(st.hasMoreTokens()) {
			final String token = st.nextToken();
			if (delims.contains(token)) {
				buffer.append("<span class=\"restriction_delim\">");
				buffer.append(token);
				buffer.append("</span>");
			} else {
				buffer.append(token);
			}
		}

		return buffer.toString();
	}

	class RestrictionComparator implements Comparator<OWLClassExpression> {
		public int compare(OWLClassExpression cls1, OWLClassExpression cls2) {
			if (cls1 instanceof OWLClass && cls2 instanceof OWLClass) {
				return Util.getBrowserText(cls1).compareTo(Util.getBrowserText(cls2));
			}			
			if (cls1 instanceof OWLClass && !(cls2 instanceof OWLClass)) {
				return -1;
			}			
			if (!(cls1 instanceof OWLClass) && cls2 instanceof OWLClass) {
				return 1;
			}
			//for all other cases
			return Util.getBrowserText(cls1).compareTo(Util.getBrowserText(cls2));
		}		
	}

}

///**
// * TODO We should test more than sup properties, also axioms that define property
// * @author dilvan
// *
// */
//class PropertyExtractor extends OWLObjectVisitorAdapter {
//
//    private Set<OWLProperty> props= new HashSet<OWLProperty>();
//    
//    public Set<OWLProperty> getProps() {
//        return props;
//    }
//
//    public void visit(OWLDataProperty prop){
//        props.add(prop);
//    }
//
//    public void visit(OWLObjectProperty prop){
//        props.add(prop);
//    }
//}



class Extractor extends OWLClassExpressionVisitorAdapter {

    private HashSet<OWLClass> clses= new HashSet<OWLClass>();

    public Set<OWLClass> getClses() {
        return clses;
    }

    public void visit(OWLClass owlClass){
        clses.add(owlClass);
    }

    //	Probaly wrong adapted from OWLAnd
    public void visit(OWLObjectUnionOf owlAnd) {
        for(Iterator it = owlAnd.getOperands().iterator(); it.hasNext(); ) {
            ((OWLClassExpression) it.next()).accept(this);
        }
    }
}
