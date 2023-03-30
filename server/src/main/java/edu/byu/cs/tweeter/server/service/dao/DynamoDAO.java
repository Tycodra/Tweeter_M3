package edu.byu.cs.tweeter.server.service.dao;

public class DynamoDAO implements DAOFactory{
    @Override
    public FollowDAOInterface getFollowDAO() {
        return new FollowDAO();
    }

    @Override
    public StoryDAOInterface getStoryDAO() {
        return new StoryDAO();
    }

    @Override
    public FeedDAOInterface getFeedDAO() {
        return new FeedDAO();
    }

    @Override
    public AuthenticationDAOInterface getAuthDAO() {
        return new AuthenticationDAO();
    }

    @Override
    public UserDAOInterface getUserDAO() {
        return new UserDAO();
    }
}
