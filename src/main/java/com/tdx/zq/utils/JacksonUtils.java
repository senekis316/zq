package com.tdx.zq.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tdx.zq.exception.JacksonParseException;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class JacksonUtils {

    public static String toJson(Object object) {
        String json;
        try {
            json = new ObjectMapper().writeValueAsString(object);
        } catch(Exception e) {
            throw new JacksonParseException(e);
        }
        return json;
    }

    public static <T> T readValue(String json, Class<T> clazz) {
        T t;
        try {
            t = new ObjectMapper().readValue(json, clazz);
        } catch(Exception e) {
            throw new JacksonParseException(e);
        }
        return t;
    }

    public static <T> T readValue(String json, TypeReference typeReference) {
        T t;
        try {
            t = new ObjectMapper().readValue(json, typeReference);
        } catch(Exception e) {
            throw new JacksonParseException(e);
        }
        return t;
    }
}
