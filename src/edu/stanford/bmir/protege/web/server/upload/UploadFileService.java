package edu.stanford.bmir.protege.web.server.upload;

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
import edu.stanford.smi.protege.util.Log;

@Path("/files")
public class UploadFileService {
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(FormDataMultiPart form) {

		FormDataBodyPart filePart = form.getField("file");

		//ContentDisposition headerOfFilePart = filePart.getContentDisposition();

		InputStream fileInputStream = filePart.getValueAs(InputStream.class);

		String savePath = getServerPath();
		saveFile(fileInputStream, savePath);

		String output = "File saved to server location: " + savePath;

		return Response.status(200).entity(output).build();
	}

	private String getServerPath() {
		StringBuffer name = new StringBuffer(ApplicationProperties.getUploadDirectory());
		name.append(new SimpleDateFormat("yyyy.MM.dd_HH:mm:ss_").format(new Date()));
		name.append(".");
		//TODO: maybe replace with apikey
		name.append(UUID.randomUUID().toString());
		return name.toString();
	}
		
	private void saveFile(InputStream uploadedInputStream, String serverLocation) {

		try {
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
		} catch (IOException e) {
			Log.getLogger().log(Level.WARNING, "Could not write file to: " + serverLocation, e);
		}
		
	}

}