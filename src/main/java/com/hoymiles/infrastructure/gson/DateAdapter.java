package com.hoymiles.infrastructure.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateAdapter<T extends Date> implements JsonSerializer<T> {

    private final SimpleDateFormat simpleDateFormat;

    public DateAdapter(String format) {
        simpleDateFormat = new SimpleDateFormat(format, Locale.getDefault());
    }

    @Override
    public JsonElement serialize(Date arg0, Type arg1, JsonSerializationContext arg2) {
        if (arg0 == null) {
            return arg2.serialize(JsonNull.INSTANCE);
        }
        String formattedDate = simpleDateFormat.format(arg0);
        return arg2.serialize(formattedDate);
    }
}