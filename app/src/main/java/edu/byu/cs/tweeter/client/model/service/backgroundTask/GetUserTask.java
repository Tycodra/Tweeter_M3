package edu.byu.cs.tweeter.client.model.service.backgroundTask;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.GetUserRequest;
import edu.byu.cs.tweeter.model.net.response.GetUserResponse;

/**
 * Background task that returns the profile for a specified user.
 */
public class GetUserTask extends AuthenticatedTask {
    private static final String URL_PATH = "/getuser";
    private static final String LOG_TAG = "getUserTask";
    public static final String USER_KEY = "user";

    /**
     * Alias (or handle) for user whose profile is being retrieved.
     */
    private final String alias;

    private User user;

    public GetUserTask(AuthToken authToken, String alias, Handler messageHandler) {
        super(authToken, messageHandler);
        this.alias = alias;
    }

    @Override
    protected void runTask() throws Exception {
        try {
            GetUserRequest request = new GetUserRequest(alias, getAuthToken());
            GetUserResponse response = getServerFacade().getUser(request, URL_PATH);

            if (response.isSuccess()) {
                user = response.getUser();
            } else {
                throw new Exception(response.getMessage());
            }
        } catch (Exception ex) {
            Log.e(LOG_TAG, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    protected void loadSuccessBundle(Bundle msgBundle) {
        msgBundle.putSerializable(USER_KEY, user);
    }
}
