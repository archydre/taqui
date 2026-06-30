package com.taqui.backend.modules.user.exception;

public class PixKeyAlreadyExistsException extends RuntimeException {

    public PixKeyAlreadyExistsException(String message) {
        super(message);
    }
}
