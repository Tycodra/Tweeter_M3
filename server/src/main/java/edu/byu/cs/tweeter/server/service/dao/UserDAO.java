package edu.byu.cs.tweeter.server.service.dao;

import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.util.FakeData;

public class UserDAO implements UserDAOInterface{

    public User getUser(String targetUserHandle) {
        User user = getFakeData().findUserByAlias(targetUserHandle);
        if (user == null) {
            user = getDummyUser();
        }

        return user;
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
