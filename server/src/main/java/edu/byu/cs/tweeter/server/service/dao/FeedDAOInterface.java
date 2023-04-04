package edu.byu.cs.tweeter.server.service.dao;

import java.util.List;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.util.Pair;

public interface FeedDAOInterface {
    Pair<List<Status>, Boolean> getFeed(String feedOwnerUsername, int pageLimit, Status lastStatus);
    void addStatus(String feedOwnerUsername, Status status);
}
