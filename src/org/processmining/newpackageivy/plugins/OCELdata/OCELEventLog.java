package org.processmining.newpackageivy.plugins.OCELdata;

import java.util.ArrayList;

import java.util.List;
import java.util.Set;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import java.util.Date;

public class OCELEventLog {
	
	public Map<String, OCELEvent> events; 
    public Map<String, OCELObject> objects; 
    public Map<String, OCELObjectType> objectTypes; 
    public Map<String, Object> globalEvent; 
    public Map<String, Object> globalObject; 
    public Map<String, Object> globalLog; 
    public OCELEventLog preFilter;
    
    public OCELEventLog() {
        this.events = new HashMap<String, OCELEvent>();
        this.objects = new HashMap<String, OCELObject>();
        this.objectTypes = new HashMap<String, OCELObjectType>();
        this.globalEvent = new HashMap<String, Object>();
        this.globalObject = new HashMap<String, Object>();
        this.globalLog = new HashMap<String, Object>();
        
        this.globalLog.put("ocel:version", "2.0");
        this.globalLog.put("ocel:ordering", "timestamp");
        this.globalLog.put("ocel:attribute-names", new HashSet<String>());
        this.globalLog.put("ocel:object-types", new HashSet<String>());
        
        this.preFilter = this;
    }
    
    
    
    
    
    
    
    
    public void register() {
    	
        for (String event : this.events.keySet()) {
            this.events.get(event).register();
        }
        
        for (String object : this.objects.keySet()) {
            this.objects.get(object).register();
        }
    }
    

    public OCELEventLog cloneEmpty() {
        OCELEventLog cloned = new OCELEventLog();
        cloned.globalEvent = new HashMap<String, Object>(this.globalEvent);
        cloned.globalObject = new HashMap<String, Object>(this.globalObject);
        cloned.globalLog = new HashMap<String, Object>(this.globalLog);
        cloned.preFilter = this;
        return cloned;
    }
    

    
    
    public void cloneEvent(OCELEvent event) {
        OCELEvent newEvent = event.clone();
        for (OCELObject obj : event.relatedObjects.keySet()) {
            OCELObject newObj = cloneObject(obj);
            newEvent.relatedObjects.put(newObj, event.relatedObjects.get(obj));
            newEvent.relatedObjectsIdentifiers.put(newObj.id, event.relatedObjects.get(obj));
            newObj.relatedEvents.add(newEvent);
        }
        this.events.put(newEvent.id, newEvent);
    }
    

    
    
    public void cloneEvent(OCELEvent event, Set<String> allowedObjectTypes) {
        OCELEvent newEvent = event.clone();
        for (OCELObject obj : event.relatedObjects.keySet()) {
            String thisType = obj.objectType.name;
            if (allowedObjectTypes.contains(thisType)) {
                OCELObject newObj = cloneObject(obj);
                newEvent.relatedObjects.put(newObj, event.relatedObjects.get(obj));
                newEvent.relatedObjectsIdentifiers.put(newObj.id, event.relatedObjects.get(obj));
                newObj.relatedEvents.add(newEvent);
            }
        }
        if (newEvent.relatedObjects.size() > 0) {
            this.events.put(newEvent.id, newEvent);
        }
    }
    

    
    
    public OCELObject cloneObject(OCELObject original) {
        if (!this.objects.containsKey(original.id)) {
            OCELObject newObject = new OCELObject(this);
            newObject.id = original.id;
            OCELObjectType otype = original.objectType;
            if (!this.objectTypes.containsKey(otype.name)) {
                this.objectTypes.put(otype.name, new OCELObjectType(this, otype.name));
            }
            newObject.objectType = this.objectTypes.get(otype.name);
            this.objects.put(newObject.id, newObject);
        }
        return this.objects.get(original.id);
    }
    

    
    public Map<String, OCELEvent> getEvents() {
        return this.events;
    }

    public Map<String, OCELObject> getObjects() {
        return this.objects;
    }


    
    public Set<String> getAttributeNames() {
        return (Set<String>) this.globalLog.get("ocel:attribute-names");
    }


    
    public Set<String> getObjectTypes() {
        return (Set<String>)this.globalLog.get("ocel:object-types");
    }


    
    public String getVersion() {
        return (String)this.globalLog.get("ocel:version");
    }

    public Map<String, Object> getGlobalEvent() {
        return this.globalEvent;
    }

    public Map<String, Object> getGlobalObject() {
        return this.globalObject;
    }


    
    
    
    
    
    
    
    
    
    public Map<String, Integer> computeSummaryStatistics() {
        Map<String, Integer> summary = new HashMap<>();

        int numberOfEvents = this.events.size();
        summary.put("numberOfEvents", numberOfEvents);

        int numberOfObjects = this.objects.size();
        summary.put("numberOfObjects", numberOfObjects);

        int numberOfEventToObjectRelationships = 0;
        for (OCELEvent event : this.events.values()) {
            numberOfEventToObjectRelationships += event.relatedObjects.size();
        }
        summary.put("numberOfEventToObjectRelationships", numberOfEventToObjectRelationships);

        int numberOfObjectToObjectRelationships = 0;
        for (OCELObject object : this.objects.values()) {
            numberOfObjectToObjectRelationships += object.relatedObjectIdentifiers.size();
        }
        summary.put("numberOfObjectToObjectRelationships", numberOfObjectToObjectRelationships);

        int numberOfObjectAttributeChanges = 0;
        for (OCELObject object : this.objects.values()) {
            for (Map<Date, Object> attributeChanges : object.timedAttributes.values()) {
                numberOfObjectAttributeChanges += attributeChanges.size();
            }
        }
        summary.put("numberOfObjectAttributeChanges", numberOfObjectAttributeChanges);

        return summary;
    }
    
   
    
  
    
    
   

    
 

}
