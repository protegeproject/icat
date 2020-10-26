package edu.stanford.bmir.protege.web.client.ui.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;

public interface iCatResource extends ClientBundle {
	public static final iCatResource INSTANCE =  GWT.create(iCatResource.class);
	
	@Source("iCatBundledStyles.css")
	public iCATCSSResource css();

	//Background Images
    @Source("images/loading.gif")
    DataResource loadingImage();
    
    @Source("images/invisible12.png")
    DataResource notLoadingImage();
	
}
