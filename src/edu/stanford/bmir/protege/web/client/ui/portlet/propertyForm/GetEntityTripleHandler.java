package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyPairs;
import edu.stanford.bmir.protege.web.client.rpc.data.Triple;
import edu.stanford.bmir.protege.web.client.ui.portlet.PropertyWidget;

/**
 * This class is responsible for making a bulk remote call for all registered widgets in a 
 * tab that implement the {@link HasGetEntityTripleHandler}. These widgets should fill their values
 * in the method {@link HasGetEntityTripleHandler#setPreloadedValues(EntityData, List)}.
 * 
 * @author ttania
 *
 */
public class GetEntityTripleHandler {

	private String prj;

	// widgets with no reified properties
	private List<PropertyWidget> widgets = new ArrayList<PropertyWidget>();
	private List<PropertyWidget> widgetsWithReifiedProps = new ArrayList<PropertyWidget>();

	private Map<String, PropertyWidget> id2Widget = new HashMap<String, PropertyWidget>();

	private Map<String, List<String>> simplePropMap = new HashMap<String, List<String>>();
	private Map<String, PropertyPairs> reifiedPropMap = new HashMap<String, PropertyPairs>();

	public GetEntityTripleHandler(String prj) {
		this.prj = prj;
	}

	public void addWidget(PropertyWidget widget) {
		List<String> props = new ArrayList<String>();
		props.add(widget.getProperty().getName());

		if (widget instanceof HasReifiedProperties) {
			widgetsWithReifiedProps.add(widget);
			reifiedPropMap.put(widget.getComponent().getId(),
					new PropertyPairs(props, ((HasReifiedProperties) widget).getReifiedProperties()));

		} else {
			widgets.add(widget);
			simplePropMap.put(widget.getComponent().getId(), props);
		}

		id2Widget.put(widget.getComponent().getId(), widget);
	}

	public void fillValues(EntityData subject) {
		
		beforeFillValues();
		
		if (subject == null) { // should not happen
			fillWidgetsValues(subject, null);
			return;
		}
		
		List<String> subjects = new ArrayList<String>();
		subjects.add(subject.getName());

		OntologyServiceManager.getInstance().getEntityTriples(prj, subjects, simplePropMap, reifiedPropMap,
				new GetTriplesHandler(subject));
	}

	private void beforeFillValues() {
		for (PropertyWidget propertyWidget : id2Widget.values()) {
			((HasGetEntityTripleHandler)propertyWidget).beforeFillValues();
		}
	}
	
	private void fillWidgetsValues(EntityData subject, Map<String, List<Triple>> values) {
		if (subject == null) {
			fillWidgetsWithEmptySubject();
			return;
		}

		for (String id : values.keySet()) {
			PropertyWidget widget = id2Widget.get(id);

			if (widget == null) { // should never happen
				continue;
			}

			((HasGetEntityTripleHandler) widget).setPreloadedValues(subject, values.get(id));
		}
	}

	private void fillWidgetsWithEmptySubject() {
		for (String id : id2Widget.keySet()) {
			PropertyWidget widget = id2Widget.get(id);

			((HasGetEntityTripleHandler) widget).setPreloadedValues(null, null); // TODO: make sure this is handled
																					// right in the widgets
		}

	}

	public boolean isHandledWidget(PropertyWidget widget) {
		return id2Widget.get(widget.getComponent().getId()) != null;
	}
	
	public void dispose() {
		id2Widget.clear();
		reifiedPropMap.clear();
		simplePropMap.clear();
		widgetsWithReifiedProps.clear();
		widgets.clear();
	}

	// ****** Remote calls ********

	class GetTriplesHandler extends AbstractAsyncHandler<Map<String, List<Triple>>> {

		private EntityData mySubject;

		public GetTriplesHandler(EntityData subject) {
			this.mySubject = subject;
		}

		@Override
		public void handleFailure(Throwable caught) {
			// TODO do something, maybe throw exception

		}

		@Override
		public void handleSuccess(Map<String, List<Triple>> result) {
			fillWidgetsValues(mySubject, result);
		}

	}
}
