package edu.byu.cs.tweeter.server.service.dao;

import edu.byu.cs.tweeter.model.domain.AuthToken;

public interface AuthenticationDAOInterface {
    AuthToken getAuthToken();
    AuthToken addAuthToken(String authToken, long timestamp);
    void removeAuthToken(String authToken);
    boolean checkTokenValidity(String authToken, long timestamp);
}
