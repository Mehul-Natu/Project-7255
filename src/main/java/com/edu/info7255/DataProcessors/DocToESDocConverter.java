package com.edu.info7255.DataProcessors;

import com.edu.info7255.utils.Utility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static com.edu.info7255.DataProcessors.Constants.JOIN_FIELD_NAME;
import static com.edu.info7255.DataProcessors.SchemaContainers.*;

@Component
public class DocToESDocConverter {


    /*
    "relations": {
          "plan": [
            "planService",
            "costShare"
          ]
        }
     */
    private static final ObjectMapper om = new ObjectMapper();

    public static ObjectNode createESDoc(ObjectNode objectNode, SchemaContainers schemaContainers, String parentId) throws IOException {
        try {
            ObjectNode esDoc = Utility.deepCopyJsonNode(objectNode, om);
            ObjectNode joinFields = om.createObjectNode();

            for (String innerObjects : schemaContainers.entityFields) {
                esDoc.remove(innerObjects);
            }

            switch (schemaContainers) {
                case plan:
                    joinFields.put("name", getJoinFiledName(schemaContainers, parentId));
                    esDoc.set(JOIN_FIELD_NAME, joinFields);
                    break;
                case memberCostShare:
                case service:
                case planService:
                    joinFields.put("name", getJoinFiledName(schemaContainers, parentId));
                    joinFields.put("parent", parentId);
                    esDoc.set(JOIN_FIELD_NAME, joinFields);
                    break;
                default:
            };
            return esDoc;
        } catch (Exception e) {
            System.out.println("Exception while creating ES doc : " + e);
            throw e;
        }
    }


    public static List<ObjectNode> createESDocs(List<ObjectNode> objectNodes, HashMap<String, ArrayNode> parentMap) throws IOException {
        try {
            List<ObjectNode> esDoc = new LinkedList<>();
            for (ObjectNode node : objectNodes) {
                switch (node.get("objectType").asText()) {
                    case "plan":
                        esDoc.add(createESDoc(node, plan, null));
                        break;
                    case "membercostshare":
                        esDoc.add(createESDoc(node, memberCostShare, getParentId(node, parentMap)));
                        break;
                    case "planservice":
                        esDoc.add(createESDoc(node, planService, getParentId(node, parentMap)));
                        break;
                    case "service":
                        esDoc.add(createESDoc(node, service, getParentId(node, parentMap)));
                        break;
                }
            }
            return esDoc;
        } catch (Exception e) {
            System.out.println("Exception while creating multiple ES doc" + e);
            throw e;
        }
    }

    private static String getParentId(ObjectNode node, HashMap<String, ArrayNode> parentMap) {
        if (parentMap.get(node.get("objectId").asText() + ":parent") != null) {
            return parentMap.get(node.get("objectId").asText() + ":parent").get(0).asText();
        }
        return "";
    }

    public static ObjectNode createESDocGeneric(ObjectNode objectNode, SchemaContainers schemaContainers) throws JsonProcessingException {
        try {
            ObjectNode esDoc = om.treeToValue(objectNode, ObjectNode.class);

            return null;

        } catch (Exception e) {
            System.out.println("Exception while creating ES doc : " + e);
            throw e;
        }
    }

    public static String getJoinFiledName(SchemaContainers schemaContainers, String parentId) {
        switch (schemaContainers) {
            case plan:
                return "plan";
            case memberCostShare:
                return parentId.contains("plan:") ? "costShare" : "planServiceCostShare";
            case planService:
                return "planService";
            case service:
                return "linkedService";
        };
        return "";
    }



}
