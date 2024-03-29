package edu.byu.cs.tweeter.client.model.service.backgroundTask;

import android.os.Handler;
import android.util.Log;

import java.util.List;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FolloweesRequest;
import edu.byu.cs.tweeter.model.net.response.FolloweesResponse;
import edu.byu.cs.tweeter.util.Pair;

/**
 * Background task that retrieves a page of other users being followed by a specified user.
 */
public class GetFollowingTask extends PagedUserTask {
    public static final String URL_PATH = "/getfollowing";
    private static final String LOG_TAG = "getFollowingTask";
    public GetFollowingTask(AuthToken authToken, User targetUser, int limit, User lastFollowee,
                            Handler messageHandler) {
        super(authToken, targetUser, limit, lastFollowee, messageHandler);
    }

    @Override
    protected Pair<List<User>, Boolean> getItems() throws Exception {
        try {
            String targetUserAlias = getTargetUser() == null ? null : getTargetUser().getAlias();
            String lastFolloweeAlias = getLastItem() == null ? null : getLastItem().getAlias();

            FolloweesRequest request = new FolloweesRequest(getAuthToken(), targetUserAlias, getLimit(), lastFolloweeAlias);
            FolloweesResponse response = getServerFacade().getFollowees(request, URL_PATH);

            if (response.isSuccess()) {
                return new Pair<>(response.getFollowees(), response.getHasMorePages());

            } else {
                throw new Exception(response.getMessage());
            }
        } catch (Exception ex) {
            Log.e(LOG_TAG, ex.getMessage(), ex);
            throw ex;
        }
    }
}
