package edu.stanford.bmir.protege.web.client.rpc.data.icd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;

public class ScaleInfoData implements Serializable {

	private static final long serialVersionUID = 3711023050201654139L;

	private PropertyEntityData property;
	private LinkedHashMap<String, EntityData> scaleValues;
	private List<EntityData> definitions;

	@SuppressWarnings("unused")
	private ScaleInfoData() {
	}
	
	public ScaleInfoData(List<EntityData> values) {
		this.scaleValues = new LinkedHashMap<String, EntityData>();
		for (EntityData value : values) {
			this.scaleValues.put(value.getName(), value);
		}
		this.definitions = new ArrayList<EntityData>(values.size());
	}

	public void setProperty(PropertyEntityData prop) {
		this.property = prop;
	}
	
	public PropertyEntityData getProperty() {
		return property;
	}
	
	public void setDefinition(String scaleValueIndividual, EntityData scaleValueDefinition) {
		int index = getIndexOf(scaleValueIndividual);
		if (index >= 0) {
			if (index > definitions.size()) {
				for (int i = definitions.size(); i <= index; i++) {
					definitions.add(null);
				}
			}
			definitions.set(index, scaleValueDefinition);
		}
	}

	private int getIndexOf(String scaleValueIndividual) {
		int index = -1;
		if (scaleValueIndividual != null) {
			Collection<String> valueIndividuals = scaleValues.keySet();
			for (Iterator<String> it = valueIndividuals.iterator(); it.hasNext();) {
				index++;
				String valueIndivName = (String) it.next();
				if (scaleValueIndividual.equals(valueIndivName)) {
					break;
				}
			}
		}
		
		return index;
	}
	
	public int getValueCount() {
		return scaleValues.size();
	}
	
	public EntityData getScaleValue(int index) {
		List<String> keyList = new ArrayList<String>(scaleValues.keySet());
		String key = keyList.get(index);
		return scaleValues.get(key);
	}
	
	public EntityData getDefinition(int index) {
		return (index < 0 || index >= definitions.size() ? null : definitions.get(index));
	}
}
