package com.edu.info7255.DataProcessors;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.edu.info7255.DataProcessors.Constants.OBJECT_TYPE;

public class JsonSchemaValidator {


    //it is used for at same directory level if it finds an Object Type matchingthen it wont check for others specifically
    //if it finds an Object which have a type not present in the system then it will reject the whole thing
    //if no object type is present at a directory it will explore the objects

    public static void validate(JsonNode jsonNode, List<String> listOfObjectTypes, Set<ValidationMessage> validationMessages) {
        try {
            if (jsonNode.has(OBJECT_TYPE)) {
                SchemaContainers schema = SchemaContainers.getJsonSchemaValidator(jsonNode.get(OBJECT_TYPE).asText());
                if (schema == null) {
                    return;// exception for no such ObjectType
                }
                //schema.schemaValidator.walk()
                listOfObjectTypes.add(schema.type);
                Set<ValidationMessage> currNodeValidationMessages = schema.schemaValidator.validate(jsonNode);

                if (!currNodeValidationMessages.isEmpty()) {
                    validationMessages.addAll(currNodeValidationMessages);
                    for (ValidationMessage validationMessage : validationMessages) {
                        System.out.println(validationMessage.getMessage());
                    }
                }
                return;
            }

            Iterator<String> it = jsonNode.fieldNames();

            while (it.hasNext()) {
                String val = it.next();
                JsonNode currNode = jsonNode.get(val);
                if (currNode.isObject()) {
                    validate(jsonNode.get(val), listOfObjectTypes, validationMessages);
                } else if (currNode.isArray()) {
                    for (int i = 0; i < currNode.size(); i++) {
                        //if ()
                        validate(currNode.get(i), listOfObjectTypes, validationMessages);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error "+e);
        }
    }
}
