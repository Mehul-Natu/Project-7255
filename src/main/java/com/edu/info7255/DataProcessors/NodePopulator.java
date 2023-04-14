package com.edu.info7255.DataProcessors;

import com.edu.info7255.Dao.Dao;
import com.edu.info7255.utils.Pair;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.ValidationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.edu.info7255.DataProcessors.Constants.OBJECT_TYPE;

@Component
public class NodePopulator {

    public static void populate(ObjectNode node, Dao dao) {
        try {
            SchemaContainers schema = SchemaContainers.getJsonSchemaValidator(node.get(OBJECT_TYPE).asText());

            for (String field : schema.entityFields) {
                JsonNode currNode = node.get(field);
                if (currNode.isTextual()) {
                    JsonNode jsonNode = dao.get(currNode.asText());
                    if (jsonNode != null || !jsonNode.isNull()) {
                        node.replace(field, jsonNode);
                    }
                } else if (currNode.isArray()) {
                    ArrayNode nodes = new ArrayNode(new JsonNodeFactory(false));
                    for (int i = 0; i < currNode.size(); i++) {
                        JsonNode jsonNode = dao.get(currNode.get(i).asText());
                        if (jsonNode != null || !jsonNode.isNull()) {
                            nodes.add(jsonNode);
                        }
                    }
                    node.replace(field, nodes);
                }
            }

            for (String field : schema.entityFields) {
                JsonNode currNode = node.get(field);
                if (currNode.isObject()) {
                    populate((ObjectNode) currNode, dao);
                } else if (currNode.isArray()) {
                    for (int i = 0; i < currNode.size(); i++) {
                        populate((ObjectNode) currNode.get(i), dao);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Exception while population by ID:"+e);
        }
    }

    public static void fetchChildParentId(ObjectNode node, List<String> ids) {
        try {
            SchemaContainers schema = SchemaContainers.getJsonSchemaValidator(node.get(OBJECT_TYPE).asText());

            for (String field : schema.entityFields) {
                JsonNode currNode = node.get(field);
                if (currNode.isTextual()) {
                    ids.add(currNode.asText());
                } else if (currNode.isArray()) {
                    for (int i = 0; i < currNode.size(); i++) {
                        ids.add(currNode.get(i).asText());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Exception while population by ID:" + e);
        }
    }

    public static void fetchAllChildIds(ObjectNode node, List<String> ids, Dao dao) {
        try {
            SchemaContainers schema = SchemaContainers.getJsonSchemaValidator(node.get(OBJECT_TYPE).asText());

            for (String field : schema.entityFields) {
                JsonNode currNode = node.get(field);
                if (currNode.isTextual()) {
                    ids.add(currNode.asText());
                    ObjectNode childNode = dao.get(currNode.asText());
                    fetchAllChildIds(childNode, ids, dao);
                } else if (currNode.isArray()) {
                    for (int i = 0; i < currNode.size(); i++) {
                        ids.add(currNode.get(i).asText());
                        ObjectNode childNode = dao.get(currNode.get(i).asText());
                        fetchAllChildIds(childNode, ids, dao);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Exception while population by ID:" + e);
        }
    }


    public static void deleteChildParentId(List<String> ids, Dao dao, String parentId) {
        try {
            for (String id : ids) {
                String parentKey = id + ":parent";
                ArrayNode parentIds = dao.getArrayNode(parentKey);
                dao.delete(parentKey);
                //this meant when there could be two parents for the same child
                /*JsonNode currNode = dao.get(id);
                for (int i = 0; i < parentIds.size(); i++) {
                    if (parentId.equals(parentIds.get(i).asText())) {
                        parentIds.remove(i);
                        break;
                    }
                }
                dao.saveParentKey(parentKey, parentIds);
                 */
            }
        } catch (Exception e) {
            System.out.println("Exception while population by ID:" + e);
            throw e;
        }
    }

    public static Map<String, HashMap<String, JsonNode>> getEntityFieldsPresentInObject(String objectType, ObjectNode node) {
        Map<String, HashMap<String, JsonNode>> map = new HashMap<>();
        try {
            SchemaContainers schemaValidator = SchemaContainers.getJsonSchemaValidator(objectType);
            if (schemaValidator == null) {
                throw new RuntimeException("No such Schema present " + objectType);
            }
            List<String> entityFields = schemaValidator.entityFields;

            for (String entity :  entityFields) {
                if (node.has(entity)) {
                    JsonNode objectNode = schemaValidator.objectNode.get("properties").get(entity);
                    if (objectNode.has("type") && "array".equals(objectNode.get("type").asText())) {
                        List<Pair<String, JsonNode>> listOfObjectId = new LinkedList<>();
                        ArrayNode nodes = (ArrayNode) node.get(entity);
                        HashMap<String, JsonNode> idsMap = new HashMap<>();
                        for (int i = 0; i < nodes.size(); i++) {
                            idsMap.put(nodes.get(i).get("objectId").asText(), nodes.get(i));
                        }
                        map.put(entity, idsMap);
                    } else {
                        HashMap<String, JsonNode> idsMap = new HashMap<>();
                        idsMap.put(node.get(entity).get("objectId").asText(), node.get(entity));
                        map.put(entity, idsMap);
                    }

                }
            }

        } catch (Exception e) {

        }
        return map;
    }

    public static void patch(ObjectNode patchNode, ObjectNode patchNodeWithIds, ObjectNode savedNode,
                                      List<ObjectNode> listOfNodeToBeSaved, Map<String, ArrayNode> parentsToBeSaved) {
        try {
            boolean haveChanges = false;
            Iterator<String> filedNames = patchNodeWithIds.fieldNames();
            SchemaContainers schema = SchemaContainers.getJsonSchemaValidator(patchNodeWithIds.get("objectType").asText());
            if (schema == null) {
                throw new RuntimeException("Wrong ObjectType");
            }
            while (filedNames.hasNext()) {
                String field = filedNames.next();
                if ("objectType".equals(field) || "objectId".equals(field)) {
                    continue;
                }
                if (savedNode.has(field)) {
                    String type = SchemaContainers.getTypeOfField(schema, field);
                    if (type == null) {
                        continue;
                    }
                    //here to generify traversing into each object node in array or be in object node and do the same
                    switch (type) {
                        case "array" :
                            ArrayNode patchNodeIds = ((ArrayNode) patchNodeWithIds.get(field));
                            HashSet<String> savedNodeIdsSet = getIdsFromArrayNode(((ArrayNode) savedNode.get(field)));
                            for (int i = 0; i < patchNodeIds.size(); i++) {
                                if (!savedNodeIdsSet.contains(patchNodeIds.get(i).asText())) {
                                    if (validate(patchNode)) {
                                        ArrayNode patchNodeArray = (ArrayNode) patchNode.get(field);
                                        String objectId = createNodeWithIds(listOfNodeToBeSaved, parentsToBeSaved, patchNodeArray.get(i),
                                                patchNode.get("objectId").asText());
                                        ((ArrayNode) savedNode.get(field)).add(objectId);
                                    }
                                }
                                //listOfNodeToBeSaved.add(objectNodesPresentPatch.get(patchNodeIds.get(i).asText()));
                            }
                            break;
                    };
                } else {
                    System.out.println("No such field available : "+field);
                }
            }
        } catch (Exception e) {
            System.out.println("exception While Merge Patch:" + e);
        }
    }



    public static void update(ObjectNode patchNode, ObjectNode savedNode,
                                  Map<String, ObjectNode> objectNodesPresentPatch,
                                  List<ObjectNode> listOfNodeToBeSaved) {
        try {
            boolean haveChanges = false;
            Iterator<String> filedNames = patchNode.fieldNames();
            SchemaContainers schema = SchemaContainers.getJsonSchemaValidator(patchNode.get("objectType").asText());
            if (schema == null) {
                throw new RuntimeException("Wrong ObjectType");
            }
            while (filedNames.hasNext()) {
                String field = filedNames.next();
                if ("objectType".equals(field) || "objectId".equals(field)) {
                    continue;
                }
                if (savedNode.has(field)) {
                    String type = SchemaContainers.getTypeOfField(schema, field);
                    if (type == null) {
                        continue;
                    }
                    switch (type) {
                        case "array" :
                            ArrayNode patchNodeIds = ((ArrayNode) patchNode.get(field));
                            HashSet<String> savedNodeIdsSet = getIdsFromArrayNode(((ArrayNode) savedNode.get(field)));
                            for (int i = 0; i < patchNodeIds.size(); i++) {
                                Dao dao = new Dao();
                                ObjectNode savedChildNode  = dao.get(patchNodeIds.get(i).asText());
                                if (savedChildNode == null) {
                                    continue;
                                }

                                if (savedNodeIdsSet.contains(patchNodeIds.get(i).asText())) {
                                    // this is going to be special case where we need to do something different
                                    update(objectNodesPresentPatch.get(patchNodeIds.get(i).asText()), savedChildNode,
                                            objectNodesPresentPatch, listOfNodeToBeSaved);
                                }
                                //listOfNodeToBeSaved.add(objectNodesPresentPatch.get(patchNodeIds.get(i).asText()));
                            }
                            break;
                        case "object" :
                            Dao dao = new Dao();
                            ObjectNode savedChildNode  = dao.get(patchNode.get(field).asText());
                            if (savedChildNode == null) {
                                continue;
                            }

                            update(objectNodesPresentPatch.get(patchNode.get(field).asText()),
                                    savedChildNode, objectNodesPresentPatch, listOfNodeToBeSaved);
                            /*
                            if (savedNode.get(field).asText().equals(patchNode.get(field).asText())) {
                                listOfNodeToBeSaved.add(objectNodesPresentPatch.get(patchNode.get(field).asText()));
                            }
                             */
                            break;
                        case "number" :
                        case "string" :
                            savedNode.replace(field, patchNode.get(field));
                            haveChanges = true;
                            break;
                        default :
                            System.out.println("No such filed");
                            break;
                    };
                } else {
                    System.out.println("No such field available : "+field);
                }
            }
            if (haveChanges) {
                listOfNodeToBeSaved.add(savedNode);
            }

        } catch (Exception e) {
            System.out.println("exception While Merge Patch:" + e);
        }
    }

    private static boolean validate(ObjectNode objectNode) {
        List<String> schemaPresentList = new LinkedList<>();
        Set<ValidationMessage> validationMessages = new HashSet<>();
        JsonSchemaValidator.validate(objectNode, schemaPresentList, validationMessages);
        return schemaPresentList.size() != 0 && validationMessages.size() == 0;
    }


    //returning objectId of highest object in hierarchy
    private static String createNodeWithIds(List<ObjectNode> listOfNodesToBeSaved, Map<String, ArrayNode> parentMap,
                                          JsonNode requestBody, String parentId) {
        return ObjectIdCreator.createObjectIdAndGetNodes((ObjectNode) requestBody, listOfNodesToBeSaved, parentId, parentMap);
    }


    private static HashSet<String> getIdsFromArrayNode(ArrayNode arrayNode) {
        HashSet<String> hashSet = new HashSet<>();
        for (JsonNode jsonNode : arrayNode) {
            hashSet.add(jsonNode.asText());
        }
        return hashSet;
    }

    /**
     * this updates and can add nodes to existing nodes, if the node wich want to add already exist
     * @param patchNode
     * @param savedNode
     * @param objectNodesPresentPatch
     * @param parentIds
     * @param listOfNodeToBeSaved
     * @param parentsToBeSaved
     */
    public static void updatePatchOld(ObjectNode patchNode, ObjectNode savedNode,
                                      Map<String, ObjectNode> objectNodesPresentPatch, Map<String, ArrayNode> parentIds,
                                      List<ObjectNode> listOfNodeToBeSaved, Map<String, ArrayNode> parentsToBeSaved) {
        try {
            boolean haveChanges = false;
            Iterator<String> filedNames = patchNode.fieldNames();
            SchemaContainers schema = SchemaContainers.getJsonSchemaValidator(patchNode.get("objectType").asText());
            if (schema == null) {
                throw new RuntimeException("Wrong ObjectType");
            }
            while (filedNames.hasNext()) {
                String field = filedNames.next();
                if ("objectType".equals(field) || "objectId".equals(field)) {
                    continue;
                }
                if (savedNode.has(field)) {
                    String type = SchemaContainers.getTypeOfField(schema, field);
                    if (type == null) {
                        continue;
                    }
                    switch (type) {
                        case "array" :
                            ArrayNode patchNodeIds = ((ArrayNode) patchNode.get(field));
                            HashSet<String> savedNodeIdsSet = getIdsFromArrayNode(((ArrayNode) savedNode.get(field)));
                            for (int i = 0; i < patchNodeIds.size(); i++) {
                                Dao dao = new Dao();
                                ObjectNode savedChildNode  = dao.get(patchNodeIds.get(i).asText());
                                if (savedChildNode == null) {
                                    continue;
                                }

                                if (savedNodeIdsSet.contains(patchNodeIds.get(i).asText())) {
                                    // this is going to be special case where we need to do something different
                                    updatePatchOld(objectNodesPresentPatch.get(patchNodeIds.get(i).asText()), savedChildNode,
                                            objectNodesPresentPatch, parentIds,
                                            listOfNodeToBeSaved, parentsToBeSaved);
                                } else {
                                    haveChanges = true;
                                    String parentKey = patchNodeIds.get(i).asText()+ ":parent";
                                    parentsToBeSaved.put(parentKey, parentIds.get(parentKey));
                                    ((ArrayNode) savedNode.get(field)).add(patchNodeIds.get(i));
                                    updatePatchOld(objectNodesPresentPatch.get(patchNodeIds.get(i).asText()), savedChildNode,
                                            objectNodesPresentPatch, parentIds,
                                            listOfNodeToBeSaved, parentsToBeSaved);
                                }
                                //listOfNodeToBeSaved.add(objectNodesPresentPatch.get(patchNodeIds.get(i).asText()));
                            }
                            break;
                        case "object" :
                            Dao dao = new Dao();
                            ObjectNode savedChildNode  = dao.get(patchNode.get(field).asText());
                            if (savedChildNode == null) {
                                continue;
                            }

                            updatePatchOld(objectNodesPresentPatch.get(patchNode.get(field).asText()),
                                    savedChildNode,
                                    objectNodesPresentPatch, parentIds,
                                    listOfNodeToBeSaved, parentsToBeSaved);
                            /*
                            if (savedNode.get(field).asText().equals(patchNode.get(field).asText())) {
                                listOfNodeToBeSaved.add(objectNodesPresentPatch.get(patchNode.get(field).asText()));
                            }
                             */
                            break;
                        case "number" :
                        case "string" :
                            savedNode.replace(field, patchNode.get(field));
                            haveChanges = true;
                            break;
                        default :
                            System.out.println("No such filed");
                            break;
                    };
                } else {
                    System.out.println("No such field available : "+field);
                }
            }
            if (haveChanges) {
                listOfNodeToBeSaved.add(savedNode);
            }

        } catch (Exception e) {
            System.out.println("exception While Merge Patch:" + e);
        }
    }


}
