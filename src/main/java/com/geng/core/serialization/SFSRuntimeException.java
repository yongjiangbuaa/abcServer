package com.geng.core.serialization;

public class SFSRuntimeException extends RuntimeException {
    public SFSRuntimeException() {
    }

    public SFSRuntimeException(String message) {
        super(message);
    }

    public SFSRuntimeException(Throwable t) {
        super(t);
    }
}
