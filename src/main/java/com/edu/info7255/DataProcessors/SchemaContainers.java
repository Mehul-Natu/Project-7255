package com.edu.info7255.DataProcessors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;

public enum SchemaContainers {

    memberCostShare("membercostshare", "membercostshareSchema.json", new LinkedList<>()),
    plan("plan", "planSchema.json", new LinkedList<>() {
        {
            add("planCostShares");
            add("linkedPlanServices");
        }
    }),
    planService("planservice", "planServiceSchema.json", new LinkedList<>() {
        {
            add("linkedService");
            add("planserviceCostShares");
        }
    }),
    service("service", "serviceSchema.json", new LinkedList<String>());

    SchemaContainers(String type, String jsonFileName, LinkedList<String> list) {
        this.type = type;
        this.jsonFileName = jsonFileName;
        this.schemaValidator = getJsonSchemaForFile(this.jsonFileName);
        this.entityFields = list;
        this.objectNode = getObjectNode(this.jsonFileName);
    }
    final String type;
    final JsonSchema schemaValidator;
    final String jsonFileName;

    final List<String> entityFields;

    ObjectNode objectNode;

    private JsonSchema getJsonSchemaForFile(String jsonFileName) {
        try {
            JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
            return schemaFactory
                    .getSchema(new FileInputStream(ResourceUtils.getFile("classpath:"+jsonFileName)));
        } catch (Exception e) {
            System.out.println("Error:" + e + ", while populating from file:" + jsonFileName);
            return null;
        }
    }

    private ObjectNode getObjectNode(String jsonFileName) {
        try {
            JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(
                    new FileInputStream(ResourceUtils.getFile("classpath:"+jsonFileName)), ObjectNode.class);
        } catch (Exception e) {
            System.out.println("Error:" + e + ", while populating Object Node from file:" + jsonFileName);
            return null;
        }
    }

    public static SchemaContainers getJsonSchemaValidator(String type) {
        for (SchemaContainers schemaValidator : SchemaContainers.values()) {
            if (schemaValidator.type.equalsIgnoreCase(type)) {
                return schemaValidator;
            }
        }
        return null;
    }

    public List<String> getEntityFields() {
        return entityFields;
    }

    public static String getTypeOfField(SchemaContainers schemaContainers, String fieldName) {
        JsonNode schemaNode = schemaContainers.objectNode;
        if (!schemaNode.get("properties").has(fieldName)) {
            return null;
        }
        JsonNode field = schemaNode.get("properties").get(fieldName);
        if (field.has("type")) {
            return field.get("type").asText();
        }

        if (field.has("$ref")) {
            String fieldObjectName = getNameFromRefString(field.get("$ref").asText());
            return schemaNode.get("$defs").get(fieldObjectName).get("type").asText();
        }
        return null;

    }

    private static String getNameFromRefString(String refString) {
        String[] strings = refString.split("/");
        return strings[strings.length - 1];
    }
}
