package edu.byu.cs.tweeter.server.service.dao;

import edu.byu.cs.tweeter.model.net.request.StoryRequest;
import edu.byu.cs.tweeter.model.net.response.StoryResponse;

public interface StoryDAOInterface {
    StoryResponse getStory(StoryRequest request);
}
