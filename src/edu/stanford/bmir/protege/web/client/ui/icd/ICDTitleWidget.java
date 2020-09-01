package edu.stanford.bmir.protege.web.client.ui.icd;

import java.util.Collection;

import com.google.gwt.user.client.ui.Anchor;
import com.gwtext.client.core.Position;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.MessageBox.AlertCallback;
import com.gwtext.client.widgets.layout.FitLayout;

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.model.event.EntityRenameEvent;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;
import edu.stanford.bmir.protege.web.client.ui.ontology.notes.NoteInputPanel;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.AbstractFieldWidget;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.InstanceTextFieldWidget;

public class ICDTitleWidget extends InstanceTextFieldWidget {

    public ICDTitleWidget(Project project) {
        super(project);
    }

    @Override
    protected void deletePropertyValue(final EntityData subject, String propName, ValueType propValueType,
            EntityData oldEntityData, Object oldDisplayedValue, String operationDescription) {
        MessageBox.alert("Change Title Error",
                "The title of an ICD entity cannot be empty!<BR>Please specifiy a valid title.",
                new AlertCallback() {
                    public void execute() {
                        if (subject.equals(getSubject())) {
                            displayValues();
                        }
                    }
                });
    }

	@Override
	protected void replacePropertyValue(final EntityData subject, final String propName, final ValueType propValueType,
			final EntityData oldEntityData, final EntityData newEntityData, final Object oldDisplayedValue,
			final String operationDescription) {

		
		MessageBox.confirm("Change Title Warning",
				"<DIV>"
						+ "You should change the title of an entity "
						+ "only if you are <b>not changing the current meaning</b> of the title "
						+ "(for example if there is a typo in the existing title or there is "
						+ "a better or more commonly accepted name for this category).<BR /><BR />"
						
						+ "<b>Does the modified title preserve the current meaning of the category?</b></DIV>",
				new MessageBox.ConfirmCallback() {
					public void execute(String btnID) {
						if (btnID.equalsIgnoreCase("Yes")) {
							EntityData oldInstanceEntityData = findInstanceForValue(oldEntityData);
							
							//should never happen
							if (oldInstanceEntityData == null) {
								if (subject.equals(getSubject())) {
									displayValues();
								}
								return;
							}

							setLoadingStatus(true);
							getField().setReadOnly(true);
							
							propertyValueUtil.replacePropertyValue(getProject().getProjectName(), oldInstanceEntityData.getName(),
									getDisplayProperty(), null, oldEntityData.toString(), newEntityData.toString(),
									false, GlobalSettings.getGlobalSettings().getUserName(), operationDescription,
									new ReplaceTitleHandler(subject, oldInstanceEntityData,
											oldEntityData, newEntityData, getValues()));
							
							requestComment(oldInstanceEntityData);
							
						} else {
							if (subject.equals(getSubject())) {
								displayValues();
							}
						}
					}
				});
	}

    private void requestComment(EntityData entity) {
        Collection<EntityData> values = getValues();
        String annotEntityName = null;
        if (values.size() > 0) {
            annotEntityName = values.iterator().next().getName();
        }

        final com.gwtext.client.widgets.Window window = new com.gwtext.client.widgets.Window() {
            @Override
            public void close() {
                ICDTitleWidget.this.refresh();
                super.close();
            }
        };
        window.setTitle("Reason for changing the title");
        window.setWidth(600);
        window.setHeight(400);//(480);
        window.setMinWidth(300);
        window.setMinHeight(350);
        window.setLayout(new FitLayout());
        window.setPaddings(5);
        window.setButtonAlign(Position.CENTER);

        //window.setCloseAction(Window.HIDE);
        window.setPlain(true);
        window.setModal(true);

        NoteInputPanel nip = new NoteInputPanel(getProject(), "Please enter the rationale for changing the title:", false,
                new EntityData(annotEntityName), window);
        nip.setSubject("[Reason for title change] ");
        nip.setNoteType("Explanation");
        window.add(nip);
        window.show();
    }


    @Override
    protected Anchor createDeleteHyperlink() {
        Anchor deleteLink = new Anchor("&nbsp<img src=\"images/delete_grey.png\" " + AbstractFieldWidget.DELETE_ICON_STYLE_STRING + "></img>", true);
        deleteLink.setWidth("22px");
        deleteLink.setHeight("22px");
        deleteLink.setTitle("Delete title value is not allowed");
        return deleteLink;
    }
    
    
    //*********** Async calls ************/
    protected class ReplaceTitleHandler extends ReplaceInstancePropertyValueHandler {

		public ReplaceTitleHandler(EntityData subject, EntityData changeSubject, EntityData oldEntityData,
				EntityData newEntityData, Collection<EntityData> oldValues) {
			super(subject, changeSubject, oldEntityData, newEntityData, oldValues);
		}
		
		@Override
		public void onSuccess(Void result) {
			super.onSuccess(result);
			
			subject.setBrowserText(newEntityData.getName());
			
			//fire rename to update class tree
			getProject().fireOntologyEvent(new EntityRenameEvent(subject, subject.getName(), GlobalSettings.getGlobalSettings().getUserName(), 0 ));
		}
    	
    }
}
