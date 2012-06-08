package edu.stanford.bmir.protege.web.client.ui.ontology.notes;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Hyperlink;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.Position;
import com.gwtext.client.data.ArrayReader;
import com.gwtext.client.data.FieldDef;
import com.gwtext.client.data.MemoryProxy;
import com.gwtext.client.data.Record;
import com.gwtext.client.data.RecordDef;
import com.gwtext.client.data.SimpleStore;
import com.gwtext.client.data.Store;
import com.gwtext.client.data.StringFieldDef;
import com.gwtext.client.util.Format;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.Window;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.form.ComboBox;
import com.gwtext.client.widgets.form.FormPanel;
import com.gwtext.client.widgets.form.HtmlEditor;
import com.gwtext.client.widgets.form.Label;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.grid.CellMetadata;
import com.gwtext.client.widgets.grid.ColumnConfig;
import com.gwtext.client.widgets.grid.ColumnModel;
import com.gwtext.client.widgets.grid.GridPanel;
import com.gwtext.client.widgets.grid.GridView;
import com.gwtext.client.widgets.grid.Renderer;
import com.gwtext.client.widgets.grid.RowParams;
import com.gwtext.client.widgets.grid.RowSelectionModel;
import com.gwtext.client.widgets.grid.event.GridCellListenerAdapter;
import com.gwtext.client.widgets.grid.event.GridRowListenerAdapter;
import com.gwtext.client.widgets.layout.AnchorLayoutData;
import com.gwtext.client.widgets.layout.FitLayout;
import com.gwtext.client.widgets.layout.HorizontalLayout;

import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.NotesData;
import edu.stanford.bmir.protege.web.client.util.Project;

/**
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 * @author Vivek Tripathi <vivekyt@stanford.edu>
 */
public class NotesGrid extends GridPanel {

	protected RecordDef recordDef;
	protected Store store;
	protected Store tempstore;
	protected boolean showPreview = false;
	protected EntityData _currentEntity;
	protected Panel headerPanel;
	private Hyperlink expandlink;
	private Hyperlink nextlink;
	private Hyperlink prevlink;
	protected Project project;	
	private int rowSelected = -1;
	private int endofpage = 5;
	private int startofpage = 0;
	private int pageNo = 0;
	private Label label;
	private boolean replyEnable = false;
	protected boolean topLevel;
	protected String[][] commentTypes;
	
	public NotesGrid(Project project) {
		this(project, false);
	}
	
	public NotesGrid(Project project, boolean topLevel) {
		this.project = project;
		this.topLevel = topLevel;
		headerPanel = new Panel();
		headerPanel.setLayout(new HorizontalLayout(10));
		label = new Label();
		
		createGrid();
		setPaddings(0, 0, 0, 30);
		fillTempStore(0,5);
		replyEnable = false;
	}

	protected void createGrid() {
		createColumns();
		loadStore();
		addHeaderLinks();
		setGridView();
		addPreviousLink();
		addNextLink();
		addRowListeners();
		addCellListeners();
		displayPageNo();
    	add(headerPanel);    	    	
	}
	
	private void toggleDetails(boolean pressed) {   
        showPreview = pressed;   
        getView().refresh();        
    }   
	
	public void setTopLevel(boolean topLevel) {
		this.topLevel = topLevel;
	}
	
	
	protected void addHeaderLinks() {		
		ClickHandler hyperlinkClickHanlder = new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (project.getUserName().equals("No user")) {
					MessageBox.alert("To post a message you need to be logged in.");
					return;
				}
				createNewPost();
			}
		};
		
		Hyperlink newTopicLink = new Hyperlink("New Topic", "");
		newTopicLink.setStyleName("discussion-link");
		newTopicLink.addClickHandler(hyperlinkClickHanlder);
		headerPanel.add(newTopicLink);
		
		ClickHandler replylinkClickHandler = new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (project.getUserName().equals("No user")) {
					MessageBox.alert("To post a message you need to be logged in.");
					return;
				}
				reply();
			}
		};
		
		Hyperlink replylink = new Hyperlink("Reply", "");
		replylink.setStyleName("discussion-link");
		replylink.addClickHandler(replylinkClickHandler);		
		headerPanel.add(replylink);
	
		ClickHandler expandlinkClickListener = new ClickHandler() {
			public void onClick(ClickEvent event) {
		    	if(expandlink.getText().equalsIgnoreCase("Expand"))	{
		    		expandlink.setText("Collapse");
		    		toggleDetails(true);
		    	}
		    	else {
		    		expandlink.setText("Expand");
		    		toggleDetails(false);
		    	}
			}
		};
		
		expandlink = new Hyperlink("Expand", "");
		expandlink.setStyleName("discussion-link");
		expandlink.addClickHandler(expandlinkClickListener);
		headerPanel.add(expandlink);
    
	}
	
	protected void loadStore() {		
		recordDef = new RecordDef(
				new FieldDef[] {
					new StringFieldDef("entity"),
					new StringFieldDef("subject"),
					new StringFieldDef("author"),
					new StringFieldDef("date"),
					new StringFieldDef("body"),
					new StringFieldDef("type")
				}
			);

		ArrayReader reader = new ArrayReader(recordDef);
		MemoryProxy dataProxy = new MemoryProxy(new Object[][]{});
		MemoryProxy dummyProxy = new MemoryProxy(new Object[][]{});
		store = new Store(dataProxy, reader);
		tempstore = new Store(dummyProxy, reader);
		store.load();
		fillTempStore(0,5);
		setStore(tempstore);

		setHeight(200);
		setAutoWidth(true);
		setAutoScroll(true);
		
		setStripeRows(true);
		setAutoExpandColumn(0);
		setSelectionModel(new RowSelectionModel());
	}
	
	protected void setGridView() {
		GridView view = new GridView() {   
            public String getRowClass(Record record, int index, RowParams rowParams, Store store) {   
                if (showPreview) {
                	rowParams.setBody(Format.format("<STYLE TYPE=\"text/css\">" +
                			"<!--.indented{padding-left: 20pt;}--></STYLE><p>" +
                			"<DIV CLASS=\"indented\">{0}</DIV><br></p></Blockquote>", record.getAsString("body")));
                    return "x-grid3-row-expanded";   
                } else {   
                    return "x-grid3-row-collapsed";   
                }   
            }   
        };   
        view.setForceFit(true);   
        view.setEnableRowBody(true);   
        setView(view);
	}
	
	protected void addNextLink() {
		ClickHandler nextlinkClickListener = new ClickHandler() {
			public void onClick(ClickEvent event) {
				if((store.getCount()-1)/5 <= pageNo) { return; }
				rowSelected = -1;
		    	pageNo++;
		    	prevlink.setVisible(true);
            	fillTempStore(endofpage, endofpage + 5);
            	getView().refresh();
			}
		};
		
		nextlink = new Hyperlink("Next>", "");
		nextlink.setStyleName("discussion-link");
		nextlink.addClickHandler(nextlinkClickListener);
		headerPanel.add(nextlink);	
	}
	
	protected void addPreviousLink() {
		ClickHandler prevlinkClickListener = new ClickHandler() {
			public void onClick(ClickEvent event) {
				if(startofpage <= 0) {return;}
				rowSelected = -1;
				pageNo--;
            	fillTempStore(startofpage - 5, startofpage);
            	nextlink.setVisible(true);
            	getView().refresh();
			}
		};
		
		prevlink = new Hyperlink("<Previous", "");
		prevlink.setStyleName("discussion-link");
		prevlink.addClickHandler(prevlinkClickListener);
		//v	prevlink.setVisible(false);
		headerPanel.add(prevlink);	
	}
	
	
	protected void displayPageNo() {
		if((store.getCount()/5 + 1) == 0) {
    		label.setText("Displaying page 0 of 0 pages");
		} else {
    		label.setText("Displaying page "+(pageNo+1)+" of "+(store.getCount()/5 + 1)+" pages");
		}
    	headerPanel.add(label);
	}
	
	protected void addRowListeners() {
		addGridRowListener(new GridRowListenerAdapter() {
        	
        	public void onRowClick(GridPanel grid, int rowIndex, EventObject e) { 
        		rowSelected = rowIndex;
        		replyEnable = true;
        	}   
        	
        	public void onRowDblClick(GridPanel grid, int rowIndex, EventObject e) {
        		openMessageInNewWindow(grid, rowIndex, e);
        	}
        	
        });

	}

	protected void openMessageInNewWindow(GridPanel grid, int rowIndex, EventObject e){ 
		rowSelected = rowIndex;
		replyEnable = true;
		Record rec = store.getRecordAt(rowSelected + pageNo*5);
		String title = rec.getAsString("subject");		
		title = "Topic: " + title + "  [Type: " + rec.getAsString("type") + "]";

		Panel p = new Panel();
	    p.setHtml(rec.getAsString("body"));
	    
		final Window window = new Window();
		window.setLayout(new FitLayout());
	    window.setTitle(title);  
	    window.setHeight(400);
	    window.setWidth(600);
	    window.setMinHeight(360);
	    window.setMinWidth(550);

		Panel footer = new Panel();
	    addNewWindowListeners(footer, window);
		window.add(p);
		window.add(footer);
		window.setPaddings(5, 5, 5, 40);
	    window.show();

	}
	
	protected void addNewWindowListeners(Panel footer, final Window window) {
	    ClickHandler replyHandler = new ClickHandler() {
			public void onClick(ClickEvent event) {
	        	reply();
	        	window.close();
            
			}
		};
		
		Hyperlink localreplylink = new Hyperlink("Reply", "");
		localreplylink.setStyleName("discussion-link");
		localreplylink.addClickHandler(replyHandler);		

		ClickHandler closeHandler = new ClickHandler() {
			public void onClick(ClickEvent event) {
				window.close();
			}
		};
		
		Hyperlink closelink = new Hyperlink("Close", "");
		closelink.setStyleName("discussion-link");
		closelink.addClickHandler(closeHandler);
					
		footer.setLayout(new HorizontalLayout(15));
		footer.add(localreplylink);
		footer.add(closelink);
		footer.setBodyBorder(true);
		footer.setBorder(true);
		footer.setPaddings(5, 5, 5, 0);

	}
	
	protected void addCellListeners() {
		addGridCellListener(new GridCellListenerAdapter() {   
            public void onCellClick(GridPanel grid, int rowIndex, int colindex,   
                                        EventObject e) {   
                rowSelected = rowIndex;
                replyEnable = true;              
            }   

            public void onCellDblClick(GridPanel grid, int rowIndex,   
			                    int colIndex, EventObject e) {
				rowSelected = rowIndex;
				replyEnable = true;
			}
        }); 
	}

	protected void createColumns() {
		ColumnConfig textCol = new ColumnConfig();
		textCol.setHeader("Subject");
		textCol.setDataIndex("subject");
		textCol.setResizable(true);
		textCol.setSortable(true);
		textCol.setCss("white-space:normal; color: rgb(0,0,255); margin-left: 10px;");
		textCol.setRenderer(renderTopic1);		
		
		ColumnConfig authorCol = new ColumnConfig();
		authorCol.setHeader("Author");
		authorCol.setDataIndex("author");
		authorCol.setResizable(true);
		authorCol.setSortable(true);

		ColumnConfig dateCol = new ColumnConfig();
		dateCol.setHeader("Date");
		dateCol.setDataIndex("date");
		dateCol.setResizable(true);
		dateCol.setSortable(true);
        
		ColumnConfig[] columns = new ColumnConfig[]{textCol, authorCol, dateCol};
		ColumnModel columnModel = new ColumnModel(columns);  
		setColumnModel(columnModel);
	}
	
	private void reply(){			
		if(rowSelected == -1) {	return; }		
		Record rec = store.getRecordAt(rowSelected + pageNo * 5);		
		if(rec == null)  { return; }		
		
		String subject = rec.getAsString("subject");
		if (subject != null) {
			subject = "Re: " + subject; 
		} 
		String text = rec.getAsString("body");		
		if (text != null && text.length() > 0) {
			text = "<br><br>===== " + rec.getAsString("author") + " wrote on " + rec.getAsString("date") + ": ======<br>" + text;
		} else {
			text = "";
		}
		
		showInWindow(true, subject, text);		  
	}
	
	protected ComboBox getTypeComboBox() {
		Store cbstore = new SimpleStore(new String[]{"commentType"}, getCommentTypes() );   
        cbstore.load();   
        ComboBox cb = new ComboBox();        
        cb.setStore(cbstore);
        cb.setForceSelection(true);   
	    cb.setMinChars(1);   
	    cb.setFieldLabel("Type");   
	    cb.setDisplayField("commentType");   
	    cb.setMode(ComboBox.LOCAL);   
	    cb.setTriggerAction(ComboBox.ALL);   
	    cb.setEmptyText("Select Type");   
	    cb.setTypeAhead(true);   
	    cb.setSelectOnFocus(true);
	    cb.setWidth(200);   
	    cb.setHideTrigger(false);	   
        return cb;
	}

	//TODO: change me
	protected void onReplyButton(String str, String subject, String type) {
		if (project.getUserName().equals("No user")) {
			MessageBox.alert("To post a message you need to be logged in.");
			return;
		}
		subject = (subject == null || subject.length() == 0 ? "(no subject)" : subject);
    	String time = getTimeString();
       	Record plant = recordDef.createRecord
       		(new Object[]{"newAnnId", "<FONT COLOR=\"#cc6600\">"+subject+"</FONT>", project.getUserName(), time, str, type});
       	store.insert(rowSelected + 1 + pageNo*5, plant);
    	fillTempStore(startofpage, startofpage + 5);
    	if(store.getCount() % 5 == 1) {
       		nextlink.setVisible(true);
    	}
    	getView().refresh();
    	
    	EntityData annotatedEntity = null;
    	Record selectedAnnotationRecord = store.getRecordAt(rowSelected+pageNo*5);
    	if (selectedAnnotationRecord != null) {
    		String annotatedEntityId =  selectedAnnotationRecord.getAsString("entity");
    		if (annotatedEntityId != null) {
    			annotatedEntity = new EntityData(annotatedEntityId);
    		}
    	}    	
    	if (annotatedEntity == null) {
    		annotatedEntity = new EntityData(_currentEntity.getName());
    	}
    	    	
    	NotesData note = new NotesData(project.getUserName(), str, time, type, subject, null, annotatedEntity, null);
    	OntologyServiceManager.getInstance().createNote(project.getProjectName(), note, false, new CreateNote());
	}
	
	private void createNewPost(){
	   showInWindow(false, "", "");
	}
	
	//TODO: change this
	protected void onPostButton(String messageBody, String subject, String type){
		if (project.getUserName().equals("No user")) {
			MessageBox.alert("To post a message you need to be logged in.");
			return;
		}
		if (subject == null || subject.length() == 0) { subject = "(no subject)";}
		String time = getTimeString();		
   	  	Record plant = recordDef.createRecord(new Object[] {"newAnnId", subject, project.getUserName(), time, messageBody, type});
      	store.insert(0,plant);
    	pageNo = 0;
      	fillTempStore(0,5);
    	if(store.getCount() > 5)
       		nextlink.setVisible(true);
      	getView().refresh();
      	
      	//make the remote call
      	NotesData note = new NotesData(project.getUserName(), messageBody, time, type, subject,
      			null, new EntityData(_currentEntity.getName()), null);
      	OntologyServiceManager.getInstance().createNote(project.getProjectName(), note, topLevel, new CreateNote());
	}
	

	protected void showInWindow(final boolean isReply, String subject, String text) {
		 //create the FormPanel and set the label position to top
        FormPanel formPanel = new FormPanel();
		formPanel.setFrame(true);		
		formPanel.setPaddings(5, 5, 5, 0);
		formPanel.setWidth(600);
		formPanel.setHeight(500);	
    
		final TextField subjectField = new TextField("Subject", "subject");		
		subjectField.setValue(subject);
		subjectField.setWidth(300);
		formPanel.add(subjectField, new AnchorLayoutData("100%"));
		
		final ComboBox typeComboBox = getTypeComboBox();
		formPanel.add(typeComboBox, new AnchorLayoutData("100%"));
       
        final HtmlEditor htmlEditor = new HtmlEditor("", "message");
        htmlEditor.setHideLabel(true);
		htmlEditor.setHeight(200);
		htmlEditor.setValue(text);
		htmlEditor.focus();
		formPanel.add(htmlEditor, new AnchorLayoutData("100% -53"));		

		final Window window = new Window();
		window.setTitle(isReply ? "Reply" : "New topic");
		window.setWidth(600);
		window.setHeight(500);
		window.setMinWidth(300);
		window.setMinHeight(200);
		window.setLayout(new FitLayout());
		window.setPaddings(5);
		window.setButtonAlign(Position.CENTER);
		
		Button send = new Button("Send", new ButtonListenerAdapter() {   
	        public void onClick(Button button, EventObject e) {   
	        	String messageBody = htmlEditor.getValueAsString();
	        	if (isReply) {
	        		onReplyButton(messageBody, subjectField.getText(), typeComboBox.getValueAsString());
	        	} else {
	        		onPostButton(messageBody, subjectField.getText(), typeComboBox.getValueAsString());
	        	}
	          	window.close();
	         }   
	    });  
		
		Button cancel = new Button("Cancel", new ButtonListenerAdapter() {   
		        public void onClick(Button button, EventObject e) {   
		        	window.close();
		         }   
		}); 
		
		window.addButton(send);
		window.addButton(cancel);
		window.setCloseAction(Window.HIDE);
		window.setPlain(true);		
		window.add(formPanel);		
		window.show();
		htmlEditor.focus();
	}
	
	
	void fillTempStore(int i, int j){		
		tempstore.removeAll();
		int gen = 0;
		endofpage = j;
		startofpage = i;
		for(int count = i; count < j; count++)
		{	
			Record record1 = store.getRecordAt(count);
			
			if(record1 != null)	{				
				tempstore.insert(gen++, record1);				
			}			
		}
		int max = store.getCount()/5;
		if(store.getCount()%5 != 0)
			max++;
		
		if(max == 0) {
    		label.setText("Displaying page 0 of 0 pages");
		}
    	else {
    		label.setText("Displaying page "+(pageNo+1)+" of "+max+" pages");
    	}
	}

	private Renderer renderTopic1 = new Renderer() {   
        public String render(Object value, CellMetadata cellMetadata, Record record,   
                                int rowIndex, int colNum, Store store) { 
        	return Format.format("<b>{0}</b>",          
                    new String[]{record.getAsString("subject")                               
                    });   
        }   
    };   
    
	public void setEntity(EntityData newEntity) {
		// Shortcut
		if (_currentEntity != null && _currentEntity.equals(newEntity)) {
			return;
		}
		store.removeAll();
		tempstore.removeAll();		
		_currentEntity = newEntity;
		if (_currentEntity == null) {			
			return;
		}
		reload();
	}

	public void reload() {
		OntologyServiceManager.getInstance().getNotes(project.getProjectName(),
				_currentEntity.getName(), topLevel, new GetNotes());
	}

	private void addReplyToStore(NotesData note, int indent){
		ArrayList<NotesData> replies = note.getReplies();
		Iterator<NotesData> it = replies.iterator();
		String head = "Re: ";
		for(int count = 1; count < indent; count++)
			head = head + "Re: ";
		
		while (it.hasNext()) {
			NotesData n = (NotesData) it.next();
			String time = shortenTime(n.getDate());
			String subject = n.getSubject();
			if (subject == null || subject.length() == 0) { subject = "(no subject)"; }
			Record record = recordDef.createRecord(new Object[] {n.getEntity().getName(),
					 subject, n.getAuthor(), time, n.getBody(), n.getType() });
			store.add(record);
			addReplyToStore(n, indent+1);
		}
	}
	
	private String[][] getCommentTypes() {   
		if (commentTypes == null) {
	        commentTypes = new String[][]{   
	                new String[]{"Comment"},   
	                new String[]{"Question"},
	                new String[]{"Example"},
	                new String[]{"AgreeDisagreeVoteProposal"},
	                new String[]{"AgreeDisagreeVote"}
	        };   
		}	    
         return commentTypes;
    }   

	public void refresh() {
		store.removeAll();
		tempstore.removeAll();		
		reload();
	}
	
	private String shortenTime(String time) {
		return time;	
	}
	
	private String getTimeString()	{
		return "time";
	}	
	
	/*
	 * Asynchronous calls
	 */
	class GetNotes extends AbstractAsyncHandler<ArrayList<NotesData>> {

		public void handleFailure(Throwable caught) {
			GWT.log("Error getting notes for " + _currentEntity, caught);	
		}

		public void handleSuccess(ArrayList<NotesData> notes) {			
			Iterator<NotesData> iterator = notes.iterator();
	
			while (iterator.hasNext()) {
				NotesData note = (NotesData) iterator.next();
				String time = shortenTime(note.getDate());
				String subject = note.getSubject();
				if (subject == null || subject.length() == 0) { subject = "(no subject)"; }
				Record record = recordDef.createRecord(new Object[] {note.getEntity().getName(),
						 subject, note.getAuthor(), time, note.getBody(), note.getType()});
				store.add(record);
				addReplyToStore(note, 1);
				
			}
			if(store.getCount() > 5)
				nextlink.setVisible(true);
			pageNo = 0;
			fillTempStore(0, 5);
			rowSelected = -1;
			getView().refresh();

		}
	}
	
	class CreateNote extends AbstractAsyncHandler{

		public void handleFailure(Throwable caught) {
			GWT.log("Error at creating note", caught);
			com.google.gwt.user.client.Window.alert("There were problems at creating the note.\n" +
					"Please try again later.");
		}
		
		public void handleSuccess(Object result) {
			//TODO - fix later
			GWT.log("Note created successfully", null);
			//TODO - hack to show annotation right away on this client - very hacky, remove
			// the hack does not work
			if (_currentEntity != null) {
				_currentEntity.setHasAnnotation(true);
			}
			store.removeAll();
			tempstore.removeAll();			
			reload();
		}
	}	
}
