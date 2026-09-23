package com.todoapp.domain;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class Json {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private Json() {}
    public static String write(Object value) {
        try { return MAPPER.writeValueAsString(value); }
        catch (Exception e) { throw new IllegalStateException("Cannot encode stored data", e); }
    }
    public static <T> T read(String value, Class<T> type) {
        try { return MAPPER.readValue(value, type); }
        catch (Exception e) { throw new IllegalStateException("Cannot decode stored data", e); }
    }
}
