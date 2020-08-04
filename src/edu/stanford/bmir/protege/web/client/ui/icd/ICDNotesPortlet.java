package edu.stanford.bmir.protege.web.client.ui.icd;

import com.google.gwt.user.client.ui.Anchor;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.layout.RowLayout;
import com.gwtext.client.widgets.layout.RowLayoutData;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.ui.ClientApplicationPropertiesCache;
import edu.stanford.bmir.protege.web.client.ui.ontology.notes.NotesPortlet;

public class ICDNotesPortlet extends NotesPortlet {
	
	private static final String ANCHOR_TEXT_PREFIX = "Click here to see notes prior to ";
	private static final String ANCHOR_TEXT_PREFIX_NO_SELECTION = "Please select an entity to see older notes.";
	
	public static final String FILE_NAME_REPLACE_REGEX = "[\\/:\"*?<>|#]+";
	public static final String FILE_NAME_REPLACE_WITH = "_";
	
	private boolean displayExternalOldNotes;
	private String maxOldNotesDate;
	private String baseOldNotesURL;
	
	private Anchor viewOldNotesAnchor;

	public ICDNotesPortlet(Project project) {
		super(project);
	}

	@Override
	public void initialize() {
		initDisplayOldChanges();
		
		super.initialize();
		
		if (displayExternalOldNotes == false) {
			super.addNotesGrid();
		} else {
			createExtendedUI();
		}
	}
	
	private void createExtendedUI() {
		Panel panel = new Panel();
		panel.setLayout(new RowLayout());
		panel.add(notesGrid, new RowLayoutData("95%"));
		panel.add(createAnchorPanel(), new RowLayoutData("5%"));
		add(panel);
		
	}
	
	private Panel createAnchorPanel() {
		Panel anchorPanel = new Panel();
		anchorPanel.setPaddings(5);
		viewOldNotesAnchor = createAnchor();
		anchorPanel.add(viewOldNotesAnchor);
		return anchorPanel;
	}
	
	private Anchor createAnchor() {
		Anchor anchor = new Anchor(getAnchorText(), getAnchorHref(), "_blank");
		return anchor;
	}
	
	private String getAnchorHref() {
		return isEntitySelected() ? getOldChangesURL() : "javascript:;";
	}

	private String getAnchorText() {
		return isEntitySelected() ? ANCHOR_TEXT_PREFIX + maxOldNotesDate : ANCHOR_TEXT_PREFIX_NO_SELECTION;
	}

	private boolean isEntitySelected() {
		return getEntity() != null;
	}
	

	private String getOldChangesURL() {
		String id = getEntity().getName();
		id = id.replaceAll(FILE_NAME_REPLACE_REGEX, FILE_NAME_REPLACE_WITH);
		return baseOldNotesURL + "/" + id;
		
	}

	private void initDisplayOldChanges() {
		maxOldNotesDate = ClientApplicationPropertiesCache.getOldNotesMaxDate();
		baseOldNotesURL = ClientApplicationPropertiesCache.getOldNotesBaseUrl();
		displayExternalOldNotes = maxOldNotesDate != null && baseOldNotesURL != null;
	}

	@Override
	protected void addNotesGrid() {
		//does nothing
	}
	
	@Override
	public void reload() {
		super.reload();
		if (displayExternalOldNotes == true) {
			updateOldChangesAnchor();
		}
	}

	private void updateOldChangesAnchor() {
		viewOldNotesAnchor.setText(getAnchorText());
		viewOldNotesAnchor.setHref(getAnchorHref());
	}
	
	
}
