package edu.stanford.bmir.protege.web.client.ui.ontology.phenologue;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HTML;

import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.SWRLParaphrasingServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.ParaphraseObject;
import edu.stanford.bmir.protege.web.client.rpc.data.SWRLNames;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractEntityPortlet;
import edu.stanford.bmir.protege.web.client.util.Project;

/**
 * @author Saeed
 * 
 */
public class PhenologueTabPortlet extends AbstractEntityPortlet {

	private String paraphraseText;

	public PhenologueTabPortlet(Project project) {
		super(project);

	}

	@Override
	public void reload() {
//		this.clear();
//		SWRLParaphrasingServiceManager.getInstance().getSWRLParaphrases(
//				this.project.getProjectName(), new GetSWRLParaphrases());
	}

	public Collection<EntityData> getSelection() {
		throw new RuntimeException("Under construction");
	}

	@Override
	public void intialize() {
		this.setTitle("Paraphrased Rules");
		this.setAutoScroll(true);
		
		SWRLParaphrasingServiceManager.getInstance().getSWRLParaphrases(
				this.project.getProjectName(), new GetSWRLParaphrases());

		SWRLParaphrasingServiceManager.getInstance().getSWRLNames(
				this.project.getProjectName(), new GetSWRLNames());
	}

	private class GetSWRLParaphrases extends AbstractAsyncHandler {

		@Override
		public void handleFailure(Throwable caught) {
			GWT.log("Error at getting SWRL Data from server", caught);
		}

		@Override
		public void handleSuccess(Object result) {
			ParaphraseObject obj = (ParaphraseObject) result;
			PhenologueTabPortlet.this.paraphraseText = obj.text;
			createUI(paraphraseText);
			RuleTreePortlet.nameExp = obj.RuleNames;
			RuleTreePortlet.sigExp = obj.RuleSignatures;

		}
	}

	private class GetSWRLNames extends AbstractAsyncHandler {

		@Override
		public void handleFailure(Throwable caught) {
			GWT.log("Error at getting SWRL Names from server", caught);
		}

		@Override
		public void handleSuccess(Object result) {
			// TODO Auto-generated method stub
			SWRLNames names = (SWRLNames) result;
			ElicitationPortlet.SWRLClassNames = names.classNames;
			ElicitationPortlet.SWRLIndvNames = names.indivNames;
			ElicitationPortlet.SWRLDataValNames = names.dataValNames;
			ElicitationPortlet.SWRLBuiltinNames = names.builtinNames;

		}
	}

	private void createUI(String paraphraseText) {
		
		String text = paraphraseText;
		text = text.replaceAll("!1!", "");
		text = text.replaceAll("!2!", "");
		text = text.replaceAll("!4!", "");
		text = text.replaceAll("!5!", "");
		text = text.replace('?', '=');
		text = text.replaceAll("=", "");
		HTML htext = new HTML(text);
		this.add(htext);
		

	}

}
