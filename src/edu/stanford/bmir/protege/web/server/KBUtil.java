package edu.stanford.bmir.protege.web.server;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.stanford.bmir.protege.web.client.ui.login.constants.AuthenticationConstants;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.server.Session;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLHasValue;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;

public class KBUtil {

    public static final String PROPERTY_IS_TEMPLATE = "http://protege.stanford.edu/plugins/owl/protege#isTemplate";
    public static final Map<KnowledgeBase, Slot> isTemplateSlotMap = new HashMap<KnowledgeBase, Slot>();

    
    // Going through a lot of pains to get the cls, when using Frames methods,
    // if a short name is used..
    public static Cls getCls(KnowledgeBase kb, String clsName) {
    	Cls cls = kb.getCls(clsName);
    	if (cls == null && kb instanceof OWLModel) {
    		RDFResource res = ((OWLModel)kb).getRDFResource(clsName);
    		if (res != null && res instanceof Cls) {
    			cls = (Cls) res;
    		}
    	}
    	
    	return cls;
    }
    
    
    public static void morphUser(KnowledgeBase kb, String user) {
        if (kb.getProject().isMultiUserClient()) {
            Session s = (Session) RemoteClientFrameStore.getCurrentSession(kb);
            s.setDelegate(user);
        } else {
            ((DefaultKnowledgeBase) kb).setUserName(user);
        }
    }

    public static void restoreUser(KnowledgeBase kb) {
        String defaultUser = ApplicationProperties.getProtegeServerUser();
        if (kb.getProject().isMultiUserClient()) {
            Session s = (Session) RemoteClientFrameStore.getCurrentSession(kb);
            s.setDelegate(null);
        } else {
            ((DefaultKnowledgeBase) kb).setUserName(defaultUser);
        }
    }

    public static boolean shouldRunInTransaction(String operationDescription) {
        return operationDescription != null && operationDescription.length() > 0;
    }

    @SuppressWarnings("unchecked")
    public static <X> Collection<X> getCollection(KnowledgeBase kb, Collection<String> names, Class<? extends X> javaInterface) {
        Collection<X> entities = new HashSet<X>();
        if (names == null) {
            return entities;
        }
        for (String name : names) {
            Frame frame = kb.getFrame(name);
            if (frame != null && javaInterface.isAssignableFrom(frame.getClass())) {
                entities.add((X)frame);
            }
        }
        return entities;
    }

    //TODO: not the best util class for this method.. find a better one
    public static String getUserInSession(HttpServletRequest request) {
        final HttpSession session = request.getSession();

        return (String) session.getAttribute(AuthenticationConstants.USER);
    }

    public static String getRemoteProjectName(Project prj) {
        URI uri = prj.getProjectURI();
        if (uri == null) { return null; }
        try {
            String path = uri.getPath();
            int index = path.lastIndexOf("/");
            if (index > -1) {
                return path.substring(index + 1);
            }
        } catch (Exception e) {
            Log.emptyCatchBlock(e);
            //do nothing
        }
        return null;
    }


    /**
     * This utility method returns a path between a class and its superclass,
     * in REVERSE order, i.e. the path will start with the superclass and will
     * end with the subclass. <BR>
     * <BR>
     * <B>Note:</B>
     * <ul>
     * <li>If there are multiple paths between the class and its superclass this
     *      method will return only one of them (not necessarily the shortest one).
     * <li>If there is no path between the class and its supposed superclass
     *      (i.e. the second argument is not a superclass of the first argument)
     *      the method will return a list containing only the class itself.
     * <li>If both the <code>srcClass</code> and <code>dstSuperclass</code>
     *      refer to the same class, and there is a cycle involving that class
     *      this method will return one of the existing cycles.
     * </ul>
     * @param srcClass a class
     * @param dstSuperclass a superclass of the first argument
     * @param path the result of this method will be collected in the path variable
     */
    public static void getPathToSuperClass(Cls srcClass, Cls dstSuperclass, ArrayList<Cls> path) {
        //in the special case src == dst add the source to the path only at the end of the algorithm,
        //after the recursive calls are over, to not interfere with the cycle check
        if (! srcClass.equals(dstSuperclass)) {
            path.add(0, srcClass);
        }

        for (Iterator<?> it = srcClass.getDirectSuperclasses().iterator(); it.hasNext(); ) {
            Object po = it.next();
            if (po instanceof Cls) {
                Cls p = (Cls) po;
                if (p.equals(dstSuperclass)) {
                    path.add(0, dstSuperclass);
                    break;
                }
                else {
                    //if we have a parent that could lead us to the destination, and is not creating a cycle in the path
                    if (p.getSuperclasses().contains(dstSuperclass) && !path.contains(p)) {
                        getPathToSuperClass(p, dstSuperclass, path);
                        if (path.contains(dstSuperclass)) {
                            break;
                        }
                        else {
                            //this parent will not bring us to the destination
                            if (!path.isEmpty() && path.get(0).equals(p)) {
                                path.remove(0);
                            }
                            else {
                                Log.getLogger().log(Level.SEVERE,
                                        "Assertion failure: something must be wrong with the getPathToSuperClass algorithm. " +
                                        "The first element in the path " + path + " is expected to be: " + p);
                            }
                        }
                    }
                }
            }
            else {
                Log.getLogger().log(Level.WARNING,
                        "Invalid type of " + po + " (superclass of " + srcClass + "). Excpected type is 'Cls'");
            }
        }

        //treat special case src == dst
        if (srcClass.equals(dstSuperclass)) {
            path.add(srcClass);
        }

    }

    public static void getPathToSuperClass(OWLClass srcClass, OWLClass dstSuperclass, ArrayList<OWLClass> path) {
        //in the special case src == dst add the source to the path only at the end of the algorithm,
        //after the recursive calls are over, to not interfere with the cycle check
        if (! srcClass.equals(dstSuperclass)) {
            path.add(0, srcClass);
        }

        for (Iterator<?> it = srcClass.getSuperclasses(false).iterator(); it.hasNext(); ) {
            Object po = it.next();
            if (po instanceof OWLNamedClass) {
                OWLNamedClass p = (OWLNamedClass) po;
                if (p.equals(dstSuperclass)) {
                    path.add(0, dstSuperclass);
                    break;
                }
                else {
                    //if we have a parent that could lead us to the destination, and is not creating a cycle in the path
                    if (p.getSuperclasses(true).contains(dstSuperclass) && !path.contains(p)) {
                        getPathToSuperClass(p, dstSuperclass, path);
                        if (path.contains(dstSuperclass)) {
                            break;
                        }
                        else {
                            //this parent will not bring us to the destination
                            if (!path.isEmpty() && path.get(0).equals(p)) {
                                path.remove(0);
                            }
                            else {
                                Log.getLogger().log(Level.SEVERE,
                                        "Assertion failure: something must be wrong with the getPathToSuperClass algorithm. " +
                                        "The first element in the path " + path + " is expected to be: " + p);
                            }
                        }
                    }
                }
            }
            else {
//                Log.getLogger().log(Level.WARNING,
//                        "Invalid type of " + po + " (superclass of " + srcClass + "). Excpected type is 'OWLNamedClass'");
            }
        }

        //treat special case src == dst
        if (srcClass.equals(dstSuperclass)) {
            path.add(srcClass);
        }

    }
    
    
    //******* Template class/instance operations
    
	public static boolean isTemplateInstance(Object value) {
		if (value instanceof Instance) {
			Instance instValue = (Instance) value;;
			Slot isTemplateSlot = getIsTemplateSlot(instValue);
			if (isTemplateSlot != null) {
				Collection<?> isTemplateValues = getOwnSlotAndSubslotValues(instValue, isTemplateSlot);
				Object isTemplateValue = (isTemplateValues == null || isTemplateValues.isEmpty() ? 
						null : 
						isTemplateValues.iterator().next());
// this nice logic does not always work, because value is sometimes String :(
//				return (isTemplateValue instanceof Boolean && 
//						((Boolean)isTemplateValue).booleanValue() == true);
				return isTemplateValue != null && isTemplateValue.toString().toLowerCase().equals("true");
			}
		}
		return false;
	}

	private static Collection<?> getOwnSlotAndSubslotValues(Instance inst, Slot isTemplateSlot) {
		//This method implements what 
		//	instValue.getOwnSlotAndSubslotValues(isTemplateSlot);
		//suppose to implement, but unfortunately does not
		ArrayList<Object> res = new ArrayList<Object>();
		Collection<?> slotValues = inst.getOwnSlotValues(isTemplateSlot);
		if (slotValues != null) {
			res.addAll(0, slotValues);
		}
		for (Object subslot : isTemplateSlot.getSubslots()) {
			slotValues = inst.getOwnSlotValues((Slot) subslot);
			if (slotValues != null) {
				res.addAll(0, slotValues);
			}
		}
		return res;
	}
	
	public static Object getCopyOfTemplateInstance(Object value) {
    	Exception e = null;
    	try {
			if (value instanceof Instance) {
				Instance instValue = (Instance) value;
				KnowledgeBase kb = instValue.getKnowledgeBase();
				Cls nonTemplateType = getNonTemplateType(instValue);
				if (nonTemplateType != null) {
					Instance copyInst = kb.createInstance(null, nonTemplateType);
					copyPropertyValues(instValue, copyInst);
					return copyInst;
				}
			}
    	}
    	catch (Exception caught) {
    		e = caught;
    	}
        Log.getLogger().log(
                Level.WARNING,
                "Failed to create a copy of the template value: " + value, e);
		return value;
	}

	private static void copyPropertyValues(Instance fromInst, Instance toInst) {
		// TODO Auto-generated method stub
		Collection<Slot> slots = fromInst.getOwnSlots();;
		Slot isTemplateSlot = getIsTemplateSlot(fromInst);
		for (Slot slot : slots) {
			if (! isTemplate(slot, isTemplateSlot) && ! slot.isSystem()) {
				Collection<?> values = fromInst.getOwnSlotValues(slot);
				Collection<Object> newValues = new ArrayList<Object>();
				for (Object val : values) {
					if (isTemplateInstance(val)) {
						System.out.print("copying templ. instance: " + val + "...");
						Object newVal = getCopyOfTemplateInstance(val);
						System.out.println(" DONE");
						newValues.add(newVal);
					}
					else {
						newValues.add(val);
					}
				}
				toInst.setOwnSlotValues(slot, newValues);
			}
		}
	}

	private static Slot getIsTemplateSlot(Instance inst) {
		KnowledgeBase kb = inst.getKnowledgeBase();
		if (isTemplateSlotMap.keySet().contains(kb)) {
			return isTemplateSlotMap.get(kb);
		}
		else {
			Slot isTemplateSlot = kb.getSlot(PROPERTY_IS_TEMPLATE);
			isTemplateSlotMap.put(kb, isTemplateSlot);
			return isTemplateSlot;
		}
	}
	
	private static boolean isTemplate(Slot slot, Slot isTemplateSlot) {
		return slot.equals(isTemplateSlot) ||
				slot.getSuperslots().contains(isTemplateSlot);
	}
	
	/**
	 * If instance has multiple types it returns the first non-template class.
	 * If all types are template classes it return the
	 * non-template superclass of the first template class.
	 * 
	 * @param instValue
	 * @return
	 */
	private static Cls getNonTemplateType(Instance instValue) {
		Collection<?> directTypes = instValue.getDirectTypes();
		Cls firstTemplateType = null;
		Slot isTemplateSlot = getIsTemplateSlot(instValue);
		for (Object object : directTypes) {
			Cls type = (Cls) object;

			if (isTemplateClass(type, isTemplateSlot)) {
				if (firstTemplateType == null) {
					firstTemplateType = type;
				}
			}
			else {
				return type;
			}
		}

		return getFirstNonTemplateSuperclass(firstTemplateType);
	}

	private static boolean isTemplateClass(Cls type, Slot isTemplateSlot) {
		if (type instanceof OWLClass) {
			OWLClass owlClass = (OWLClass)type;
			//check for annotation property values
			Object isTemplateAnnPropValue = owlClass.getPropertyValue((RDFProperty)isTemplateSlot);
			if (isTemplateAnnPropValue instanceof Boolean &&
					((Boolean)isTemplateAnnPropValue).booleanValue() == true) {
				return true;
			}
			//check for class expression "protege:isTemplate value true"
			Collection<?> superclassExpressions = owlClass.getSuperclasses(true);
			for (Object object : superclassExpressions) {
				if (object instanceof OWLClass) {
					OWLClass superclassExpr = (OWLClass) object;
					if (superclassExpr instanceof OWLHasValue) {
						OWLHasValue hasValueExpr = (OWLHasValue) superclassExpr;
						Slot hasValueSlot = hasValueExpr.getOnProperty();
						if (isTemplate(hasValueSlot, isTemplateSlot) &&
								hasValueExpr.getHasValue().equals(Boolean.TRUE)) {
							return true;
						}
					}
				}
			}
		}
		else {
			//TODO this is a stub
			type.getOwnSlotAndSubslotValues(isTemplateSlot);
			assert false : "The code is currently not ready to deal with template classes "
					+ "in Protege-Frames ontologies";
		}
		return false;
	}

	private static Cls getFirstNonTemplateSuperclass(Cls type) {
		Collection<Cls> directSuperclasses = type.getDirectSuperclasses();
		Slot isTemplateSlot = getIsTemplateSlot(type);
		for (Cls superclass : directSuperclasses) {
			if ( ! isTemplateClass(superclass, isTemplateSlot)) {
				return superclass;
			}
		}
		return getFirstNonTemplateSuperclass(directSuperclasses.iterator().next());
	}

}
