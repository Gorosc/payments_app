package org.cgoro.model;

public class Dto {

    private boolean success;
    private String error;
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public Dto setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public String getError() {
        return error;
    }

    public Dto setError(String error) {
        this.error = error;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Dto setMessage(String message) {
        this.message = message;
        return this;
    }
}
