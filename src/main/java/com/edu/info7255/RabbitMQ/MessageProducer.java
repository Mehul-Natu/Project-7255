package com.edu.info7255.RabbitMQ;

import com.edu.info7255.Service.ESMessageWrapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class MessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishMessage(ObjectNode objectNode, String messageId, Date messageDate) {
        CustomMessage customMessage = new CustomMessage(messageId, objectNode.asText(), messageDate);
        rabbitTemplate.convertAndSend(RabbitMqConfig.ES_DOC_EXCHANGE, RabbitMqConfig.ES_DOC_ROUTING_KEY, customMessage);
    }

    public void publishMessage(ObjectNode objectNode) {
        CustomMessage customMessage = new CustomMessage(UUID.randomUUID().toString(), objectNode.toString(), new Date());
        rabbitTemplate.convertAndSend(RabbitMqConfig.ES_DOC_EXCHANGE, RabbitMqConfig.ES_DOC_ROUTING_KEY, customMessage);
    }

    public void publishMessage(ESMessageWrapper esMessageWrapper) {
        CustomMessage customMessage = new CustomMessage(UUID.randomUUID().toString(), esMessageWrapper.toString(), new Date());
        rabbitTemplate.convertAndSend(RabbitMqConfig.ES_DOC_EXCHANGE, RabbitMqConfig.ES_DOC_ROUTING_KEY, customMessage);
    }

}
