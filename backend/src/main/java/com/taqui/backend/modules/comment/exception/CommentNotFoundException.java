package com.taqui.backend.modules.comment.exception;

public class CommentNotFoundException extends RuntimeException {

    public CommentNotFoundException(String message) {
        super(message);
    }
}
