package edu.stanford.bmir.protege.web.server.bioportal;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.stanford.bmir.protege.web.client.rpc.bioportal.BioportalProposals;
import edu.stanford.bmir.protege.web.server.URLUtil;
import edu.stanford.smi.protege.util.Log;

public class BioPortalProposalsImpl  extends RemoteServiceServlet implements BioportalProposals {

    private static final long serialVersionUID = -4997960524274542340L;


    public String getBioportalProposals(String projectName, String entityURI) {
        //return  URLUtil.getURLContent("http://bioportal.bioontology.org/notes/virtual/1104/?noteid=Note_608855cc-21ef-4f36-96c7-a42b7ba52d36");

        //return  URLUtil.getURLContent("http://rest.bioontology.org/bioportal/notes/39002/");

        try {
            entityURI = URLEncoder.encode(entityURI, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            Log.getLogger().log(Level.WARNING, "Error at encoding url: " + entityURI, e1);
            return "";
        }

        return URLUtil.getURLContent("http://rest.bioontology.org/bioportal/notes/44450?conceptid=" +
                entityURI + "&threaded=true");
    }

}
