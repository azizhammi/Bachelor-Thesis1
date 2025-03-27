package org.processmining.newpackageivy.plugins.OCELdata;

import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import java.util.Set;
import java.util.HashSet;


public class OCELObjectType {

	
    public OCELEventLog eventLog;
    public String name;
    public Set<OCELObject> objects;
    
    public Map<String, Object> attributes; 


   
    
    public OCELObjectType(OCELEventLog eventLog, String name) {
        this.eventLog = eventLog;
        this.name = name; 
        this.objects = new HashSet<OCELObject>(); 
        
        this.attributes = new HashMap<>(); 
        
    }
    
   
}
