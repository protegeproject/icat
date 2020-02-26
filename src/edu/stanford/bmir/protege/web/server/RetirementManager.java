package edu.stanford.bmir.protege.web.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.model.impl.OWLUtil;

public class RetirementManager {

	public static String NON_RETIRABLE_URL_PROP = "non.retirerable.url";
	public static String NON_RETIRABLE_URL_DEFAULT = "";
	
	public static String NON_RETIRABLE_LOCAL_FILE_PROP = "non.retireable.local.file";
	public static String NON_RETIRABLE_LOCAL_FILE_DEFAULT = "non-retirable-classes.csv";
	
	final public static String TO_BE_RETIRED_STR = "to be retired";
	final public static String DECISION_TO_BE_MADE_STR = "needing a decision";
	
	
	private static Set<String> nonRetirableClses = new HashSet<String>();
	
	private static boolean isInitialized = false;

	public static boolean isNonRetirableId(String id) {
		if (isInitialized == false) {
			init();
		}
		
		return nonRetirableClses.contains(id);
	}
	
	
	public static void init() {
		Log.getLogger().info("Initializing the non-retiring classes ...");
		boolean success = initLocalFileFromUrl();
		
		if (success == false) { 
			initFromLocalFile();
		}
		
		isInitialized = true;
	}
	

	/**
	 * Initializes the local cache from the local file
	 */
	private static boolean initFromLocalFile() {
		File localFile = new File(getNonRetireableLocalFile());
		BufferedReader localReader = null;
		try {
			localReader = new BufferedReader(new FileReader(localFile));
		} catch (Exception e) {
			Log.getLogger().log(Level.SEVERE, "Could not open local non-retirable classes for reading at: " +
					localFile.getAbsolutePath(), e);
			return false;
		}
		
		int count = 0;
		String row = null;
		try {
			while (( row = localReader.readLine()) != null) {
				nonRetirableClses.add(row);
				count ++;
			}
			
			localReader.close();
		} catch (IOException e) {
			Log.getLogger().log(Level.SEVERE, "IO Exception at processing row: " + row, e);
		}
		
		Log.getLogger().info("Read " + count + " non-retirable classes from local file: " + localFile.getAbsolutePath());
		return true;
	}


	/**
	 * Copies the remote non-retireable classes from URL to a local file
	 * and initializes the local cache.
	 * 
	 * Returns true if it was able to copy the remote file and initialize the local cache.
	 */
	private static boolean initLocalFileFromUrl() {
		BufferedReader urlReader = null;
		
		String url = getNonRetireableUrl();
		try {
			urlReader = URLUtil.read(url);
		} catch (Exception e) {
			Log.getLogger().log(Level.SEVERE, "Could not read non-retirable classes at URL: " + url +
					". Using fallback local file.", e);
			return false;
		}
				
		File localFile = new File(getNonRetireableLocalFile());
		BufferedWriter localWriter = null;
		try {
			localWriter = new BufferedWriter(new FileWriter(localFile));
		} catch (Exception e) {
			Log.getLogger().log(Level.SEVERE, "Could not open local non-retirable classes for writing at: " +
					localFile.getAbsolutePath(), e);
			return false;
		}
		
		Log.getLogger().info("Writing non-retirable classes file from " + url + " to local file: " + localFile.getAbsolutePath());
		
		int count = 0;
		String row = null;
		try {
			while (( row = urlReader.readLine()) != null) {
				row = row.trim();
				
				localWriter.write(row);
				localWriter.newLine();
				
				nonRetirableClses.add(row);
				
				count ++;
			}
			
			localWriter.close();
			urlReader.close();
		} catch (IOException e) {
			Log.getLogger().log(Level.SEVERE, "IO Exception at processing row: " + row, e);
		}
		
		Log.getLogger().info("Wrote " + count + " non-retirable classes to local file: " + localFile.getAbsolutePath());
		return true;
	}
	
	private static String getNonRetireableUrl() {
		return ApplicationProperties.getString(NON_RETIRABLE_URL_PROP, NON_RETIRABLE_URL_DEFAULT);
	}

	private static String getNonRetireableLocalFile() {
		return ApplicationProperties.getString(NON_RETIRABLE_LOCAL_FILE_PROP, NON_RETIRABLE_LOCAL_FILE_DEFAULT);
	}
	
	
	public static boolean isInRetiredTree(OWLModel owlModel, RDFSNamedClass cls) {
		ICDContentModel cm = new ICDContentModel(owlModel);
		if (isRetiredCls(cm, cls) == true) {
			return true;
		}
		
		Collection<List<Cls>> paths = OWLUtil.getPathsToRoot(cls);
		
		for (List<Cls> path : paths) {
			boolean isRetired = isRetiredPath(cm, path);
			if (isRetired == true) {
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean isRetiredPath(ICDContentModel cm, List<Cls> path) {
		for (Cls cls : path) {
			if (isRetiredCls(cm, cls) == true) {
				return true;
			}
		}
		return false;
	}


	private static boolean isRetiredCls(ICDContentModel cm, Cls cls) {
		if (cls instanceof RDFSNamedClass == false) {
			return false;
		}
		
		String title = cm.getTitleLabel((RDFSNamedClass)cls);
		if (title != null) {
			title = title.toLowerCase();
			
			if (title.contains(TO_BE_RETIRED_STR) || title.contains(DECISION_TO_BE_MADE_STR)) {
				return true;
			}
		}
		
		return false;
	}
	
}
