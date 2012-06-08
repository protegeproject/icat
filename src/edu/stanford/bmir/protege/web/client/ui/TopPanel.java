package edu.stanford.bmir.protege.web.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ImageBundle;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.Window;
import com.gwtext.client.widgets.layout.HorizontalLayout;

/**
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 */
public class TopPanel extends Panel {

	private HTML aboutLink = new HTML("<a id='about_web_protege' href='javascript:;'>About</a>");
	private HTML feedbackLink = new HTML("<a id='feedback' href='javascript:;'><u><b>Send us feedback!</b></u> </a>");
	private HTML docsLink = new HTML("<a href='http://protegewiki.stanford.edu/index.php/WebProtege' target='_blank'>Documentation</a>&nbsp;|");
	private HTML pWebLink = new HTML("<a href='http://protege.stanford.edu/' target='_blank'>Prot&eacute;g&eacute;&nbsp;Web&nbsp;site</a>&nbsp;|");
	private HTML pWikiLink = new HTML("<a href='http://protegewiki.stanford.edu/index.php/Main_Page' target='_blank'>Prot&eacute;g&eacute;&nbsp;Wiki</a>&nbsp;|");
	
	//private TopPanelImages images = (TopPanelImages) GWT.create(TopPanelImages.class);
	
	public interface TopPanelImages extends ImageBundle {
		@Resource("edu/stanford/bmir/protege/web/public/images/ProtegeLogo.gif") 
		public AbstractImagePrototype protegeLogo();
	}
	
	public TopPanel() {
		setLayout(new HorizontalLayout(0));
		//setLayout(new FitLayout());
		//setAutoWidth(true);
		setBorder(false);
		setHeight(30); //75
		setPaddings(5);
		
		add(getLogoPanel());
		add(getLinksPanel());
	}
	
	private Panel getLogoPanel() {
		//final Image logo = images.protegeLogo().createImage();

		Panel logoPanel = new Panel();
		//logoPanel.setLayout(new HorizontalLayout(0));
		logoPanel.setBorder(false);
		logoPanel.setWidth(200);
		//logoPanel.add(logo);

		return logoPanel;
	}
	
	private Panel getLinksPanel() {
		Panel linksPanel = new Panel();
		linksPanel.setBorder(false);
		linksPanel.setCls("header");
		linksPanel.setLayout(new HorizontalLayout(5));

		linksPanel.add(feedbackLink);
		linksPanel.add(docsLink);
		linksPanel.add(pWebLink);
		linksPanel.add(pWikiLink);
		linksPanel.add(aboutLink);
		
		aboutLink.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final Window window = new Window();  
				window.setTitle("About WebProt\u00E9g\u00E9");
				window.setClosable(true);  
				window.setWidth(500);  
				window.setHeight(220);
				window.setHtml(getAboutText());
				window.setPaddings(7);
				window.setCloseAction(Window.HIDE);
				window.show("about_web_protege");
			}
		});
		
		feedbackLink.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final Window window = new Window();  
				window.setTitle("Send us feedback!");
				window.setClosable(true);  
				window.setWidth(400); 
				window.setHeight(200);
				window.setHtml(getFeebackText());
				window.setPaddings(7);
				window.setCloseAction(Window.HIDE);
				window.show("feedback");
			}
		});

		
		return linksPanel;
	}
	
	private static String getAboutText() {
		return "<br /><b>WebProtege 0.5 alpha, build 200</b><br /><br />" +
			"<br />WebProt&eacute;g&eacute is being developed at the <a href='" +
			"http://bmir.stanford.edu/' target='_blank'>Stanford " +
			"Center for Biomedical Informatics Research</a>.<br /><br />" +
			"Send feedback, questions, and bugs to the <a href='" +
			"https://mailman.stanford.edu/mailman/listinfo/protege-discussion' " +
			"target='_blank'>protege-discussion</a> mailing list.<br /><br />" +
			"Open source from: <a href='http://smi-protege.stanford.edu/repos/protege/web-protege' " +
			"target='_blank'>http://smi-protege.stanford.edu/repos/protege/web-protege</a>" +
			"<br /><br /><br />&copy; Copyright 2009 Stanford Center for Biomedical Informatics Research";
	}
	
	private static String getFeebackText() {
		return "<br /> Thank you for using WebProt&eacute;g&eacute! " +
			"<br /><br /> Your feedback is very important to us. " +
			"Please send your comments, questions, feature requests, bugs, etc. to the <a href='" +
			"https://mailman.stanford.edu/mailman/listinfo/protege-discussion' " +
			"target='_blank'>protege-discussion</a> mailing list.<br /><br />" +
			"If you would like to contribute to the development of WebProt&eacute;g&eacute, " +
			"please contact us using the mailing list. Thank you!";
	}
}
