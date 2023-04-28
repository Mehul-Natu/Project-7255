package com.edu.info7255.Dao;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.*;
import com.edu.info7255.DataProcessors.SchemaContainers;
import com.edu.info7255.ElasticSearchConfig;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ElasticSearchDao {

    private static final String MEDICAL_PLAN_INDEX = "medical_plan";

    public void saveESDoc(ObjectNode objectNode, SchemaContainers schemaContainers) throws IOException {
        try  {
            ElasticsearchClient elasticsearchClient = ElasticSearchConfig.getClient();
            IndexRequest<ObjectNode> indexRequest = IndexRequest.of(i ->
                    i.index(MEDICAL_PLAN_INDEX)
                            .id(objectNode.get("objectId").asText())
                            .document(objectNode).refresh(Refresh.True)
            );
            IndexResponse response = elasticsearchClient.index(indexRequest);
            System.out.println("Indexed with version " + response.version());
        } catch (Exception e) {
            System.out.println("Exception while saving doc in ES doc" + objectNode + "\n exception : " + e);
            throw e;
        }
    }

    public void saveESDoc(ObjectNode objectNode, SchemaContainers schemaContainers, String parentId) throws IOException {
        try  {
            ElasticsearchClient elasticsearchClient = ElasticSearchConfig.getClient();
            IndexRequest<ObjectNode> indexRequest = IndexRequest.of(i ->
                    i.index(MEDICAL_PLAN_INDEX)
                            .id(objectNode.get("objectId").asText())
                            .document(objectNode).routing(parentId).refresh(Refresh.True)
            );
            IndexResponse response = elasticsearchClient.index(indexRequest);
            System.out.println("Indexed with version " + response.version());
        } catch (Exception e) {
            System.out.println("Exception while saving doc in ES doc" + objectNode + "parent ID " + parentId +
                    "\n exception : " + e);
            throw e;
        }
    }

    public void deleteEsDoc(String id) throws IOException {
        try  {
            ElasticsearchClient elasticsearchClient = ElasticSearchConfig.getClient();
            DeleteRequest deleteRequest = DeleteRequest.of(i ->
                    i.index(MEDICAL_PLAN_INDEX)
                            .id(id).refresh(Refresh.True)
            );
            DeleteResponse response = elasticsearchClient.delete(deleteRequest);
            System.out.println("Indexed with version " + response.version());
        } catch (Exception e) {
            System.out.println("Exception while deleting doc in ES id" + id +
                    "\n exception : " + e);
            throw e;
        }
    }


    public void updateEsDoc(ObjectNode node, String id) throws IOException {
        try  {
            ElasticsearchClient elasticsearchClient = ElasticSearchConfig.getClient();
            UpdateRequest updateRequest = UpdateRequest.of(i ->
                    i.index(MEDICAL_PLAN_INDEX)
                            .id(id).doc(node).refresh(Refresh.True)
            );
            UpdateResponse<ObjectNode> response = elasticsearchClient.update(updateRequest, ObjectNode.class);
            System.out.println("Indexed with version " + response);
        } catch (Exception e) {
            System.out.println("Exception while deleting doc in ES id" + id +
                    "\n exception : " + e);
            throw e;
        }
    }
}
