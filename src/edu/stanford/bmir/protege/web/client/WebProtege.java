package edu.stanford.bmir.protege.web.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.gwtext.client.core.Ext;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.Viewport;
import com.gwtext.client.widgets.layout.AnchorLayout;
import com.gwtext.client.widgets.layout.AnchorLayoutData;
import com.gwtext.client.widgets.layout.FitLayout;

import edu.stanford.bmir.protege.web.client.rpc.AdminServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.ProjectConfigurationServiceManager;
import edu.stanford.bmir.protege.web.client.ui.OntologyContainer;
import edu.stanford.bmir.protege.web.client.ui.TopPanel;
import edu.stanford.bmir.protege.web.client.util.Session;

/**
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 * @author Tania Tudorache <tudorache@stanford.edu>
 */
public class WebProtege implements EntryPoint, NativePreviewHandler {

	//TODO: not clear if we should use here module base or host base
	private static String BASE_URL = GWT.getHostPageBaseURL(); 

	public void onModuleLoad() {
		//force initialization of servlets - needed when it runs in browser	
		AdminServiceManager.getInstance();
		OntologyServiceManager.getInstance();
		ProjectConfigurationServiceManager.getInstance();

		Ext.getBody().mask("Initializing <br>WebProt&eacute;g&eacute ", "x-mask-loading");
		Event.addNativePreviewHandler(this);

		Timer timer = new Timer() {
			public void run() {
				buildUI();
				Ext.getBody().unmask();
			}
		};
		timer.schedule(200);		
	}


	private void buildUI(){
		Panel fitPanel = new Panel();
		fitPanel.setLayout(new FitLayout());
		fitPanel.setCls("white-bg");

		final Panel wrapperPanel = new Panel();        
		wrapperPanel.setLayout(new AnchorLayout());
		wrapperPanel.setCls("white-bg");
		wrapperPanel.setBorder(false);

		wrapperPanel.add(new TopPanel(), new AnchorLayoutData("100% 5%"));
		wrapperPanel.add(new OntologyContainer(new Session("No user")), new AnchorLayoutData("100% 95%"));         

		fitPanel.add(wrapperPanel);        
		Viewport viewport = new Viewport(fitPanel);

		fitPanel.doLayout();        
	}

	native String getTagName(Element element) 
	/*-{ return element.tagName; }-*/;


	/* 
	 * Necessary to hijack external links to open in a new browser window	
	 */
	public void onPreviewNativeEvent(NativePreviewEvent event) {
		if (event.getTypeInt() == Event.ONCLICK) {				
			EventTarget eventTarget = event.getNativeEvent().getEventTarget(); 
			Element target = eventTarget.cast();
			if ("a".equalsIgnoreCase(getTagName(target))) {
				String href = DOM.getElementAttribute(target, "href");
				String hrefTarget = DOM.getElementAttribute(target, "target");
				if (!href.startsWith(BASE_URL) && !href.startsWith("#")) {					
					if (hrefTarget == null || hrefTarget.length() == 0) {
						DOM.setElementAttribute(target, "target", "_blank");
					}
				}

			} 
		}		
	}
}
