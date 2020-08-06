package edu.stanford.bmir.protege.web.client.ui.icd;

import com.google.gwt.user.client.ui.Anchor;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.layout.RowLayout;
import com.gwtext.client.widgets.layout.RowLayoutData;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.ui.ClientApplicationPropertiesCache;
import edu.stanford.bmir.protege.web.client.ui.ontology.changes.ChangesPortlet;

public class ICDChangesPortlet extends ChangesPortlet {
	
	private static final String ANCHOR_TEXT_PREFIX = "Click here to see changes prior to ";
	private static final String ANCHOR_TEXT_PREFIX_NO_SELECTION = "Please select an entity to see older changes.";
	
	public static final String FILE_NAME_REPLACE_REGEX = "[\\/:\"*?<>|#]+";
	public static final String FILE_NAME_REPLACE_WITH = "_";
	
	private boolean displayExternalOldChanges;
	private String maxOldChangesDate;
	private String baseOldChangesURL;
	
	private Anchor viewOldChangesAnchor;
	
	public ICDChangesPortlet(Project project) {
		super(project);
	}
	
	@Override
	public void initialize() {
		initDisplayOldChanges();
		
		super.initialize();
		
		if (displayExternalOldChanges == false) {
			super.addChangesGrid();
		} else {
			createExtendedUI();
		}
	}
	
	private void createExtendedUI() {
		Panel panel = new Panel();
		panel.setLayout(new RowLayout());
		panel.add(changesGrid, new RowLayoutData("95%"));
		panel.add(createAnchorPanel(), new RowLayoutData("5%"));
		add(panel);
		
	}
	
	private Panel createAnchorPanel() {
		Panel anchorPanel = new Panel();
		anchorPanel.setPaddings(5);
		viewOldChangesAnchor = createAnchor();
		anchorPanel.add(viewOldChangesAnchor);
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
		return isEntitySelected() ? ANCHOR_TEXT_PREFIX + maxOldChangesDate : ANCHOR_TEXT_PREFIX_NO_SELECTION;
	}

	private boolean isEntitySelected() {
		return getEntity() != null;
	}
	

	private String getOldChangesURL() {
		String id = getEntity().getName();
		id = id.replaceAll(FILE_NAME_REPLACE_REGEX, FILE_NAME_REPLACE_WITH);
		return baseOldChangesURL + "/" + id + ".html";
		
	}

	private void initDisplayOldChanges() {
		maxOldChangesDate = ClientApplicationPropertiesCache.getOldChangesMaxDate();
		baseOldChangesURL = ClientApplicationPropertiesCache.getOldChangesBaseUrl();
		displayExternalOldChanges = maxOldChangesDate != null && baseOldChangesURL != null;
	}

	@Override
	protected void addChangesGrid() {
		//does nothing
	}
	
	@Override
	public void reload() {
		super.reload();
		if (displayExternalOldChanges == true) {
			updateOldChangesAnchor();
		}
	}

	private void updateOldChangesAnchor() {
		viewOldChangesAnchor.setText(getAnchorText());
		viewOldChangesAnchor.setHref(getAnchorHref());
	}
	
}
