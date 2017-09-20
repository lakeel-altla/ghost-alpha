package com.lakeel.altla.ghost.alpha.nearbysearch.place.model.service;

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

public final class SearchResponseStatusHandler implements JsonSerializer<SearchResponse.Status>,
                                                          JsonDeserializer<SearchResponse.Status> {

    public static final SearchResponseStatusHandler INSTANCE = new SearchResponseStatusHandler();

    private static final Log LOG = LogFactory.getLog(SearchResponseStatusHandler.class);

    private SearchResponseStatusHandler() {
    }

    @Override
    public SearchResponse.Status deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        try {
            return SearchResponse.Status.valueOf(json.getAsString());
        } catch (IllegalArgumentException e) {
            LOG.e(String.format("Received an undefined value: '%s'", json.getAsString()));
            return SearchResponse.Status.UNDEFINED;
        }
    }

    @Override
    public JsonElement serialize(SearchResponse.Status src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.name());
    }
}
