package com.lakeel.altla.ghost.alpha.google.place.web.retrofit;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.google.place.web.Scope;

import java.lang.reflect.Type;

public final class ScopeHandler implements JsonSerializer<Scope>, JsonDeserializer<Scope> {

    public static final ScopeHandler INSTANCE = new ScopeHandler();

    private static final Log LOG = LogFactory.getLog(ScopeHandler.class);

    private ScopeHandler() {
    }

    @Override
    public Scope deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        try {
            return Scope.valueOf(json.getAsString());
        } catch (IllegalArgumentException e) {
            LOG.e(String.format("Received an undefined value: '%s'", json.getAsString()));
            return Scope.UNDEFINED;
        }
    }

    @Override
    public JsonElement serialize(Scope src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.name());
    }
}
