package edu.byu.cs.tweeter.server.service.dao;

import java.util.List;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.util.Pair;

public interface StoryDAOInterface {
    Pair<List<Status>, Boolean> getStory(String authorUsername, int pageLimit, Status lastStatus);
    void addToStory(Status newStatus);
}
