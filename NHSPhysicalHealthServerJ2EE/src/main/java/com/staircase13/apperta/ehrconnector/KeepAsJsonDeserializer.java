package com.staircase13.apperta.ehrconnector;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Keeps json value as json and converts to a string.
 */
public class KeepAsJsonDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {

        TreeNode tree = jp.getCodec().readTree(jp);
        return tree.toString();
    }
}

