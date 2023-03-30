package edu.byu.cs.tweeter.server.service.dao;

public interface DAOFactory {
    FollowDAOInterface getFollowDAO();
    StoryDAOInterface getStoryDAO();
    FeedDAOInterface getFeedDAO();
    AuthenticationDAOInterface getAuthDAO();
    UserDAOInterface getUserDAO();
}
