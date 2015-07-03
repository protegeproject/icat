package edu.stanford.bmir.protege.web.server.icd.proposals;

import java.util.ArrayList;
import java.util.Collections;
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
	
	public List<ImportResultRow> getRows(){
		return Collections.unmodifiableList(rows);
	}
	
	public int getSuccessRowCount() {
		return getRowWithStatusCount(ImportRowStatus.SUCCESS);
	}
	
	public int getIgnoreRowCount() {
		return getRowWithStatusCount(ImportRowStatus.IGNORED);
	}
	
	public int getFailRowCount() {
		return getRowWithStatusCount(ImportRowStatus.FAIL);
	}
	
	public int getRowWithStatusCount(ImportRowStatus status) {
		int count = 0;
		for (ImportResultRow row : rows) {
			if (status.toString().equals(row.getStatus())) {
				count ++;
			}
		}
		return count;
	}
	
	
}
