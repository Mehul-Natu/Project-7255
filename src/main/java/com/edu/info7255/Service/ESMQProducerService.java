package com.edu.info7255.Service;

import com.edu.info7255.RabbitMQ.MessageProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.edu.info7255.DataProcessors.Constants.ES_DOCS;
import static com.edu.info7255.DataProcessors.Constants.OPERATION;

@Component
public class ESMQProducerService {

    @Autowired
    MessageProducer messageProducer;


    private static ObjectMapper objectMapper = new ObjectMapper();

    public void pushESDoc(List<ObjectNode> esDocs, ESOperation operation) {
        try {
            ObjectNode esDocWrapper = objectMapper.createObjectNode();
            ArrayNode arrayNode = objectMapper.createArrayNode();
            esDocWrapper.set(ES_DOCS, arrayNode.addAll(esDocs));
            esDocWrapper.put(OPERATION, operation.toString());
            messageProducer.publishMessage(esDocWrapper);
        } catch (Exception e) {
            System.out.println("Exception while producing ES doc to save e: " + e);
            throw e;
        }
    }


    public void pushESDocStrings(List<String> esDocs, ESOperation operation) {
        try {
            ObjectNode esDocWrapper = objectMapper.createObjectNode();
            ArrayNode arrayNode = objectMapper.createArrayNode();
            esDocs.forEach(arrayNode::add);
            esDocWrapper.set(ES_DOCS, arrayNode);
            esDocWrapper.put(OPERATION, operation.toString());
            messageProducer.publishMessage(esDocWrapper);
        } catch (Exception e) {
            System.out.println("Exception while producing ES doc to save e: " + e);
            throw e;
        }
    }

}
