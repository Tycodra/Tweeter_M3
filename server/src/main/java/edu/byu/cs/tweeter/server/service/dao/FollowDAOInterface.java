package edu.byu.cs.tweeter.server.service.dao;

import java.util.List;

import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.util.Pair;

public interface FollowDAOInterface {
    Pair<List<User>, Boolean> getFollowees(
            String followerUsername,
            int pageLimit,
            String lastFolloweeUsername);
    Pair<List<User>, Boolean> getFollowers(
            String followeeUsername,
            int pageLimit,
            String lastFollowerUsername);

    boolean isFollower(
            String followerUsername,
            String followeeUsername);
    void addFollow(User follower, User followee);
    void removeFollow(
            String followerUsername,
            String followeeUsername);
}
