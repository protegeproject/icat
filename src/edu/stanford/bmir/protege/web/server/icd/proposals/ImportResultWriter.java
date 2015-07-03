package edu.stanford.bmir.protege.web.server.icd.proposals;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import edu.stanford.smi.protege.util.Log;


public class ImportResultWriter {
		
	private ImportResult importResult;
	
	public ImportResultWriter(ImportResult importResult) {
		this.importResult = importResult;
	}
	
	public void writeImportOutput(UploadProposalsResponse response) {
		
		checkExistingFile();
		File outputFile = new File(ImportProposalsUtil.getResultOutputPath());
		
		PrintWriter out = null;
		BufferedWriter bw = null;
		FileWriter fw = null;
		try {
			fw = new FileWriter(outputFile, true);
			bw = new BufferedWriter(fw);
			out = new PrintWriter(bw);
			
			writeOutputRows(out);
			
		} catch (IOException e) {
			response.setHttpCode(500);
			response.setMessage(e.getMessage()
					+ " Could not write the output file: " + outputFile.getAbsolutePath());
			Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
		} finally {
			try {
				if (out != null) {
					out.close(); // Will close bw and fw too
				} else if (bw != null) {
					bw.close(); // Will close fw too
				} else if (fw != null) {
					fw.close();
				}
			} catch (IOException e) {
				response.setHttpCode(500);
				response.setMessage(e.getMessage());
				Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}
	
	private void writeOutputRows(PrintWriter out){
		for(ImportResultRow row : importResult.getRows()){
			out.println(getRowText(row));
		}
	}
	
	private String getRowText(ImportResultRow row) {
		String separator = ImportProposalsUtil.getOutputSeparator();
		StringBuffer buffer = new StringBuffer(row.getContributionId());
		buffer.append(separator);
		buffer.append(row.getStatus());
		buffer.append(separator);
		buffer.append(row.getComment());
		buffer.append(separator);
		buffer.append(row.getDate());
		return buffer.toString();
	}
	
	private void checkExistingFile(){
		File resultFile = new File(ImportProposalsUtil.getResultOutputPath());
		if (resultFile.exists() && (resultFile.length() > ImportProposalsUtil.getResultOutputFileMaxSize())) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd_HHmmss");
			resultFile.renameTo(new File(resultFile.getAbsolutePath() + "_" + 
										format.format(new Date()))); 
		}
	}
	
}
