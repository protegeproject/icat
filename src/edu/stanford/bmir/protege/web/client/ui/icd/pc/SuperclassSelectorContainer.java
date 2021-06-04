package edu.stanford.bmir.protege.web.client.ui.icd.pc;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;

public interface SuperclassSelectorContainer {
	
	public SuperclassSelectorWidget createSuperClassSelectorWidget();
	
	public void onSuperclassChanged(EntityData newSuperclass);

}
