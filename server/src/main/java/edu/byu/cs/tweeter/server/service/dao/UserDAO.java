package edu.byu.cs.tweeter.server.service.dao;

import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.server.service.Dynamos.UserBean;
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

    public User getUser(String targetUsername) {
        Key key = Key.builder()
                .partitionValue(targetUsername)
                .build();
        UserBean userBean = table.getItem(key);
        User user = new User(userBean.getFirstName(),
                    userBean.getLastName(),
                    userBean.getUsername(),
                    userBean.getImageUrl());
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

    @Override
    public int getNumberFollowers(String username) {
        Key key = Key.builder().partitionValue(username).build();
        UserBean userBean = table.getItem(key);
        if (userBean != null) {
            return userBean.getNumberFollowers();
        } else {
            return 0;
        }
    }

    @Override
    public int getNumberFollowees(String username) {
        Key key = Key.builder().partitionValue(username).build();
        UserBean userBean = table.getItem(key);
        if (userBean != null) {
            return userBean.getNumberFollowees();
        } else {
            return 0;
        }
    }

    @Override
    public void updateNumberFollowers(String username, boolean increase) {
        Key key = Key.builder().partitionValue(username).build();
        UserBean userBean = table.getItem(key);
        if (userBean != null) {
            int currentCount = userBean.getNumberFollowers();
            if (increase) {
                userBean.setNumberFollowers(currentCount + 1);
                table.updateItem(userBean);
            } else { // decreasing
                userBean.setNumberFollowers(currentCount - 1);
                table.updateItem(userBean);
            }
        }
    }

    @Override
    public void updateNumberFollowees(String username, boolean increase) {
        Key key = Key.builder().partitionValue(username).build();
        UserBean userBean = table.getItem(key);
        if (userBean != null) {
            int currentCount = userBean.getNumberFollowees();
            if (increase) {
                userBean.setNumberFollowees(currentCount + 1);
                table.updateItem(userBean);
            } else { // decreasing
                userBean.setNumberFollowees(currentCount - 1);
                table.updateItem(userBean);
            }
        }
    }
}
