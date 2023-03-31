package edu.byu.cs.tweeter.server.service.dao;

import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.server.service.Dynamos.UserBean;
import edu.byu.cs.tweeter.util.FakeData;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

public class UserDAO implements UserDAOInterface{
    private static final String tableName = "Tweeter_User";
    private final DynamoDbTable<UserBean> table;
    public UserDAO(DynamoDbEnhancedClient enhancedClient) {
        table = enhancedClient.table(tableName, TableSchema.fromBean(UserBean.class));
    }

    public User getUser(String targetUserHandle) {
        Key key = Key.builder()
                .partitionValue(targetUserHandle)
                .build();
        UserBean userBean = table.getItem(key);
        User user;
        if (userBean == null) {
            user = getDummyUser();
        } else {
            user = new User(userBean.getFirstName(),
                    userBean.getLastName(),
                    userBean.getUsername(),
                    userBean.getImageUrl());
        }
        return user;
    }

    @Override
    public boolean verifyLogin(String username, String password) {
        Key key = Key.builder().partitionValue(username).build();
        UserBean userBean = table.getItem(key);
        if (userBean == null) {
            return false;
        } else return userBean.getPassword().equals(password);
    }

    @Override
    public boolean verifyUsernameAvailability(String username) {
        Key key = Key.builder().partitionValue(username).build();
        UserBean userBean = table.getItem(key);
        return userBean == null; // null if the name wasn't in the table
    }

    @Override
    public User registerUser(String username, String hashedPassword, String firstName, String lastName, String imageUrl, int numberFollowers, int numberFollowees) {
        String tempImageUrl = "https://faculty.cs.byu.edu/~jwilkerson/cs340/tweeter/images/daisy_duck.png";

        UserBean userBean = new UserBean();
        userBean.setUsername(username);
        userBean.setPassword(hashedPassword);
        userBean.setFirstName(firstName);
        userBean.setLastName(lastName);
        userBean.setImageUrl(tempImageUrl);
        userBean.setNumberFollowers(0);
        userBean.setNumberFollowees(0);

        table.putItem(userBean);
        userBean = table.getItem(Key.builder().partitionValue(username).build());

        return new User(userBean.getFirstName(),
                userBean.getLastName(),
                userBean.getUsername(),
                tempImageUrl);
    }

    /**
     * Returns the dummy user to be returned by the login operation.
     * This is written as a separate method to allow mocking of the dummy user.
     *
     * @return a dummy user.
     */
    private User getDummyUser() {
        return getFakeData().getFirstUser();
    }
    /**
     * Returns the {@link FakeData} object used to generate dummy users and auth tokens.
     * This is written as a separate method to allow mocking of the {@link FakeData}.
     *
     * @return a {@link FakeData} instance.
     */
    private FakeData getFakeData() {
        return FakeData.getInstance();
    }
}
