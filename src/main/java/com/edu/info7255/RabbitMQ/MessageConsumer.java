package com.edu.info7255.RabbitMQ;

import com.edu.info7255.Service.ESMQConsumerService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer {

    @Autowired
    ESMQConsumerService esmqConsumerService;

    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    public void listener(CustomMessage customMessage) {
        System.out.println("Got the message" + customMessage);
        esmqConsumerService.consume(customMessage);
    }

}
