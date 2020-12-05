package com.animsh.notehut.converters;

import androidx.room.TypeConverter;

import com.animsh.notehut.entities.TODO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class TODOTypeConverters {
    static Gson gson = new Gson();

    @TypeConverter
    public static List<TODO> stringToTODOList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<TODO>>() {
        }.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String todoListToString(List<TODO> someObjects) {
        return gson.toJson(someObjects);
    }
}
