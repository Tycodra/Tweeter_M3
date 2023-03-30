package edu.byu.cs.tweeter.server.service.dao;

import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FollowersRequest;
import edu.byu.cs.tweeter.model.net.request.FollowingRequest;
import edu.byu.cs.tweeter.model.net.request.IsFollowerRequest;
import edu.byu.cs.tweeter.model.net.response.FollowersResponse;
import edu.byu.cs.tweeter.model.net.response.FollowingResponse;

public interface FollowDAOInterface {
    FollowingResponse getFollowees(FollowingRequest request);
    FollowersResponse getFollowers(FollowersRequest request);
    int getFollowingCount(User targetUser);
    int getFollowersCount(User targetUser);
    boolean isFollower(IsFollowerRequest request);
}
