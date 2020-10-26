package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValues;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyPairs;
import edu.stanford.bmir.protege.web.client.ui.portlet.PropertyWidget;

/**
 * This class is responsible for making a bulk remote call for all registered widgets in a 
 * tab that implement the {@link HasGetEntityPropertyValueHandler}. These widgets should fill their values
 * in the method {@link HasGetEntityPropertyValueHandler#setPreloadedValues(EntityData, List)}.
 * 
 * @author ttania
 *
 */
public class GetEntityPropertyValuesHandler {

	private String prj;

	// widgets with no reified properties
	private List<PropertyWidget> widgetsWithReifiedProps = new ArrayList<PropertyWidget>();

	private Map<String, PropertyWidget> id2Widget = new HashMap<String, PropertyWidget>();

	private Map<String, PropertyPairs> reifiedPropMap = new HashMap<String, PropertyPairs>();

	public GetEntityPropertyValuesHandler(String prj) {
		this.prj = prj;
	}

	public void addWidget(PropertyWidget widget) {
		List<String> props = new ArrayList<String>();
		props.add(widget.getProperty().getName());

		if (widget instanceof HasReifiedProperties) {
			widgetsWithReifiedProps.add(widget);
			reifiedPropMap.put(widget.getComponent().getId(),
					new PropertyPairs(props, ((HasReifiedProperties) widget).getReifiedProperties()));

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

		OntologyServiceManager.getInstance().getEntityPropertyValues(prj, subjects, reifiedPropMap,
				new GetTriplesHandler(subject));
	}

	private void beforeFillValues() {
		for (PropertyWidget propertyWidget : id2Widget.values()) {
			((HasGetEntityPropertyValueHandler)propertyWidget).beforeFillValues();
		}
	}
	
	private void fillWidgetsValues(EntityData subject, Map<String, List<EntityPropertyValues>> values) {
		if (subject == null) {
			fillWidgetsWithEmptySubject();
			return;
		}

		for (String id : values.keySet()) {
			PropertyWidget widget = id2Widget.get(id);

			if (widget == null) { // should never happen
				continue;
			}

			((HasGetEntityPropertyValueHandler) widget).setPreloadedPropertyValues(subject, values.get(id));
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
		widgetsWithReifiedProps.clear();
	}
	
	// ****** Remote calls ********

	protected class GetTriplesHandler extends AbstractAsyncHandler<Map<String, List<EntityPropertyValues>>> {

		private EntityData mySubject;

		public GetTriplesHandler(EntityData subject) {
			this.mySubject = subject;
		}

		@Override
		public void handleFailure(Throwable caught) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void handleSuccess(Map<String, List<EntityPropertyValues>> result) {
			fillWidgetsValues(mySubject, result);
		}

	}
}
