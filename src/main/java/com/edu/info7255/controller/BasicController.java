package com.edu.info7255.controller;

import com.edu.info7255.Dao.Dao;
import com.edu.info7255.DataProcessors.DocToESDocConverter;
import com.edu.info7255.DataProcessors.JsonSchemaValidator;
import com.edu.info7255.DataProcessors.NodePopulator;
import com.edu.info7255.DataProcessors.ObjectIdCreator;
import com.edu.info7255.JwtUtils;
import com.edu.info7255.ResponseNodeWrapper;
import com.edu.info7255.Service.ESDocService;
import com.edu.info7255.utils.Pair;
import com.edu.info7255.utils.Utility;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.ValidationMessage;
import jakarta.ws.rs.core.EntityTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.edu.info7255.DataProcessors.Constants.ETAG;
import static com.edu.info7255.DataProcessors.Constants.OBJECT_ID;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;

@RestController
public class BasicController {

    @Autowired
    Dao dao;

    @Autowired
    ESDocService esDocService;

    @PostMapping(value = "/MedicalPlan/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> saveNew(@RequestHeader(value = "Authorization") String bearerToken,
                                          @RequestBody ObjectNode requestBody) {
        try {

            if (!JwtUtils.verifier(bearerToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Access Token");
            }

            List<String> schemaPresentList = new LinkedList<>();
            Set<ValidationMessage> validationMessages = new HashSet<>();
            JsonSchemaValidator.validate(requestBody, schemaPresentList, validationMessages);


            if (schemaPresentList.size() == 0) {
                //return no valid schema found bad request
                return new ResponseEntity<>("No valid Schema found", BAD_REQUEST);
            }

            if (validationMessages.size() != 0) {
                //add here to add list of failures
                return new ResponseEntity<>(validationMessages, BAD_REQUEST);
            }

            List<ObjectNode> listOfNodesToBeSaved = new LinkedList<>();
            HashMap<String, ArrayNode> parentMap = new HashMap<>();

            ObjectIdCreator.createObjectIdAndGetNodes(requestBody, listOfNodesToBeSaved, null, parentMap);

            saveObjectNodes(listOfNodesToBeSaved);
            saveParentNode(parentMap);

            List<ObjectNode> edDocs = DocToESDocConverter.createESDocs(listOfNodesToBeSaved, parentMap);

            esDocService.saveEsDocs(edDocs, parentMap);

            populateNodes(listOfNodesToBeSaved);

            List<Pair<String, Integer>> pairList = createEtagAndGet(listOfNodesToBeSaved);
            saveEtags(pairList);

            ResponseEntity<Object> response = ResponseEntity.of(Optional.of(createResponseWrapperNode(listOfNodesToBeSaved)));

            return response;
        } catch (Exception e) {
            System.out.println("Error while creating plan for" + requestBody + " Exception:"+e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/MedicalPlan/{id}/get", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> get(@RequestHeader(value = "Authorization") String bearerToken,
                                      @RequestHeader(HttpHeaders.IF_MODIFIED_SINCE) Integer eTag,
                                      @PathVariable(value = "id") String id) {

        try {

            if (!JwtUtils.verifier(bearerToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Access Token");
            }

            ObjectNode node = dao.get(id);
            //SchemaContainers.getJsonSchemaValidator("membercostshare");

            if (node == null) {
                return new ResponseEntity<>("No such object Found", HttpStatus.NOT_FOUND);
            }

            Integer etagValue = dao.getEtag(getEtagKey(id));

            if (etagValue == null) {
                NodePopulator.populate(node, dao);
                etagValue = new EntityTag(node.toString()).hashCode();
                dao.saveEtag(getEtagKey(node.get(OBJECT_ID).asText()), getEtag(node));
            }
            ResponseEntity<Object> response;

            if (etagValue.equals(eTag)) {
                //System.out.println("Etag:" + etagValue);
                response = new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
            } else {
                NodePopulator.populate(node, dao);
                response = ResponseEntity.ok().eTag(String.valueOf(etagValue)).body(createResponseWrapperNode(node));
            }

            return response;
        } catch (Exception e) {
            System.out.println("Error while fetching plan for id: "+id+" Exception:"+e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping(value = "/MedicalPlan/{id}/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> delete(@RequestHeader(value = "Authorization") String bearerToken,
                                         @RequestHeader(HttpHeaders.IF_MATCH) Integer eTag,
                                         @PathVariable(value = "id") String id) {
        try {

            if (!JwtUtils.verifier(bearerToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Access Token");
            }

            ObjectNode node = dao.get(id);

            if (node == null) {
                return new ResponseEntity<>("No such object Found", HttpStatus.OK);
            }

            Integer etagValue = dao.getEtag(getEtagKey(id));

            if (etagValue == null) {
                NodePopulator.populate(node, dao);
                etagValue = new EntityTag(node.toString()).hashCode();
                dao.saveEtag(getEtagKey(node.get(OBJECT_ID).asText()), getEtag(node));
            }

            String deletedMessage = null;

            if (etagValue.equals(eTag)) {
                List<String> relatedIds = new LinkedList<>();
                NodePopulator.fetchAllChildIds(node, relatedIds, dao);
                // to make these two operations atomic
                //also here we are now not able to delete just the child anymore, whole plan will be delete now
                //NodePopulator.deleteChildParentId(relatedIds, dao, id);
                relatedIds.add(id);

                dao.delete(relatedIds.stream().map(i -> getParentKey(i)).collect(Collectors.toList()));
                deletedMessage = dao.delete(relatedIds) ? "Object with ID: " + id + " deleted successfully"
                        : "Something went wrong try again";
                dao.delete(relatedIds.stream().map(i -> getEtagKey(i)).collect(Collectors.toList()));
                esDocService.deleteEsDocs(relatedIds);

            } else {
                return ResponseEntity.status(PRECONDITION_FAILED).eTag(String.valueOf(etagValue)).build();
            }
            return new ResponseEntity<>(deletedMessage, HttpStatus.OK);

        } catch (Exception e) {
            System.out.println("Error while deleting Node with id: "+id+" Exception:"+e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping(value = "/MedicalPlan/{objectType}/{objectId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity patch(@RequestHeader(value = "Authorization") String bearerToken,
                                @RequestHeader(HttpHeaders.IF_MATCH) Integer eTag, @PathVariable("objectType") String objectType,
                                @PathVariable("objectId") String objectId
    , @RequestBody ObjectNode patchNode) {
        try {

            if (!JwtUtils.verifier(bearerToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Access Token");
            }

            ObjectNode node = dao.get(objectId);

            if (node == null) {
                return new ResponseEntity<>("No such object Found", HttpStatus.OK);
            }

            Integer etagValue = dao.getEtag(getEtagKey(objectId));

            if (etagValue == null) {
                NodePopulator.populate(node, dao);
                etagValue = new EntityTag(node.toString()).hashCode();
                dao.saveEtag(getEtagKey(node.get(OBJECT_ID).asText()), getEtag(node));
            }

            if (!etagValue.equals(eTag)) {
                return ResponseEntity.status(PRECONDITION_FAILED).eTag(String.valueOf(etagValue)).build();
            }


            ObjectNode savedNode = dao.get(objectId);

            Map<String, ObjectNode> nodesPresentInPatch = new HashMap<>();
            Map<String, ArrayNode> parentIdsForPatch = new HashMap<>();
            ObjectNode patchNodeWithIds = Utility.deepCopyJsonNode(patchNode);
            ObjectIdCreator.getNodesAndPlaceTheirIds(patchNodeWithIds, nodesPresentInPatch, null, parentIdsForPatch);


            List<ObjectNode> objectNodesToBeSaved = new LinkedList<>();
            HashMap<String, ArrayNode> parentsToBeSaved = new HashMap<>();
            NodePopulator.patch(patchNode, patchNodeWithIds,  savedNode, objectNodesToBeSaved,
                    parentsToBeSaved);

            //saving docs to ES
            List<ObjectNode> edDocs = DocToESDocConverter.createESDocs(objectNodesToBeSaved, parentsToBeSaved);
            esDocService.saveEsDocs(edDocs, parentsToBeSaved);

            //no need to update the parent node in which we are adding new object because ES hai child to parent relation
            //not parent to child

            objectNodesToBeSaved.add(savedNode);
            saveParentNode(parentsToBeSaved);
            saveObjectNodes(objectNodesToBeSaved);

            System.out.println("hello");
            NodePopulator.populate(savedNode, dao);

            List<Pair<String, Integer>> list = createEtagAndGet(objectNodesToBeSaved);
            saveEtags(list);

            return ResponseEntity.of(Optional.of(createResponseWrapperNode(savedNode)));


        } catch (Exception e) {
            System.out.println("Exception " + e);
            return ResponseEntity.internalServerError().build();
        }

    }


    @PutMapping(value = "/MedicalPlan/{objectType}/{objectId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity update(@RequestHeader(value = "Authorization") String bearerToken,
                                @RequestHeader(HttpHeaders.IF_MATCH) Integer eTag, @PathVariable("objectType") String objectType,
                                @PathVariable("objectId") String objectId
            , @RequestBody ObjectNode patchNode) {
        try {

            if (!JwtUtils.verifier(bearerToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Access Token");
            }

            ObjectNode node = dao.get(objectId);

            if (node == null) {
                return new ResponseEntity<>("No such object Found", HttpStatus.OK);
            }

            Integer etagValue = dao.getEtag(getEtagKey(objectId));

            if (etagValue == null) {
                NodePopulator.populate(node, dao);
                etagValue = new EntityTag(node.toString()).hashCode();
                dao.saveEtag(getEtagKey(node.get(OBJECT_ID).asText()), getEtag(node));
            }

            if (!etagValue.equals(eTag)) {
                return ResponseEntity.status(PRECONDITION_FAILED).eTag(String.valueOf(etagValue)).build();
            }


            ObjectNode savedNode = dao.get(objectId);

            Map<String, ObjectNode> nodesPresentInPatch = new HashMap<>();
            Map<String, ArrayNode> parentIdsForPatch = new HashMap<>();
            ObjectNode patchNodeWithIds = Utility.deepCopyJsonNode(patchNode);
            ObjectIdCreator.getNodesAndPlaceTheirIds(patchNodeWithIds, nodesPresentInPatch, null, parentIdsForPatch);


            List<ObjectNode> objectNodesToBeSaved = new LinkedList<>();
            HashMap<String, ArrayNode> parentsToBeSaved = new HashMap<>();

            NodePopulator.update(patchNodeWithIds, savedNode, nodesPresentInPatch, objectNodesToBeSaved);

            //saving docs to ES
            List<ObjectNode> edDocs = DocToESDocConverter.createESDocs(objectNodesToBeSaved, parentsToBeSaved);
            esDocService.updateEsDocs(edDocs);

            //no need to update the parent node in which we are adding new object because ES hai child to parent relation
            //not parent to child

            objectNodesToBeSaved.add(savedNode);
            saveObjectNodes(objectNodesToBeSaved);

            System.out.println("hello");
            NodePopulator.populate(savedNode, dao);

            List<Pair<String, Integer>> list = createEtagAndGet(objectNodesToBeSaved);
            saveEtags(list);

            return ResponseEntity.of(Optional.of(createResponseWrapperNode(savedNode)));


        } catch (Exception e) {
            System.out.println("Exception " + e);
            return ResponseEntity.internalServerError().build();
        }

    }


    private void saveObjectNodes(List<ObjectNode> list) {
        //List<ObjectNode>
        list.forEach(p -> {
            dao.saveNode(p);
        });
    }

    private List<ResponseNodeWrapper> createResponseWrapperNode(List<ObjectNode> list) {
        List<ResponseNodeWrapper> responseList = new LinkedList<>();
        //EntityTag entityTag = new EntityTag(node.toString());
        list.forEach(p -> {
            System.out.println("creating etag for = " + p);
            responseList.add(new ResponseNodeWrapper(new EntityTag(p.toString()).hashCode(), p));
        });
        return responseList;
    }

    private List<Pair<String, Integer>> createEtagAndGet(List<ObjectNode> objectNodeList) {
        List<Pair<String, Integer>> pairList = new LinkedList<>();
        objectNodeList.forEach(p -> {pairList.add(new Pair<>(p.get("objectId").asText(),
                new EntityTag(p.toString()).hashCode()));
            System.out.println("Object ID = " + p.get("objectId").asText() + " Etag: " + p.toString().hashCode());
        });
        return pairList;
    }

    private ResponseNodeWrapper createResponseWrapperNode(ObjectNode node) {
        EntityTag entityTag = new EntityTag(node.toString());
        return new ResponseNodeWrapper(entityTag.hashCode(), node);
    }

    private void saveParentNode(HashMap<String, ArrayNode> map) {
        for (String key : map.keySet()) {
            dao.saveParentKey(key, map.get(key));
        }
    }

    private void saveEtags(List<Pair<String, Integer>> list) {
        for (Pair<String, Integer> etagPair : list) {
            dao.saveEtag(etagPair.getX(), etagPair.getY());
        }
    }

    private void populateNodes(List<ObjectNode> listOfNodes) {
        listOfNodes.forEach(p -> {
            NodePopulator.populate(p, dao);
        });
    }

    private int getEtag(ObjectNode objectNode) {
        return new EntityTag(objectNode.toString()).hashCode();
    }

    private String getEtagKey(String key) {
        return key + ":" + ETAG;
    }

    private String getParentKey(String key) {
        return key + ":parent";
    }

}
