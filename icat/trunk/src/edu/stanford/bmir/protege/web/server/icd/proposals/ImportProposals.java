package edu.stanford.bmir.protege.web.server.icd.proposals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.google.gwt.dev.util.collect.HashMap;

import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;

/**
 * The class that does the actual import of the proposal into iCAT.
 * Proposals from the same proposal group need to be imported as one transaction:
 * if one row fails, the entire group fails.
 * 
 * @author ttania
 *
 */
public class ImportProposals {
	
	private String user;
	private OWLModel owlModel;
	
	private Map<String, List<ICDProposal>> group2proposal = new HashMap<String, List<ICDProposal>>();
	private UploadProposalsResponse response;
		
	public ImportProposals(OWLModel owlModel, String user) {
		this.owlModel = owlModel;
		this.user = user;
		this.response = new UploadProposalsResponse();
	}

	public UploadProposalsResponse importProposals(File proposalsFile) {
		readFile(proposalsFile);
		doImport();
		
		return response;
	}

	private void readFile(File proposalsFile) {
		try {
			BufferedReader input = new BufferedReader(new FileReader(proposalsFile));
			try {
				String line = null;
				while ((line = input.readLine()) != null) {
					if (line != null) {
						try {
							processLine(line);
						} catch (Exception e) {
							Log.getLogger().log(Level.WARNING," Could not read line: " + line, e);
						}
					}
				}				
			} catch (IOException e) {
				Log.getLogger().log(Level.WARNING, "Error at parsing csv file: " + proposalsFile.getAbsolutePath(), e);
				return;
			} finally {
				if (input != null) {
					input.close();
				}
			}
		} catch (IOException ex) {
			Log.getLogger().log(Level.WARNING, "Error at accessing csv file: " + proposalsFile.getAbsolutePath(), ex);
		}
	}

	private void processLine(String line) {
		String[] values = line.split("\\|");
		String contributionId = getValue(values, 0);
		String contributableId = getValue(values, 1);
		String entityId = getValue(values, 2);
		String entityPublicId = getValue(values, 3);
		String contributorFullName = getValue(values, 4);
		String entryDateTime = getValue(values, 5);
		String status = getValue(values, 6);
		String rationale = getValue(values, 7);
		String proposalType = getValue(values, 8);
		String proposalGroupId = getValue(values, 9);
		String url = getValue(values, 10);
		String propertyId = getValue(values, 11);
		String oldValue = getValue(values, 12);
		String newValue = getValue(values, 13);
		String idFromValueSet = getValue(values, 14);
		String valueSetName = getValue(values, 15);
		
		if (ProposalTypes.AddContent.toString().equals(proposalGroupId)) {
				addProposalRowToGroup(ICDProposalFactory.createAddContentProposal(contributionId, contributableId, 
						entityId, entityPublicId, contributorFullName, entryDateTime, status, rationale, 
						proposalType, proposalGroupId, url, propertyId, oldValue, newValue, idFromValueSet, valueSetName));
		} else if (ProposalTypes.EditContent.toString().equals(proposalGroupId)) {
			addProposalRowToGroup(ICDProposalFactory.createEditContentProposal(contributionId, contributableId, 
					entityId, entityPublicId, contributorFullName, entryDateTime, status, rationale, 
					proposalType, proposalGroupId, url, propertyId, oldValue, newValue, idFromValueSet, valueSetName));
		} else if (ProposalTypes.DeleteContent.toString().equals(proposalGroupId)) {
			addProposalRowToGroup(ICDProposalFactory.createDeleteContentProposal(contributionId, contributableId, 
					entityId, entityPublicId, contributorFullName, entryDateTime, status, rationale, 
					proposalType, proposalGroupId, url, propertyId, oldValue, newValue, idFromValueSet, valueSetName));
		} else {
			logErrorRow(new ICDProposalResponse(contributionId, contributableId, entityId, entityPublicId, 
					contributorFullName, entryDateTime, status, rationale, proposalType, proposalGroupId, url, 
					propertyId, oldValue, newValue, idFromValueSet, valueSetName, "Unrecognized proposal type: " + proposalType));
		}
			
	}

	private void logErrorRow(ICDProposalResponse rowResponse) {
		Log.getLogger().warning(rowResponse.getComment());
		response.addRowError(rowResponse);
	}

	private void addProposalRowToGroup(ICDProposal proposal) {
		List<ICDProposal> list = group2proposal.get(proposal.getProposalGroupId());
		if (list == null) {
			list = new ArrayList<ICDProposal>();
		}
		list.add(proposal);
		group2proposal.put(proposal.getProposalGroupId(), list);
	}
	
	private String getValue(String[] values, int i) {
		return i < values.length ? removeQuotes(values[i]) : null;
	}

	private String removeQuotes(String str) {
		if (str == null) {
			return null;
		}
		String ret = str.trim();
		if (str.startsWith("\"") && str.endsWith("\"")) {
			ret = str.substring(1, str.length() - 1);
		}
		return ret.trim();
	}
	
	private void doImport(){
		
	}

	
}
