package com.edu.info7255.Service;

import com.edu.info7255.RabbitMQ.CustomMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.edu.info7255.DataProcessors.Constants.ES_DOCS;
import static com.edu.info7255.DataProcessors.Constants.OPERATION;

@Component
public class ESMQConsumerService {


    @Autowired
    ESDocService esDocService;
    private static final ObjectMapper objectMapper = new ObjectMapper();


    public void consume(CustomMessage customMessage) {
        try {
            ObjectNode objectNode = objectMapper.readValue(customMessage.getMessage(), ObjectNode.class);
            esOperationOperator(objectNode);
            System.out.println("HEREEEEEEEEEEEEEE - " + objectNode);
        } catch (Exception e) {
            System.out.println("Exception while consuming message e: " + e);
        }
    }

    public void esOperationOperator(ObjectNode objectNode) {
        try {
            ESOperation operation = ESOperation.valueOf(objectNode.get(OPERATION).asText());
            ArrayNode esDocs = (ArrayNode) objectNode.get(ES_DOCS);

            switch (operation) {
                case save:
                    esDocService.saveEsDocs(esDocs);
                    break;
                case delete:
                    esDocService.deleteEsDocs(esDocs);
                    break;
                case update:
                    esDocService.updateEsDocs(esDocs);
                    break;
                case patch:
                    break;
            }
        } catch (Exception e) {
            System.out.println("Exception while operating on ES doc e: " + e);
        }
    }

}
