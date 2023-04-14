package com.edu.info7255.Dao;

import com.edu.info7255.RedisConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.json.Path;

import java.util.List;

import static com.edu.info7255.DataProcessors.Constants.ETAG;
import static com.edu.info7255.DataProcessors.Constants.OBJECT_ID;

@Component
public class Dao {

    @Autowired
    ObjectMapper objectMapper;

    public ObjectNode saveNode(ObjectNode jsonNode) {
        try (UnifiedJedis unifiedJedis = RedisConfiguration.getResources()) {
            unifiedJedis.jsonSet(jsonNode.get(OBJECT_ID).asText(), jsonNode);
            System.out.println("Yes = " + jsonNode);
            jsonNode = objectMapper.convertValue(unifiedJedis.jsonGet(jsonNode.get("objectId").asText()), ObjectNode.class);
            System.out.println("Yes = convewrted" + jsonNode);
            return jsonNode;
        } catch (Exception e) {
            System.out.println("exception while saving data in Redis e:"+e+", JsonNode: "+jsonNode);
            throw e;
        }
    }

    public ObjectNode saveNodeAndEtag(ObjectNode jsonNode) {
        try (UnifiedJedis unifiedJedis = RedisConfiguration.getResources()) {
            unifiedJedis.jsonSet(jsonNode.get(OBJECT_ID).asText(), jsonNode);
            System.out.println("Yes = " + jsonNode);
            jsonNode = objectMapper.convertValue(unifiedJedis.jsonGet(jsonNode.get("objectId").asText()), ObjectNode.class);
            System.out.println("Yes = convewrted" + jsonNode);
            return jsonNode;
        } catch (Exception e) {
            System.out.println("exception while saving data in Redis e:"+e+", JsonNode: "+jsonNode);
            return null;
        }
    }

    public JsonNode saveParentKey(String key, ArrayNode node) {
        try (UnifiedJedis unifiedJedis = RedisConfiguration.getResources()) {
            unifiedJedis.jsonSet(key, node);
            return node;
        } catch (Exception e) {
            System.out.println("exception while saving data in Redis e:"+e+", parentKey:" +" ArrayNode: "+node);
            return null;
        }
    }

    public Integer saveEtag(String key, Integer etag) {
        try (UnifiedJedis unifiedJedis = RedisConfiguration.getResources()) {
            unifiedJedis.jsonSet(key + ":" + ETAG, etag);
            return etag;
        } catch (Exception e) {
            System.out.println("exception while saving etag in Redis e:" + e + ", etag key: " + key + " ArrayNode: " + etag);
            return null;
        }
    }

    public ObjectNode get(String key) {
        try (UnifiedJedis unifiedJedis = RedisConfiguration.getResources()) {
            ObjectMapper objectMapper = new ObjectMapper();
            //ObjectReader reader = objectMapper.readerFor(JsonNode.class);
            //objectMapper.configure(DeserializationFeature.USE_D, true);
            //JsonNode jsonNode = reader.readValue((JsonParser) unifiedJedis.jsonGet(key));
            //System.out.println("GET - value = " + unifiedJedis.jsonGetAsPlainString(key, Path.ROOT_PATH));
            return objectMapper.readValue(unifiedJedis.jsonGetAsPlainString(key, Path.ROOT_PATH), ObjectNode.class);
        /*} catch (IOException e) {
            System.out.println("IOException while fetching data in Redis e:"+e+", key: "+key);
            return null ;
        }
         */
        } catch (Exception e) {

            System.out.println("Exception while fetching data in Redis e:"+e+", key: "+key);
            //throw e;
            return null;
        }
    }

    public Integer getEtag(String key) {
        try (UnifiedJedis unifiedJedis = RedisConfiguration.getResources()) {
            ObjectMapper objectMapper = new ObjectMapper();
            //ObjectReader reader = objectMapper.readerFor(JsonNode.class);
            //objectMapper.configure(DeserializationFeature.USE_D, true);
            //JsonNode jsonNode = reader.readValue((JsonParser) unifiedJedis.jsonGet(key));
            //System.out.println("GET - value = " + unifiedJedis.jsonGetAsPlainString(key, Path.ROOT_PATH));
            return objectMapper.readValue(unifiedJedis.jsonGetAsPlainString(key, Path.ROOT_PATH), Integer.class);
        /*} catch (IOException e) {
            System.out.println("IOException while fetching data in Redis e:"+e+", key: "+key);
            return null ;
        }
         */
        } catch (Exception e) {

            System.out.println("Exception while fetching data in Redis e:"+e+", key: "+key);
            //throw e;
            return null;
        }
    }
    public ArrayNode getArrayNode(String key) {
        try (UnifiedJedis unifiedJedis = RedisConfiguration.getResources()) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.convertValue(unifiedJedis.jsonGet(key), ArrayNode.class);
        } catch (Exception e) {
            System.out.println("Exception while fetching array node from Redis e:"+e+", key: "+key);
            return null;
        }
    }

    public boolean delete(String key) {
        try (UnifiedJedis unifiedJedis = RedisConfiguration.getResources()) {
            ObjectMapper objectMapper = new ObjectMapper();
            return unifiedJedis.del(key) == 1;
        } catch (Exception e) {
            System.out.println("Exception while deleting data in Redis e:"+e+", key: "+key);
            return false;
        }
    }

    public boolean delete(List<String> keys) {
        try (UnifiedJedis unifiedJedis = RedisConfiguration.getResources()) {
            for (String key : keys) {
                //ObjectMapper objectMapper = new ObjectMapper();
                unifiedJedis.del(key);
            }
            return true;
        } catch (Exception e) {
            System.out.println("Exception while deleting data in Redis e:" + e + ", key: " + keys);
            return false;
        }
    }

}
