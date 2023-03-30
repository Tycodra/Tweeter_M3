package edu.byu.cs.tweeter.server.service.dao;

import java.util.ArrayList;
import java.util.List;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.net.request.StoryRequest;
import edu.byu.cs.tweeter.model.net.response.StoryResponse;
import edu.byu.cs.tweeter.util.FakeData;

public class StoryDAO implements StoryDAOInterface{
    public StoryResponse getStory(StoryRequest request) {
        assert request.getLimit() > 0;
        assert request.getUserAlias() != null;

        List<Status> allStatuses = getFakeData().getFakeStatuses();
        List<Status> responseStatuses = new ArrayList<>(request.getLimit());

        boolean hasMorePages = false;

        if (request.getLimit() > 0) {
            int storyIndex = getStartingIndex(request.getLastStatus(), allStatuses);

            for (int limitCounter = 0; storyIndex < allStatuses.size() && limitCounter < request.getLimit(); storyIndex++, limitCounter++) {
                responseStatuses.add(allStatuses.get(storyIndex));
            }
            hasMorePages = storyIndex < allStatuses.size();
        }
        return new StoryResponse(responseStatuses, hasMorePages);
    }

    private int getStartingIndex(Status lastStatus, List<Status> allStatuses) {
        int startingIndex = 0;

        if (lastStatus != null) {
            for (int i = 0; i < allStatuses.size(); i++) {
                if(lastStatus.equals(allStatuses.get(i))) {
                    startingIndex = i + 1;
                    break;
                }
            }
        }
        return startingIndex;
    }

    FakeData getFakeData() {
        return FakeData.getInstance();
    }
}
