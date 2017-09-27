package com.lakeel.altla.ghost.alpha.nearbysearch.place.retrofit;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.nearbysearch.place.PlaceType;

import java.lang.reflect.Type;

public final class PlaceTypeHandler implements JsonSerializer<PlaceType>, JsonDeserializer<PlaceType> {

    public static final PlaceTypeHandler INSTANCE = new PlaceTypeHandler();

    private static final Log LOG = LogFactory.getLog(PlaceTypeHandler.class);

    @Override
    public PlaceType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        try {
            return PlaceType.valueOf(json.getAsString());
        } catch (IllegalArgumentException e) {
            LOG.e(String.format("Received an undefined value: '%s'", json.getAsString()));
            return PlaceType.undefined;
        }
    }

    @Override
    public JsonElement serialize(PlaceType src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.name());
    }
}
