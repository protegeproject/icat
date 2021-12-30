package edu.stanford.bmir.protege.web.client.ui.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.Position;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.Window;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.layout.FitLayout;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.ICDServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;

public class SelectionUtil {

    private static final String NO_PARENT_SELECTED = "<Linearization parent not set>";

    public static void selectClses(Project project, boolean allowMultiple, String topClass, final SelectionCallback callback) {
        final Collection<EntityData> selection = new HashSet<EntityData>();

        final ClassSelectionPanel classSelectionPanel = new ClassSelectionPanel(project, allowMultiple, topClass);
        final Window selectWindow = new com.gwtext.client.widgets.Window();
        selectWindow.setTitle("Select class");
        selectWindow.setWidth(600);
        selectWindow.setHeight(480);
        selectWindow.setMinWidth(300);
        selectWindow.setMinHeight(350);
        selectWindow.setLayout(new FitLayout());
        selectWindow.setPaddings(5);
        selectWindow.setButtonAlign(Position.CENTER);

        selectWindow.setPlain(true);

        com.gwtext.client.widgets.Button cancelButton = new com.gwtext.client.widgets.Button("Cancel");
        cancelButton.addListener(new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button, EventObject e) {
                selectWindow.hide();
                selectWindow.destroy();
            }
        });

        com.gwtext.client.widgets.Button selectButton = new com.gwtext.client.widgets.Button("Select");
        selectButton.addListener(new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button, EventObject e) {
                Collection<EntityData> tmpselection = classSelectionPanel.getSelection();
                if (tmpselection == null || tmpselection.size() == 0) {
                    MessageBox.alert("No selection", "No class selected. Please select a class from the tree.");
                    return;
                } else {
                    for (EntityData sel : tmpselection) {
                        selection.add(sel);
                    }
                    if (callback != null) {
                        callback.onSelect(selection);
                    }
                }
                selectWindow.hide();
                selectWindow.destroy();
            }
        });

        selectWindow.add((Component)classSelectionPanel.getSelectable());
        selectWindow.addButton(selectButton);
        selectWindow.addButton(cancelButton);

        selectWindow.show();
        selectWindow.center();
    }


    public static void selectIndividuals(Project project, Collection<EntityData> clses, 
    		boolean showToolbar, boolean allowMultiple, boolean showClsesPanel, final SelectionCallback callback) {
        final IndividualsWithClassSelectionPanel classSelectionPanel = new IndividualsWithClassSelectionPanel(
        		project, clses, showToolbar, allowMultiple, showClsesPanel);
        final Window selectWindow = new com.gwtext.client.widgets.Window();
        selectWindow.setTitle("Select individuals");
        selectWindow.setWidth(800);
        selectWindow.setHeight(500);
        selectWindow.setLayout(new FitLayout());
        selectWindow.setPaddings(5);
        selectWindow.setButtonAlign(Position.CENTER);
        selectWindow.setPlain(true);

        com.gwtext.client.widgets.Button cancelButton = new com.gwtext.client.widgets.Button("Cancel");
        cancelButton.addListener(new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button, EventObject e) {
                selectWindow.hide();
                selectWindow.destroy();
            }
        });

        com.gwtext.client.widgets.Button selectButton = new com.gwtext.client.widgets.Button("Select");
        selectButton.addListener(new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button, EventObject e) {
                Collection<EntityData> tmpselection = classSelectionPanel.getSelection();
                if (tmpselection == null || tmpselection.size() == 0) {
                    MessageBox.alert("No selection", "No entity selected. Please make a selection in the right panel.");
                    return;
                } else {
                    final Collection<EntityData> selection = new HashSet<EntityData>();
                    for (EntityData sel : tmpselection) {
                        selection.add(sel);
                    }
                    if (callback != null) {
                        callback.onSelect(selection);
                    }
                }
                selectWindow.hide();
                selectWindow.destroy();
            }
        });

        selectWindow.add(classSelectionPanel);
        selectWindow.addButton(selectButton);
        selectWindow.addButton(cancelButton);

        selectWindow.show();
        selectWindow.center();
    }


    public interface SelectionCallback {
        void onSelect(Collection<EntityData> selection);
    }

    public static void selectNewParents(Project project, EntityData subject, EntityData oldParent,
    		boolean showNoParentOption, final SelectionCallback callback) {
    	
    	final ListBox lb = new ListBox();

    	if (subject == null || subject.getName() == null) {
    		return;
    	}
    	
    	OntologyServiceManager.getInstance().getParents(project.getProjectName(), subject.getName(), true, 
    			new AsyncCallback<List<EntityData>>() {
			
    		@Override
			public void onFailure(Throwable caught) {
				MessageBox.alert("Error", "There was an error at retrieving the direct parents.");
			}
    		
			@Override
			public void onSuccess(List<EntityData> parents) {
				createAndDisplayParentsList(parents, oldParent, showNoParentOption, 
						"Select linearization parent (one of the direct Foundation parents)", callback);
			}

		});
    	
    }
    
    public static void selectSuperclass(Project project, EntityData subject, EntityData oldParent,
    		boolean showNoParentOption, final SelectionCallback callback) {
    	
    	final ListBox lb = new ListBox();
    	
    	if (subject == null || subject.getName() == null) {
    		return;
    	}
    	
    	ICDServiceManager.getInstance().getAllSuperEntities(project.getProjectName(), subject,
    			new AsyncCallback<List<EntityData>>() {
    		
    		@Override
    		public void onFailure(Throwable caught) {
    			MessageBox.alert("Error", "There was an error at retrieving the superclasses.");
    		}
    		
    		@Override
    		public void onSuccess(List<EntityData> parents) {
				createAndDisplayParentsList(parents, oldParent, showNoParentOption, 
						"Select precoordination superclass (one of the \"ancestors\" of the class)", callback);
    		}
    		
    	});
    	
    }

	protected static void createAndDisplayParentsList( List<EntityData> parents,
			EntityData oldParent, boolean showNoParentOption, String parentSelectionWindowTitle, final SelectionCallback callback) {
    	final ListBox lb = new ListBox();
		List<EntityData> directParents = new ArrayList<EntityData>();
		
		if (showNoParentOption) {
			lb.addItem(NO_PARENT_SELECTED);
			directParents.add(new EntityData(NO_PARENT_SELECTED, NO_PARENT_SELECTED));
		}
		
		for (EntityData parent : parents) {
			lb.addItem(UIUtil.getDisplayText(parent));
			directParents.add(parent);
		}
		
		int visibileRows = parents.size();
		lb.setVisibleItemCount(visibileRows <= 1 ? 2: visibileRows);
		
		lb.setMultipleSelect(false);
		
		selectOldParent(lb, directParents, oldParent);
		
		showParentsList(parentSelectionWindowTitle, lb, directParents, callback);
    }
    
    private static void selectOldParent(ListBox lb, List<EntityData> directParents, EntityData oldParent) {
    	int index = getOldParentIndex(directParents, oldParent);
    	
    	if (index != -1) {
    		lb.setSelectedIndex(index);
    		lb.setItemText(index, "* " + lb.getItemText(index));
    	}
  	}
    
    private static int getOldParentIndex(List<EntityData> directParents, EntityData oldParent) {
    	if (oldParent == null || oldParent.getName() == null) {
    		return -1;
    	}
    	
    	for (int i = 0; i < directParents.size(); i++) {
			EntityData parent = directParents.get(i);
			if (parent != null && parent.equals(oldParent)) {
				return i;
			}
		}
    	
    	return -1;
    }

	private static void showParentsList(String parentSelectionWindowTitle, final ListBox parentsListBox, final List<EntityData> directParents, SelectionCallback callback) {
    	final Window win = createParentsSelectionWindow(parentSelectionWindowTitle);
        
        Button cancelButton = new Button("Cancel");
        cancelButton.addListener(new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button, EventObject e) {
                win.hide();
                win.close();
            }
        });

        Button selectButton = new Button("Select");
        selectButton.addListener(new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button, EventObject e) {
            	int selectedIndex = parentsListBox.getSelectedIndex();
                
                if (selectedIndex == -1) {
                    MessageBox.alert("No selection", "No class selected. Please select a parent from the list.");
                    return;
                }
                
                EntityData parent = directParents.get(selectedIndex);
                if (NO_PARENT_SELECTED.equals(parent.getName())) { //this is a delete case
                	parent = null;
                }

                List<EntityData> selectedParents = Collections.singletonList(parent);
                callback.onSelect(selectedParents);
                
                win.hide();
                win.close();
            }
        });

        win.add(parentsListBox);
        win.addButton(selectButton);
        win.addButton(cancelButton);
       
        win.setModal(true);
       
        win.show();
        win.center();
        
	}
    
    private static Window createParentsSelectionWindow(String parentSelectionWindowTitle) {
    	Window win = new Window();
        win.setTitle(parentSelectionWindowTitle);
        win.setWidth(425);
        win.setHeight(200);
       
        win.setLayout(new FitLayout());
        win.setPaddings(5);
        win.setButtonAlign(Position.CENTER);

        win.setCloseAction(Window.HIDE);
        win.setPlain(true);
        return win;
    }



}
