package edu.stanford.bmir.protege.web.server.icd.proposals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

import edu.stanford.bmir.protege.web.server.ApplicationProperties;
import edu.stanford.bmir.protege.web.server.ProjectManagerFactory;
import edu.stanford.bmir.protege.web.server.Protege3ProjectManager;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.metaproject.User;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;

@Path("/proposals")
public class UploadProposals {
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(FormDataMultiPart form) {
		
		FormDataBodyPart apiKeyPart = form.getField("apikey");
		String apiKey = apiKeyPart.getValueAs(String.class);
			
		if (checkApiKey(apiKey) == false) {
			return Response.status(401).entity("Not authorized to upload proposals. Check your API KEY.").build();
		}

		FormDataBodyPart projectPart = form.getField("project");
		String project = projectPart.getValueAs(String.class);
		
		OWLModel owlModel = getOWLModel(project);
		if (owlModel == null ) {
			return Response.status(401).entity("Invalid project name or no project argument specified.").build();
		}
		
		FormDataBodyPart filePart = form.getField("file");
		InputStream fileInputStream = filePart.getValueAs(InputStream.class);

		String serverPath = getServerPath();
		try {
			saveFile(fileInputStream, serverPath);
		} catch (IOException e) {
			Log.getLogger().log(Level.WARNING, "Could not write proposals file to: " + serverPath, e);
			return Response.status(500).entity("Server error: Could not write proposals file to file system.").build();
		}

		UploadProposalsResponse response = new ImportProposals(owlModel, getUser(apiKey).getName()).
				importProposals(new File(serverPath));
			
		return Response.status(response.getHttpCode()).entity(response.getMessage()).build();
	}

	private boolean checkApiKey(String apiKey) {
		//TODO: for the future: might want to check permission if the user is allowed to upload proposals
		return getUser(apiKey) != null;
	}
	
	private User getUser(String apiKey) {
		return Protege3ProjectManager.getProjectManager().getMetaProjectManager().getMetaProject().getUserByApiKey(apiKey);
	}
	
	private OWLModel getOWLModel(String projectName) {
		if (projectName == null) {
			return null;
		}
        Project prj = ProjectManagerFactory.getProtege3ProjectManager().getProject(projectName);
        return prj == null ? null : (OWLModel) prj.getKnowledgeBase();
    }
	
	private String getServerPath() {
		StringBuffer name = new StringBuffer(ApplicationProperties.getUploadDirectory());
		name.append(new SimpleDateFormat("yyyy.MM.dd_HH:mm:ss_").format(new Date()));		
		name.append(UUID.randomUUID().toString());
		return name.toString();
	}
		
	private void saveFile(InputStream uploadedInputStream, String serverLocation) throws IOException {
			OutputStream outpuStream = new FileOutputStream(new File(serverLocation));
			int read = 0;
			byte[] bytes = new byte[1024];

			outpuStream = new FileOutputStream(new File(serverLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				outpuStream.write(bytes, 0, read);
			}

			outpuStream.flush();
			outpuStream.close();
						
			uploadedInputStream.close();
	}

}