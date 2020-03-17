package edu.stanford.bmir.protege.web.server.bioportal;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.ncbo.stanford.bean.concept.ClassBean;
import org.ncbo.stanford.bean.ontology.OntologyBean;
import org.ncbo.stanford.util.BioPortalServerConstants;
import org.ncbo.stanford.util.BioPortalUtil;
import org.ncbo.stanford.util.BioPortalViewOntologyMap;
import org.ncbo.stanford.util.BioportalConcept;
import org.ncbo.stanford.util.HTMLUtil;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.stanford.bmir.protege.web.client.rpc.BioPortalAccess;
import edu.stanford.bmir.protege.web.client.rpc.data.BioPortalReferenceData;
import edu.stanford.bmir.protege.web.client.rpc.data.BioPortalSearchData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.server.WebProtegeKBUtil;
import edu.stanford.bmir.protege.web.server.OntologyServiceImpl;
import edu.stanford.bmir.protege.web.server.ProjectManagerFactory;
import edu.stanford.bmir.protege.web.server.URLUtil;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;


public class BioPortalAccessImpl extends RemoteServiceServlet implements BioPortalAccess {

    private static HashMap<String, BioPortalViewOntologyMap> bpViewOntologyMaps = new HashMap<String, BioPortalViewOntologyMap>();

    protected Project getProject(String projectName) {
        return ProjectManagerFactory.getProtege3ProjectManager().getProject(projectName);
    }

    public String getBioPortalSearchContent(String projectName, String entityName, BioPortalSearchData bpSearchData) {
        Project project = getProject(projectName);
        KnowledgeBase kb = project.getKnowledgeBase();
        Frame frame = kb.getFrame(entityName);
        if (frame == null) {
            return URLUtil.getURLContent(getBioPortalSearchUrl(entityName, bpSearchData));
        }

        return "";
    }

    public String getBioPortalSearchContentDetails(String projectName, BioPortalSearchData bpSearchData,
            BioPortalReferenceData bpRefData) {
        BioportalConcept bpc = new BioportalConcept();
        String encodedConceptId = bpRefData.getConceptId();
        try {
            encodedConceptId = URLEncoder.encode(bpRefData.getConceptId(), "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            Log.getLogger().log(Level.WARNING, "Error at encoding BP search url", e1);
        }
        String urlString = bpSearchData.getBpRestBaseUrl() + BioPortalServerConstants.CONCEPTS_REST + "/"
        + bpRefData.getOntologyVersionId() + "/?conceptid=" + encodedConceptId;
        urlString = BioPortalUtil.addRestCallSuffixToUrl(urlString, bpSearchData.getBpRestCallSuffix());
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            Log.getLogger().log(Level.WARNING, "Invalid BP search URL: " + urlString, e);
        }
        if (url == null) {
            return "";
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("<html><body>");
        buffer.append("<table width=\"100%\" class=\"servicesT\" style=\"border-collapse:collapse;border-width:0px;padding:5px\"><tr>");

        buffer.append("<td class=\"servHd\" style=\"background-color:#8E798D;color:#FFFFFF;\">Property</td>");
        buffer.append("<td class=\"servHd\" style=\"background-color:#8E798D;color:#FFFFFF;\">Value</td>");

        String oddColor = "#F4F2F3";
        String evenColor = "#E6E6E5";

        ClassBean cb = bpc.getConceptProperties(url);
        if (cb == null) {
            return "<html><body><i>Details could not be retrieved.</i></body></html>";
        }

        Map<Object, Object> relationsMap = cb.getRelations();
        int i = 0;
        for (Object obj : relationsMap.keySet()) {
            Object value = relationsMap.get(obj);
            if (value != null) {
                String text = HTMLUtil.replaceEOF(ProtegeUtil.getDisplayText(value));
                if (text.startsWith("[")) {
                    text = text.substring(1, text.length() - 1);
                }
                if (text.length() > 0) {
                    String color = i % 2 == 0 ? evenColor : oddColor;
                    buffer.append("<tr>");
                    buffer.append("<td class=\"servBodL\" style=\"background-color:" + color + ";padding:7px;font-weight: bold;\" >");
                    buffer.append(ProtegeUtil.getDisplayText(obj));
                    buffer.append("</td>");
                    buffer.append("<td class=\"servBodL\" style=\"background-color:" + color + ";padding:7px;\" >");
                    buffer.append(text);
                    buffer.append("</td>");
                    buffer.append("</tr>");
                    i++;
                }
            }
        }
        buffer.append("</table>");

        String directLink = bpRefData.getBpUrl();
        if (directLink != null && directLink.length() > 0) {
            buffer.append("<div style=\"padding:5px;\"><br><b>Direct link in BioPortal:</b> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
            buffer.append("<a href=\"");
            buffer.append(directLink);
            buffer.append("\" target=\"_blank\">");
            buffer.append(directLink);
            buffer.append("</a></div>");
        }
        buffer.append("</body></html>");

        return buffer.toString();
    }

    private static String getBioPortalSearchUrl(String text, BioPortalSearchData bpSearchData) {
        text = text.replaceAll(" ", "%20");
        String urlString = bpSearchData.getBpRestBaseUrl() + BioPortalServerConstants.SEARCH_REST + "/" +
        text + createSearchUrlQueryString(bpSearchData);
        urlString = BioPortalUtil.addRestCallSuffixToUrl(urlString, bpSearchData.getBpRestCallSuffix());
        return urlString;
    }

    private static String createSearchUrlQueryString(BioPortalSearchData bpSearchData) {
        String res = "";
        String ontIds = bpSearchData.getSearchOntologyIds();
        String srchOpts = bpSearchData.getSearchOptions();
        String pgOpt = bpSearchData.getSearchPageOption();
        boolean firstSep = true;
        if (ontIds != null) {
            res += (firstSep ? "?" : "&") + "ontologyids=" + ontIds;
            firstSep = false;
        }
        if (srchOpts != null) {
            res += (firstSep ? "?" : "&") + srchOpts;
            firstSep = false;
        }
        if (pgOpt != null) {
            res += (firstSep ? "?" : "&") + pgOpt;
            firstSep = false;
        }
        return res;
    }

    public EntityData createExternalReference(String projectName, String entityName, BioPortalReferenceData bpRefData,
            String user, String operationDescription) {
        Project project = getProject(projectName);
        KnowledgeBase kb = project.getKnowledgeBase();

        Instance instance = kb instanceof OWLModel ?
                ((OWLModel) kb).getRDFResource(entityName) :
                    kb.getInstance(entityName);

                if (instance == null) {
                    throw new RuntimeException("Failed to import reference. Entity does not exist: " + entityName);
                }

                String referenceProperty = bpRefData.getReferencePropertyName();

                Slot slot = kb instanceof OWLModel ? ((OWLModel) kb).getRDFProperty(referenceProperty) : kb
                        .getSlot(referenceProperty);

                if (slot == null) {
                    throw new RuntimeException("Could not create reference for " + entityName
                            + " because the reference property is not part of the ontology. Property name: "
                            + referenceProperty);
                }

                ReferenceModel referenceModel = null;
                Instance refInstance = null;
                synchronized (kb) {
                    WebProtegeKBUtil.morphUser(kb, user);

                    boolean runsInTransaction = WebProtegeKBUtil.shouldRunInTransaction(operationDescription);
                    try {
                        if (runsInTransaction) {
                            kb.beginTransaction(operationDescription);
                        }

                        String viewOnOntologyVersionId = getIsViewOnOntologyId(
                                bpRefData.getBpRestBaseUrl(), bpRefData.getOntologyVersionId(), bpRefData.getBpRestCallSuffix());
                        String viewOnOntologyWithLabel =
                            (viewOnOntologyVersionId == null ? null : getIsViewOnOntologyWithLabel(
                                    bpRefData.getBpRestBaseUrl(), bpRefData.getOntologyVersionId(), bpRefData.getBpRestCallSuffix()));

                        referenceModel = new ReferenceModel(kb, bpRefData.importFromOriginalOntology(), bpRefData.createAsClass(),
                                bpRefData.getReferenceClassName(), bpRefData.getUrlPropertyName(),
                                bpRefData.getOntologyNamePropertyName(), bpRefData.getOntologyNameAltPropertyName(),
                                bpRefData.getOntologyIdPropertyName(), bpRefData.getConceptIdPropertyName(),
                                bpRefData.getConceptIdAltPropertyName(), bpRefData.getPreferredLabelPropertyName());

                        referenceModel.setReference_cls(bpRefData.getReferenceClassName());
                        refInstance = referenceModel.createReference(
                                bpRefData.getBpUrl(), bpRefData.getConceptId(), bpRefData.getConceptIdShort(), bpRefData.getOntologyVersionId(),
                                viewOnOntologyVersionId, bpRefData.getPreferredName(), bpRefData.getOntologyName(),
                                viewOnOntologyWithLabel);
                        if (refInstance == null) {
                            Log.getLogger().log(Level.SEVERE,
                                    "Could not create reference in " + projectName + " for entity " + entityName);

                            if (runsInTransaction) {
                                kb.commitTransaction();
                            }
                            throw new RuntimeException("Could not create reference for entity " + entityName);
                        }
                        instance.addOwnSlotValue(slot, refInstance);

                        if (runsInTransaction) {
                            kb.commitTransaction();
                        }
                    } catch (Exception e) {
                        Log.getLogger().log(Level.SEVERE,
                                "Could not import reference in " + projectName + " for entity " + entityName, e);
                        if (runsInTransaction) {
                            kb.rollbackTransaction();
                        }
                        throw new RuntimeException("Could not import reference for entity " + entityName + ". Message: "
                                + e.getMessage(), e);
                    } finally {
                        WebProtegeKBUtil.restoreUser(kb);
                    }
                }
                return OntologyServiceImpl.createEntityData(refInstance);
    }

    public EntityData replaceExternalReference(String projectName, String entityName, BioPortalReferenceData bpRefData,
            EntityData oldValueEntityData, String user, String operationDescription) {
        Project project = getProject(projectName);
        if (project == null) {
            return null;
        }

        KnowledgeBase kb = project.getKnowledgeBase();

        synchronized (kb) {
            WebProtegeKBUtil.morphUser(kb, user);
            boolean runsInTransaction = WebProtegeKBUtil.shouldRunInTransaction(operationDescription);
            try {
                if (runsInTransaction) {
                    kb.beginTransaction(operationDescription);
                }

                Instance subj = kb.getInstance(entityName);
                Slot pred = kb.getSlot(bpRefData.getReferencePropertyName());
                Instance obj = kb.getInstance(oldValueEntityData.getName());

                subj.removeOwnSlotValue(pred, obj);

                WebProtegeKBUtil.morphUser(kb, user); //hack
                final EntityData data = createExternalReference(projectName, entityName, bpRefData, user, operationDescription);
                WebProtegeKBUtil.morphUser(kb, user); //hack

                if (runsInTransaction) {
                    kb.commitTransaction();
                }

                return data;
            } catch (Exception e) {
                Log.getLogger().log(Level.SEVERE,  "Could not replace reference in " + projectName + " for entity " + entityName, e);
                if (runsInTransaction) {
                    kb.rollbackTransaction();
                }
                throw new RuntimeException("Could not replace reference for entity " + entityName + ". Message: " + e.getMessage(), e);
            } finally {
                WebProtegeKBUtil.restoreUser(kb);
            }
        }
    }


    private String getIsViewOnOntologyId(String bpRestBaseUrl, String ontologyVersionId, String bpRestCallSuffix) {
        String viewOnOntologyVersionId = null;
        if (bpRestBaseUrl != null && ontologyVersionId != null) {
            BioPortalViewOntologyMap bpViewOntologyMap = getBPViewOntologyMap(bpRestBaseUrl);
            int bpOntologyVersionId = Integer.parseInt(ontologyVersionId);
            int bpViewOnOntologyId = bpViewOntologyMap.getViewOnOntologyId(bpOntologyVersionId);
            if (bpViewOnOntologyId == BioPortalViewOntologyMap.UNKNOWN) {
                //calculate bpViewOnOntologyId
                bpViewOnOntologyId = org.ncbo.stanford.util.BioPortalUtil.getViewOnOntologyId(bpRestBaseUrl, bpOntologyVersionId, bpRestCallSuffix);
                //if calculation was successfully
                if (bpViewOnOntologyId != BioPortalViewOntologyMap.UNKNOWN) {
                    OntologyBean bpViewOnOntology = (bpViewOnOntologyId == BioPortalViewOntologyMap.NOT_A_VIEW ? null :
                        org.ncbo.stanford.util.BioPortalUtil.getViewOnOntology(bpRestBaseUrl, bpViewOnOntologyId, bpRestCallSuffix));
                    bpViewOntologyMap.setViewOnOntologyId(bpOntologyVersionId, bpViewOnOntologyId, bpViewOnOntology);
                }
            }
            if (bpViewOnOntologyId > 0) {
                viewOnOntologyVersionId = new Integer(bpViewOnOntologyId).toString();
            }
        }
        return viewOnOntologyVersionId;
    }

    private String getIsViewOnOntologyWithLabel(String bpRestBaseUrl, String ontologyVersionId, String bpRestCallSuffix) {
        String viewOnOntologyWithLabel = null;
        if (bpRestBaseUrl != null && ontologyVersionId != null) {
            BioPortalViewOntologyMap bpViewOntologyMap = getBPViewOntologyMap(bpRestBaseUrl);
            int bpOntologyVersionId = Integer.parseInt(ontologyVersionId);
            viewOnOntologyWithLabel = bpViewOntologyMap.getViewOnOntologyDisplayLabel(bpOntologyVersionId);
        }
        return viewOnOntologyWithLabel;
    }

    private BioPortalViewOntologyMap getBPViewOntologyMap(String bpRestBaseURL) {
        BioPortalViewOntologyMap bpViewOntologyMap = bpViewOntologyMaps.get(bpRestBaseURL);
        if (bpViewOntologyMap == null) {
            bpViewOntologyMap = new BioPortalViewOntologyMap();
            bpViewOntologyMaps.put(bpRestBaseURL, bpViewOntologyMap);
        }
        return bpViewOntologyMap;
    }


}
