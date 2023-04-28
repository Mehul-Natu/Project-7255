package com.edu.info7255.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.Serializable;
import java.util.List;

public class ESMessageWrapper implements Serializable {

    private List<ObjectNode> esDoc;

    private ESOperation operation;

    public ESMessageWrapper() {
    }

    public ESMessageWrapper(List<ObjectNode> esDoc, ESOperation operation) {
        this.esDoc = esDoc;
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "ESMessageWrapper{" +
                "esDoc=" + esDoc +
                ", operation=" + operation +
                '}';
    }


    public List<ObjectNode> getEsDoc() {
        return esDoc;
    }

    public void setEsDoc(List<ObjectNode> esDoc) {
        this.esDoc = esDoc;
    }

    public ESOperation getOperation() {
        return operation;
    }

    public void setOperation(ESOperation operation) {
        this.operation = operation;
    }
}
