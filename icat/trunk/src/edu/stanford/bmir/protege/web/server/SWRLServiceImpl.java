package edu.stanford.bmir.protege.web.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.stanford.bmir.protege.web.client.rpc.SWRLService;
import edu.stanford.bmir.protege.web.client.rpc.data.SWRLData;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLFactory;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLImp;

/**
 * @author Mike Uehara-Bingen <mike.bingen@stanford.edu>
 */
public class SWRLServiceImpl
        extends RemoteServiceServlet
        implements SWRLService {

	private static final long serialVersionUID = 3632098151944559827L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		if (Log.getLogger().isLoggable(Level.FINE)) {
			Log.getLogger().fine("In init of SWRLServiceImpl");
		}

		ServletContext context = config.getServletContext();
		ProjectManager.getProjectManager().setRealPath(context.getRealPath("/"));
	}

	public List<SWRLData> getData(String projectName) {
		SWRLFactory factory = createSWRLFactory(projectName);
		return toSWRLData(factory.getImps());
	}

	private SWRLFactory createSWRLFactory(String projectName) {
		Project project =
		        ProjectManager.getProjectManager().getProject(projectName);
		OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
		return new SWRLFactory(owlModel);
	}

	private static List<SWRLData> toSWRLData(Collection imps) {
		List<SWRLData> data = new ArrayList<SWRLData>();
		for (Object objImp : imps) {
			SWRLImp imp = (SWRLImp) objImp;
			data.add(new SWRLData(imp.getPrefixedName(), imp.getBrowserText(),
			        imp.isEnabled()));
		}
		return data;
	}

}
