package edu.stanford.bmir.protege.web.client.ui.icd;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class IcdApiSearchFieldOld extends Composite {

	interface MyUiBinder extends UiBinder<Widget, IcdApiSearchFieldOld> {
	}

	private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	@UiField
	TextBox searchField;
	@UiField
	HTMLPanel searchResultsDiv;

	String selection;

	private String iNo;

	public IcdApiSearchFieldOld(String iNo) {
		initWidget(uiBinder.createAndBindUi(this));
		
		this.iNo = iNo;
		
		searchField.getElement().setAttribute("data-ctw-ino", iNo);
		searchResultsDiv.getElement().setAttribute("data-ctw-ino", iNo);
	}

	void setSelection(String selection) {
		this.selection = selection;
	}

	public String getiNo() {
		return iNo;
	}

	public String getPublicIdSelection() {
		return selection;
	}
	
}
