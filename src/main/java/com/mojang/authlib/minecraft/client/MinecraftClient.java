package com.mojang.authlib.minecraft.client;

import com.mojang.authlib.exceptions.MinecraftClientException;
import com.mojang.authlib.exceptions.MinecraftClientException.ErrorType;
import com.mojang.authlib.exceptions.MinecraftClientHttpException;
import com.mojang.authlib.yggdrasil.response.ErrorResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;


/**
 *
 * Client to use when communicating with Minecraft service API.
 *
 *
 */
public class MinecraftClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinecraftClient.class);
    public static final int CONNECT_TIMEOUT_MS = 5000;
    public static final int READ_TIMEOUT_MS = 5000;

    @Nullable
    private final String accessToken;
    private final Proxy proxy;
    private final ObjectMapper objectMapper = ObjectMapper.create();

    public MinecraftClient(@Nullable final String accessToken, final Proxy proxy) {
        this.accessToken = accessToken;
        this.proxy = Validate.notNull(proxy);
    }

    public static MinecraftClient unauthenticated(final Proxy proxy) {
        return new MinecraftClient(null, proxy);
    }

    @Nullable
    public <T> T get(final URL url, final Class<T> responseClass) {
        Validate.notNull(url);
        Validate.notNull(responseClass);
        final HttpURLConnection connection = createUrlConnection(url);
        if (accessToken != null) {
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        }

        return readInputStream(url, responseClass, connection);
    }

    @Nullable
    public <T> T post(final URL url, final Class<T> responseClass) {
        Validate.notNull(url);
        Validate.notNull(responseClass);
        final HttpURLConnection connection = postInternal(url, new byte[0]);
        return readInputStream(url, responseClass, connection);
    }

    @Nullable
    public <T> T post(final URL url, final Object body, final Class<T> responseClass) {
        Validate.notNull(url);
        Validate.notNull(body);
        Validate.notNull(responseClass);
        final String bodyAsJson = objectMapper.writeValueAsString(body);
        final byte[] postAsBytes = bodyAsJson.getBytes(StandardCharsets.UTF_8);
        final HttpURLConnection connection = postInternal(url, postAsBytes);
        return readInputStream(url, responseClass, connection);
    }

    @Nullable
    private <T> T readInputStream(final URL url, final Class<T> clazz, final HttpURLConnection connection) {

        InputStream inputStream = null;
        try {
            final int status = connection.getResponseCode();

            final String result;
            if (status < 400) {
                inputStream = connection.getInputStream();
                result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                if (result.isEmpty()) {
                    return null;
                }
                return objectMapper.readValue(result, clazz);
            } else {
                inputStream = connection.getErrorStream();
                final ErrorResponse errorResponse;
                if (inputStream != null) {
                    result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                    errorResponse = objectMapper.readValue(result, ErrorResponse.class);
                    throw new MinecraftClientHttpException(status, errorResponse);
                } else {
                    throw new MinecraftClientHttpException(status);
                }
            }
        } catch (final IOException e) {
            //Connection errors
            throw new MinecraftClientException(
                ErrorType.SERVICE_UNAVAILABLE , "Failed to read from " + url + " due to " + e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private HttpURLConnection postInternal(final URL url, final byte[] postAsBytes) {

        final HttpURLConnection connection = createUrlConnection(url);
        OutputStream outputStream = null;
        try {
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Content-Length", "" + postAsBytes.length);
            if (accessToken != null) {
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            }
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            outputStream = connection.getOutputStream();
            IOUtils.write(postAsBytes, outputStream);
        } catch (final IOException io) {
            throw new MinecraftClientException(ErrorType.SERVICE_UNAVAILABLE, "Failed to POST " + url, io);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
        return connection;

    }

    private HttpURLConnection createUrlConnection(final URL url) {
        try {
            LOGGER.debug("Connecting to {}", url);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setUseCaches(false);
            return connection;
        } catch (final IOException io) {
            throw new MinecraftClientException(ErrorType.SERVICE_UNAVAILABLE, "Failed connecting to " + url, io);
        }
    }
}
