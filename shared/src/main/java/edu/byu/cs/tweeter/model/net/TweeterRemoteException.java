package edu.byu.cs.tweeter.model.net;

import java.util.List;

public class TweeterRemoteException extends Exception {

    private final String remoteExceptionType;
    private final List<String> remoteStackTrace;

    protected TweeterRemoteException(String message, String remoteExceptionType, List<String> remoteStackTrace) {
        super(message);
        this.remoteExceptionType = remoteExceptionType;
        this.remoteStackTrace = remoteStackTrace;
    }

    public String getRemoteExceptionType() {
        return remoteExceptionType;
    }

    public List<String> getRemoteStackTrace() {
        return remoteStackTrace;
    }
}
