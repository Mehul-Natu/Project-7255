package com.edu.info7255;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static com.edu.info7255.DataProcessors.Constants.OBJECT_ID;
import static com.edu.info7255.DataProcessors.Constants.OBJECT_TYPE;


public class ResponseNodeWrapper {
    private int eTag;
    private ObjectNode node;

    public ResponseNodeWrapper(int eTag, ObjectNode node) {
        this.eTag = eTag;
        this.node = node;
        System.out.println("Etag : " + eTag + ", ObjectID: " + node.get(OBJECT_ID));
        if ("membercostshare".equals(node.get(OBJECT_TYPE))) {
            System.out.println(node);
        }
        System.out.println(node);
    }

    public int geteTag() {
        return eTag;
    }

    public void seteTag(int eTag) {
        this.eTag = eTag;
    }

    public ObjectNode getNode() {
        return node;
    }

    public void setNode(ObjectNode node) {
        this.node = node;
    }
}
