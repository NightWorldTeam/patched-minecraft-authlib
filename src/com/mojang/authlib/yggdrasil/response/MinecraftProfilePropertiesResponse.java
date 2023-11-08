package com.mojang.authlib.yggdrasil.response;

import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

public record MinecraftProfilePropertiesResponse(
    @SerializedName("id")
    UUID id,
    @SerializedName("name")
    String name,
    @SerializedName("properties")
    PropertyMap properties,
    @SerializedName("profileActions")
    @Nullable Set<ProfileAction> profileActions
) {
    public GameProfile toProfile() {
        final GameProfile profile = new GameProfile(id, name);
        profile.getProperties().putAll(properties);
        return profile;
    }

    @Override
    public Set<ProfileAction> profileActions() {
        return profileActions != null ? profileActions : Set.of();
    }
}
