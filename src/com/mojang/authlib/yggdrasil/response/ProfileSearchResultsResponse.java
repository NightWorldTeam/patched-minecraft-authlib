package com.mojang.authlib.yggdrasil.response;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.GameProfile;

import java.lang.reflect.Type;
import java.util.List;

public record ProfileSearchResultsResponse(List<GameProfile> profiles) {
    public static class Serializer implements JsonDeserializer<ProfileSearchResultsResponse> {
        @Override
        public ProfileSearchResultsResponse deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            return new ProfileSearchResultsResponse(context.deserialize(json, TypeToken.getParameterized(List.class, GameProfile.class).getType()));
        }
    }
}
