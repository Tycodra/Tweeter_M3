package edu.byu.cs.tweeter.server.service.dao;

import edu.byu.cs.tweeter.model.net.request.FeedRequest;
import edu.byu.cs.tweeter.model.net.response.FeedResponse;

public interface FeedDAOInterface {
    FeedResponse getFeed(FeedRequest request);
}
