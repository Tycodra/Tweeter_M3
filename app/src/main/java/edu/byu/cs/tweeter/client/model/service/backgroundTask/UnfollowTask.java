package edu.byu.cs.tweeter.client.model.service.backgroundTask;

import android.os.Handler;
import android.util.Log;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.UnfollowRequest;
import edu.byu.cs.tweeter.model.net.response.UnfollowResponse;

/**
 * Background task that removes a following relationship between two users.
 */
public class UnfollowTask extends AuthenticatedTask {
    private static final String URL_PATH = "/unfollow";
    private static final String LOG_TAG = "unfollowTask";
    /**
     * The user that is being followed.
     */
    private final User followee;

    public UnfollowTask(AuthToken authToken, User followee, Handler messageHandler) {
        super(authToken, messageHandler);
        this.followee = followee;
    }

    @Override
    protected void runTask() throws Exception {
        try {
            User currentUser = Cache.getInstance().getCurrUser();
            UnfollowRequest request = new UnfollowRequest(currentUser, followee, getAuthToken());
            UnfollowResponse response = getServerFacade().unfollow(request, URL_PATH);

            if (response.isSuccess()) {
                //Congrats, you did it
            } else {
                throw new Exception(response.getMessage());
            }
        } catch (Exception ex) {
            Log.e(LOG_TAG, ex.getMessage(), ex);
            throw ex;
        }
    }
}
