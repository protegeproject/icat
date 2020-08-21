package edu.stanford.bmir.protege.web.client.ui.ontology.scripting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.gwtext.client.widgets.layout.AnchorLayout;
import com.gwtext.client.widgets.layout.AnchorLayoutData;

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.ScriptingServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.scripting.ScriptCommand;
import edu.stanford.bmir.protege.web.client.rpc.data.scripting.ScriptResult;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractEntityPortlet;

public class PythonScriptingPortlet extends AbstractEntityPortlet {
	
	private TextArea resultArea;
	private TextArea commandLine;
	
	private List<String> cmdHistory = new ArrayList<String>();
	private int currentHistoryIndex = 0;
	
	public PythonScriptingPortlet(Project project) {
		super(project);
	}
	
	@Override
	public void initialize() {
		setTitle("Phyton Scripting");
		
		setLayout(new AnchorLayout());
		setAutoScroll(true);

		HorizontalPanel actionPanel = new HorizontalPanel();
		actionPanel.add(createClearResultsAnchor());
		
		resultArea = new TextArea();
		resultArea.setStylePrimaryName("condition_editor");
		resultArea.setReadOnly(true);
		
		commandLine = new TextArea();
		commandLine.addKeyUpHandler(getKeyUpHandler());
		
		add(actionPanel, new AnchorLayoutData("100% 5%"));
		add(resultArea, new AnchorLayoutData("100% 75%"));
		add(commandLine, new AnchorLayoutData("100% 20%"));
	}
	
	private Anchor createClearResultsAnchor() {
		Anchor clearResultsAnchor = new Anchor("Clear");
		clearResultsAnchor.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				resultArea.setText("");
			}
		});
		return clearResultsAnchor;
	}
	

	private KeyUpHandler getKeyUpHandler() {
		return new KeyUpHandler() {
			
			@Override
			public void onKeyUp(KeyUpEvent event) {
				int keyCode = event.getNativeKeyCode();
				if (keyCode == KeyCodes.KEY_ENTER) {
					executeCommand();
				} else if (event.isDownArrow()) {
					event.preventDefault();
					event.stopPropagation();
					
					commandLine.cancelKey();
					
					setCmdFromHistory(-1);
					
				} else if (event.isUpArrow()) {
					event.preventDefault();
					event.stopPropagation();
					
					commandLine.cancelKey();
					
					setCmdFromHistory(1);
				}
			}
		};
	}
	
	private void executeCommand() {
		String cmd = getCommand();
		
		addToCmdHistory(cmd);
		currentHistoryIndex = 0;
		
		appendResult("> " + cmd);
		commandLine.setText("");
		commandLine.setFocus(true);
		
		ScriptingServiceManager.getInstance().executePythonScript(getProject().getProjectName(),
				GlobalSettings.getGlobalSettings().getUserName(), 
				new ScriptCommand(cmd), new ExecutePythonScript());
	}
	
	
	
	private void addToCmdHistory(String cmd) {
		cmdHistory.add(0, cmd);
	}
	
	private void setCmdFromHistory(int incrm) {
		if (currentHistoryIndex < 0) {
			currentHistoryIndex = 0; //check
			return;
		}
		
		if (currentHistoryIndex >= cmdHistory.size()) {
			currentHistoryIndex = cmdHistory.size() -1;
			return;
		}
		
		String currentHistoryCmd = cmdHistory.get(currentHistoryIndex);
		if (currentHistoryCmd == null || currentHistoryCmd.length() == 0) {
			currentHistoryIndex = 0; //check
			return;
		}
		
		commandLine.setText(currentHistoryCmd);
		currentHistoryIndex = currentHistoryIndex + incrm;
	}

	private void appendResult(String text) {
		if (text == null || text.length() == 0) {
			return;
		}
		
		text = text.trim();
		
		String oldText = resultArea.getText();
		text = (oldText == null || oldText.length() == 0) ? text : oldText + "\n" + text;
		
		resultArea.setText(text);
		resultArea.setCursorPos(text.length());
		
		resultArea.getElement().setScrollTop(resultArea.getElement().getScrollHeight());
	}

	private String getCommand() {
		String cmd = commandLine.getText();
		cmd = cmd.replaceAll("\\n", "");
		cmd = cmd.trim();
		return cmd;
	}
	
	@Override
	public Collection<EntityData> getSelection() {
		return null;
	}

	@Override
	public void reload() {
		// TODO Auto-generated method stub
	}

	class ExecutePythonScript extends AbstractAsyncHandler<ScriptResult> {

		@Override
		public void handleFailure(Throwable caught) {
			appendResult("Error at execution: " + caught.getMessage());
		}

		@Override
		public void handleSuccess(ScriptResult result) {
			if (result.hasResult()) {
				appendResult(result.getResult());
			} 
			
			if (result.hasError()) {
				appendResult(result.getError());
			}
			
		}
		
	}
}
