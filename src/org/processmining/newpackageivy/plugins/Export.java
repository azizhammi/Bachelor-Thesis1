package org.processmining.newpackageivy.plugins;

import org.graphstream.graph.Node;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;

import java.util.*;


public class Export {
	
	
    private Graph graph;

    public Export(Graph graph) {
        this.graph = graph; 
    }
    
    public void JSONExport(String filePath) {
    	
        JSONObject JSONExport = new JSONObject();
        JSONArray EventTypes = new JSONArray();
        JSONArray ObjectTypes = new JSONArray();
        JSONArray Events = new JSONArray();
        JSONArray Objects = new JSONArray();
 
        
        Map<String, Set<String>> eventTypeToAttributes = new HashMap<>();
        Map<String, Set<String>> objectTypeToAttributes = new HashMap<>();

        
        //Go Through Every Node
        
        for (Node node : graph) {
        	
            String nodeID = node.getAttribute("ui.label").toString();

            
            //An Event Node?
            if (node.hasAttribute("Activity")) {
            	
                JSONObject EventJson = new JSONObject();

                String EventType = node.getAttribute("Activity").toString();    
                
                EventJson.put("id", nodeID);
                EventJson.put("type", EventType);
                
                //Add Event type, If Not Already Added
                eventTypeToAttributes.computeIfAbsent(EventType, k -> new HashSet<>());

                //Add Event Timestamp
                String Timestamp = node.hasAttribute("Timestamp") ? node.getAttribute("Timestamp").toString() : null;
                EventJson.put("time", Timestamp != null ? ISODate(Timestamp) : "");

                //Add Other Attributes
                JSONArray Attributes = new JSONArray();
                
                for (String AttributeKey : node.attributeKeys().collect(Collectors.toList())) {
                	
                	//Verify If Not Already Added
                    if (Arrays.asList("ui.class", "ui.label", "xyz", "Activity", "Timestamp").contains(AttributeKey)) {
                        continue;
                    }

                    Object AttributeValue = node.getAttribute(AttributeKey);
                    if (AttributeValue != null) {
                    	
                        JSONObject attribute = new JSONObject();
                        
                        attribute.put("name", AttributeKey);
                        attribute.put("value", AttributeValue.toString());
                        Attributes.put(attribute);

                        eventTypeToAttributes.get(EventType).add(AttributeKey);

                    }
                }
                
                EventJson.put("attributes", Attributes);

                // Add E2O Relationships
                JSONArray Relationships = new JSONArray();
                
                for (Edge edge : node.edges().collect(Collectors.toList())) {
                	
                	//Get Connected Node
                    Node target = edge.getOpposite(node);
                    
                    //E2O
                    if (!target.hasAttribute("Activity")) {
                        JSONObject relationship = new JSONObject();
                        relationship.put("objectId", target.getAttribute("ui.label").toString());
                        relationship.put("qualifier", edge.getAttribute("ui.label").toString());
                        Relationships.put(relationship);
                    }
                }
                
                EventJson.put("relationships", Relationships);

                Events.put(EventJson);
                
                
              //An Object Node  
            } else if (node.hasAttribute("Type")) {
               
                JSONObject ObjectJson = new JSONObject();

                String ObjectType = node.getAttribute("Type").toString();
                ObjectJson.put("id", nodeID);
                ObjectJson.put("type", ObjectType);

                //Add Other Attributes
                JSONArray Attributes = new JSONArray();
                
                for (String AttributeKey : node.attributeKeys().collect(Collectors.toList())) {
                	
                	//Verify If Not Already Added
                    if (Arrays.asList("ui.class", "ui.label", "xyz", "Type").contains(AttributeKey)) {
                        continue;
                    }

                    Object AttributeValue = node.getAttribute(AttributeKey);
                    
                    if (AttributeValue != null) {
                        if (AttributeValue instanceof Map) {
                       
                            Map<String, String> timedMap = (Map<String, String>) AttributeValue;
                            
                            for (Map.Entry<String, String> entry : timedMap.entrySet()) {
                                JSONObject attr = new JSONObject();
                                attr.put("name", AttributeKey);
                                attr.put("value", entry.getValue());
                                attr.put("time", ISODate(entry.getKey()));
                                Attributes.put(attr);
                            }
                            
                        } else if (AttributeValue instanceof String && isTimedAttribute(AttributeValue.toString())) {
                        	
                            Map<String, String> parsed = parseTimedAttribute(AttributeValue.toString());
                            
                            for (Map.Entry<String, String> entry : parsed.entrySet()) {
                                JSONObject attr = new JSONObject();
                                attr.put("name", AttributeKey);
                                attr.put("value", entry.getValue());
                                attr.put("time", ISODate(entry.getKey()));
                                Attributes.put(attr);
                            }
                            
                        } else {
                        	
                            // Static Attributes
                            JSONObject attr = new JSONObject();
                            attr.put("name", AttributeKey);
                            attr.put("value", AttributeValue.toString());
                            Attributes.put(attr);
                        }

                        //Object Type Attribute
                        objectTypeToAttributes
                                .computeIfAbsent(ObjectType, k -> new HashSet<>())
                                .add(AttributeKey);
                    }
                }
                
                ObjectJson.put("attributes", Attributes);

                // O2O Relationships
                
                JSONArray Relationships = new JSONArray();
                
                for (Edge edge : node.leavingEdges().collect(Collectors.toList())) { 
                    
                	//Get Neighbor Node
                    Node Neighbor = edge.getTargetNode(); 
                    	
                    
                    JSONObject relationship = new JSONObject();
                    relationship.put("objectId", Neighbor.getAttribute("ui.label").toString());
                    relationship.put("qualifier", edge.getAttribute("ui.label").toString());
                    Relationships.put(relationship);
                      
                    
                }
                ObjectJson.put("relationships", Relationships);

                Objects.put(ObjectJson);
            }
        }

        //Event Types
        
        for (String eventType : eventTypeToAttributes.keySet()) {
            JSONObject EventTypeJson = new JSONObject();
            
            EventTypeJson.put("name", eventType);
            
            EventTypes.put(EventTypeJson);
        }


        //Object Types
        
        for (Map.Entry<String, Set<String>> entry : objectTypeToAttributes.entrySet()) {
            JSONObject ObjectTypeJson = new JSONObject();
            
            ObjectTypeJson.put("name", entry.getKey());

            JSONArray Attributes = new JSONArray();
            
            for (String attribute : entry.getValue()) {
                JSONObject AttributeJson = new JSONObject();
                
                AttributeJson.put("name", attribute);
                AttributeJson.put("type", "string"); // Adjust types if necessary
                Attributes.put(AttributeJson);
            }

            ObjectTypeJson.put("attributes", Attributes);
            ObjectTypes.put(ObjectTypeJson);
        }

        
        JSONExport.put("eventTypes", EventTypes);
        JSONExport.put("objectTypes", ObjectTypes);
        JSONExport.put("events", Events);
        JSONExport.put("objects", Objects);

        
        //Write file
        try (FileWriter file = new FileWriter(filePath)) {
        	
            file.write(JSONExport.toString(4));
            file.flush();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isTimedAttribute(String value) {
        return value != null && value.contains(":") && value.contains(";");
    }

    
    
    private Map<String, String> parseTimedAttribute(String AttributeValue) {
    	
        Map<String, String> timedAttribute = new HashMap<>();

        if (AttributeValue == null || AttributeValue.isEmpty()) {
            return timedAttribute;
        }

        String[] entries = AttributeValue.split(";");

        for (String entry : entries) {
        	
            entry = entry.trim();
            
            if (entry.isEmpty()) continue;

            int Divider = entry.lastIndexOf(":");

            if (Divider != -1) {
                String Date = entry.substring(0, Divider).trim();
                String Value = entry.substring(Divider + 1).trim();

                timedAttribute.put(Date, Value);
            }
        }

        return timedAttribute;
    }

    
    private String ISODate(String Date) {
        try {
        	
        	//Get Date
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
            Date date = inputFormat.parse(Date);


            //Convert It
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            return outputFormat.format(date);
            
        } catch (ParseException e) {
            e.printStackTrace();
            return Date; 
        }
    }
    
}
