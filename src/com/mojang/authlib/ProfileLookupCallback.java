package com.mojang.authlib;

public interface ProfileLookupCallback {
    void onProfileLookupSucceeded(GameProfile profile);

    void onProfileLookupFailed(String profileName, Exception exception);
}
