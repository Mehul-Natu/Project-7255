package com.edu.info7255;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.edu.info7255.RabbitMQ.RabbitMqConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectApplication.class, args);
		RedisConfiguration.configure();
		ElasticsearchClient client = ElasticSearchConfig.getClient();
		RabbitMqConfig rabbitMqConfig = new RabbitMqConfig();
		//rabbitMqConfig.
	}


}
