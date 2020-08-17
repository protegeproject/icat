package edu.stanford.bmir.protege.web.client.ui;

import com.gwtext.client.core.RegionPosition;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.Window;
import com.gwtext.client.widgets.event.WindowListenerAdapter;
import com.gwtext.client.widgets.layout.AnchorLayout;
import com.gwtext.client.widgets.layout.AnchorLayoutData;
import com.gwtext.client.widgets.layout.BorderLayout;
import com.gwtext.client.widgets.layout.BorderLayoutData;
import com.gwtext.client.widgets.layout.FitLayout;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.data.ProjectData;

public class EditMetaprojectUtil {

	private final static String METAPROJECT_PROJECT_NAME = "MP";

	public void editMetaProject() {

		Window win = createWindow();
		Panel mpPanel = buildUI(getMetaProject());

		win.add(mpPanel, new BorderLayoutData(RegionPosition.CENTER));
		win.show();
		
		mpPanel.doLayout();
	}

	protected Panel buildUI(Project project) {
		// Main panel (contains entire user interface)
		Panel main = new Panel();
		main.setLayout(new FitLayout());
		main.setBorder(false);

		// Wrapper panel (for top and ontology panels)
		final Panel wrapper = new Panel();
		wrapper.setLayout(new AnchorLayout());
		wrapper.setBorder(false);

		Ontology ontology = new Ontology(project);
		ontology.layoutProject();
		wrapper.add(ontology, new AnchorLayoutData("100% 100%"));

		// Add main panel to viewport
		main.add(wrapper);
		return main;
	}


	private Project getMetaProject() {
		ProjectData data = new ProjectData();
		data.setName(METAPROJECT_PROJECT_NAME);
		return new Project(data);
	}

	private Window createWindow() {
		final Window win = new Window();

		win.setLayout(new BorderLayout());
		win.setTitle("Edit metaproject");
		win.setClosable(true);
		win.setWidth(800);
		win.setHeight(600);
		win.setPaddings(7);
		win.setModal(true);
		win.setCloseAction(Window.CLOSE);

		win.addListener(new WindowListenerAdapter() {
			@Override
			public void onHide(Component component) {
				super.onHide(component);
			}
		});

		return win;
	}
}
