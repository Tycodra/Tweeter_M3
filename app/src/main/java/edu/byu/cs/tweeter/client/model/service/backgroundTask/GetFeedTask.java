package edu.byu.cs.tweeter.client.model.service.backgroundTask;

import android.os.Handler;
import android.util.Log;

import java.util.List;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FeedRequest;
import edu.byu.cs.tweeter.model.net.response.FeedResponse;
import edu.byu.cs.tweeter.util.Pair;

/**
 * Background task that retrieves a page of statuses from a user's feed.
 */
public class GetFeedTask extends PagedStatusTask {
    public static final String URL_PATH = "/getfeed";
    private static final String LOG_TAG = "getFeedTask";
    public GetFeedTask(AuthToken authToken, User targetUser, int limit, Status lastStatus,
                       Handler messageHandler) {
        super(authToken, targetUser, limit, lastStatus, messageHandler);
    }

    @Override
    protected Pair<List<Status>, Boolean> getItems() throws Exception{
        try {
            String targetUserAlias = getTargetUser() == null ? null : getTargetUser().getAlias();
            Status lastStatus = getLastItem() == null ? null : getLastItem();

            FeedRequest request = new FeedRequest(getAuthToken(), targetUserAlias, getLimit(), lastStatus);
            FeedResponse response = getServerFacade().getFeed(request, URL_PATH);

            if (response.isSuccess()) {
                return new Pair<>(response.getFeed(), response.getHasMorePages());
            } else {
                throw new Exception(response.getMessage());
            }
        } catch (Exception ex) {
            Log.e(LOG_TAG, ex.getMessage(), ex);
            throw ex;
        }
    }
}
