package edu.byu.cs.tweeter.server.service.dao;

import edu.byu.cs.tweeter.model.domain.User;

public interface UserDAOInterface {
    User getUser(String targetUserHandle);
    boolean verifyLogin(String username, String password);
    boolean verifyUsernameAvailability(String username);
    User registerUser(String username, String hashedPassword, String firstName, String lastName, String imageUrl, int numberFollowers, int numberFollowees);
}
