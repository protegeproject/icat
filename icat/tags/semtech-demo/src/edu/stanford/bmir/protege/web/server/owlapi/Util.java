package edu.stanford.bmir.protege.web.server.owlapi;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.IRIShortFormProvider;
import org.semanticweb.owlapi.util.OWLObjectVisitorAdapter;
import org.semanticweb.owlapi.util.OntologyIRIShortFormProvider;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.ImportsData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyType;
import edu.stanford.bmir.protege.web.client.rpc.data.Triple;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;

public class Util {

	final static public String RDFS= "http://www.w3.org/2000/01/rdf-schema#";
	final static public String OWL=   "http://www.w3.org/2002/07/owl#";
	final static public String PROTEGE= "http://protege.stanford.edu/plugins/owl/protege#";

	public static Comparator<OWLObject> getOWLObjectComparator() {
		return new Comparator<OWLObject>(){

			public int compare(OWLObject o1, OWLObject o2) {
				return Util.getBrowserText(o1).compareTo(Util.getBrowserText(o2));
			}

		};
	}

	public static EntityData createEntityData(OWLObject object, boolean annot) {
		return createEntityData(object);
	}

	/**
	 * For the time being no annotations in OWLAPI
	 */
	public static EntityData createEntityData(OWLObject object) {
		if (object == null) {
            return null;
        }

		if (OWLEntity.class.isAssignableFrom(object.getClass())) {

			//	It needs to include annotation properties
			OWLEntity entity= (OWLEntity) object;
			if (OWLProperty.class.isAssignableFrom(entity.getClass()) || OWLAnnotationProperty.class.isAssignableFrom(entity.getClass())) {
				PropertyEntityData entityProp;
				entityProp= new PropertyEntityData(entity.getIRI().toURI().toString(), getBrowserText(entity));
				//					(computeAnnotations) ? ChAOUtil.hasAnnotations(objFrame) : false);
				entityProp.setPropertyType(getPropertyType(entity));
				return entityProp;
			}



//			if (entity.isOWLDatatype() && entity.asOWLDatatype().isString()) {
//				return new EntityData(entity.asOWLDatatype().toStringID(), getBrowserText(entity));
//			}



			return new EntityData(entity.getIRI().toURI().toString(), getBrowserText(entity));
			//						(computeAnnotations) ? ChAOUtil.hasAnnotations(objFrame) : false);
		}
        if (OWLLiteral.class.isAssignableFrom(object.getClass())  ){
            OWLLiteral literal = (OWLLiteral) object;
               PropertyEntityData entityProp= new PropertyEntityData(literal.getLiteral(), getBrowserText(object));
            entityProp.setPropertyType(PropertyType.DATATYPE);
                if(literal.isOWLTypedLiteral()){
                    entityProp.setValueType(ValueType.TypedLiteral);
                }          
                if(literal.isOWLStringLiteral()){
                    if (((OWLStringLiteral)object).getLang()!=null){
                        entityProp.setValueType(ValueType.StringLiteralWithLanguage);
                        entityProp.setLanguage(((OWLStringLiteral)object).getLang());
                    } else {
                        entityProp.setValueType(ValueType.StringLiteral);
                    }
                }
				return entityProp;
            }



		// TODO take out this hack to make compatible with web-protege language tag

		return new EntityData(fixLanguageTagBug(object.toString()));
	}

	private static String fixLanguageTagBug(String str){
		int length= str.length();
		if (str.lastIndexOf("\"@") == length-4) {
			return "~#" + str.substring(length-2, length) + " " + str.substring(1, length-4);
		}
		return str;
	}

	public static PropertyEntityData createPropertyEntityData(OWLOntology onto, OWLEntity property, boolean computeAnnotations) {
		if (property == null) {
            return null;
        }

		PropertyEntityData ped = new PropertyEntityData(property.toStringID(), getBrowserText(property));
        ped.setValueType(ValueType.String);
//				null, computeAnnotations ? ChAOUtil.hasAnnotations(property) : false);

//		ped.setMinCardinality(cls == null ?
//				property.getMinimumCardinality() :
//					cls.getTemplateSlotMinimumCardinality(property));
//		ped.setMaxCardinality(cls == null ?
//				property.getMaximumCardinality() :
//					cls.getTemplateSlotMaximumCardinality(property));


		//	Annotation properties
		//


		//	Data properties
		//	It takes just the first datatype in range

		if (property.isOWLDataProperty()) {
            ped.setPropertyType(PropertyType.DATATYPE);

	 		OWLDataProperty dataProp= property.asOWLDataProperty();

	 		//	Just take the first one
	 		if (dataProp.getRanges(onto).iterator().hasNext()) {

	 			//	Considering that only built-in data types are allowed
	 			OWL2Datatype range= dataProp.getRanges(onto).iterator().next().asOWLDatatype().getBuiltInDatatype();


	 			if(range == OWL2Datatype.XSD_STRING) {
	 				ped.setValueType(ValueType.String);
	 			} else if (range == OWL2Datatype.XSD_BOOLEAN) {
	 				ped.setValueType(ValueType.Boolean);

	 				//	} else if (property.getValueType() == ValueType.INSTANCE || property.getValueType() == ValueType.CLS) {
	 				//		ped.setValueType(edu.stanford.bmir.protege.web.client.rpc.data.ValueType.Instance);
	 				//		ped.setAllowedValues(createEntityList((List)property.getAllowedClases())); //TODO: fix me
	 				//	} else if (property.getValueType() == ValueType.SYMBOL) {
	 				//		ped.setValueType(edu.stanford.bmir.protege.web.client.rpc.data.ValueType.Symbol);
	 				//		ped.setAllowedValues(createEntityList((List<Object>) property.getAllowedValues()));
	 			} else {
	 				ped.setValueType(ValueType.Any);
	 			}
	 		}
	 	}

	 	//	Object Properties
	 	//	Load all classes in range, regardless of what role they have in the range class expression

		else if (property.isOWLObjectProperty()) {
            ped.setPropertyType(PropertyType.OBJECT);
			List<EntityData> edList = new ArrayList<EntityData>();

	 		for (OWLClassExpression cls : property.asOWLObjectProperty().getRanges(onto)) {
	 			edList.addAll(createEntityList(cls.getClassesInSignature()));
	 		}
	 		ped.setValueType(edu.stanford.bmir.protege.web.client.rpc.data.ValueType.Instance);
	 		ped.setAllowedValues(edList); //TODO: fix me
	 	} else {
            ped.setPropertyType(PropertyType.ANNOTATION);
        }

		return ped;
	}

	/*
	public static PropertyEntityData createPropertyEntityData(OWLProperty property, boolean computeAnnotations) {
		if (property == null) return null;

		PropertyEntityData ped = new PropertyEntityData(property.getURI().toString(), getBrowserText(property.getURI()), null, false);
//				null, computeAnnotations ? ChAOUtil.hasAnnotations(property) : false);
//		ped.setMinCardinality(property..get.getMinimumCardinality());
//					cls.getTemplateSlotMinimumCardinality(property));
//		ped.setMaxCardinality(cls == null ?
//				property.getMaximumCardinality() :
//					cls.getTemplateSlotMaximumCardinality(property));

//		OWLOntology onto=null;
//		for (OWLDescription desc : property.getDomains(onto)) {
//			if (!desc.isAnonymous()) {
//
//			}
//		}


		return ped;
	}
	*/
//	static Collection<Triple> getTriples(OWLEntity inst, OWLAnnotation slot) {
//		return getTriples(inst, slot, inst.getOwnSlotValues(slot));
//	}

//	static Collection<Triple> getTriples(OWLEntity inst, Slot slot, Collection values) {
//		ArrayList<Triple> triples = new ArrayList<Triple>();
//
//		for (Object object : values) {
//			triples.add(createTriple(inst, slot, object));
//		}
//
//		return triples;
//	}

	static Triple createTriple(OWLOntology onto, OWLEntity instance, OWLEntity slot, OWLObject object) {
		EntityData subj = createEntityData(instance);
		PropertyEntityData pred = createPropertyEntityData(onto, slot, false);
		EntityData obj = createEntityData(object, false);
		return new Triple(subj, pred, obj);
	}

	public static ArrayList<EntityData> createEntityList(Collection<? extends OWLEntity> list) {
		ArrayList<EntityData> edList = new ArrayList<EntityData>();
		for (OWLEntity entity : list) {
			edList.add(createEntityData(entity));
		}
		return edList;
	}

	public static PropertyType getPropertyType(OWLEntity entity) {
		if (OWLObjectProperty.class.isAssignableFrom(entity.getClass())) {
			return PropertyType.OBJECT;
		} else if (OWLDataProperty.class.isAssignableFrom(entity.getClass())) {
			return PropertyType.DATATYPE;
		} else if (OWLAnnotationProperty.class.isAssignableFrom(entity.getClass())) {
			return PropertyType.ANNOTATION;
		}
		return null;
	}

	static public String getBrowserText(OWLObject obj){
		Expression exp= new Expression();

		exp.getBrowserText(obj);
		return exp.getExpression();
	}

	static public ImportsData copyOWLTree(ImportsData id, OWLOntology ontology) {
		//ImportsData id = data;
		//id.setName(ontology.getOntologyID().getOntologyIRI().toURI().toString());

		for (OWLOntology childOnt : ontology.getImports()) {
			ImportsData childID = new ImportsData(childOnt.getOntologyID().getOntologyIRI().toURI().toString());
			//	OWL 2 getImports gets all imported ontologies, if A imports B that imports C, it will return B and C
			childID = copyOWLTree(childID, childOnt);
			id.addImport(childID);
		}

		return id;
	}
}

/**
 * Class to render OWL objects, may be expanded to render everything OWL to a format
 * that can be sent to the editor to a widget that would allow assisted unrestricted
 * edition. The edition result can be sent back, parsed and changes be made to the
 * ontology.
 *
 * There is a claa that does the parsing, maybe there is another for the writing.
 *
 * @author dilvan
 *
 */
class Expression extends OWLObjectVisitorAdapter {
	StringBuffer exp= new StringBuffer();

	public String getExpression() { return exp.toString();}

    @Override
    public void visit(OWLStringLiteral node) {
        exp.append(node.getLiteral());
    }

    @Override
    public void visit(OWLTypedLiteral node) {
        exp.append(node.getLiteral());
    }

    @Override
    public void visit(OWLAnonymousIndividual owlInd){
		exp.append(owlInd.toStringID());
	}

	@Override
    public void visit(OWLObjectUnionOf union) {
		exp.append("(UnionOf ");
		for(OWLClassExpression cls : union.getOperands() ) {
			exp.append(" ");
			getBrowserText(cls);
		}
		exp.append(")");
	}

	@Override
    public void visit(OWLObjectSomeValuesFrom some) {
//		exp.append("(");
		getBrowserText(some.getProperty());
		exp.append(" some ");
		getBrowserText(some.getFiller());
//		exp.append(")");
	}

	public void getBrowserText(OWLObject obj) {
		if (obj instanceof OWLNamedObject) {
			getBrowserText(((OWLNamedObject) obj).getIRI());
		}
		obj.accept(this);
	}

	public void getBrowserText(IRI uri) {
		if (uri.toString().startsWith(Util.RDFS)) {
			exp.append("rdfs:" + uri.toString().substring(Util.RDFS.length(), uri.toString().length()));
			return;
		}
		if (uri.toString().startsWith(Util.OWL)) {
			exp.append("owl:" + uri.toString().substring(Util.OWL.length(), uri.toString().length()));
			return;
		}
		if (uri.toString().startsWith(Util.PROTEGE)) {
			exp.append("protege:" + uri.toString().substring(Util.PROTEGE.length(), uri.toString().length()));
			return;
		}
		IRIShortFormProvider uriShortFormProvider = new SimpleIRIShortFormProvider();
        String shortForm = uriShortFormProvider.getShortForm(uri);
        if (shortForm.contains("<")){
            shortForm = shortForm.replaceAll("<", "");
        }
        if (shortForm.contains(">")){
            shortForm = shortForm.replaceAll(">", "");
        }
        exp.append(shortForm);

	}
}
