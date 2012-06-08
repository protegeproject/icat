/**
 *
 */
package edu.stanford.bmir.protege.web.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;
import edu.stanford.bmir.protege.web.client.rpc.OntologyService;
import edu.stanford.bmir.protege.web.client.rpc.data.AnnotationData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValues;
import edu.stanford.bmir.protege.web.client.rpc.data.ImportsData;
import edu.stanford.bmir.protege.web.client.rpc.data.MetricData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyType;
import edu.stanford.bmir.protege.web.client.rpc.data.SubclassEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.Triple;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;
import edu.stanford.bmir.protege.web.server.owlapi.OntologyServiceImpl;

/**
 * @author dilvan
 */
public class OntologyServiceTest extends TestCase {

    enum Api {
        PROTEGE, OWLAPI
    }

    final public static Api BACKEND = Api.OWLAPI;


    final static public String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
    final static public String OWL = "http://www.w3.org/2002/07/owl#";
    final static public String PROTEGE = "http://protege.stanford.edu/plugins/owl/protege#";

    final static String PIZZA = "http://www.co-ode.org/ontologies/pizza/2005/10/18/pizza.owl#";
    final static String CELL = "http://purl.org/obo/owl/CL#";
    final static String OBOINOWL = "http://www.geneontology.org/formats/oboInOwl#";

    final static EntityData THING_ENTITY = new EntityData(OWL + "Thing", "owl:Thing");

    OntologyService projects = null;

    /**
     * @param name
     */
    public OntologyServiceTest(String name) {
        super(name);
    }

    /* (non-Javadoc)
      * @see junit.framework.TestCase#setUp()
      */

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        if (projects != null) {
            return;
        }

        FileUtil.init("testOntologies/");

        if (BACKEND == Api.OWLAPI) {
            //	Teste owlapi ontologies
/*
            OntologyServiceImpl.initOnto(ImmutableList.of(
                    new ProjectData("Pizza Project", "projects/pizza/pizza.owl", "Pizza", "dilvan"),
                    new ProjectData("Cell Project", "projects/cell/cell.owl", "Cell", "dilvan")
            ));
*/
            projects = new OntologyServiceImpl();
            ((OntologyServiceImpl)projects).init(null);
            return;
        }
        //	Test protege 3 ontologies

        projects = new edu.stanford.bmir.protege.web.server.OntologyServiceImpl();
    }

    /* (non-Javadoc)
      * @see junit.framework.TestCase#tearDown()
      */

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Tests basic equality for classes that don't have the equals method defined.
     *
     * @param obj1
     * @param obj2
     * @return True if the objects are equal false otherwise
     */
    boolean isEqual(Object obj1, Object obj2) {
        // test null conditions
        if (obj1 == null && obj2 == null) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
            return false;
        }
        if (!obj1.getClass().equals(obj2.getClass())) {
            return false;
        }

        //	MetricData
        if (obj1 instanceof MetricData && obj2 instanceof MetricData) {
            MetricData data1 = (MetricData) obj1;
            MetricData data2 = (MetricData) obj2;
            if (!data1.getMetricName().equals(data2.getMetricName())) {
                return false;
            }
            if (!data1.getMetricValue().equals(data2.getMetricValue())) {
                return false;
            }
            return true;
        }

        if (obj1 instanceof ValueType && obj2 instanceof ValueType) {
            return obj1.equals(obj2);
        }

        //	EntityData
        if (obj1 instanceof EntityData && obj2 instanceof EntityData) {
            EntityData ent1 = (EntityData) obj1;
            EntityData ent2 = (EntityData) obj2;

            if (!ent1.getName().equals(ent2.getName())) {
                return false;
            }
            if (!ent1.getBrowserText().equals(ent2.getBrowserText())) {
                return false;
            }

//			if (ent1.hasAnnotation()!=ent2.hasAnnotation()) return false;
            if (!isEqual(ent1.getValueType(), ent2.getValueType())) {
                return false;
            }
            if (ent1.getValueType() != ent2.getValueType()) {
                return false;
            }

            //	SubclassEntityData
            if (obj1 instanceof SubclassEntityData && obj2 instanceof SubclassEntityData) {
                SubclassEntityData subent1 = (SubclassEntityData) obj1;
                SubclassEntityData subent2 = (SubclassEntityData) obj2;

                //	In some cases protege3 is returning a different class hierarchy
                if (subent1.getSubclassCount() != subent2.getSubclassCount()) {
                    return false;
                }
            }

            //	PropertyEntityData
            if (obj1 instanceof PropertyEntityData && obj2 instanceof PropertyEntityData) {
                PropertyEntityData prop1 = (PropertyEntityData) obj1;
                PropertyEntityData prop2 = (PropertyEntityData) obj2;

                if (prop1.getMaxCardinality() != prop2.getMaxCardinality()) {
                    return false;
                }
                if (prop1.getMinCardinality() != prop2.getMinCardinality()) {
                    return false;
                }
                if (prop1.getPropertyType() != prop2.getPropertyType()) {
                    return false;
                }

                //	Test allowed values
                if (prop1.getAllowedValues() != null && prop2.getAllowedValues() != null) {
                    if (prop1.getAllowedValues().size() != prop2.getAllowedValues().size()) {
                        return false;
                    }
                    for (int i = 0; i < prop1.getAllowedValues().size(); i++) {
                        if (!isEqual(prop1.getAllowedValues().get(i), prop2.getAllowedValues().get(i))) {
                            return false;
                        }
                    }
                } else {
                    if (prop1.getAllowedValues() != null || prop2.getAllowedValues() != null) {
                        return false;
                    }
                }
            }

            return true;
        }

        // Triples
        if (obj1 instanceof Triple && obj2 instanceof Triple) {
            Triple ent1 = (Triple) obj1;
            Triple ent2 = (Triple) obj2;

            if (!isEqual(ent1.getEntity(), ent2.getEntity())) {
                return false;
            }
            if (!isEqual(ent1.getProperty(), ent2.getProperty())) {
                return false;
            }
            if (!isEqual(ent1.getValue(), ent2.getValue())) {
                return false;
            }

            return true;
        }
        return false;
    }

    /**
     * Test if obj is in list l using the method isEqual defined in this file to test equality.
     *
     * @param l
     * @param obj
     * @return
     */
    boolean hasEqualInList(List l, Object obj) {
        for (Object objList : l) {
            if (isEqual(objList, obj)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Test if obj is in list l using the method isEqual defined in this file to test equality.
     *
     * @param l
     * @param obj
     * @return
     */
    boolean hasEqualInCollection(Collection l, Comparer obj) {
        for (Object objList : l) {
            if (obj.compare(objList)) {
                return true;
            }
        }
        return false;
    }

    private abstract class Comparer<T> {
        abstract boolean compare(T input);
    }

    /**
     * Test method for {@link edu.stanford.bmir.protege.web.client.rpc.OntologyService#getEntity(java.lang.String, java.lang.String)}.
     */
    public void testGetEntityStringString() {
        EntityData ent = projects.getEntity("Pizza", PIZZA + "Pizza");

        EntityData pizza = new EntityData(PIZZA + "Pizza", "Pizza");

        assertTrue(isEqual(ent, pizza));

        ent = projects.getEntity("Pizza", RDFS + "label");

        //	Built-in AnnotationProperty: rdfs:label Range: String
        PropertyEntityData label = new PropertyEntityData(RDFS + "label", "rdfs:label", null);
        //	Inconsistency: in getEntityTriples it comes with value type information
        //label.setValueType(ValueType.String);
        label.setPropertyType(PropertyType.ANNOTATION);

        assertTrue(isEqual(ent, label));

        ent = projects.getEntity("Pizza", PIZZA + "hasBase");

        //	ObjectProperty: hasBase Range: PizzaBase
        PropertyEntityData hasBase = new PropertyEntityData(PIZZA + "hasBase", "hasBase", null);
        hasBase.setPropertyType(PropertyType.OBJECT);

        //	Inconsistency: it should come also with this information
        //hasBase.setValueType(ValueType.Instance);

        //	Inconsistency: it should come also with this information
        //List<EntityData> allowed= new ArrayList<EntityData>();
        //allowed.add(new EntityData(PIZZA+"PizzaBase", "PizzaBase", null));
        //hasBase.setAllowedValues(allowed);

        assertTrue(isEqual(ent, hasBase));

    }


    public void testGetEntityTriplesStringString() {
        List<Triple> triples = projects.getEntityTriples("Pizza", PIZZA + "Pizza");

        if (BACKEND == Api.PROTEGE) {
            assertEquals(1, triples.size());
//		if (BACKEND == Api.OWLAPI)
//			assertEquals(2, triples.size());
        }

        EntityData pizza = new EntityData(PIZZA + "Pizza", "Pizza");

        //	Built-in AnnotationProperty: rdfs:label Range: String
        PropertyEntityData label = new PropertyEntityData(RDFS + "label", "rdfs:label", null);
        label.setValueType(ValueType.String);

        //	ObjectProperty: hasBase Range: PizzaBase
        PropertyEntityData hasBase = new PropertyEntityData(PIZZA + "hasBase", "hasBase", null);
        hasBase.setValueType(ValueType.Instance);
        List<EntityData> allowed = new ArrayList<EntityData>();
        allowed.add(new EntityData(PIZZA + "PizzaBase", "PizzaBase", null));
        hasBase.setAllowedValues(allowed);

        //	Didn't change in owlapi as I think it is wrong to assert this about properties
        if (BACKEND == Api.PROTEGE) {
            hasBase.setMaxCardinality(1);
        }


        //Problem:
        //	Protege 3 returns ~#en Pizza while owlapi "Pizza"@en
        Triple triple1 = new Triple(pizza, label, new EntityData("~#en Pizza", "~#en Pizza"));
        Triple triple2 = new Triple(pizza, hasBase, new EntityData(PIZZA + "PizzaBase", "PizzaBase"));

        assertTrue(hasEqualInList(triples, triple1));
        if (BACKEND == Api.OWLAPI) {
            assertTrue(hasEqualInList(triples, triple2));
        }

        //	2nd round of tests

        triples = projects.getEntityTriples("Pizza", PIZZA + "American");

        EntityData american = new EntityData(PIZZA + "American", "American");

        PropertyEntityData hasTopping = new PropertyEntityData(PIZZA + "hasTopping", "hasTopping", null);
        hasTopping.setValueType(ValueType.Instance);
        allowed = new ArrayList<EntityData>();
        allowed.add(new EntityData(PIZZA + "PizzaTopping", "PizzaTopping", null));
        hasTopping.setAllowedValues(allowed);

        Triple triple3 = new Triple(american, label, new EntityData("~#pt Americana", "~#pt Americana"));
        Triple triple4 = new Triple(american, hasTopping, new EntityData(PIZZA + "MozzarellaTopping", "MozzarellaTopping"));
        Triple triple5 = new Triple(american, hasTopping, new EntityData(PIZZA + "PeperoniSausageTopping", "PeperoniSausageTopping"));
        Triple triple6 = new Triple(american, hasTopping, new EntityData(PIZZA + "TomatoTopping", "TomatoTopping"));


        assertTrue(hasEqualInList(triples, triple3));
        if (BACKEND == Api.OWLAPI) {
            assertTrue(hasEqualInList(triples, triple4));
            assertTrue(hasEqualInList(triples, triple5));
            assertTrue(hasEqualInList(triples, triple6));

            assertEquals(4, triples.size());

        }
    }

    public void testGetRelatedProperties(){
        final String subjectClass = PIZZA + "RealItalianPizza";
        final List<Triple> triples = projects.getRelatedProperties("Pizza", subjectClass);
        assertTrue(hasEqualInCollection(triples, new Comparer<Triple>() {
            @Override
            boolean compare(Triple input) {
                return input.getEntity().getName().equals(subjectClass) && input.getProperty().getName().contains("http://www.co-ode.org/ontologies/pizza/2005/10/18/pizza.owl#hasTopping") && input.getValue().toString().equals("PizzaTopping");
            }
        }));
        assertTrue(hasEqualInCollection(triples, new Comparer<Triple>() {
            @Override
            boolean compare(Triple input) {
                return input.getEntity().getName().equals(subjectClass) && input.getProperty().getName().contains("http://www.co-ode.org/ontologies/pizza/2005/10/18/pizza.owl#hasBase") && input.getValue().toString().equals("PizzaBase");
            }
        }));

    }

    public void testGetEntityPropertyValues() {
        final String subjectClass = PIZZA + "RealItalianPizza";
        final String instanceLevel1Instance1 = PIZZA + "level1Insstance1";
        final String instanceLevel1Instance2Branch2 = PIZZA + "level1Insstance2";
        final String instanceLevel2Instance1 = PIZZA + "level2Instance1";
        final String instanceLevel2Instance2Branch2 = PIZZA + "level2Instance2";
        final String instanceLevel3Instance1 = PIZZA + "level3Instance1";
        final String instanceLevel3Instance2 = PIZZA + "level3Instance2";
        final String instanceLevel3Instance3Branch2 = PIZZA + "level3Instance3";
        final EntityData subjectClassAsEntityData = projects.getEntity("Pizza", subjectClass);
        projects.createInstance("Pizza", instanceLevel1Instance1, subjectClass, null, null);
        projects.createInstance("Pizza", instanceLevel1Instance2Branch2, subjectClass, null, null);
        projects.createInstance("Pizza", instanceLevel2Instance1, subjectClass, null, null);
        projects.createInstance("Pizza", instanceLevel2Instance2Branch2, subjectClass, null, null);
        projects.createInstance("Pizza", instanceLevel3Instance1, subjectClass, null, null);
        projects.createInstance("Pizza", instanceLevel3Instance2, subjectClass, null, null);
        projects.createInstance("Pizza", instanceLevel3Instance3Branch2, subjectClass, null, null);

        final String propertyNameLevel1Branch1 = PIZZA + "LinkinProperty";

        final PropertyEntityData propertyLevel1 = createNewLink(instanceLevel1Instance1, propertyNameLevel1Branch1, instanceLevel2Instance1);

        // now set up the link between the second level and the first instance on the third ....
        final String propertyNameLevel2Instance1 = PIZZA + "LinkinProperty2";
        final PropertyEntityData propertyLevel2Instance1 = createNewLink(instanceLevel2Instance1, propertyNameLevel2Instance1, instanceLevel3Instance1);

        // now do the same for the second instance on the third level....
        final String propertyNameLevel2Instance2 = PIZZA + "LinkinProperty2Instance2";
        final PropertyEntityData propertyLevel2Instance2 = createNewLink(instanceLevel2Instance1, propertyNameLevel2Instance2, instanceLevel3Instance2);

        // now set up our alternate branch ...
        final String propertyNameLevel1Branch2 = PIZZA + "LinkinProperty1Instance2";

        final PropertyEntityData propertyLevel1Instance2Branch2 = createNewLink(instanceLevel1Instance2Branch2, propertyNameLevel1Branch2, instanceLevel2Instance2Branch2);
        final String propertyNameLevel2Branch2 = PIZZA + "LinkinProperty2Instance3";
        final PropertyEntityData propertyLevel2Instance3Branch2 = createNewLink(instanceLevel2Instance2Branch2, propertyNameLevel2Branch2, instanceLevel3Instance3Branch2);


        final List<EntityPropertyValues> entityPropertyValues = projects.getEntityPropertyValues("Pizza",
                Arrays.asList(
                        instanceLevel1Instance1,
                        instanceLevel1Instance2Branch2),
                Arrays.asList(
                        propertyLevel1.getName(),
                        propertyLevel1Instance2Branch2.getName()),
                Arrays.asList(propertyLevel2Instance2.getName(), propertyLevel2Instance1.getName(),
                        propertyLevel2Instance3Branch2.getName()));
        EntityPropertyValues entityProperties = entityPropertyValues.get(0);
        assertEquals(instanceLevel3Instance2, entityProperties.getPropertyValues(new PropertyEntityData(propertyNameLevel2Instance2)).get(0).getName());
        assertEquals(instanceLevel3Instance1, entityProperties.getPropertyValues(new PropertyEntityData(propertyNameLevel2Instance1)).get(0).getName());
        entityProperties = entityPropertyValues.get(1);
        assertEquals(instanceLevel3Instance3Branch2, entityProperties.getPropertyValues(new PropertyEntityData(propertyNameLevel2Branch2)).get(0).getName());

    }

    public void testRenameEntity() {
        final String superClassName = PIZZA + "RealItalianPizza";
        final String oldClassName = PIZZA + "OldRealItalianPizza";
        final String newClassName = PIZZA + "NewRealItalianPizza";
        final String originalInstanceName = PIZZA + "InstanceToRename";
        final String resultInstanceName = PIZZA + "InstanceToRenameTo";
        final String immutableInstance = PIZZA + "ImmutableInstance";
        projects.createCls("Pizza", oldClassName, superClassName, null ,null);
        projects.createInstance("Pizza", originalInstanceName, oldClassName, null, null);
        projects.createInstance("Pizza", immutableInstance, oldClassName, null, null);
        final String originalPropertyName = PIZZA + "OriginalProperty";
        final String resultPropertyName = PIZZA + "ResultProperty";
        createNewLink(originalInstanceName, originalPropertyName, immutableInstance);
        projects.renameEntity("Pizza", originalInstanceName, resultInstanceName, null, null);
        assertNull(projects.getEntity("Pizza", originalInstanceName));
        EntityData returnedEntity = projects.getEntity("Pizza", resultInstanceName);
        assertEquals(returnedEntity.getName(), resultInstanceName);
        projects.renameEntity("Pizza", originalPropertyName, resultPropertyName, null, null);
        assertNull(projects.getEntity("Pizza", originalPropertyName));
        returnedEntity = projects.getEntity("Pizza", resultPropertyName);
        assertEquals(returnedEntity.getName(), resultPropertyName);

        projects.renameEntity("Pizza", oldClassName, newClassName, null, null);
        assertNull(projects.getEntity("Pizza", oldClassName));
        returnedEntity = projects.getEntity("Pizza", newClassName);
        assertEquals(returnedEntity.getName(), newClassName);
    }

    private PropertyEntityData createNewLink(String origin, String linkName, String destination) {
        EntityData value;
        final PropertyEntityData propertyLevel2Instance2 = (PropertyEntityData) projects.createObjectProperty("Pizza", linkName, null, null, null);
        propertyLevel2Instance2.setValueType(ValueType.Instance);
        value = new EntityData(destination, "IgnoredValue");
        value.setValueType(ValueType.Instance);
        projects.addPropertyValue("Pizza", origin, propertyLevel2Instance2, value, null, null);
        return propertyLevel2Instance2;
    }

    public void testGetEntityTriplesStringListOfStringListOfString() {

        List<String> entities = new ArrayList<String>();
        entities.add(PIZZA + "Pizza");
        entities.add(PIZZA + "PizzaTopping");
        entities.add(PIZZA + "hasBase");

        List<String> properties = new ArrayList<String>();
        properties.add(PIZZA + "hasBase");
        properties.add(RDFS + "label");

        List<Triple> triples = projects.getEntityTriples("Pizza", entities, properties);

        //	TESTS

        EntityData pizza = new EntityData(PIZZA + "Pizza", "Pizza");

        //	Built-in AnnotationProperty: rdfs:label Range: String
        PropertyEntityData label = new PropertyEntityData(RDFS + "label", "rdfs:label", null);
        label.setValueType(ValueType.String);

        //	ObjectProperty: hasBase Range: PizzaBase
        PropertyEntityData hasBase = new PropertyEntityData(PIZZA + "hasBase", "hasBase", null);
        hasBase.setValueType(ValueType.Instance);
        List<EntityData> allowed = new ArrayList<EntityData>();
        allowed.add(new EntityData(PIZZA + "PizzaBase", "PizzaBase", null));
        hasBase.setAllowedValues(allowed);
        //	Didn't change in owlapi as I think it is wrong to assert this about properties
        if (BACKEND == Api.PROTEGE) {
            hasBase.setMaxCardinality(1);
        }

        Triple triple0 = null;
        if (BACKEND == Api.PROTEGE) {
            triple0 = new Triple(pizza, hasBase, null);
        }
        if (BACKEND == Api.OWLAPI) {
            triple0 = new Triple(pizza, hasBase, new EntityData(PIZZA + "PizzaBase", "PizzaBase", null));
        }

        Triple triple1 = new Triple(pizza, label, new EntityData("~#en Pizza", "~#en Pizza"));

        //	Inconsistency: Other triples in protege3 [2, 3, 4] make no sense

        Triple triple5 = new Triple(hasBase, label, null);

        triple0 = triples.get(0);

        assertTrue(hasEqualInList(triples, triple0));
        assertTrue(hasEqualInList(triples, triple1));

        if (BACKEND == Api.OWLAPI) {
            return;  //	OWLAPI has only 3 triples, all consistent
        }

        //	Inconsistency: Here hasBase has to have different values
        hasBase.setAllowedValues(null);
        hasBase.setValueType(null);
        hasBase.setPropertyType(PropertyType.OBJECT);
        hasBase.setMaxCardinality(-1);

        assertTrue(hasEqualInList(triples, triple5));
    }

//	/**
//	 * Test method for {@link edu.stanford.bmir.protege.web.client.rpc.OntologyService#getEvents(java.lang.String, long)}.
//	 */
//	public void testGetEvents() {
//		fail("Not yet implemented");
//	}

    /**
     * Test method for {@link edu.stanford.bmir.protege.web.client.rpc.OntologyService#getImportedOntologies(java.lang.String)}.
     */
    public void testGetImportedOntologies() {

        //	Just calling projects.getImportedOntologies("Pizza") gets a casting exception
//		if (BACKEND == Api.PROTEGE) {
//			assertTrue(true);
//			return;
//		}

        ImportsData imps = projects.getImportedOntologies("Pizza");

        assertEquals(PIZZA, imps.getName() + "#");
        assertEquals(1, imps.getImports().size());
        assertEquals("http://protege.stanford.edu/plugins/owl/protege", ((ImportsData) imps.getImports().get(0)).getName());
        assertEquals(0, ((ImportsData) imps.getImports().get(0)).getImports().size());
        //assertEquals(CELL, ((ImportsData) imps.getImports().get(0)).getName()+"#");
    }

    /**
     * Test method for {@link edu.stanford.bmir.protege.web.client.rpc.OntologyService#getIndividuals(java.lang.String, java.lang.String)}.
     */
    public void testGetIndividuals() {
        List<EntityData> inds = projects.getIndividuals("Pizza", PIZZA + "Country");
        assertEquals(5, inds.size());

        EntityData country = new EntityData(PIZZA + "America", "America");
        assertTrue(hasEqualInList(inds, country));
        country = new EntityData(PIZZA + "England", "England");
        assertTrue(hasEqualInList(inds, country));
        country = new EntityData(PIZZA + "France", "France");
        assertTrue(hasEqualInList(inds, country));
        country = new EntityData(PIZZA + "Germany", "Germany");
        assertTrue(hasEqualInList(inds, country));
        country = new EntityData(PIZZA + "Italy", "Italy");
        assertTrue(hasEqualInList(inds, country));

        inds = projects.getIndividuals("Cell", OBOINOWL + "DbXref");
        assertEquals(343, inds.size());
    }

    /**
     * Test method for {@link edu.stanford.bmir.protege.web.client.rpc.OntologyService#getMetrics(java.lang.String)}.
     */
    public void testGetMetrics() {
        List<MetricData> metrics = projects.getMetrics("Pizza");

        if (BACKEND == Api.PROTEGE) {
            assertTrue(hasEqualInList(metrics, new MetricData("Class count", "97")));
            assertTrue(hasEqualInList(metrics, new MetricData("Datatype property count", "0")));
            assertTrue(hasEqualInList(metrics, new MetricData("Object property count", "8")));
            assertTrue(hasEqualInList(metrics, new MetricData("Annotation property count", "0")));
            assertTrue(hasEqualInList(metrics, new MetricData("Individual count", "5")));
            assertTrue(hasEqualInList(metrics, new MetricData("DL Expressivity", "S H O I N")));
        }

        if (BACKEND == Api.OWLAPI) {
            assertTrue(hasEqualInList(metrics, new MetricData("Class count", "98")));
            assertTrue(hasEqualInList(metrics, new MetricData("Data property count", "0")));
            assertTrue(hasEqualInList(metrics, new MetricData("Object property count", "8")));
            assertTrue(hasEqualInList(metrics, new MetricData("Individual count", "5")));
            assertTrue(hasEqualInList(metrics, new MetricData("DL expressivity", "SHOIN")));
            assertTrue(hasEqualInList(metrics, new MetricData("Axiom count", "918")));
        }
    }

    /**
     * Test method for {@link edu.stanford.bmir.protege.web.client.rpc.OntologyService#getOntologyURI(java.lang.String)}.
     */
    public void testGetOntologyURI() {
        String uri = projects.getOntologyURI("Pizza");

        assertEquals(PIZZA, uri + "#");
    }

    /**
     */
    public void testGetParentsStringString() {
        List<EntityData> parents = projects.getParents("Pizza", PIZZA + "American", true);

        assertTrue(hasEqualInList(parents, new EntityData(PIZZA + "NamedPizza", "NamedPizza")));

        parents = projects.getParents("Pizza", PIZZA + "American", false);

        assertTrue(hasEqualInList(parents, new EntityData(PIZZA + "NamedPizza", "NamedPizza")));
        assertTrue(hasEqualInList(parents, new EntityData(PIZZA + "Pizza", "Pizza")));
    }


    public void testCreateAndAddAndRemoveAnnotation() {
        final PropertyEntityData entityData = (PropertyEntityData) projects.createAnnotationProperty("Pizza", PIZZA + "MyProp", null, null, "op");
        assertEquals(entityData.getName(), "http://www.co-ode.org/ontologies/pizza/2005/10/18/pizza.owl#MyProp");
        assertEquals(entityData.getBrowserText(), "MyProp");
        assertEquals(entityData.getValueType(), null);
        assertEquals(entityData.getLocalAnnotationsCount(), 0);
        final EntityData data = new EntityData("MyPropValue", "IgnoredValue");
        data.setValueType(ValueType.String);
        projects.addPropertyValue("Pizza", PIZZA + "American", entityData, data, "user", "op");
        List<AnnotationData> annotations = projects.getAnnotationProperties("Pizza", PIZZA + "American");
        assertEquals("Expected to retrieve label and annotation, but retrieved only label", 2, annotations.size());
        assertTrue(hasEqualInCollection(annotations, new Comparer<AnnotationData>() {
            boolean compare(AnnotationData input) {
                return input.getName().equals("MyProp") && input.getValue().contains("MyPropValue");
            }
        }));
        projects.removePropertyValue("Pizza", PIZZA + "American", entityData, data, "user", "op");
        annotations = projects.getAnnotationProperties("Pizza", PIZZA + "American");
        assertFalse(hasEqualInCollection(annotations, new Comparer<AnnotationData>() {
            boolean compare(AnnotationData input) {
                return input.getName().equals("MyProp");
            }
        }));

    }

    public void testCreateAndAddAndRemoveAnnotationWithBadInput() {
        final PropertyEntityData entityData = (PropertyEntityData) projects.createAnnotationProperty("Pizza", PIZZA + "MyProp", null, null, "op");
        assertEquals(entityData.getName(), "http://www.co-ode.org/ontologies/pizza/2005/10/18/pizza.owl#MyProp");
        assertEquals(entityData.getBrowserText(), "MyProp");
        final String propertyValueWithLanguageEncoded = "~#en Any pizza that has at least one meat topping";
        assertEquals(entityData.getValueType(), null);
        assertEquals(entityData.getLocalAnnotationsCount(), 0);
        final EntityData data = new EntityData("Any pizza that has at least one meat topping", "IgnoredValue");
        data.setValueType(ValueType.String);
        projects.addPropertyValue("Pizza", PIZZA + "American", entityData, data, "user", "op");
        data.setBrowserText(propertyValueWithLanguageEncoded);
        data.setName(propertyValueWithLanguageEncoded);
        List<AnnotationData> annotations = projects.getAnnotationProperties("Pizza", PIZZA + "American");
        assertEquals("Expected to retrieve label and annotation, but retrieved only label", 2, annotations.size());
        assertTrue(hasEqualInCollection(annotations, new Comparer<AnnotationData>() {
            boolean compare(AnnotationData input) {
                return input.getName().equals("MyProp") && input.getValue().contains("Any pizza that has at least one meat topping");
            }
        }));
        projects.removePropertyValue("Pizza", PIZZA + "American", entityData, data, "user", "op");
        annotations = projects.getAnnotationProperties("Pizza", PIZZA + "American");
        assertFalse(hasEqualInCollection(annotations, new Comparer<AnnotationData>() {
            boolean compare(AnnotationData input) {
                return input.getName().equals("MyProp");
            }
        }));

    }

    public void testCreateAndReplaceAnnotation() {
        final PropertyEntityData entityData = (PropertyEntityData) projects.createAnnotationProperty("Pizza", PIZZA + "MyMovableProp", null, null, "op");
        assertEquals(entityData.getName(), "http://www.co-ode.org/ontologies/pizza/2005/10/18/pizza.owl#MyMovableProp");
        assertEquals(entityData.getBrowserText(), "MyMovableProp");
        assertEquals(entityData.getValueType(), null);
        assertEquals(entityData.getLocalAnnotationsCount(), 0);
        final EntityData originalValue = new EntityData("InitialValue", "IgnoredValue");
        originalValue.setValueType(ValueType.String);
        projects.addPropertyValue("Pizza", PIZZA + "American", entityData, originalValue, "user", "op");
        List<AnnotationData> annotations = projects.getAnnotationProperties("Pizza", PIZZA + "American");
        final int originalNumberOfAnnotations = annotations.size();
        assertTrue(hasEqualInCollection(annotations, new Comparer<AnnotationData>() {
            boolean compare(AnnotationData input) {
                return input.getName().equals("MyMovableProp") && input.getValue().contains("InitialValue");
            }
        }));
        final EntityData replacementValue = new EntityData("NewValue", "IgnoredValue");
        replacementValue.setValueType(ValueType.String);
        projects.replacePropertyValue("Pizza", PIZZA + "American", entityData, originalValue, replacementValue, "user", "op");
        annotations = projects.getAnnotationProperties("Pizza", PIZZA + "American");
        assertTrue(hasEqualInCollection(annotations, new Comparer<AnnotationData>() {
            boolean compare(AnnotationData input) {
                return input.getName().equals("MyMovableProp") && input.getValue().contains("NewValue");
            }
        }));
        assertEquals(originalNumberOfAnnotations, annotations.size());
    }

    public void testReplacePropertyValue() {
        String entityName = "http://www.co-ode.org/ontologies/pizza/2005/10/18/pizza.owl#IceCream";
        PropertyEntityData propertyEntity = new PropertyEntityData("http://www.w3.org/2000/01/rdf-schema#label", "http://www.w3.org/2000/01/rdf-schema#label");
        EntityData oldValue = new EntityData("Sorvete", "Sorvete");
        oldValue.setValueType(ValueType.String);
        EntityData newValue = new EntityData("Sorvetexxx", "Sorvetexxx");
        oldValue.setValueType(ValueType.String);
        projects.replacePropertyValue("Pizza", entityName, propertyEntity, oldValue, newValue, null, null);
        List annotations = projects.getAnnotationProperties("Pizza", entityName);
        assertTrue(hasEqualInCollection(annotations, new Comparer<AnnotationData>() {
            boolean compare(AnnotationData input) {
                return input.getName().contains("label") && input.getValue().contains("Sorvetexxx");
            }
        }));
        // bit weird, that the original method would not delete the old label, but it does hang around ....
        assertTrue(hasEqualInCollection(annotations, new Comparer<AnnotationData>() {
            boolean compare(AnnotationData input) {
                return input.getName().contains("label") && input.getValue().contains("Sorvete");
            }
        }));

    }

    public void testReturnValue() {
List<Triple> triples = projects.getEntityTriples("Pizza", PIZZA + "IceCream");

        if (BACKEND == Api.PROTEGE) {
            assertEquals(1, triples.size());
//		if (BACKEND == Api.OWLAPI)
//			assertEquals(2, triples.size());
        }
        assertEquals("Sorvete", triples.get(1).getValue().getName());
        assertEquals("Sorvete", triples.get(1).getValue().getBrowserText());
        assertEquals("rdfs:label", triples.get(1).getProperty().getBrowserText());
        assertEquals(ValueType.StringLiteralWithLanguage, triples.get(1).getValue().getValueType());
        assertEquals(ValueType.String, triples.get(1).getProperty().getValueType());

        EntityData pizza = new EntityData(PIZZA + "Pizza", "Pizza");

        

    }

    public void testCreateAndAddAndRemoveDatatypeProperty() {
        final String americanPizzaIri = PIZZA + "American";
        final PropertyEntityData entityData = (PropertyEntityData) projects.createDatatypeProperty("Pizza", PIZZA + "MyDatatypeProp", null, null, "op");
        assertEquals(entityData.getName(), "http://www.co-ode.org/ontologies/pizza/2005/10/18/pizza.owl#MyDatatypeProp");
        assertEquals(entityData.getBrowserText(), "MyDatatypeProp");
        assertEquals(entityData.getValueType(), null);
        assertEquals(entityData.getLocalAnnotationsCount(), 0);
        final EntityData data = new EntityData("MyPropValue", "IgnoredValue");
        data.setValueType(ValueType.String);
        projects.addPropertyValue("Pizza", americanPizzaIri, entityData, data, "user", "op");
        List<PropertyData> datatypeProperties = projects.getDatatypeProperties("Pizza", americanPizzaIri);
        assertTrue(hasEqualInCollection(datatypeProperties, new Comparer<PropertyData>() {
            boolean compare(PropertyData input) {
                return input.getName().equals("MyDatatypeProp") && input.getValue().contains("MyPropValue");
            }
        }));
        projects.removePropertyValue("Pizza", americanPizzaIri, entityData, data, "user", "op");
        datatypeProperties = projects.getDatatypeProperties("Pizza", americanPizzaIri);
        assertFalse(hasEqualInCollection(datatypeProperties, new Comparer<PropertyData>() {
            boolean compare(PropertyData input) {
                return input.getName().equals("MyDatatypeProp") && input.getValue().contains("MyPropValue");
            }
        }));

    }

    public void testCreateAndAddAndRemoveObjectProperty() {
        final PropertyEntityData propertyData = (PropertyEntityData) projects.createObjectProperty("Pizza", PIZZA + "MyObjectProp", null, null, "op");
        assertEquals(propertyData.getName(), "http://www.co-ode.org/ontologies/pizza/2005/10/18/pizza.owl#MyObjectProp");
        assertEquals(propertyData.getBrowserText(), "MyObjectProp");
        assertEquals(propertyData.getValueType(), null);
        assertEquals(propertyData.getLocalAnnotationsCount(), 0);
        final EntityData data = new EntityData("http://www.co-ode.org/ontologies/pizza/2005/10/18/pizza.owl#France", "IgnoredValue");
        data.setValueType(ValueType.String);
        projects.addPropertyValue("Pizza", PIZZA + "American", propertyData, data, "user", "op");
        List<PropertyData> objectProperties = projects.getObjectProperties("Pizza", PIZZA + "American");
        assertTrue(hasEqualInCollection(objectProperties, new Comparer<PropertyData>() {
            boolean compare(PropertyData input) {
                return input.getName().equals("MyObjectProp") && input.getValue().contains("France");
            }
        }));
        projects.removePropertyValue("Pizza", PIZZA + "American", propertyData, data, "user", "op");
        objectProperties = projects.getObjectProperties("Pizza", PIZZA + "American");
        assertFalse(hasEqualInCollection(objectProperties, new Comparer<AnnotationData>() {
            boolean compare(AnnotationData input) {
                return input.getName().equals("MyObjectProp");
            }
        }));
    }


    public void testSetPropertyValues() {
        final String subjectClass = PIZZA + "RealItalianPizza";
        final String subjectInstance = PIZZA + "SetPropertyValuesInstance";
        final String objectInstanceName1 = PIZZA + "SetPropertyValuesInstanceObject";
        final String objectInstanceName2 = PIZZA + "SetPropertyValuesInstanceObject2";
        final EntityData subjectClassAsEntityData = projects.getEntity("Pizza", subjectClass);
        projects.createInstance("Pizza", subjectInstance, subjectClass, null, null);
        final EntityData objectInstance1 = projects.createInstance("Pizza", objectInstanceName1, subjectClass, null, null);
        final EntityData objectInstance2 = projects.createInstance("Pizza", objectInstanceName2, subjectClass, null, null);

        List<EntityData> inputs = Arrays.asList(objectInstance1, objectInstance2);

        final PropertyEntityData propertyData = (PropertyEntityData) projects.createObjectProperty("Pizza", PIZZA + "MyBulkProp", null, null, "op");

        final EntityData data = new EntityData("http://www.co-ode.org/ontologies/pizza/2005/10/18/pizza.owl#France", "IgnoredValue");
        data.setValueType(ValueType.String);
        projects.setPropertyValues("Pizza", subjectInstance,propertyData, inputs, "user", "op");
        List<PropertyData> objectProperties = projects.getObjectProperties("Pizza", subjectInstance);
        assertTrue(hasEqualInCollection(objectProperties, new Comparer<PropertyData>() {
            boolean compare(PropertyData input) {
                return input.getName().equals("MyBulkProp") && input.getValue().contains(objectInstanceName1);
            }
        }));
        assertTrue(hasEqualInCollection(objectProperties, new Comparer<PropertyData>() {
            boolean compare(PropertyData input) {
                return input.getName().equals("MyBulkProp") && input.getValue().contains(objectInstanceName2);
            }
        }));

    }

    public void testCreateInstanceValue() {
        System.out.println(projects.getSubclasses("Pizza", PIZZA + "Pizza"));
        final String propertyName = PIZZA + "BrandNewProperty";
        // the subject is the existing individual we add stuff to
        final String subjectClass = PIZZA + "RealItalianPizza";
        final String subjectInstance = PIZZA + "RealItalianPizzaInstance";
        final String subjectInstance2 = PIZZA + "RealItalianPizzaInstance2";
        final EntityData subjectClassAsEntityData = projects.getEntity("Pizza", subjectClass);
        projects.createInstance("Pizza", subjectInstance, subjectClass, null, null);
        projects.createInstance("Pizza", subjectInstance2, subjectClass, null, null);
        final String newInstance = PIZZA + "MyNewInstance";

        System.out.println(projects.getIndividuals("Pizza", subjectClass));
        final PropertyEntityData name = (PropertyEntityData) projects.createObjectProperty("Pizza", propertyName, null, null, null);
        name.setAllowedValues(Arrays.asList(subjectClassAsEntityData));
        name.setValueType(ValueType.Instance);
        final EntityData value = new EntityData(subjectInstance2, "IgnoredValue");
        value.setValueType(ValueType.Instance);
        projects.addPropertyValue("Pizza", subjectInstance, name, value, null, null);

        projects.createInstanceValue("Pizza", newInstance, subjectClass, subjectInstance, propertyName, null, "op");

        List<PropertyData> objectProperties = projects.getObjectProperties("Pizza", subjectInstance);
        assertEquals("Bizarrely, both the originally added property and the new property must both be returned", 2, objectProperties.size());
        assertTrue(hasEqualInCollection(objectProperties, new Comparer<PropertyData>() {
            boolean compare(PropertyData input) {
                return input.getName().equals("BrandNewProperty") && input.getValue().contains("http://www.co-ode.org/ontologies/pizza/2005/10/18/pizza.owl#MyNewInstance");
            }
        }));
    }

    public void testCreateInstanceValueWithNulls() {

        String projectName = "ICD";
        String instName = null;
        String typeName = null;
        String subjectEntity = "http://who.int/icd#A00.1";
        String propertyEntity = "http://who.int/icd#definition";



//        final EntityData subjectClassAsEntityData = projects.getEntity("Pizza", subjectClass);
        final String user = "Guest";
        final EntityData data = projects.createInstanceValue(projectName, instName, typeName, subjectEntity, propertyEntity, user, "Added a new definition to A00.1 -- Apply to: http://who.int/icd#A00.1");

        String entityName = data.getName();
        PropertyEntityData propertyEntityData = new PropertyEntityData("http://who.int/icd#label");
        propertyEntityData.setPropertyType(PropertyType.OBJECT);
        propertyEntityData.setValueType(ValueType.String);
        EntityData oldValue = new EntityData("");
        oldValue.setValueType(ValueType.String);
        EntityData newValue = new EntityData("eeeee");
        newValue.setValueType(ValueType.String);
        projects.replacePropertyValue(projectName, entityName, propertyEntityData, oldValue, newValue, null, null);


        final List<EntityPropertyValues> entityPropertyValues = projects.getEntityPropertyValues(projectName, Arrays.asList("http://who.int/icd#A00.1"), Arrays.asList("http://who.int/icd#definition"), Arrays.asList("http://who.int/icd#label"));
        for (EntityPropertyValues entityPropertyValue : entityPropertyValues) {
            System.out.println("entityPropertyValue = " + entityPropertyValue);
        }
        assertFalse(entityPropertyValues.isEmpty());

    }

    public void testCreateInstanceValueWithPropertyValue() {
        System.out.println(projects.getSubclasses("Pizza", PIZZA + "Pizza"));
        final String propertyName = PIZZA + "BrandNewProperty";
        // the subject is the existing individual we add stuff to
        final String subjectClass = PIZZA + "RealItalianPizza";
        final String subjectInstance = PIZZA + "RealItalianPizzaInstance";
        final String subjectInstance2 = PIZZA + "RealItalianPizzaInstance2";
        final EntityData subjectClassAsEntityData = projects.getEntity("Pizza", subjectClass);
        projects.createInstance("Pizza", subjectInstance, subjectClass, null, null);
        projects.createInstance("Pizza", subjectInstance2, subjectClass, null, null);
        final String newInstance = PIZZA + "MyNewInstance";

        System.out.println(projects.getIndividuals("Pizza", subjectClass));
        final PropertyEntityData name = (PropertyEntityData) projects.createObjectProperty("Pizza", propertyName, null, null, null);
        name.setAllowedValues(Arrays.asList(subjectClassAsEntityData));
        name.setValueType(ValueType.Instance);

        final EntityData value = new EntityData(subjectInstance2, "IgnoredValue");
        value.setValueType(ValueType.Instance);

        projects.createInstanceValueWithPropertyValue("Pizza", newInstance, subjectClass, subjectInstance, propertyName, name, value, null, "op");

        List<PropertyData> objectProperties = projects.getObjectProperties("Pizza", subjectInstance);
        assertTrue(hasEqualInCollection(objectProperties, new Comparer<PropertyData>() {
            boolean compare(PropertyData input) {
                return input.getName().equals("BrandNewProperty") && input.getValue().contains("http://www.co-ode.org/ontologies/pizza/2005/10/18/pizza.owl#MyNewInstance");
            }
        }));
    }

    public void testCreateAndRemoveInstance() {
        final String myInstanceIri = PIZZA + "MyInstance";
        final EntityData originalEntity = projects.createInstance("Pizza", myInstanceIri, PIZZA + "American", null, "op");
        assertEquals(originalEntity.getName(), "http://www.co-ode.org/ontologies/pizza/2005/10/18/pizza.owl#MyInstance");
        assertEquals(originalEntity.getBrowserText(), "MyInstance");
        assertEquals(originalEntity.getValueType(), null);
        assertEquals(originalEntity.getLocalAnnotationsCount(), 0);
        EntityData returnedEntity = projects.getEntity("Pizza", myInstanceIri);
        assertEquals(originalEntity.getName(), returnedEntity.getName());
        assertEquals(originalEntity.getLocalAnnotationsCount(), returnedEntity.getLocalAnnotationsCount());
        assertEquals(originalEntity.getBrowserText(), returnedEntity.getBrowserText());
        assertEquals(originalEntity.getValueType(), returnedEntity.getValueType());
        projects.deleteEntity("Pizza", myInstanceIri, null, "op");
        returnedEntity = projects.getEntity("Pizza", myInstanceIri);
        assertNull(returnedEntity);
    }


    public void testCreateAndRemoveClass() {
        final String myClassIri = PIZZA + "MyClass";
        final EntityData originalEntity = projects.createCls("Pizza", myClassIri, PIZZA + "American", null, "op");
        assertEquals(originalEntity.getName(), "http://www.co-ode.org/ontologies/pizza/2005/10/18/pizza.owl#MyClass");
        assertEquals(originalEntity.getBrowserText(), "MyClass");
        assertEquals(originalEntity.getValueType(), null);
        assertEquals(originalEntity.getLocalAnnotationsCount(), 0);
        EntityData returnedEntity = projects.getEntity("Pizza", myClassIri);
        assertEquals(originalEntity.getName(), returnedEntity.getName());
        assertEquals(originalEntity.getLocalAnnotationsCount(), returnedEntity.getLocalAnnotationsCount());
        assertEquals(originalEntity.getBrowserText(), returnedEntity.getBrowserText());
        assertEquals(originalEntity.getValueType(), returnedEntity.getValueType());
        projects.deleteEntity("Pizza", myClassIri, null, "op");
        returnedEntity = projects.getEntity("Pizza", myClassIri);
        assertNull(returnedEntity);
    }

    public void testAddAndRemoveSuperClass() {
        final String subclassIri = "http://www.co-ode.org/ontologies/pizza/2005/10/18/pizza.owl#France";
        final String firstSuperClassIri = PIZZA + "NamedPizza";
        final String secondSuperClassIri = PIZZA + "Country";
        // first add the subclass to the first super class and test
        projects.addSuperCls("Pizza", subclassIri, firstSuperClassIri, null, "op");
        List<SubclassEntityData> subclassEntityData = projects.getSubclasses("Pizza", firstSuperClassIri);
        assertTrue(hasEqualInCollection(subclassEntityData, new Comparer<SubclassEntityData>() {
            boolean compare(SubclassEntityData input) {
                return input.getName().equals(subclassIri);
            }
        }));
        // now add the subclass to the second superclass and test
        projects.addSuperCls("Pizza", subclassIri, secondSuperClassIri, null, "op");
        subclassEntityData = projects.getSubclasses("Pizza", secondSuperClassIri);
        assertTrue(hasEqualInCollection(subclassEntityData, new Comparer<SubclassEntityData>() {
            boolean compare(SubclassEntityData input) {
                return input.getName().equals(subclassIri);
            }
        }));
        // now remove the first one only and test
        projects.removeSuperCls("Pizza", subclassIri, firstSuperClassIri, null, "op");
        subclassEntityData = projects.getSubclasses("Pizza", firstSuperClassIri);
        assertFalse(hasEqualInCollection(subclassEntityData, new Comparer<SubclassEntityData>() {
            boolean compare(SubclassEntityData input) {
                return input.getName().equals(subclassIri);
            }
        }));
        // but confirm that we deleted _only_ the first super class relationship
        subclassEntityData = projects.getSubclasses("Pizza", secondSuperClassIri);
        assertTrue(hasEqualInCollection(subclassEntityData, new Comparer<SubclassEntityData>() {
            boolean compare(SubclassEntityData input) {
                return input.getName().equals(subclassIri);
            }
        }));
    }

    public void testMoveSuperClass() {
        final String subclassIri = "http://www.co-ode.org/ontologies/pizza/2005/10/18/pizza.owl#CheeseyPizza";
        final String firstSuperClassIri = PIZZA + "NamedPizza";
        final String secondSuperClassIri = PIZZA + "Country";
        // first add the subclass to the first super class and test
        projects.addSuperCls("Pizza", subclassIri, firstSuperClassIri, null, "op");
        List<SubclassEntityData> subclassEntityData = projects.getSubclasses("Pizza", firstSuperClassIri);
        assertTrue(hasEqualInCollection(subclassEntityData, new Comparer<SubclassEntityData>() {
            boolean compare(SubclassEntityData input) {
                return input.getName().equals(subclassIri);
            }
        }));
        // now move the class to another part of the tree
        projects.moveCls("Pizza", subclassIri, firstSuperClassIri, secondSuperClassIri, null, null);
        subclassEntityData = projects.getSubclasses("Pizza", secondSuperClassIri);
        assertTrue(hasEqualInCollection(subclassEntityData, new Comparer<SubclassEntityData>() {
            boolean compare(SubclassEntityData input) {
                return input.getName().equals(subclassIri);
            }
        }));
        //confirm that it has been removed from the original superclass
        subclassEntityData = projects.getSubclasses("Pizza", firstSuperClassIri);
        assertFalse(hasEqualInCollection(subclassEntityData, new Comparer<SubclassEntityData>() {
            boolean compare(SubclassEntityData input) {
                return input.getName().equals(subclassIri);
            }
        }));
    }


    /**
     * Test method for {@link edu.stanford.bmir.protege.web.client.rpc.OntologyService#getPathToRoot(java.lang.String, java.lang.String)}.
     */
    public void testGetPathToRoot() {

        List<EntityData> clses = projects.getPathToRoot("Pizza", PIZZA + "American");

        List<EntityData> path = new ArrayList<EntityData>();
        path.add(THING_ENTITY);
        path.add(new EntityData(PIZZA + "DomainConcept", "DomainConcept"));
        path.add(new EntityData(PIZZA + "Pizza", "Pizza"));
        path.add(new EntityData(PIZZA + "NamedPizza", "NamedPizza"));
        path.add(new EntityData(PIZZA + "American", "American"));

        assertEquals(5, clses.size());

        assertTrue(isEqual(clses.get(0), path.get(0)));
        assertTrue(isEqual(clses.get(1), path.get(1)));
        assertTrue(isEqual(clses.get(2), path.get(2)));
        assertTrue(isEqual(clses.get(3), path.get(3)));
        assertTrue(isEqual(clses.get(4), path.get(4)));
    }

    /**
     * Test method for {@link edu.stanford.bmir.protege.web.client.rpc.OntologyService#getRestrictionHtml(java.lang.String, java.lang.String)}.
     */
    public void testGetRestrictionHtml() {
        String html = projects.getRestrictionHtml("Pizza", PIZZA + "Pizza");

        assertEquals("<table width=\"100%\" border=\"0\" cellspacing=\"3\"  class=\"restriction_table\"></table><table width=\"100%\" border=\"0\" cellspacing=\"3\"  class=\"restriction_table\"><hr><div class=\"restiction_title\">Superclasses (Necessary conditions)</div><tr><td>DomainConcept</td></tr><tr><td>hasBase <span class=\"restriction_delim\">some</span> PizzaBase</td></tr></table>",
                html);
//		/				"<table width=\"100%\" border=\"0\" cellspacing=\"3\"  class=\"restriction_table\"></table><table width=\"100%\" border=\"0\" cellspacing=\"3\"  class=\"restriction_table\"><hr><div class=\"restiction_title\">Superclasses (Necessary conditions)</div><tr><td>DomainConcept</td></tr><tr><td>hasBase  <span class=\"restriction_delim\">some</span> PizzaBase</td></tr></table>",
    }

    /**
     * Test method for {@link edu.stanford.bmir.protege.web.client.rpc.OntologyService#getRootEntity(java.lang.String)}.
     */
    public void testGetRootEntity() {

        EntityData entity = projects.getRootEntity("Pizza");
        assertTrue(isEqual(THING_ENTITY, entity));

        entity = projects.getRootEntity("Cell");
        assertTrue(isEqual(THING_ENTITY, entity));
    }

    /**
     * Test method for {@link edu.stanford.bmir.protege.web.client.rpc.OntologyService#getSubclasses(java.lang.String, java.lang.String)}.
     */
    public void testGetSubclasses() {
        List<SubclassEntityData> classes = projects.getSubclasses("Pizza", PIZZA + "DomainConcept");

        SubclassEntityData country = new SubclassEntityData(PIZZA + "Country", "Country", null, 0);
        SubclassEntityData iceCream = new SubclassEntityData(PIZZA + "IceCream", "IceCream", null, 0);
        SubclassEntityData pizza = new SubclassEntityData(PIZZA + "Pizza", "Pizza", null, 11);
        SubclassEntityData pizzaBase = new SubclassEntityData(PIZZA + "PizzaBase", "PizzaBase", null, 2);
        SubclassEntityData pizzaTopping = new SubclassEntityData(PIZZA + "PizzaTopping", "PizzaTopping", null, 10);

        //	Cannot make owlapi read equivalent axiom as subclass
//		if (ServerFactory.BACKEND == Api.PROTEGE)
        assertTrue(hasEqualInList(classes, country));
        assertTrue(hasEqualInList(classes, iceCream));
        assertTrue(hasEqualInList(classes, pizza));
        assertTrue(hasEqualInList(classes, pizzaBase));
        assertTrue(hasEqualInList(classes, pizzaTopping));
        assertEquals(5, classes.size());

        classes = projects.getSubclasses("Pizza", THING_ENTITY.getName());

        SubclassEntityData domain = new SubclassEntityData(PIZZA + "DomainConcept", "DomainConcept", null, 5);
        SubclassEntityData value = new SubclassEntityData(PIZZA + "ValuePartition", "ValuePartition", null, 1);
        assertTrue(hasEqualInList(classes, domain));
        assertTrue(hasEqualInList(classes, value));
        assertEquals(2, classes.size());
    }

    /**
     * Test method for {@link edu.stanford.bmir.protege.web.client.rpc.OntologyService#getSubproperties(java.lang.String, java.lang.String)}.
     */
    public void testGetSubproperties() {
        List<EntityData> props = projects.getSubproperties("Pizza", PIZZA + "hasIngredient");

        assertEquals(2, props.size());
        PropertyEntityData prop1 = new PropertyEntityData(PIZZA + "hasTopping", "hasTopping", null);
        prop1.setPropertyType(PropertyType.OBJECT);

        PropertyEntityData prop2 = new PropertyEntityData(PIZZA + "hasBase", "hasBase", null);
        prop2.setPropertyType(PropertyType.OBJECT);

        assertTrue(hasEqualInList(props, prop1));
        assertTrue(hasEqualInList(props, prop2));

        //	Test if it gets the root properties
        props = projects.getSubproperties("Pizza", null);

        PropertyEntityData prop3 = new PropertyEntityData(PIZZA + "hasSpiciness", "hasSpiciness", null);
        prop3.setPropertyType(PropertyType.OBJECT);
        PropertyEntityData prop4 = new PropertyEntityData(PIZZA + "hasIngredient", "hasIngredient", null);
        prop4.setPropertyType(PropertyType.OBJECT);
        PropertyEntityData prop5 = new PropertyEntityData(PIZZA + "hasCountryOfOrigin", "hasCountryOfOrigin", null);
        prop5.setPropertyType(PropertyType.OBJECT);
        PropertyEntityData prop6 = new PropertyEntityData(PIZZA + "isIngredientOf", "isIngredientOf", null);
        prop6.setPropertyType(PropertyType.OBJECT);
        PropertyEntityData prop7 = new PropertyEntityData(PROTEGE + "defaultLanguage", "protege:defaultLanguage", null);

        if (BACKEND == Api.OWLAPI) {
            prop7.setPropertyType(PropertyType.ANNOTATION);
            assertTrue(hasEqualInList(props, prop7));
        }

        if (BACKEND == Api.PROTEGE) {
            assertEquals(25, props.size());
        } else if (BACKEND == Api.OWLAPI) {
            assertEquals(47, props.size());
        }

        assertTrue(hasEqualInList(props, prop3));
        assertTrue(hasEqualInList(props, prop4));
        assertTrue(hasEqualInList(props, prop5));
        assertTrue(hasEqualInList(props, prop6));
//		assertTrue(hasEqualInList(props, prop7));
    }


}
