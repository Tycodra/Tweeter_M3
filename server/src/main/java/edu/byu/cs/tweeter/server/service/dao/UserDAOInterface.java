package edu.byu.cs.tweeter.server.service.dao;

import edu.byu.cs.tweeter.model.domain.User;

public interface UserDAOInterface {
    User getUser(String targetUserHandle);
}
