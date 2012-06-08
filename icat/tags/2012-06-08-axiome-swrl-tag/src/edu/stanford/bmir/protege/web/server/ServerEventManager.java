package edu.stanford.bmir.protege.web.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import edu.stanford.bmir.protege.web.client.event.AbstractEvent;
import edu.stanford.bmir.protege.web.client.event.EntityCreateEvent;
import edu.stanford.bmir.protege.web.client.event.EntityDeleteEvent;
import edu.stanford.bmir.protege.web.client.event.EntityRenameEvent;
import edu.stanford.bmir.protege.web.client.event.EventType;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.smi.protege.event.ClsAdapter;
import edu.stanford.smi.protege.event.ClsEvent;
import edu.stanford.smi.protege.event.ClsListener;
import edu.stanford.smi.protege.event.KnowledgeBaseAdapter;
import edu.stanford.smi.protege.event.KnowledgeBaseEvent;
import edu.stanford.smi.protege.event.KnowledgeBaseListener;
import edu.stanford.smi.protege.event.SlotAdapter;
import edu.stanford.smi.protege.event.SlotEvent;
import edu.stanford.smi.protege.event.SlotListener;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.CollectionUtilities;

public class ServerEventManager {
	
	private ServerProject serverProject;	
	private KnowledgeBaseListener kbListener;
	private ClsListener clsListener;
	private SlotListener slotListener;
	private List<AbstractEvent> events;
	
	/*
	 * We should start with a version number (not 0)
	 */
	public ServerEventManager(ServerProject serverProject) {
		this.serverProject = serverProject;
		this.events = new ArrayList<AbstractEvent>();
		createListeners();
		addListeners();
	}
	
	private void createListeners() {
		kbListener = createKBListener();
		clsListener = createClsListener();
		slotListener = createSlotListener();
	}

	private KnowledgeBaseListener createKBListener() {
		kbListener = new KnowledgeBaseAdapter() {
			@Override
			public void clsCreated(KnowledgeBaseEvent event) {
				if (event.isReplacementEvent()) return;
				events.add(createEvent(event, EventType.CLASS_CREATED, 
						getEntityDataList(event.getCls().getDirectSuperclasses())));
			}
			
			@Override
			public void slotCreated(KnowledgeBaseEvent event) {
				if (event.isReplacementEvent()) return;
				events.add(createEvent(event, EventType.PROPERTY_CREATED,
						getEntityDataList(event.getSlot().getDirectSuperslots())));
			}
			
			@Override
			public void instanceCreated(KnowledgeBaseEvent event) {
				
				events.add(createEvent(event, EventType.INDIVIDUAL_CREATED, null));
			}
			
			@Override
			public void clsDeleted(KnowledgeBaseEvent event) {
				if (event.isReplacementEvent()) return;
				events.add(deleteEvent(event, EventType.CLASS_DELETED, 
						getEntityDataList(event.getCls().getDirectSuperclasses())));
			}
			
			@Override
			public void slotDeleted(KnowledgeBaseEvent event) {
				if (event.isReplacementEvent()) return;
				events.add(deleteEvent(event, EventType.PROPERTY_DELETED, 
						getEntityDataList(event.getSlot().getDirectSuperslots())));		
				}
			
			@Override
			public void frameReplaced(KnowledgeBaseEvent event) {
				events.add(replaceEvent(event, EventType.ENTITY_RENAMED, event.getOldName()));
			}
		};			
		return kbListener;		
	}
	
	private ClsListener createClsListener() {
		clsListener = new ClsAdapter() {					
			@Override
			public void directSubclassRemoved(ClsEvent event) {
				if (event.isReplacementEvent()) return;
				events.add(createEvent(event, EventType.SUBCLASS_REMOVED, 
						getEntityDataList(CollectionUtilities.createCollection(event.getSubclass())))); //3rd arg - the subclass
			}
			
			@Override
			public void directSubclassAdded(ClsEvent event) {
				if (event.isReplacementEvent()) return;
				events.add(createEvent(event, EventType.SUBCLASS_ADDED, 
						getEntityDataList(CollectionUtilities.createCollection(event.getSubclass()))));				
			}
		};		
		return clsListener;
	}

	private SlotListener createSlotListener() {
		slotListener = new SlotAdapter() {					
			@Override
			public void directSubslotRemoved(SlotEvent event) {
				if (event.isReplacementEvent()) return;
				events.add(createEvent(event, EventType.SUBPROPERTY_REMOVED, 
						getEntityDataList(CollectionUtilities.createCollection(event.getSubslot())))); //3rd arg - the subclass
			}
			
			@Override
			public void directSubslotAdded(SlotEvent event) {
				if (event.isReplacementEvent()) return;
				events.add(createEvent(event, EventType.SUBPROPERTY_ADDED, 
						getEntityDataList(CollectionUtilities.createCollection(event.getSubslot()))));				
			}
		};		
		return slotListener;
	}

	
	public void startListening() {
		addListeners();
	}
	
	private void addListeners() {
		KnowledgeBase kb = serverProject.getProject().getKnowledgeBase();		
		kb.addKnowledgeBaseListener(kbListener);
		kb.addClsListener(clsListener);
		kb.addSlotListener(slotListener);
	}
	
	private void removeListeners() {
		KnowledgeBase kb = serverProject.getProject().getKnowledgeBase();		
		kb.removeKnowledgeBaseListener(kbListener);	
		kb.removeClsListener(clsListener);
	}
	
	
	public int getServerVersion() {
		return events.size();
	}
	
	public ArrayList<AbstractEvent> getEvents(long fromVersion) {
		return getEvents(fromVersion, events.size());
	}
		
	public ArrayList<AbstractEvent> getEvents(long fromVersion, long toVersion) {
		ArrayList<AbstractEvent> fromToEvents  = new ArrayList<AbstractEvent>();
			
		//TODO: check these conditions
		if (fromVersion < 0) {
			fromVersion = 0;
		}		
		if (toVersion > events.size()) {
			toVersion = events.size();
		}		
		for (long i = fromVersion; i < toVersion; i++) {
			fromToEvents.add(events.get((int)i)); //fishy
		}		
		
		//Log.getLogger().info("SERVER: GetEvents from: " + fromVersion + " to: " + toVersion + " events size: " + events.size() + " Events: " + fromToEvents);
		
		return fromToEvents;
	}
	
	public void dispose() {
		removeListeners();
	}
	
	/*
	 * Utility methods
	 */
	ArrayList<EntityData> getEntityDataList(Collection frames) {
		if (frames == null) {
			return null;
		}
		
		ArrayList<EntityData> entityDataList = new ArrayList<EntityData>();
		
		for (Iterator iterator = frames.iterator(); iterator.hasNext();) {			
			entityDataList.add(OntologyServiceImpl.createEntityData(iterator.next(), false));						
		}
		return entityDataList;
	}
	
	private AbstractEvent createEvent(KnowledgeBaseEvent event, int type, 
			ArrayList<EntityData> superEntities) {
		EntityData entity = OntologyServiceImpl.createEntityData(event.getFrame());
		return new EntityCreateEvent(entity, type, event.getUserName(), superEntities);
	}

	private AbstractEvent createEvent(ClsEvent event, int type, 
			ArrayList<EntityData> superEntities) {
		EntityData entity = OntologyServiceImpl.createEntityData(event.getCls());
		//TODO: change type of event
		return new EntityCreateEvent(entity, type, event.getUserName(), superEntities);
	}
	
	private AbstractEvent createEvent(SlotEvent event, int type, 
			ArrayList<EntityData> superEntities) {
		EntityData entity = OntologyServiceImpl.createEntityData(event.getSlot());
		//TODO: change type of event
		return new EntityCreateEvent(entity, type, event.getUserName(), superEntities);
	}
	
	private AbstractEvent deleteEvent(KnowledgeBaseEvent event, int type, 
			ArrayList<EntityData> superEntities) {
		EntityData entity = OntologyServiceImpl.createEntityData(event.getFrame());
		return new EntityDeleteEvent(entity, type, event.getUserName(), superEntities);
	}

	private AbstractEvent replaceEvent(KnowledgeBaseEvent event, int type, String oldName) {
		EntityData entity = OntologyServiceImpl.createEntityData(event.getNewFrame());
		return new EntityRenameEvent(entity, oldName, event.getUserName());
	}

	
}
