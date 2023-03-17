package edu.byu.cs.tweeter.client.model.service.backgroundTask;

import android.os.Handler;
import android.util.Log;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FollowersCountRequest;
import edu.byu.cs.tweeter.model.net.response.FollowersCountResponse;

/**
 * Background task that queries how many followers a user has.
 */
public class GetFollowersCountTask extends GetCountTask {
    private static String URL_PATH = "/getfollowerscount";
    private static String LOG_TAG = "getFollowersCount";

    public GetFollowersCountTask(AuthToken authToken, User targetUser, Handler messageHandler) {
        super(authToken, targetUser, messageHandler);
    }

    @Override
    protected int runCountTask() throws Exception{
        try {
            FollowersCountRequest request = new FollowersCountRequest(getTargetUser(), getAuthToken());
            FollowersCountResponse response = getServerFacade().getFollowersCount(request, URL_PATH);

            if (response.isSuccess()) {
                return response.getCount();
            } else {
                throw new Exception(response.getMessage());
            }
        } catch (Exception ex) {
            Log.e(LOG_TAG, "Failed to get followers count");
            throw ex;
        }
    }
}
