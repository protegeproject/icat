(ns test.ontology_test
    (:use test.import-static)
	(:gen-class
     :extends edu.stanford.bmir.protege.web.server.OntologyServiceTest
;     :extends junit.framework.TestCase
	 :state state
	 :init init 
	 :methods [
               [testGetEntityStringString [] void]
               [testGetEntityTriplesStringString [] void]
               [testCreateCls [] void]
               [testCreateInstance [] void]
               [testCreateDatatypeProperty [] void]
               [testCreateObjectProperty [] void]
               [testCreateAnnotationProperty [] void]
               
               [testAddPropertyValue [] void]
               [testRemovePropertyValue [] void]
               [testReplacePropertyValue [] void]
                
               [testDeleteEntity [] void]
              ]					 
     :exposes-methods {setUp superSetUp}
	 ) 
	(:import [edu.stanford.bmir.protege.web.server FileUtil]
	         [edu.stanford.bmir.protege.web.server.owlapi OntologyServiceImpl]
	         [edu.stanford.bmir.protege.web.client.rpc.data ProjectData EntityData PropertyEntityData PropertyType Triple ValueType]
    )  
;   (:use [clojure.test-is])
)

(import-static edu.stanford.bmir.protege.web.server.OntologyServiceTest 
        areEqual hasEqualInList PIZZA RDFS)
(import-static junit.framework.Assert assertEquals assertTrue)

(defn -init [name]
	[[name] 
     (do 
        (println "Init.") 
;        (edu.stanford.bmir.protege.web.server.OntologyServiceTest/setP3Api)
        (atom []))
    ])

(defn -main [args]
  (println "Hi !!!"))

(defn p3api? [] (. edu.stanford.bmir.protege.web.server.OntologyServiceTest p3Api))

(defn createProject []
  (do
    (FileUtil/init "testOntologies/")
     
    (if (p3api?)
      (edu.stanford.bmir.protege.web.server.OntologyServiceImpl.)
      
      (do 
        ; Test owlapi ontologies
        (OntologyServiceImpl/initOnto 
            (list
;                (ProjectData. "ICD Project"   "projects/icd/ICD.owl"     "ICD"   "dilvan")
                (ProjectData. "Pizza Project" "projects/pizza/pizza.owl" "Pizza" "dilvan")
                (ProjectData. "Cell Project"  "projects/cell/cell.owl"   "Cell"  "dilvan")))
        (OntologyServiceImpl.)
        ))))

(defn -setUp [this]  
  (do
    (.superSetUp this)
    (def projects (createProject))))

(defn uri [base name]
    (.concat base name))

(defn -testGetEntityStringString [this]
    (let [
        pizza    (EntityData. (uri PIZZA "Pizza") "Pizza")
        ent      (.getEntity projects "Pizza" (uri PIZZA "Pizza"))
        ent2     (.getEntity projects "Pizza" (uri RDFS "label"))
        ent3     (.getEntity projects "Pizza" (uri PIZZA "hasBase"))
        ent4     (.getEntity projects "Pizza" (uri PIZZA "America"))
                
        label    (PropertyEntityData. (uri RDFS "label") "rdfs:label" nil)
        hasBase  (PropertyEntityData. (uri PIZZA "hasBase") "hasBase" nil)
        america  (EntityData. (uri PIZZA "America") "America" nil)
        
        createdIns1 (.createInstance projects "Pizza" (uri PIZZA "TestConcept") (uri PIZZA "DomainConcept") "dilvan" "Test createIns")
        ent5        (.getEntity projects "Pizza" (uri PIZZA "TestConcept"))

        ]
 
        (assertTrue (areEqual ent pizza))
        (.setPropertyType label PropertyType/ANNOTATION)
        (assertTrue (areEqual ent2 label))
        (.setPropertyType hasBase PropertyType/OBJECT)
        (assertTrue (areEqual ent3 hasBase))
        (assertTrue (areEqual ent4 america))
))
 
(defn -testGetEntityTriplesStringString [this]
    (let [
        pizza       (EntityData. (uri PIZZA "Pizza") "Pizza")
        triples     (.getEntityTriples projects "Pizza" (uri PIZZA "Pizza"))
        label       (PropertyEntityData. (uri RDFS "label") "rdfs:label" nil)
        hasBase     (PropertyEntityData. (uri PIZZA "hasBase") "hasBase" nil)
        allowed     (list (EntityData. (uri PIZZA "PizzaBase") "PizzaBase", nil))   
        ;Problem:
        ;Protege 3 returns ~#en Pizza while owlapi "Pizza"@en
;        triple1     (Triple. pizza label   (EntityData. "~#en Pizza" "~#en Pizza"))
        triple1     (Triple. pizza label   (EntityData. "Pizza" "Pizza"))
        triple2     (Triple. pizza hasBase (EntityData. (uri PIZZA "PizzaBase") "PizzaBase"))  

        ;   2nd round of tests    
        triples2    (.getEntityTriples projects "Pizza" (uri PIZZA "American"))

        american    (EntityData. (uri PIZZA "American") "American")
        
        hasTopping  (PropertyEntityData. (uri PIZZA "hasTopping") "hasTopping" nil)
;        hasTopping.setValueType(ValueType.Instance);
;        allowed= new ArrayList<EntityData>();
        allowed2     (list (EntityData. (uri PIZZA "PizzaTopping") "PizzaTopping" nil))
;        hasTopping.setAllowedValues(allowed);

;        triple3     (Triple. american label (EntityData. "~#pt Americana" "~#pt Americana"))
        triple3     (Triple. american label (EntityData. "Americana" "Americana"))
        triple4     (Triple. american hasTopping (EntityData. (uri PIZZA "MozzarellaTopping") "MozzarellaTopping"))
        triple5     (Triple. american hasTopping (EntityData. (uri PIZZA "PeperoniSausageTopping") "PeperoniSausageTopping"))
        triple6     (Triple. american hasTopping (EntityData. (uri PIZZA "TomatoTopping") "TomatoTopping"))
        ]
 
        (if (p3api?)
            (assertEquals 1 (.size triples)))        
 
;//		if (BACKEND == Api.OWLAPI)
;//			assertEquals(2, triples.size());

		;	Built-in AnnotationProperty: rdfs:label Range: String
		(.setValueType label ValueType/String)
		
		;	ObjectProperty: hasBase Range: PizzaBase 
		(.setValueType hasBase ValueType/Instance)
		(.setAllowedValues hasBase allowed)
		
		;	Didn't change in owlapi as I think it is wrong to assert this about properties
		(if (p3api?)
			(.setMaxCardinality hasBase 1))
		
		(assertTrue (hasEqualInList triples triple1))

		(if-not (p3api?)
			(assertTrue (hasEqualInList triples triple2)))
		
		;	2nd round of tests
		
		(.setValueType hasTopping ValueType/Instance)
		(.setAllowedValues hasTopping allowed2)
		
		(assertTrue (hasEqualInList triples2 triple3))
		(when-not (p3api?)            
			  (assertTrue (hasEqualInList triples2 triple4))
			  (assertTrue (hasEqualInList triples2 triple5))
			  (assertTrue (hasEqualInList triples2 triple6))
			
			  (assertEquals 4  (.size triples2))
            )
))	 
                                                                                                
;    public EntityData createCls(String projectName, String clsName, String superClsName, String user, 
;                String operationDescription) { 
(defn -testCreateCls [this]
  (let [  
        newCls1     (EntityData. (uri PIZZA "Test") "Test")         
        createdCls1 (.createCls projects "Pizza" "Test" "http://www.w3.org/2002/07/owl#Thing" "dilvan" "Test createCls")
        newCls2     (EntityData. (uri PIZZA "TestPizza") "TestPizza")         
        createdCls2 (.createCls projects "Pizza" "TestPizza" (uri PIZZA "Pizza") "dilvan" "Test createCls")
        newCls3     (.getEntity projects "Pizza" (uri PIZZA "TestPizza"))
        ]
       (assertTrue (areEqual createdCls1 newCls1))
       (assertTrue (areEqual createdCls2 newCls2))
       (assertTrue (areEqual createdCls2 newCls3))
    ))

;public EntityData createInstance(String projectName, String instName, String typeName, String user,
;            String operationDescription) {
(defn -testCreateInstance [this]
  (let [  
        newIns1     (EntityData. (uri PIZZA "TestConcept") "TestConcept")         
        createdIns1 (.createInstance projects "Pizza" (uri PIZZA "TestConcept") (uri PIZZA "DomainConcept") "dilvan" "Test createIns")
        newIns2      (.getEntity projects "Pizza" (uri PIZZA "TestConcept"))
        ]
       (assertTrue (areEqual createdIns1 newIns1))
       (assertTrue (areEqual createdIns1 newIns2))
    ))

;  public EntityData createDatatypeProperty(String projectName, String propertyName, String superPropName,
;            String user, String operationDescription) {
(defn -testCreateDatatypeProperty [this]
  (let [  
        newProp1      (PropertyEntityData. (uri PIZZA "TestDatatypeProp") "TestDatatypeProp" PropertyType/DATATYPE)         
        createdProp1 (.createDatatypeProperty projects "Pizza" "TestDatatypeProp" nil "dilvan" "Test createDatatypeProperty")
        ]
       (assertTrue (areEqual createdProp1 newProp1))
       (.deleteEntity projects "Pizza" (uri PIZZA "TestDatatypeProp") "dilvan" "Test createDatatypeProperty")
    ))

;  public EntityData createObjectProperty(String projectName, String propertyName, String superPropName,
;            String user, String operationDescription) {
(defn -testCreateObjectProperty [this]
  (let [  
        newProp1      (PropertyEntityData. (uri PIZZA "TestObjectProp") "TestObjectProp" PropertyType/OBJECT)         
        createdProp1  (.createObjectProperty projects "Pizza" "TestObjectProp" nil "dilvan" "Test createObjectProperty")
        newProp2      (PropertyEntityData. (uri PIZZA "TestObjectSubprop") "TestObjectSubprop" PropertyType/OBJECT)         
        createdProp2  (.createObjectProperty projects "Pizza" "TestObjectSubprop" (uri PIZZA "hasBase") "dilvan" "Test createObjectSubproperty")
        ]
       (assertTrue (areEqual createdProp1 newProp1))
       (assertTrue (areEqual createdProp2 newProp2))
    ))

;  public EntityData createAnnotationProperty(String projectName, String propertyName, String superPropName,
;            String user, String operationDescription) {
(defn -testCreateAnnotationProperty [this]
  (let [  
        newProp1      (PropertyEntityData. (uri PIZZA "TestAnnotationProp") "TestAnnotationProp" PropertyType/ANNOTATION)         
        createdProp1  (.createAnnotationProperty projects "Pizza" "TestAnnotationProp" nil "dilvan" "Test createAnnotationProperty")
        newProp2      (PropertyEntityData. (uri PIZZA "TestAnnotationSubprop") "TestAnnotationSubprop" PropertyType/ANNOTATION)         
        createdProp2  (.createAnnotationProperty projects "Pizza" "TestAnnotationSubprop" (uri PIZZA "TestAnnotationProp") "dilvan" "Test createAnnotationSubproperty")
        ]
       (assertTrue (areEqual createdProp1 newProp1))
       (assertTrue (areEqual createdProp2 newProp2))
    ))

;public void addPropertyValue(String projectName, String entityName, PropertyEntityData propertyEntity,
;            EntityData valueEntityData, String user, String operationDescription) {
(defn -testAddPropertyValue [this]
  (let [  
        newValue      (EntityData. "test string" "test string") 

        datatypeProp  (.createDatatypeProperty projects "Pizza" (uri PIZZA "TestDatatypeProp") nil "dilvan" "Test createDatatypeProperty")        
        pizzaInd      (.createInstance projects "Pizza" (uri PIZZA "PizzaInd") (uri PIZZA "Pizza") "dilvan" "Test createIns")
        
        triple      (Triple. pizzaInd datatypeProp newValue)
        ]
    
        ;If I don' add the following line I get (not very useful message):
        ;SEVERE: Could not create instance http://www.co-ode.org/ontologies/pizza/2005/10/18/pizza.owl#PizzaInd of type http://www.co-ode.org/ontologies/pizza/2005/10/18/pizza.owl#Pizza
        ;-- java.lang.IllegalArgumentException: http://www.co-ode.org/ontologies/pizza/2005/10/18/pizza.owl#PizzaInd not unique
        (.setValueType newValue ValueType/String)
        
        (.addPropertyValue projects "Pizza" (uri PIZZA "PizzaInd") datatypeProp newValue "dilvan" "Test addPropertyValue")

        (let [ret (.getEntityTriples projects "Pizza" (uri PIZZA "PizzaInd"))]
           (.setValueType newValue nil)
;           (.setValueType datatypeProp ValueType/Any)
           (.setPropertyType datatypeProp nil)

           (assertTrue (hasEqualInList ret  triple))
       )        
       (.deleteEntity projects "Pizza" (uri PIZZA "TestDatatypeProp") "dilvan" "Test deleteEntity")
       (.deleteEntity projects "Pizza" (uri PIZZA "PizzaInd") "dilvan" "Test deleteEntity")
    ))

;    public void removePropertyValue(String projectName, String entityName, PropertyEntityData propertyEntity,
;            EntityData valueEntityData, String user, String operationDescription) {
(defn -testRemovePropertyValue [this]
  (let [  
        newValue    (EntityData. "test string" "test string") 

        datatypeProp  (.createDatatypeProperty projects "Pizza" (uri PIZZA "TestDataProp") nil "dilvan" "Test createDatatypeProperty")        
        pizzaInd      (.createInstance projects "Pizza" (uri PIZZA "PizzaInd") (uri PIZZA "Pizza") "dilvan" "Test createIns")
        
        triple      (Triple. pizzaInd datatypeProp newValue)
        ]
        (.setValueType newValue ValueType/String)
        
;        (.setValueType datatypeProp ValueType/Any)
        (.setPropertyType datatypeProp nil)
                
        (.addPropertyValue projects "Pizza" (uri PIZZA "PizzaInd") datatypeProp newValue "dilvan" "Test removePropertyValue")
        
        (.setValueType newValue nil)
        
        (let [ret (.getEntityTriples projects "Pizza" (uri PIZZA "PizzaInd"))]
           (assertTrue (hasEqualInList ret  triple))
        )
                
        (.removePropertyValue projects "Pizza" (uri PIZZA "PizzaInd") datatypeProp newValue "dilvan" "Test femovePropertyValue")

        (let [ret (.getEntityTriples projects "Pizza" (uri PIZZA "PizzaInd"))]
           (assertTrue (or (= ret nil) (not (hasEqualInList ret  triple))))
        )
        (.deleteEntity projects "Pizza" (uri PIZZA "TestDataProp") "dilvan" "Test deleteEntity")
        (.deleteEntity projects "Pizza" (uri PIZZA "PizzaInd") "dilvan" "Test deleteEntity")

    ))

;public void replacePropertyValue(String projectName, String entityName, PropertyEntityData propertyEntity,
;            EntityData oldValue, EntityData newValue, String user, String operationDescription) {
(defn -testReplacePropertyValue [this]
  (let [  
        oldValue    (EntityData. "" "") 
        newValue    (EntityData. "test string" "test string") 
        newerValue  (EntityData. "new test string" "new test string") 

        datatypeProp  (.createDatatypeProperty projects "Pizza" (uri PIZZA "TestDatatypeProp") nil "dilvan" "Test createDatatypeProperty")        
        pizzaInd      (.createInstance projects "Pizza" (uri PIZZA "PizzaInd") (uri PIZZA "Pizza") "dilvan" "Test createIns")
        
        ]
     (.setValueType oldValue ValueType/String)
     (.setValueType newValue ValueType/String)
     (.setValueType newerValue ValueType/String)
        
;     (.setValueType datatypeProp ValueType/Any)
     (.setPropertyType datatypeProp nil)

     ;It can replace a no existing value to an existing one
     (.replacePropertyValue projects "Pizza" (uri PIZZA "PizzaInd") datatypeProp oldValue newValue "dilvan" "Test replacePropertyValue")

     (let [ret (.getEntityTriples projects "Pizza" (uri PIZZA "PizzaInd"))
           triple      (Triple. pizzaInd datatypeProp newValue)
           ]
           (.setValueType newValue nil)
           (assertTrue (hasEqualInList ret  triple))
     )
     
     (.replacePropertyValue projects "Pizza" (uri PIZZA "PizzaInd") datatypeProp newValue newerValue "dilvan" "Test replacePropertyValue")

     (let [
           ret (.getEntityTriples projects "Pizza" (uri PIZZA "PizzaInd"))        
           triple      (Triple. pizzaInd datatypeProp newerValue)
          ]
          (.setValueType newerValue nil)
          (assertTrue (hasEqualInList ret  triple))
     )

     (.deleteEntity projects "Pizza" (uri PIZZA "PizzaInd") "dilvan" "Test deleteEntity")
     (.deleteEntity projects "Pizza" (uri PIZZA "TestDatatypeProp") "dilvan" "Test deleteEntity")
    ))

; public void deleteEntity(String projectName, String entityName, String user, String operationDescription) {
(defn -testDeleteEntity [this]
  (let []
       (.deleteEntity projects "Pizza" (uri PIZZA "Test") "dilvan" "Test deleteEntity")
       (assertTrue (= (.getEntity projects "Pizza" (uri PIZZA "Test")) nil))

       (.deleteEntity projects "Pizza" (uri PIZZA "TestPizza") "dilvan" "Test deleteEntity")
       (assertTrue (= (.getEntity projects "Pizza" (uri PIZZA "TestPizza")) nil))

       (.deleteEntity projects "Pizza" (uri PIZZA "TestConcept") "dilvan" "Test deleteEntity")
       (assertTrue (= (.getEntity projects "Pizza" (uri PIZZA "TestConcept")) nil))
))

