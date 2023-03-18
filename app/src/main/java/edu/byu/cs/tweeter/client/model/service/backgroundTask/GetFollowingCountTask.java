package edu.byu.cs.tweeter.client.model.service.backgroundTask;

import android.os.Handler;
import android.util.Log;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FollowingCountRequest;
import edu.byu.cs.tweeter.model.net.response.FollowingCountResponse;

/**
 * Background task that queries how many other users a specified user is following.
 */
public class GetFollowingCountTask extends GetCountTask {
    private static final String URL_PATH = "/getfollowingcount";
    private static final String LOG_TAG = "getFollowingCountTask";
    public GetFollowingCountTask(AuthToken authToken, User targetUser, Handler messageHandler) {
        super(authToken, targetUser, messageHandler);
    }

    @Override
    protected int runCountTask() throws Exception{
        try {
            FollowingCountRequest request = new FollowingCountRequest(getTargetUser(), getAuthToken());
            FollowingCountResponse response = getServerFacade().getFollowingCount(request, URL_PATH);

            if (response.isSuccess()) {
                return response.getCount();
            } else {
                throw new Exception(response.getMessage());
            }
        } catch (Exception ex) {
            Log.e(LOG_TAG, ex.getMessage(), ex);
            throw ex;
        }
    }
}
