package edu.stanford.bmir.protege.web.client.ui.portlet;

import java.util.Collection;

import com.gwtext.client.widgets.Component;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;

/**
 * The interface for widgets used to display the value of a property at a
 * instance (or class, or property)
 *
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public interface PropertyWidget {

    Collection<EntityData> getValues();

    void setValues(Collection<EntityData> values);

    void setSubject(EntityData subject);

    void setProperty(PropertyEntityData property);

    EntityData getSubject();

    PropertyEntityData getProperty();

    Component getComponent();

    void refresh();

}
