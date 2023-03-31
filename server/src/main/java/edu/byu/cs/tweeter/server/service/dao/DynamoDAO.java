package edu.byu.cs.tweeter.server.service.dao;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoDAO implements DAOFactory{
    protected DynamoDbClient dynamoDbClient;
    protected DynamoDbEnhancedClient enhancedClient;
    public DynamoDAO() {
        dynamoDbClient = DynamoDbClient.builder()
                .region(Region.US_WEST_2)
                .build();
        enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }
    @Override
    public FollowDAOInterface getFollowDAO() {
        return new FollowDAO(enhancedClient);
    }

    @Override
    public StoryDAOInterface getStoryDAO() {
        return new StoryDAO(enhancedClient);
    }

    @Override
    public FeedDAOInterface getFeedDAO() {
        return new FeedDAO(enhancedClient);
    }

    @Override
    public AuthenticationDAOInterface getAuthDAO() {
        return new AuthenticationDAO(enhancedClient);
    }

    @Override
    public UserDAOInterface getUserDAO() {
        return new UserDAO(enhancedClient);
    }
}
