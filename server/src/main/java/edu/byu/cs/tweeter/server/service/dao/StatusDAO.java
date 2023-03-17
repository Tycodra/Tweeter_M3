package edu.byu.cs.tweeter.server.service.dao;

import java.util.ArrayList;
import java.util.List;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.net.request.FeedRequest;
import edu.byu.cs.tweeter.model.net.response.FeedResponse;
import edu.byu.cs.tweeter.util.FakeData;

public class StatusDAO {
    public FeedResponse getFeed(FeedRequest request) {
        assert request.getLimit() > 0;
        assert request.getUserAlias() != null;

        List<Status> allStatuses = getFakeData().getFakeStatuses();
        List<Status> responseStatuses = new ArrayList<>(request.getLimit());

        boolean hasMorePages = false;

        if (request.getLimit() > 0) {
            if (allStatuses != null) {
                int feedIndex = getFeedStartingIndex(request.getLastStatus(), allStatuses);

                for (int limitCounter = 0; feedIndex < allStatuses.size() && limitCounter < request.getLimit(); feedIndex++, limitCounter++) {
                    responseStatuses.add(allStatuses.get(feedIndex));
                }
                hasMorePages = feedIndex < allStatuses.size();
            }
        }

        return new FeedResponse(responseStatuses, hasMorePages);
    }

    private int getFeedStartingIndex(Status lastStatus, List<Status> allStatuses) {
        int feedIndex = 0;

        if (lastStatus != null) {
            for (int i = 0; i < allStatuses.size(); i++) {
                if(lastStatus.equals(allStatuses.get(i))) {
                    feedIndex = i + 1;
                    break;
                }
            }
        }
        return feedIndex;
    }

    FakeData getFakeData() {
        return FakeData.getInstance();
    }
}
