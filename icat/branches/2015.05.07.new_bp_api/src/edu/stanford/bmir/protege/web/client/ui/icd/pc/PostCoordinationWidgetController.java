package edu.stanford.bmir.protege.web.client.ui.icd.pc;

import java.util.Collection;

import com.gwtext.client.widgets.Panel;

import edu.stanford.bmir.protege.web.client.ui.portlet.PropertyWidget;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.FormGenerator;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.WidgetController;;

public class PostCoordinationWidgetController extends WidgetController {

	private FixedScaleValuePresenter fixedScaleValuePresenterWidget;
	private boolean initialized = false;
	
	public PostCoordinationWidgetController(Panel tabPanel,
			FormGenerator formGenerator) {
		super(tabPanel, formGenerator);
	}
	
	@Override
	public void showWidgetForProperty(String propertyName) {
		PropertyWidget widget = getWidgetForProperty(propertyName);
		if (widget != null) {
			widget.getComponent().show();
		}
		else {
			if (getFixedScaleValuePresenterWidget() != null) {
				getFixedScaleValuePresenterWidget().show(propertyName);
			}
		}
	}
	
	@Override
	public void hideWidgetForProperty(String propertyName) {
		PropertyWidget widget = getWidgetForProperty(propertyName);
		if (widget != null) {
			widget.getComponent().hide();
		}
		else {
			if (getFixedScaleValuePresenterWidget() != null) {
				getFixedScaleValuePresenterWidget().hide(propertyName);
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
