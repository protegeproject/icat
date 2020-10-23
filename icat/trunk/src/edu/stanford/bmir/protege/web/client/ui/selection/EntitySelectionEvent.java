package edu.stanford.bmir.protege.web.client.ui.selection;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;

public class EntitySelectionEvent extends SelectionEvent {
	
	private EntityData selectedEntity;

	public EntitySelectionEvent(Selectable selectable, EntityData selectedEntity) {
		super(selectable);
		this.selectedEntity = selectedEntity;
	}

	public EntityData getSelectedEntity() {
		return selectedEntity;
	}
}
