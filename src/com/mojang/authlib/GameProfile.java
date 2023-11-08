package com.mojang.authlib;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.authlib.properties.PropertyMap;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.UUID;

public class GameProfile {
    private final UUID id;
    private final String name;
    private final PropertyMap properties = new PropertyMap();

    /**
     * Constructs a new Game Profile with the specified ID and name.
     *
     * @param id Unique ID of the profile
     * @param name Display name of the profile
     * @throws java.lang.NullPointerException if either id or name are {@code null}
     */
    public GameProfile(final UUID id, final String name) {
        this.id = Objects.requireNonNull(id, "Profile ID must not be null");
        this.name = Objects.requireNonNull(name, "Profile name must not be null");
    }

    /**
     * Gets the unique ID of this game profile.
     * <p />
     *
     * @return ID of the profile
     */
    public UUID getId() {
        return id;
    }

    /**
     * Gets the display name of this game profile.
     * <p />
     *
     * @return Name of the profile
     */
    public String getName() {
        return name;
    }

    /**
     * Returns any known properties about this game profile.
     *
     * @return Modifiable map of profile properties.
     */
    public PropertyMap getProperties() {
        return properties;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final GameProfile that = (GameProfile) o;
        return id.equals(that.id) && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", id)
            .append("name", name)
            .append("properties", properties)
            .toString();
    }

    public static class Serializer implements JsonSerializer<GameProfile>, JsonDeserializer<GameProfile> {
        @Override
        public GameProfile deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            final JsonObject object = (JsonObject) json;
            final UUID id = context.deserialize(object.get("id"), UUID.class);
            final String name = object.getAsJsonPrimitive("name").getAsString();
            return new GameProfile(id, name);
        }

        @Override
        public JsonElement serialize(final GameProfile src, final Type typeOfSrc, final JsonSerializationContext context) {
            final JsonObject result = new JsonObject();
            if (src.getId() != null) {
                result.add("id", context.serialize(src.getId()));
            }
            if (src.getName() != null) {
                result.addProperty("name", src.getName());
            }
            return result;
        }
    }
}
