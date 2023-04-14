package com.edu.info7255.DataProcessors;

import com.edu.info7255.utils.TimeHexaGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

import static com.edu.info7255.DataProcessors.Constants.*;

public class ObjectIdCreator {

    public static String createObjectIdAndGetNodes(ObjectNode node, List<ObjectNode> nodesToBeSaved, String parentId,
                                                   Map<String, ArrayNode> parentMap) {
        try {
            String objectId = null;
            if (node.has(OBJECT_TYPE)) {
                SchemaContainers schema = SchemaContainers.getJsonSchemaValidator(node.get(OBJECT_TYPE).asText());
                if (schema != null) {
                    objectId = schema.type + ":" + TimeHexaGenerator.getHexaValue();
                    node.put(OBJECT_ID, objectId);
                    if (parentId != null) {
                        //ArrayNode arrayNode = node.get(PARENT_IDS) == null || node.get(PARENT_IDS).isNull()
                        //        ? node.putArray(PARENT_IDS) : (ArrayNode) node.get(PARENT_IDS);
                        JsonNodeFactory jsonFactory = new JsonNodeFactory(false);
                        ArrayNode arrayNode = new ArrayNode(jsonFactory);
                        // what to do when adding a new parent with an pre-existing child
                        arrayNode.add(parentId);
                        parentMap.put(objectId + ":parent", arrayNode);
                    }
                    System.out.println("node being saved = "+node);
                    nodesToBeSaved.add(node);
                }
            }

            Iterator<String> it = node.fieldNames();
            HashMap<String, Object> childIdMap = new HashMap<>();

            while (it.hasNext()) {
                String val = it.next();
                if (PARENT_IDS.equals(val))
                    continue;
                JsonNode currNode = node.get(val);
                JsonNodeFactory factory = new JsonNodeFactory(false);

                if (currNode.isObject()) {
                    //ObjectNode oN = (ObjectNode) currNode;
                    //System.out.println("here I am = "+oN);
                    String childId = createObjectIdAndGetNodes((ObjectNode) currNode, nodesToBeSaved, objectId, parentMap);
                    if (childId != null) {
                        childIdMap.put(val, childId);
                    }
                } else if (currNode.isArray()) {
                    ArrayNode list = new ArrayNode(factory);
                    for (int i = 0; i < currNode.size(); i++) {
                        //System.out.println("here I am = "+currNode.get(i));
                        String childId = createObjectIdAndGetNodes((ObjectNode) currNode.get(i), nodesToBeSaved, objectId, parentMap);
                        if (childId != null) {
                            list.add(childId);
                        }
                    }
                    childIdMap.put(val, list);
                } else if (currNode.isNumber()) {
                    //System.out.println(currNode);
                    //currNode.;
                    //todo mehul do something about the integer values
                }
            }

            addChildIdsInPlaceOfJsonNode(node, childIdMap);
            return objectId;
        } catch (Exception e) {
            System.out.println("Error "+e);
            return null;
        }
    }

    private static void addChildIdsInPlaceOfJsonNode(ObjectNode node, HashMap<String, Object> childIdMap) {

        for (String key : childIdMap.keySet()) {
            node.remove(key);
            if (childIdMap.get(key) instanceof ArrayNode) {
                node.set(key, (ArrayNode) childIdMap.get(key));
            } else {
                node.put(key, (String) childIdMap.get(key));
            }
        }
    }


    public static String getNodesAndPlaceTheirIds(ObjectNode node, Map<String, ObjectNode> nodesPresent, String parentId,
                                                   Map<String, ArrayNode> parentMap) {
        try {
            String objectId = node.get("objectId").asText();
            if (node.has(OBJECT_TYPE)) {
                SchemaContainers schema = SchemaContainers.getJsonSchemaValidator(node.get(OBJECT_TYPE).asText());
                if (schema != null) {
                    if (parentId != null) {
                        //ArrayNode arrayNode = node.get(PARENT_IDS) == null || node.get(PARENT_IDS).isNull()
                        //        ? node.putArray(PARENT_IDS) : (ArrayNode) node.get(PARENT_IDS);
                        JsonNodeFactory jsonFactory = new JsonNodeFactory(false);
                        ArrayNode arrayNode = new ArrayNode(jsonFactory);
                        // what to do when adding a new parent with an pre-existing child
                        arrayNode.add(parentId);
                        parentMap.put(objectId + ":parent", arrayNode);
                    }
                    System.out.println("node being  = "+node);
                    nodesPresent.put(objectId, node);
                }
            }

            Iterator<String> it = node.fieldNames();
            HashMap<String, Object> childIdMap = new HashMap<>();

            while (it.hasNext()) {
                String val = it.next();
                JsonNode currNode = node.get(val);
                JsonNodeFactory factory = new JsonNodeFactory(false);

                if (currNode.isObject()) {
                    //ObjectNode oN = (ObjectNode) currNode;
                    //System.out.println("here I am = "+oN);
                    String childId = getNodesAndPlaceTheirIds((ObjectNode) currNode, nodesPresent, objectId, parentMap);
                    if (childId != null) {
                        childIdMap.put(val, childId);
                    }
                } else if (currNode.isArray()) {
                    ArrayNode list = new ArrayNode(factory);
                    for (int i = 0; i < currNode.size(); i++) {
                        //System.out.println("here I am = "+currNode.get(i));
                        String childId = getNodesAndPlaceTheirIds((ObjectNode) currNode.get(i), nodesPresent, objectId, parentMap);
                        if (childId != null) {
                            list.add(childId);
                        }
                    }
                    childIdMap.put(val, list);
                } else if (currNode.isNumber()) {
                    //System.out.println(currNode);
                    //currNode.;
                    //todo mehul do something about the integer values
                }
            }

            addChildIdsInPlaceOfJsonNode(node, childIdMap);
            return objectId;
        } catch (Exception e) {
            System.out.println("Error "+e);
            return null;
        }
    }


}
