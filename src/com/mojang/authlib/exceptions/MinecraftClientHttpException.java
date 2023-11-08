package com.mojang.authlib.exceptions;

import com.mojang.authlib.yggdrasil.response.ErrorResponse;
import java.util.Optional;
import java.util.StringJoiner;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

public class MinecraftClientHttpException extends MinecraftClientException {

    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    private final int status;
    @Nullable
    private final ErrorResponse response;

    public MinecraftClientHttpException(final int status) {
        super(ErrorType.HTTP_ERROR, getErrorMessage(status, null));
        this.status = status;
        this.response = null;
    }

    public MinecraftClientHttpException(final int status, final ErrorResponse response) {
        super(ErrorType.HTTP_ERROR, getErrorMessage(status, response));
        this.status = status;
        this.response = response;
    }

    public int getStatus() {
        return status;
    }

    public Optional<ErrorResponse> getResponse() {
        return Optional.ofNullable(response);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MinecraftClientHttpException.class.getSimpleName() + "[", "]")
                   .add("type=" + type)
                   .add("status=" + status)
                   .add("response=" + response)
                   .toString();
    }

    @Override
    public AuthenticationException toAuthenticationException() {
        if (hasError("ForbiddenOperationException")) {
            return new InvalidCredentialsException(getMessage());
        } else if (hasError("multiplayer.access.banned")) {
            return new UserBannedException();
        } else if (hasError("FORCED_USERNAME_CHANGE")) {
            return new ForcedUsernameChangeException();
        } else if (hasError("InsufficientPrivilegesException")) {
            return new InsufficientPrivilegesException(getMessage(), this);
        }

        if (status == UNAUTHORIZED) {
            return new InvalidCredentialsException(getMessage(), this);
        }

        if (status >= 500) {
            return new AuthenticationUnavailableException(getMessage(), this);
        }

        return new AuthenticationException(getMessage(), this);
    }

    private Optional<String> getError() {
        return getResponse()
                   .map(ErrorResponse::error)
                   .filter(StringUtils::isNotEmpty);
    }

    private static String getErrorMessage(final int status, final ErrorResponse response) {
        final String errorMessage;
        if (response != null) {
            if (StringUtils.isNotEmpty(response.errorMessage())) {
                errorMessage = response.errorMessage();
            } else if (StringUtils.isNotEmpty(response.error())) {
                errorMessage = response.error();
            } else {
                errorMessage = "Status: " + status;
            }
        } else {
            errorMessage = "Status: " + status;
        }
        return errorMessage;
    }

    private boolean hasError(final String error) {
        return getError()
                   .filter(value -> value.equalsIgnoreCase(error))
                   .isPresent();
    }
}
