package edu.stanford.bmir.protege.web.client.ui.icd.pc;

import java.util.Collection;
import java.util.List;

import com.gwtext.client.widgets.Panel;

import edu.stanford.bmir.protege.web.client.rpc.data.layout.WidgetConfiguration;
import edu.stanford.bmir.protege.web.client.ui.portlet.PropertyWidget;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.FormConstants;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.FormGenerator;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.WidgetController;

public class PostCoordinationWidgetController extends WidgetController {

	private FixedScaleValuePresenter fixedScaleValuePresenterWidget;
	private boolean initialized = false;
	
	public PostCoordinationWidgetController(Panel tabPanel,
			FormGenerator formGenerator) {
		super(tabPanel, formGenerator);
	}
	
	@Override
	public void showWidgetForProperty(String propertyName) {
		List<String> allRelatedProperties = getAllRelatedProperties(propertyName);
		for (String relProp : allRelatedProperties) {
			PropertyWidget widget = getWidgetForProperty(relProp);
			if (widget != null) {
				boolean isHidden = new WidgetConfiguration(widget.getWidgetConfiguration()).getBooleanProperty(FormConstants.HIDDEN, false);
				if ( ! isHidden ) {
					widget.getComponent().show();
				}
			}
			else {
				if (getFixedScaleValuePresenterWidget() != null) {
					getFixedScaleValuePresenterWidget().show(relProp);
				}
			}
		}
	}
	
	@Override
	public void hideWidgetForProperty(String propertyName) {
		List<String> allRelatedProperties = getAllRelatedProperties(propertyName);
		for (String relProp : allRelatedProperties) {
			PropertyWidget widget = getWidgetForProperty(relProp);
			if (widget != null) {
				boolean toHide = new WidgetConfiguration(widget.getWidgetConfiguration()).getBooleanProperty(FormConstants.HIDDEN, true);
				if ( toHide ) {
					widget.getComponent().hide();
				}
			}
			else {
				if (getFixedScaleValuePresenterWidget() != null) {
					getFixedScaleValuePresenterWidget().hide(relProp);
				}
			}
		}
	}
	
	private FixedScaleValuePresenter getFixedScaleValuePresenterWidget() {
		//initialize on demand
		if (!initialized) {
			Collection<PropertyWidget> widgets = getFormGenerator().getWidgets();
			for (PropertyWidget propertyWidget : widgets) {
				if (propertyWidget.getClass().equals(FixedScaleValuePresenter.class)) {
					fixedScaleValuePresenterWidget = (FixedScaleValuePresenter) propertyWidget;
					break;
				}
			}
			initialized = true;
		}
		
		return fixedScaleValuePresenterWidget;
	}

}
