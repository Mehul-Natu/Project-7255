package com.edu.info7255.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

public class Utility {
    public static ObjectNode deepCopyJsonNode(ObjectNode objectNode, ObjectMapper om) throws IOException {
        JsonNodeFactory jsonNodeFactory = om.getNodeFactory();
        //ObjectCodec objectCodec = objectMapper.getCodec();
        String jsonString = om.writeValueAsString(objectNode);
        return om.readValue(jsonString, ObjectNode.class);
    }

    public static ObjectNode deepCopyJsonNode(ObjectNode objectNode) throws IOException {
        ObjectMapper om = new ObjectMapper();
        JsonNodeFactory jsonNodeFactory = om.getNodeFactory();
        //ObjectCodec objectCodec = objectMapper.getCodec();
        String jsonString = om.writeValueAsString(objectNode);
        return om.readValue(jsonString, ObjectNode.class);
    }
}
