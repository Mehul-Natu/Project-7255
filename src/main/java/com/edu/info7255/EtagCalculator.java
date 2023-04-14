package com.edu.info7255;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.core.EntityTag;

public class EtagCalculator {

    public static String calculateEtag(ObjectNode node) {
        EntityTag entityTag = new EntityTag(node.toString());
        return null;//entityTag.hashCode();
    }
}
