package edu.stanford.bmir.protege.web.server.icd.proposals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImportResult {

	private List<ImportResultRow> rows = new ArrayList<ImportResultRow>();
	
	public void addRow(ImportResultRow row) {
		rows.add(row);
	}
	
	public void recordResult(String contributionId, String message, ImportRowStatus status) {
		addRow(new ImportResultRow(contributionId, new Date(), status, message));
	}
	
}
