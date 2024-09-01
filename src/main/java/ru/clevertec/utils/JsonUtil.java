package ru.clevertec.utils;

import com.google.gson.Gson;

import java.io.Reader;

public class JsonUtil {
    private static final Gson gson = new Gson();

    public static <T> T fromJson(Reader reader, Class<T> clazz) {
        return gson.fromJson(reader, clazz);
    }

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }
}
