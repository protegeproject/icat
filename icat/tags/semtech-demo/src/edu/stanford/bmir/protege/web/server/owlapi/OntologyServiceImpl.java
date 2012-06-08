package edu.stanford.bmir.protege.web.server.owlapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.stanford.bmir.protege.web.server.OWLAPIProjectManager;
import edu.stanford.bmir.protege.web.server.Protege3ProjectManager;
import edu.stanford.bmir.protege.web.server.ServerProject;
import org.semanticweb.owlapi.metrics.AxiomCount;
import org.semanticweb.owlapi.metrics.DLExpressivity;
import org.semanticweb.owlapi.metrics.OWLMetric;
import org.semanticweb.owlapi.metrics.ReferencedClassCount;
import org.semanticweb.owlapi.metrics.ReferencedDataPropertyCount;
import org.semanticweb.owlapi.metrics.ReferencedIndividualCount;
import org.semanticweb.owlapi.metrics.ReferencedObjectPropertyCount;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.stanford.bmir.protege.web.client.model.event.OntologyEvent;
import edu.stanford.bmir.protege.web.client.rpc.OntologyService;
import edu.stanford.bmir.protege.web.client.rpc.data.AnnotationData;
import edu.stanford.bmir.protege.web.client.rpc.data.BioPortalReferenceData;
import edu.stanford.bmir.protege.web.client.rpc.data.BioPortalSearchData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValues;
import edu.stanford.bmir.protege.web.client.rpc.data.ImportsData;
import edu.stanford.bmir.protege.web.client.rpc.data.MetricData;
import edu.stanford.bmir.protege.web.client.rpc.data.PaginationData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyType;
import edu.stanford.bmir.protege.web.client.rpc.data.SubclassEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.Triple;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;
import edu.stanford.bmir.protege.web.server.ProjectManagerFactory;
import edu.stanford.smi.protege.model.Frame;

/**
 * Be careful here, servlets can be instantiated multiples times on the
 * same or different thread. Also the same instance can be reused in the same
 * or different thread.
 *
 * @author Dilvan Moreira <dilvan@gmail.com>
 */

public class OntologyServiceImpl extends RemoteServiceServlet implements OntologyService {

    private static final long serialVersionUID = -3459701556900785897L;

    static final boolean IMPLEM_TEST = true;

    static OWLOntologyManager owlOntologyManager;

    @Override
    public void init(javax.servlet.ServletConfig config) throws javax.servlet.ServletException {
        super.init(config);
        owlOntologyManager = ProjectManagerFactory.getOWLAPIProjectManager().getOwlOntologyManager();
    };


    /**
     * Handles the conversion between our (internal) ValueType objects and their corresponding OWLDatatypes.
     *
     * @param valueType
     * @return
     */
    OWLDatatype getDatatype(ValueType valueType) {
        if (valueType == ValueType.Integer) {
            return owlOntologyManager.getOWLDataFactory().getIntegerOWLDatatype();
        }
        if (valueType == ValueType.Boolean) {
            return owlOntologyManager.getOWLDataFactory().getBooleanOWLDatatype();
        }
        if (valueType == ValueType.Float) {
            return owlOntologyManager.getOWLDataFactory().getFloatOWLDatatype();
        }

        return owlOntologyManager.getOWLDataFactory().getOWLDatatype(OWL2Datatype.XSD_STRING.getIRI());
    }

    /**
     *
     */
    ValueType getValueType(OWLDatatype dataType) {
        if (dataType.isBoolean()) {
            return ValueType.Boolean;
        }
        if (dataType.isFloat()) {
            return ValueType.Float;
        }
        if (dataType.isInteger()) {
            return ValueType.Integer;
        }
        return ValueType.Any;
    }

    /**
     * Actually creates the property passed in.
     */
    public void addPropertyValue(String projectName, String entityName,
                                 PropertyEntityData propertyEntity, EntityData value, String user, String operationDescription) {
        final AuxOnto onto = getOnto(projectName);
        final OWLDataFactory dataFactory = owlOntologyManager.getOWLDataFactory();
        PropertyType propertyType = propertyEntity.getPropertyType();
        if (propertyType == PropertyType.DATATYPE) {
            OWLDataProperty dataProperty = dataFactory.getOWLDataProperty(IRI.create(propertyEntity.getName()));
            final OWLNamedIndividual owlIndividual = owlOntologyManager.getOWLDataFactory().getOWLNamedIndividual(IRI.create(entityName));
            OWLAxiom axiom = dataFactory.getOWLDataPropertyAssertionAxiom(dataProperty,
                    owlIndividual,
                    dataFactory.getOWLTypedLiteral(value.getName()));
            owlOntologyManager.applyChange(new AddAxiom(onto.getOntology(), axiom));

        }
        if (propertyType == PropertyType.ANNOTATION) {
            OWLAnnotation commentAnno = dataFactory.getOWLAnnotation(
                    dataFactory.getOWLAnnotationProperty(IRI.create(propertyEntity.getName())),
                    dataFactory.getOWLStringLiteral(value.getName(), "en"));

            OWLAxiom axiom = dataFactory.getOWLAnnotationAssertionAxiom(IRI.create(entityName), commentAnno);
            owlOntologyManager.applyChange(new AddAxiom(onto.getOntology(), axiom));
        }
        if (propertyType == PropertyType.OBJECT) {
            OWLObjectProperty property = dataFactory.getOWLObjectProperty(IRI.create(propertyEntity.getName()));
            OWLObjectPropertyAssertionAxiom assertion = dataFactory.getOWLObjectPropertyAssertionAxiom(
                    property,
                    dataFactory.getOWLNamedIndividual(IRI.create(entityName)),
                    dataFactory.getOWLNamedIndividual(IRI.create(value.getName())));
            owlOntologyManager.applyChange(new AddAxiom(onto.getOntology(), assertion));
        }

    }

    /* (non-Javadoc)
      * @see edu.stanford.bmir.protege.web.client.rpc.OntologyService#addSuperCls(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
      */

    public void addSuperCls(String projectName, String clsName, String superClsName, String user,
                            String operationDescription) {
        final OWLDataFactory dataFactory = owlOntologyManager.getOWLDataFactory();
        final OWLClass subClass = dataFactory.getOWLClass(IRI.create(clsName));
        final OWLClass superClass = dataFactory.getOWLClass(IRI.create(superClsName));
        OWLAxiom axiom = dataFactory.getOWLSubClassOfAxiom(subClass, superClass);
        final AuxOnto onto = getOnto(projectName);
        AddAxiom addAxiom = new AddAxiom(onto.getOntology(), axiom);
        owlOntologyManager.applyChange(addAxiom);
    }

    /**
     * Creates the data object for the annotation property - a placeholder for the actual data which we send back to the client.
     */
    public EntityData createAnnotationProperty(String projectName, String propertyName, String superPropName,
                                               String user, String operationDescription) {
        final OWLDataFactory dataFactory = owlOntologyManager.getOWLDataFactory();
        OWLAnnotationProperty dataProperty = dataFactory.getOWLAnnotationProperty(IRI.create(propertyName));
        return getPropertyEntityData(dataProperty, null, PropertyType.ANNOTATION);

    }

    /* (non-Javadoc)
      * @see edu.stanford.bmir.protege.web.client.rpc.OntologyService#createCls(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
      */

    public EntityData createCls(String projectName, String clsName, String superClsName, String user,
                                String operationDescription) {
        final OWLDataFactory dataFactory = owlOntologyManager.getOWLDataFactory();
        OWLClass cls = dataFactory.getOWLClass(IRI.create(clsName));
        OWLClass superCls = dataFactory.getOWLClass(IRI.create(superClsName));
        OWLAxiom axiom = dataFactory.getOWLSubClassOfAxiom(cls, superCls);
        final AuxOnto onto = getOnto(projectName);
        AddAxiom addAxiom = new AddAxiom(onto.getOntology(), axiom);
        owlOntologyManager.applyChange(addAxiom);

        //now get the entity out of the datastore, not the most efficient thing in the entire world
        return getEntity(projectName, clsName);
    }

    public EntityData createDatatypeProperty(String projectName, String propertyName, String superPropName,
                                             String user, String operationDescription) {
        final OWLDataFactory dataFactory = owlOntologyManager.getOWLDataFactory();
        OWLDataProperty dataProperty = dataFactory.getOWLDataProperty(IRI.create(propertyName));
        return getPropertyEntityData(dataProperty, null, PropertyType.DATATYPE);
    }

    PropertyEntityData getPropertyEntityData(OWLEntity dataProperty, final ValueType valueType, final PropertyType datatype) {
        final PropertyEntityData data = new PropertyEntityData(dataProperty.getIRI().toString());
        data.setBrowserText(dataProperty.getIRI().getFragment());
        data.setPropertyType(datatype);
        data.setValueType(valueType);
        return data;
    }

    public static String getBrowserText(Frame frame) {
        String bt = frame.getBrowserText();
        if (bt.contains("'")) {
            //delete any leading and trailing 's if present
            bt = bt.replaceAll("^'|'$", "");
            //delete all 's preceding or following any of these characters: [SPACE].-_
            bt = bt.replaceAll("'([ .-_])|([ .-_])'", "$1$2");
        }
        return bt;
    }



    public EntityData createExternalReference(String projectName, String entityName, BioPortalReferenceData bpRefData,
                                              String user, String operationDescription) {
        // TODO Auto-generated method stub

        if (IMPLEM_TEST) {
            throw new RuntimeException("Method not implemented");
        }

        return null;
    }


    public EntityData createInstance(String projectName, String instName, String typeName, String user,
                                     String operationDescription) {
        final OWLDataFactory dataFactory = owlOntologyManager.getOWLDataFactory();

        final OWLClass owlClass = dataFactory.getOWLClass(IRI.create(typeName));
        OWLIndividual individual ;
        if (instName == null){
            individual = dataFactory.getOWLAnonymousIndividual();
        } else {
            individual = dataFactory.getOWLNamedIndividual(IRI.create(instName));
        }
        OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom(owlClass, individual);
        final AuxOnto onto = getOnto(projectName);
        owlOntologyManager.applyChange(new AddAxiom(onto.getOntology(), classAssertion));
        return Util.createEntityData(individual);
    }

    /**
     * Creates a new object instance, then intializes the field of another object (the subject) to that value.
     *
     * @param projectName
     * @param instName
     * @param typeName
     * @param subjectEntity
     * @param propertyEntity
     * @param user
     * @param operationDescription
     * @return
     */
    public EntityData createInstanceValue(String projectName, String instName, String typeName, String subjectEntity,
                                          String propertyEntity, String user, String operationDescription) {
        PropertyEntityData propEntityData = (PropertyEntityData) createObjectProperty(projectName, propertyEntity, null, user, null);
        if (typeName == null) {
            final OWLDataFactory dataFactory = owlOntologyManager.getOWLDataFactory();
            final OWLObjectProperty dataProperty = dataFactory.getOWLObjectProperty(IRI.create(propertyEntity));
            final Set<OWLClassExpression> owlClassExpressions = dataProperty.getRanges(getOnto(projectName).getOntology());
            typeName = owlClassExpressions.iterator().next().toString();
        }
        EntityData valueData = createInstance(projectName, instName, typeName, user, null);

        addPropertyValue(projectName, subjectEntity, propEntityData, valueData, user, null);
        return valueData;
    }

    /* (non-Javadoc)
      * @see edu.stanford.bmir.protege.web.client.rpc.OntologyService#createInstanceValueWithPropertyValue(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData, edu.stanford.bmir.protege.web.client.rpc.data.EntityData, java.lang.String, java.lang.String)
      */

    public EntityData createInstanceValueWithPropertyValue(String projectName, String instName, String typeName, String subjectEntity,
                                                           String propertyEntity, PropertyEntityData instancePropertyEntity, EntityData valueEntityData, String user, String operationDescription) {
          PropertyEntityData propEntityData = (PropertyEntityData) createObjectProperty(projectName, propertyEntity, null, user, null);
        if (typeName == null) {
            final OWLDataFactory dataFactory = owlOntologyManager.getOWLDataFactory();
            final OWLObjectProperty dataProperty = dataFactory.getOWLObjectProperty(IRI.create(propertyEntity));
            final Set<OWLClassExpression> owlClassExpressions = dataProperty.getRanges(getOnto(projectName).getOntology());
            typeName = owlClassExpressions.iterator().next().toString();
        }
        createObjectProperty(projectName, propEntityData.getName(), null, user, operationDescription);
        EntityData valueData = createInstance(projectName, instName, typeName, user, null);

        addPropertyValue(projectName, subjectEntity, propEntityData, valueData, user, null);
        return valueData;
    }

    public EntityData createObjectProperty(String projectName, String propertyName, String superPropName, String user,
                                           String operationDescription) {
        final OWLDataFactory dataFactory = owlOntologyManager.getOWLDataFactory();
        final OWLObjectProperty dataProperty = dataFactory.getOWLObjectProperty(IRI.create(propertyName));
        return getPropertyEntityData(dataProperty, ValueType.Instance, PropertyType.OBJECT);
    }

    public void deleteEntity(String projectName, String entityName,
                             String user, String operationDescription) {
        final OWLOntology ontology = getOnto(projectName).getOntology();
        OWLEntityRemover remover = new OWLEntityRemover(owlOntologyManager, Collections.singleton(ontology));
        for (OWLEntity individual : ontology.getEntitiesInSignature(IRI.create(entityName))) {
            individual.accept(remover);
        }
        owlOntologyManager.applyChanges(remover.getChanges());
        // Not really sure if this is necessary, but it's in the docs!
        remover.reset();
    }

    private AnnotationData getAnnotationData(OWLAnnotation annotation) {
        AnnotationData annotationData = new AnnotationData();
        annotationData.setName(annotation.getProperty().getIRI().getFragment());
        annotationData.setValue(annotation.getValue().toString());
        return annotationData;
    }

    private PropertyData getPropertyData(OWLObjectPropertyAssertionAxiom assertionAxiom) {
        PropertyData annotationData = new PropertyData();
        annotationData.setName(assertionAxiom.getProperty().getNamedProperty().getIRI().getFragment());
        annotationData.setValue(assertionAxiom.getObject().toString());
        return annotationData;
    }

    private PropertyData getPropertyData(OWLDataPropertyAssertionAxiom assertionAxiom) {
        PropertyData annotationData = new PropertyData();
        annotationData.setName(assertionAxiom.getProperty().asOWLDataProperty().getIRI().getFragment());
        annotationData.setValue(assertionAxiom.getObject().getLiteral());
        return annotationData;
    }

    public ArrayList<AnnotationData> getAnnotationProperties(
            String projectName, String entityName) {
        final AuxOnto onto = getOnto(projectName);
        final OWLClass owlClass = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create(entityName));
        final Set<OWLAnnotation> annotations = owlClass.getAnnotations(onto.getOntology());
        ArrayList<AnnotationData> collector = new ArrayList<AnnotationData>();
        for (OWLAnnotation annotation : annotations) {
            collector.add(getAnnotationData(annotation));
        }
        return collector;
    }

    public ArrayList<PropertyData> getObjectProperties(
            String projectName, String entityName) {
        final AuxOnto onto = getOnto(projectName);
        final OWLNamedIndividual owlIndividual = owlOntologyManager.getOWLDataFactory().getOWLNamedIndividual(IRI.create(entityName));
        final Set<OWLObjectPropertyAssertionAxiom> objectProperties = onto.getOntology().getObjectPropertyAssertionAxioms(owlIndividual);
        ArrayList<PropertyData> collector = new ArrayList<PropertyData>();
        for (OWLObjectPropertyAssertionAxiom objectProperty : objectProperties) {
            collector.add(getPropertyData(objectProperty));
        }
        return collector;
    }

    public ArrayList<PropertyData> getDatatypeProperties(
            String projectName, String entityName) {
        final AuxOnto onto = getOnto(projectName);
        final OWLNamedIndividual owlIndividual = owlOntologyManager.getOWLDataFactory().getOWLNamedIndividual(IRI.create(entityName));
        final Set<OWLDataPropertyAssertionAxiom> objectProperties = onto.getOntology().getDataPropertyAssertionAxioms(owlIndividual);
        ArrayList<PropertyData> collector = new ArrayList<PropertyData>();
        for (OWLDataPropertyAssertionAxiom objectProperty : objectProperties) {
            collector.add(getPropertyData(objectProperty));
        }
        return collector;
    }

    /* (non-Javadoc)
      * @see edu.stanford.bmir.protege.web.client.rpc.OntologyService#getBioPortalSearchContent(java.lang.String, java.lang.String, edu.stanford.bmir.protege.web.client.rpc.data.BioPortalSearchData)
      */

    public String getBioPortalSearchContent(String projectName,
                                            String entityName, BioPortalSearchData bpSearchData) {
        // TODO Auto-generated method stub

        if (IMPLEM_TEST) {
            throw new RuntimeException("Method not implemented");
        }

        return "";
    }

    /* (non-Javadoc)
      * @see edu.stanford.bmir.protege.web.client.rpc.OntologyService#getBioPortalSearchContentDetails(java.lang.String, edu.stanford.bmir.protege.web.client.rpc.data.BioPortalSearchData, edu.stanford.bmir.protege.web.client.rpc.data.BioPortalReferenceData)
      */

    public String getBioPortalSearchContentDetails(String projectName, BioPortalSearchData bpSearchData,
                                                   BioPortalReferenceData bpRefData) {
        // TODO Auto-generated method stub

        if (IMPLEM_TEST) {
            throw new RuntimeException("Method not implemented");
        }

        return "";
    }


//	//VERY bad implementation
//	static OWLEntity findEntity(OWLOntology onto, IRI iri) {
//		FindEntity fe= new FindEntity(iri);
//
//		onto.accept(fe);
//		return fe.getEntity();
//	}

    /* (non-Javadoc)
      * @see edu.stanford.bmir.protege.web.client.rpc.OntologyService#getEntity(java.lang.String, java.lang.String)
      */

    public EntityData getEntity(String projectName, String entityName) {

        AuxOnto o1 = getOnto(projectName);
        OWLOntology o2 = o1.getOntology();

        Set<OWLEntity> entities =
                o2.getEntitiesInSignature(IRI.create(entityName), true);

        if (entities.isEmpty()) {
            return null;        // Is that correct?
        }
        //	Gets only the first
        OWLEntity entity = entities.iterator().next();

        return Util.createEntityData(entity);
    }

    public List<Triple> getEntityTriples(String projectName,
                                         List<String> entities, List<String> properties) {

        ArrayList<Triple> triples = new ArrayList<Triple>();

        for (String entityName : entities) {
            triples.addAll(packEntityTriples(projectName, properties, entityName));
        }
        return triples;
    }

    public List<Triple> getEntityTriples(String projectName,
                                         List<String> entities, List<String> properties,
                                         List<String> reifiedProps) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
      * @see edu.stanford.bmir.protege.web.client.rpc.OntologyService#getEntityTriples(java.lang.String, java.lang.String)
      */

    public List<Triple> getEntityTriples(String projectName,
                                         String entityName) {

        return packEntityTriples(projectName, null, entityName);
    }

    /* (non-Javadoc)
      * @see edu.stanford.bmir.protege.web.client.rpc.OntologyService#getEvents(java.lang.String, long)
      */

    public List<OntologyEvent> getEvents(String projectName,
                                         long fromVersion) {
        ServerProject serverProject = OWLAPIProjectManager.getProjectManager().getServerProject(projectName, false);
        if (serverProject == null) {
            throw new RuntimeException("Could not get ontology: " + projectName + " from server.");
        }
        return serverProject.isLoaded() ? serverProject.getEvents(fromVersion) : null;
    }

    /* (non-Javadoc)
      * @see edu.stanford.bmir.protege.web.client.rpc.OntologyService#getImportedOntologies(java.lang.String)
      */

    public ImportsData getImportedOntologies(String projectName) {
        AuxOnto onto = getOnto(projectName);
        if (onto == null) {
            return null;
        }

        ImportsData id = new ImportsData(onto.getOntology().getOntologyID().getOntologyIRI().toURI().toString());

        //				for (OWLImportsDeclaration childOnt : onto.getImportsDeclarations()) {
        //					ImportsData childID = new ImportsData(childOnt.getURI().toString());
        //					//	OWL 2 getImports gets all imported ontologies, if A imports B that imports C, it will return B and C
        //					//childID = copyOWLTree(childID, childOnt);
        //					id.addImport(childID);
        //				}

        Util.copyOWLTree(id, onto.getOntology());

        //				for (OWLOntology childOnt : onto.getImports()) {
        //					ImportsData childID = new ImportsData(childOnt.getOntologyID().getOntologyIRI().toURI().toString());
        //					//	OWL 2 getImports gets all imported ontologies, if A imports B that imports C, it will return B and C
        //					childID = copyOWLTree(childID, childOnt);
        //					id.addImport(childID);
        //				}
        return id;

    }

    public List<EntityData> getAllImplementedClasses(String projectName, EntityData data) {

        AuxOnto onto = getOnto(projectName);
        final OWLNamedIndividual individual = owlOntologyManager.getOWLDataFactory().getOWLNamedIndividual(IRI.create(data.getName()));
        final Set<OWLClassAssertionAxiom> owlClassAssertionAxioms = onto.getOntology().getClassAssertionAxioms(individual);
        List<EntityData> collector = new ArrayList<EntityData>();
        for (OWLClassAssertionAxiom owlClassAssertionAxiom : owlClassAssertionAxioms) {
            final EntityData entityData = new EntityData(owlClassAssertionAxiom.getClassExpression().getClassExpressionType().getName());
            entityData.setValueType(ValueType.Cls);
            collector.add(entityData);
        }
        return collector;
    }

    public List<EntityData> getIndividuals(String projectName, String className) {

        List<EntityData> instancesData = new ArrayList<EntityData>();
        AuxOnto onto = getOnto(projectName);

        if (onto.getOntology() == null) {
            return instancesData;
        }

        //	Get class
        OWLClass cls = onto.getOWLClass(className);

        if (cls == null) {
            return instancesData;
        }

        Set<OWLIndividual> inds = cls.getIndividuals(onto.getOntology());

        ArrayList<OWLIndividual> instances = new ArrayList<OWLIndividual>(inds);

        Collections.sort(instances, Util.getOWLObjectComparator());

        for (OWLIndividual ind : instances) {
            instancesData.add(Util.createEntityData(ind));
        }

        return instancesData;
    }


    /* (non-Javadoc)
      * @see edu.stanford.bmir.protege.web.client.rpc.OntologyService#getMetrics(java.lang.String)
      */

    public List<MetricData> getMetrics(String projectName) {

        List<MetricData> metrics = new ArrayList<MetricData>();
        OWLOntology onto = getOnto(projectName).getOntology();

        OWLMetric metric = new ReferencedClassCount(onto.getOWLOntologyManager());
        metric.setOntology(onto);
        metrics.add(new MetricData(metric.getName(), metric.getValue().toString()));

        metric = new ReferencedObjectPropertyCount(onto.getOWLOntologyManager());
        metric.setOntology(onto);
        metrics.add(new MetricData(metric.getName(), metric.getValue().toString()));

        metric = new ReferencedDataPropertyCount(onto.getOWLOntologyManager());
        metric.setOntology(onto);
        metrics.add(new MetricData(metric.getName(), metric.getValue().toString()));

        metric = new ReferencedIndividualCount(onto.getOWLOntologyManager());
        metric.setOntology(onto);
        metrics.add(new MetricData(metric.getName(), metric.getValue().toString()));

        metric = new DLExpressivity(onto.getOWLOntologyManager());
        metric.setOntology(onto);
        metrics.add(new MetricData(metric.getName(), metric.getValue().toString()));

        metric = new AxiomCount(onto.getOWLOntologyManager());
        metric.setOntology(onto);
        metrics.add(new MetricData(metric.getName(), metric.getValue().toString()));

        return metrics;
    }

    AuxOnto getOnto(String project) {
        OWLOntology ont = ProjectManagerFactory.getOWLAPIProjectManager().getProject(project);
        if (ont == null) {
            throw new RuntimeException("Project " + project + " does not exist. (maybe an error occured while loading it)");
        }
        return new AuxOnto(ont);
    }

    /* (non-Javadoc)
      * @see edu.stanford.bmir.protege.web.client.rpc.OntologyService#getOntologyURI(java.lang.String)
      */

    public String getOntologyURI(String projectName) {
        return getOnto(projectName).getOntology().getOntologyID().getOntologyIRI().toURI().toString();
    }

    /* (non-Javadoc)
      * @see edu.stanford.bmir.protege.web.client.rpc.OntologyService#getParents(java.lang.String, java.lang.String, boolean)
      */

    public List<EntityData> getParents(String projectName, String className, boolean direct) {

        AuxOnto onto = getOnto(projectName);

        return Util.createEntityList(onto.getParents(className, direct));
    }

    /* (non-Javadoc)
      * @see edu.stanford.bmir.protege.web.client.rpc.OntologyService#getParentsHtml(java.lang.String, java.lang.String, boolean)
      */

    public String getParentsHtml(String projectName, String className, boolean direct) {
        StringBuffer buffer = new StringBuffer();
        List<EntityData> parents = getParents(projectName, className, direct);

        buffer.append("<table width=\"100%\" border=\"0\" cellspacing=\"3\"  class=\"restriction_table\">");
        for (EntityData parent : parents) {
            buffer.append("<tr><td>");
            buffer.append(UIUtil.getDisplayText(parent));
            buffer.append("</td>");
            buffer.append("<td class=\"parent-column-right\"><a href=\"");
            buffer.append(UIUtil.LOCAL);
            buffer.append(UIUtil.REMOVE_PREFIX);
            buffer.append(parent.getName());
            buffer.append("\">remove</a></td></tr>");
        }

        buffer.append("</table>");
        return buffer.toString();
    }


    /* (non-Javadoc)
      * @see edu.stanford.bmir.protege.web.client.rpc.OntologyService#getPathToRoot(java.lang.String, java.lang.String)
      */

    public List<EntityData> getPathToRoot(String projectName,
                                          String entityName) {

        AuxOnto onto = getOnto(projectName);
        //		TODO Correct?
        //			if (onto.getOntology()==null) return path;


        List<EntityData> path = new ArrayList<EntityData>();

        OWLClass cls = onto.getOWLClass(entityName);

        //for now it works only with classes
        if (cls == null) {
            return path;
        }

        for (List<OWLClass> lst : onto.getPathToRoot(cls)) {
            if (path.size() == 0 ||
                    path.size() > lst.size()) {
                path = Util.createEntityList(lst);
            }
        }

        return path;

        //		    List<OWLClass> path1 = new ArrayList<OWLClass>();
        //		    path1.add(0, entity.asOWLClass());
        //		    path1= onto.getPathToRoot(path1);
        //		    //	owl:Thing added to be compatible with protege 3
        //		    path1.add(0, onto.getOWLThing());
        //
        //		    return (ArrayList<EntityData>) Util.createEntityList( path1);
    }

    public List<Triple> getRelatedProperties(String projectName,
                                             String className) {
//        final OWLNamedIndividual individual = iManager.getOWLDataFactory().getOWLNamedIndividual(IRI.create(className));


        final OWLOntology ontology = getOnto(projectName).getOntology();
        final Set<OWLObjectProperty> objectPropertySet = ontology.getObjectPropertiesInSignature();
        final Set<OWLObjectPropertyDomainAxiom>  domainAssertions = new HashSet<OWLObjectPropertyDomainAxiom>();
        for (OWLObjectProperty owlObjectProperty : objectPropertySet) {
            domainAssertions.addAll(ontology.getObjectPropertyDomainAxioms(owlObjectProperty));
        }

        final List<Triple> results = new ArrayList<Triple>();
        walk(ontology, owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create(className)), new HashSet<String>(), domainAssertions, results);


//        final Set<OWLDataPropertyAssertionAxiom> set1 = ontology.getDataPropertyAssertionAxioms(individual);
//        final Set set2 = new HashSet();
//        for (OWLDataPropertyAssertionAxiom owlObjectPropertyAssertionAxiom : set1) {
//            set2.add(owlObjectPropertyAssertionAxiom.getProperty().getDomains(ontology));
//        }
//
//        final Set<OWLAnnotationAssertionAxiom> set = ontology.getAnnotationAssertionAxioms(individual.getIRI());
//        List<Triple> properties = new ArrayList<Triple>();
//        for (OWLAnnotationAssertionAxiom owlAnnotationAssertionAxiom : set) {
//            properties.add(Util.createTriple(ontology, individual, owlAnnotationAssertionAxiom.getProperty(), owlAnnotationAssertionAxiom.getValue()));
//        }
//
//        final Set<OWLObjectPropertyAssertionAxiom> owlObjectPropertyAssertionAxiomSet = ontology.getObjectPropertyAssertionAxioms(individual);
//        for (OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom : owlObjectPropertyAssertionAxiomSet ) {
//            properties.add(Util.createTriple(ontology, individual, objectPropertyAssertionAxiom.getProperty().getNamedProperty(), objectPropertyAssertionAxiom.getObject()));
//        }
//
//        final Set<OWLDataPropertyAssertionAxiom> owlDataPropertyAssertionAxiomSet = ontology.getDataPropertyAssertionAxioms(individual);
//        for (OWLDataPropertyAssertionAxiom dataPropertyAssertionAxiom : owlDataPropertyAssertionAxiomSet) {
//            properties.add(Util.createTriple(ontology, individual, dataPropertyAssertionAxiom.getProperty().asOWLDataProperty(), dataPropertyAssertionAxiom.getObject()));
//        }

        return results;
    }

    private void walk(OWLOntology ontology, final OWLClass owlClass, final Set<String> visited, Set<OWLObjectPropertyDomainAxiom> domainAxioms, final List<Triple> results) {
        // first drop out if we've visited this node before
        if (!visited.add(owlClass.getIRI().toString()) ) {
            return ;
        }


        for (OWLObjectPropertyDomainAxiom domainAxiom : domainAxioms) {
            if (domainAxiom.getDomain().getClassExpressionType().equals(ClassExpressionType.OWL_CLASS)){
                final OWLClass domain = (OWLClass) domainAxiom.getDomain();
                results.add(Util.createTriple(ontology,
                    domain,
                    domainAxiom.getProperty().getNamedProperty(),
                    domain));
            }
        }

        //TODO: replace this with Tim's superclass reasoner:
        final Set<OWLClassExpression> classes = owlClass.getSuperClasses(ontology);
        for (OWLClassExpression aClass : classes) {
            aClass.getClassesInSignature();
            // don't climb up anonymous classes
            if (!aClass.isAnonymous()) {
                walk(ontology, aClass.asOWLClass(), visited, domainAxioms, results);
            }
        }
    }

    /* (non-Javadoc)
      * @see edu.stanford.bmir.protege.web.client.rpc.OntologyService#getRestrictionHtml(java.lang.String, java.lang.String)
      */

    public String getRestrictionHtml(String projectName, String className) {
        AuxOnto onto = getOnto(projectName);

        OWLClass cls;
        try {
            cls = onto.getOWLClass(className);
        }
        catch (Exception e) {
            return "";
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append(onto.getEquivalentClassesHtml(cls));
        buffer.append(onto.getSuperClassesHtml(cls));
        return buffer.toString();
    }


    /* (non-Javadoc)
      * @see edu.stanford.bmir.protege.web.client.rpc.OntologyService#getRootEntity(java.lang.String)
      */

    public EntityData getRootEntity(String projectName) {
        AuxOnto onto = getOnto(projectName);

        if (onto.getOntology() == null) {
            return new EntityData("Root", "Root");
        }

        OWLClass cls = onto.getOWLThing();
        return new EntityData(cls.getIRI().toURI().toString(), Util.getBrowserText(cls));
    }

    public List<SubclassEntityData> getSubclasses(String projectName,
                                                  String className) {
        List<SubclassEntityData> subclassesData = new ArrayList<SubclassEntityData>();

        AuxOnto onto = getOnto(projectName);
        OWLClass cls = onto.getOWLClass(className);
        if (cls == null) {
            return subclassesData;
        }

        List<OWLClass> classes = new ArrayList<OWLClass>(onto.getSubclasses(cls));

        Collections.sort(classes, Util.getOWLObjectComparator());

        for (OWLClass subcls : classes) {
            subclassesData.add(
                    new SubclassEntityData(subcls.toStringID(),
                            Util.getBrowserText(subcls), null,
                            //	ChAOUtil.hasAnnotations(subcls),
                            onto.getSubclasses(subcls).size()));
        }

        return subclassesData;
    }

    /* (non-Javadoc)
      * @see edu.stanford.bmir.protege.web.client.rpc.OntologyService#getSubproperties(java.lang.String, java.lang.String)
      */

    public List<EntityData> getSubproperties(String projectName,
                                             String propertyName) {

        return Util.createEntityList(getOnto(projectName).getSubproperties(propertyName));
    }

    /* (non-Javadoc)
      * @see edu.stanford.bmir.protege.web.client.rpc.OntologyService#hasWritePermission(java.lang.String, java.lang.String)
      */

    public Boolean hasWritePermission(String projectName, String userName) {
        // TODO Auto-generated method stub

        if (IMPLEM_TEST) {
            throw new RuntimeException("Method not implemented");
        }

        return new Boolean(true);
    }


    public Integer loadProject(String projectName) {
         return 0;
    }


    public void moveCls(String projectName, String clsName,
                        String oldParentName, String newParentName,
                        String user, String operationDescription) {
        removeSuperCls(projectName, clsName, oldParentName, user, operationDescription);
        addSuperCls(projectName, clsName, newParentName, user, operationDescription);
    }

    private List<Triple> packEntityTriples(String projectName, List<String> properties, String entityName) {

        ArrayList<Triple> triples = new ArrayList<Triple>();

        OWLOntology ontology = getOnto(projectName).getOntology();
        if (ontology == null) {
            return triples;
        }

        //		OWLEntity entity= findEntity(getOnto(projectName).getOntology(), IRI.create(entityName));
        //
        //		if (entity==null) return triples;

        Set<OWLEntity> sameEntities = ontology.getEntitiesInSignature(IRI.create(entityName), true);

        //	In OWL 2 two entities can have the same IRI, so we do it for both
        for (OWLEntity entity : sameEntities) {

            //	Try to find annotations
            //
            for (OWLAnnotation annotation : entity.getAnnotations(ontology)) {
                OWLAnnotationProperty property = annotation.getProperty();
                OWLAnnotationValue value = annotation.getValue();

                if (properties != null && !properties.contains(property.getIRI().toString())) {
                    continue;
                }

                //if (!slot.isSystem()) {
                triples.add(Util.createTriple(ontology, entity, property, value));
                //triples.addAll(getTriples(entity, annotation.getAnnotation()));
            }

            //	Try to find properties from individuals
            //
            if (entity.isOWLNamedIndividual()) {
                for (OWLIndividualAxiom axiom : ontology.getAxioms(entity.asOWLNamedIndividual())) {
                    if (OWLPropertyAssertionAxiom.class.isAssignableFrom(axiom.getClass())) {
                        OWLPropertyAssertionAxiom pAxiom = (OWLPropertyAssertionAxiom) axiom;

                        if (pAxiom.getProperty().isAnonymous()) {
                            break;
                        }

                        OWLProperty property = (OWLProperty) pAxiom.getProperty();
                        OWLObject object = pAxiom.getObject();

                        if (properties == null || !properties.contains(property.getIRI().toString())) {
                            continue;
                        }

                        //if (!slot.isSystem()) {
                        triples.add(Util.createTriple(ontology, entity, property, object));
                        //triples.addAll(getTriples(entity, annotation.getAnnotation()));
                    }
                }
            }

            //	Try to find SomeValuesFrom class
            //   subclassOf (restriction (someValuesFrom objectProperty class))
            //
            if (entity.isOWLClass()) {
                for (OWLClassAxiom axiom : ontology.getAxioms(entity.asOWLClass())) {
                    if (axiom instanceof OWLSubClassOfAxiom &&
                            ((OWLSubClassOfAxiom) axiom).getSuperClass() instanceof OWLObjectSomeValuesFrom) {
                        OWLObjectSomeValuesFrom some = (OWLObjectSomeValuesFrom) ((OWLSubClassOfAxiom) axiom).getSuperClass();

                        if (some.getProperty().isAnonymous()) {
                            break;
                        }
                        OWLObjectProperty property = some.getProperty().asOWLObjectProperty();

                        if (some.getFiller().isAnonymous()) {
                            break;
                        }
                        OWLClass cls = some.getFiller().asOWLClass();

                        if (properties != null && !properties.contains(property.getIRI().toString())) {
                            continue;
                        }

                        //if (!slot.isSystem()) {
                        triples.add(Util.createTriple(ontology, entity, property, cls));
                        //triples.addAll(getTriples(entity, annotation.getAnnotation()));
                    }
                }
            }
        }
        //	No DataProperties on classes allowed. How should they be detected?
        //		}
        return triples;
    }

    public void removePropertyValue(String projectName, String entityName, PropertyEntityData propertyEntity,
                                    EntityData value, String user, String operationDescription) {
        final AuxOnto onto = getOnto(projectName);
        final OWLDataFactory dataFactory = owlOntologyManager.getOWLDataFactory();
        final PropertyType propertyType = propertyEntity.getPropertyType();
        if (propertyType == PropertyType.DATATYPE) {
            dataFactory.getOWLStringLiteral("");
            OWLDataProperty dataProperty = dataFactory.getOWLDataProperty(IRI.create(propertyEntity.getName()));
            final Set<OWLDataPropertyAssertionAxiom> owlDataPropertyAssertionAxiomSet = onto.getOntology().getDataPropertyAssertionAxioms(dataFactory.getOWLNamedIndividual(IRI.create(entityName)));
            for (OWLDataPropertyAssertionAxiom owlDataPropertyAssertionAxiom : owlDataPropertyAssertionAxiomSet) {
                if (owlDataPropertyAssertionAxiom.getProperty().asOWLDataProperty().getIRI().toString().equals(propertyEntity.getName())) {
                    owlOntologyManager.applyChange(new RemoveAxiom(onto.getOntology(), owlDataPropertyAssertionAxiom));
                    break;
                }
            }
        }

        if (propertyType == PropertyType.ANNOTATION) {
            String oldPropertyValue = value.getName();
            if (value.getName().startsWith("~#")){
                oldPropertyValue = value.getName().substring(5);
            }
            final OWLNamedIndividual individual = owlOntologyManager.getOWLDataFactory().getOWLNamedIndividual(IRI.create(entityName));
            final Set<OWLAnnotationAssertionAxiom> assertionAxioms = individual.getAnnotationAssertionAxioms(getOnto(projectName).getOntology());
            for (OWLAnnotationAssertionAxiom assertionAxiom : assertionAxioms) {
                if((OWLStringLiteral.class.isAssignableFrom(assertionAxiom.getAnnotation().getValue().getClass()))){
                    final OWLStringLiteral stringLiteral = (OWLStringLiteral) assertionAxiom.getAnnotation().getValue();
                    String literalValueWithoutLanguage = stringLiteral.getLiteral();
                    if(literalValueWithoutLanguage.startsWith("~#")){
                        literalValueWithoutLanguage = stringLiteral.getLiteral().substring(5);
                    }
                    if (literalValueWithoutLanguage.equals(oldPropertyValue)){
                        owlOntologyManager.applyChange(new RemoveAxiom(onto.getOntology(), assertionAxiom));
                    }
                }
                
            }
        }
        if (propertyType == PropertyType.OBJECT) {
            OWLObjectProperty property = dataFactory.getOWLObjectProperty(IRI.create(propertyEntity.getName()));
            OWLObjectPropertyAssertionAxiom assertion = dataFactory.getOWLObjectPropertyAssertionAxiom(
                    property,
                    dataFactory.getOWLNamedIndividual(IRI.create(entityName)),
                    dataFactory.getOWLNamedIndividual(IRI.create(value.getName())));
            RemoveAxiom addAxiomChange = new RemoveAxiom(onto.getOntology(), assertion);
            owlOntologyManager.applyChange(addAxiomChange);

        }
    }

    public void removeSuperCls(String projectName, String clsName, String superClsName, String user,
                               String operationDescription) {
        OWLDataFactory factory = owlOntologyManager.getOWLDataFactory();
        final OWLOntology ontology = getOnto(projectName).getOntology();
        OWLClass subClass = factory.getOWLClass(IRI.create(clsName));
        OWLClass superClass = factory.getOWLClass(IRI.create(superClsName));

        OWLAxiom axiom = factory.getOWLSubClassOfAxiom(subClass, superClass);
        owlOntologyManager.removeAxiom(ontology, axiom);
    }

    public EntityData renameEntity(String projectName, String oldName, final String newName, String user,
                                   String operationDescription) {
        final EntityData data = getEntity(projectName, oldName);
        if (data == null){
            throw new IllegalArgumentException("Could not find entity " +oldName + " to rename");
        }
        final OWLOntology o2 =getOnto(projectName).getOntology();

        Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
        ontologies.add(o2);
        OWLEntityRenamer renamer = new OWLEntityRenamer(owlOntologyManager, ontologies);
        final List<OWLOntologyChange> changes = renamer.changeIRI(IRI.create(oldName), IRI.create(newName));
        owlOntologyManager.applyChanges(changes);

        return getEntity(projectName, newName);

    }

    public void replacePropertyValue(String projectName, String entityName, PropertyEntityData propertyEntity,
                                     EntityData oldValue, EntityData newValue, String user, String operationDescription) {
        removePropertyValue(projectName, entityName, propertyEntity, oldValue, user, operationDescription);
        addPropertyValue(projectName, entityName, propertyEntity, newValue, user, operationDescription);
    }

    /* (non-Javadoc)
      * @see edu.stanford.bmir.protege.web.client.rpc.OntologyService#search(java.lang.String, java.lang.String)
      */

    public List<EntityData> search(String projectName, String searchString) {
        // TODO Auto-generated method stub

        if (IMPLEM_TEST) {
            throw new RuntimeException("Method not implemented");
        }

        return new ArrayList<EntityData>();
    }

    public List<EntityData> search(String projectName, String searchString,
                                   ValueType valueType) {
        // TODO Auto-generated method stub

        if (IMPLEM_TEST) {
            throw new RuntimeException("Method not implemented");
        }

        return null;
    }

    public PaginationData<EntityData> search(String projectName,
                                             String searchString, ValueType valueType, int start, int limit,
                                             String sort, String dir) {
        // TODO Auto-generated method stub

        if (IMPLEM_TEST) {
            throw new RuntimeException("Method not implemented");
        }

        return null;
    }

    public List<EntityPropertyValues> getEntityPropertyValues(String projectName, List<String> entities, List<String> properties, List<String> reifiedProps) {
        AuxOnto onto = getOnto(projectName);

        final OWLDataFactory dataFactory = owlOntologyManager.getOWLDataFactory();

        List<EntityPropertyValues> collector = new ArrayList<EntityPropertyValues>(entities.size());
        for (String entity : entities) {
            EntityPropertyValues values = new EntityPropertyValues();
            final OWLIndividual root = dataFactory.getOWLNamedIndividual(IRI.create(entity));
            final EntityData data = getEntity(projectName, entity);
            if (data == null) {
                continue;
            }
            for (String outerPropertyName : properties) {
                final OWLIndividual secondLevelIndividual = findIndividual(onto, root, outerPropertyName);
                if (secondLevelIndividual == null) {
                    continue;
                }

                for (String innerPropertyName : reifiedProps) {
                    List<EntityData> innerCollector = new ArrayList<EntityData>();
                    final OWLNamedIndividual thirdLevelIndividual = findIndividual(onto, secondLevelIndividual, innerPropertyName);
                    if (thirdLevelIndividual != null){
                        values.setSubject(new EntityData(entity));
                        innerCollector.add(new EntityData(thirdLevelIndividual.getIRI().toString(), thirdLevelIndividual.getIRI().getFragment()));
                    }
                    values.setPropertyValues(new PropertyEntityData(innerPropertyName), innerCollector);
                }

            }
            if (values.getSubject() != null){
                collector.add(values);
            }
        }

        return collector;
    }

    private OWLNamedIndividual findIndividual(AuxOnto onto, OWLIndividual root, String propertyName) {
        final Set<OWLObjectPropertyAssertionAxiom> owlObjectPropertyAssertionAxiomSet = onto.getOntology().getObjectPropertyAssertionAxioms(root);
        for (OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom : owlObjectPropertyAssertionAxiomSet) {
            if (propertyName.equals(objectPropertyAssertionAxiom.getProperty().getNamedProperty().getIRI().toString())){
                final OWLNamedIndividual secondLevelIndividual = (OWLNamedIndividual) objectPropertyAssertionAxiom.getObject();
                return secondLevelIndividual;
            }
        }
        return null;
    }

    public void setPropertyValues(String projectName, String entityName, PropertyEntityData propertyEntity, List<EntityData> values, String user, String operationDescription) {
        for (EntityData data : values) {
            addPropertyValue(projectName, entityName, propertyEntity, data, user, operationDescription);
        }
    }
}
