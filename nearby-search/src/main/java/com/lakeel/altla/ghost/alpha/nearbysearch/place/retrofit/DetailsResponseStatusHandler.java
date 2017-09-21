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

import java.lang.reflect.Type;

public final class DetailsResponseStatusHandler implements JsonSerializer<DetailsResponse.Status>,
                                                           JsonDeserializer<DetailsResponse.Status> {

    public static final DetailsResponseStatusHandler INSTANCE = new DetailsResponseStatusHandler();

    private static final Log LOG = LogFactory.getLog(DetailsResponseStatusHandler.class);

    private DetailsResponseStatusHandler() {
    }

    @Override
    public DetailsResponse.Status deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        try {
            return DetailsResponse.Status.valueOf(json.getAsString());
        }catch (IllegalArgumentException e) {
            LOG.e(String.format("Received an undefined value: '%s'", json.getAsString()));
            return DetailsResponse.Status.UNDEFINED;
        }
    }

    @Override
    public JsonElement serialize(DetailsResponse.Status src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.name());
    }
}
