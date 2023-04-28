package com.edu.info7255.Service;

import com.edu.info7255.Dao.Dao;
import com.edu.info7255.Dao.ElasticSearchDao;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.edu.info7255.DataProcessors.Constants.JOIN_FIELD_NAME;
import static com.edu.info7255.DataProcessors.Constants.OBJECT_ID;

@Component
public class ESDocService {

    @Autowired
    private ElasticSearchDao esDao;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public boolean saveEsDocs(List<ObjectNode> esDocs, Map<String, ArrayNode> parentMap) {
        try {
            for (ObjectNode objectNode : esDocs) {
                ObjectNode joinFieldNode = (ObjectNode) objectNode.get("my_join_field");
                if (joinFieldNode.has("parent")) {
                    esDao.saveESDoc(objectNode, null, joinFieldNode.get("parent").asText());
                } else {
                    esDao.saveESDoc(objectNode, null);
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("Exception while saving es Docs list : " + e);
            return false;
        }
    }

    public boolean saveEsDocs(ArrayNode esDocs) {
        try {
            for (JsonNode objectNode : esDocs) {
                ObjectNode joinFieldNode = (ObjectNode) objectNode.get("my_join_field");
                if (joinFieldNode.has("parent")) {
                    esDao.saveESDoc((ObjectNode) objectNode, null, joinFieldNode.get("parent").asText());
                } else {
                    esDao.saveESDoc((ObjectNode) objectNode, null);
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("Exception while saving es Docs list : " + e);
            return false;
        }
    }


    public boolean deleteEsDoc(ObjectNode objectNode) {
        try {
            esDao.deleteEsDoc(objectNode.get("objectId").asText());
            return true;
        } catch (Exception e) {
            System.out.println("Exception while deleting es Docs list : " + e);
            return false;
        }
    }

    public boolean deleteEsDocs(List<String> ids) {
        try {
            for (String id : ids) {
                esDao.deleteEsDoc(id);
            }
            return true;
        } catch (Exception e) {
            System.out.println("Exception while deleting es Docs list : " + e);
            return false;
        }
    }

    public boolean deleteEsDocs(ArrayNode ids) {
        try {
            for (JsonNode id : ids) {
                esDao.deleteEsDoc(id.asText());
            }
            return true;
        } catch (Exception e) {
            System.out.println("Exception while deleting es Docs list : " + e);
            return false;
        }
    }

    public boolean updateEsDocs(List<ObjectNode> esDocs) {
        try {
            for (ObjectNode node : esDocs) {
                node.remove(JOIN_FIELD_NAME);
                esDao.updateEsDoc(node, node.get(OBJECT_ID).asText());
            }
            return true;
        } catch (Exception e) {
            System.out.println("Exception while updating es Docs list : " + e);
            return false;
        }
    }

    public boolean updateEsDocs(ArrayNode esDocs) {
        try {
            for (JsonNode node : esDocs) {
                ObjectNode objectNode = (ObjectNode) node;
                objectNode.remove(JOIN_FIELD_NAME);
                esDao.updateEsDoc(objectNode, node.get(OBJECT_ID).asText());
            }
            return true;
        } catch (Exception e) {
            System.out.println("Exception while updating es Docs list : " + e);
            return false;
        }
    }


    //private String getParentId
}
