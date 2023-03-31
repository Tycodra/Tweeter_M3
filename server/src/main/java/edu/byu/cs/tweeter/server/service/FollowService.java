package edu.byu.cs.tweeter.server.service;

import java.util.List;

import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FollowRequest;
import edu.byu.cs.tweeter.model.net.request.FollowersCountRequest;
import edu.byu.cs.tweeter.model.net.request.FollowersRequest;
import edu.byu.cs.tweeter.model.net.request.FolloweesCountRequest;
import edu.byu.cs.tweeter.model.net.request.FolloweesRequest;
import edu.byu.cs.tweeter.model.net.request.IsFollowerRequest;
import edu.byu.cs.tweeter.model.net.request.UnfollowRequest;
import edu.byu.cs.tweeter.model.net.response.FollowResponse;
import edu.byu.cs.tweeter.model.net.response.FollowersCountResponse;
import edu.byu.cs.tweeter.model.net.response.FollowersResponse;
import edu.byu.cs.tweeter.model.net.response.FolloweesCountResponse;
import edu.byu.cs.tweeter.model.net.response.FolloweesResponse;
import edu.byu.cs.tweeter.model.net.response.IsFollowerResponse;
import edu.byu.cs.tweeter.model.net.response.UnfollowResponse;
import edu.byu.cs.tweeter.server.service.Dynamos.DataPage;
import edu.byu.cs.tweeter.server.service.dao.AuthenticationDAOInterface;
import edu.byu.cs.tweeter.server.service.dao.DAOFactory;
import edu.byu.cs.tweeter.server.service.dao.FollowDAO;
import edu.byu.cs.tweeter.server.service.dao.FollowDAOInterface;
import edu.byu.cs.tweeter.server.service.dao.UserDAOInterface;
import edu.byu.cs.tweeter.util.Pair;

/**
 * Contains the business logic for getting the users a user is following.
 */
public class FollowService {
    private final FollowDAOInterface followDAO;
    private final UserDAOInterface userDAO;
    private final AuthenticationDAOInterface authDAO;
    public FollowService(DAOFactory factory) {
        this.followDAO = factory.getFollowDAO();
        this.userDAO = factory.getUserDAO();
        this.authDAO = factory.getAuthDAO();
    }
    public FollowResponse follow(FollowRequest request) {
        if (request.getAuthToken() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have an authToken");
        } else if (request.getFollower() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a followee");
        } else if (request.getFollowee() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a followee");
        }
        if (authDAO.checkTokenValidity(request.getAuthToken().getToken(), System.currentTimeMillis())) {
            followDAO.addFollow(request.getFollower(), request.getFollowee());
            userDAO.updateNumberFollowees(request.getFollower().getAlias(), true);
            userDAO.updateNumberFollowers(request.getFollowee().getAlias(), true);
            return new FollowResponse();
        } else {
            return new FollowResponse("Session token expired. Please log out and sign back in.");
        }
    }

    public UnfollowResponse unfollow(UnfollowRequest request) {
        if (request.getAuthToken() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have an authToken");
        } else if (request.getFollower() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a followee");
        } else if (request.getFollowee() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a followee");
        }
        if (authDAO.checkTokenValidity(request.getAuthToken().getToken(), System.currentTimeMillis())) {
            followDAO.removeFollow(
                    request.getFollower().getAlias(),
                    request.getFollowee().getAlias());
            userDAO.updateNumberFollowees(request.getFollower().getAlias(), false);
            userDAO.updateNumberFollowers(request.getFollowee().getAlias(), false);
            return new UnfollowResponse();
        } else {
            return new UnfollowResponse("Session token expired. Please log out and sign back in.");
        }
    }

    public IsFollowerResponse isFollower(IsFollowerRequest request) {
        if (request.getFollower() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a follower:User");
        } else if (request.getFollowee() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a followee:User");
        } else if (request.getAuthToken() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have an authToken");
        }
        if (authDAO.checkTokenValidity(request.getAuthToken().getToken(), System.currentTimeMillis())) {
            boolean isFollower = followDAO.isFollower(
                    request.getFollower().getAlias(),
                    request.getFollowee().getAlias());

            return new IsFollowerResponse(isFollower);
        } else {
            return new IsFollowerResponse("Session token expired. Please log out and sign back in.");
        }
    }

    /**
     * Returns the users that the user specified in the request is following. Uses information in
     * the request object to limit the number of followees returned and to return the next set of
     * followees after any that were returned in a previous request. Uses the {@link FollowDAO} to
     * get the followees.
     *
     * @param request contains the data required to fulfill the request.
     * @return the followees.
     */
    public FolloweesResponse getFollowees(FolloweesRequest request) {
        if(request.getFollowerAlias() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a follower alias");
        } else if(request.getLimit() <= 0) {
            throw new RuntimeException("[Bad Request] Request needs to have a positive limit");
        }
        if (authDAO.checkTokenValidity(request.getAuthToken().getToken(), System.currentTimeMillis())) {
            Pair<List<User>, Boolean> pageOfFollowees = followDAO.getFollowees(
                    request.getFollowerAlias(),
                    request.getLimit(),
                    request.getLastFolloweeAlias());

            return new FolloweesResponse(pageOfFollowees.getFirst(), pageOfFollowees.getSecond());
        } else {
            return new FolloweesResponse("Session token expired. Please log out and sign back in.");
        }
    }

    public FollowersResponse getFollowers(FollowersRequest request) {
        if (request.getTargetUserAlias() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a targetUser alias");
        } else if (request.getLimit() <= 0) {
            throw new RuntimeException("[Bad Request] Request needs to have a positive limit");
        }
        if (authDAO.checkTokenValidity(request.getAuthToken().getToken(), System.currentTimeMillis())) {
            Pair<List<User>, Boolean> pageOfFollowers = followDAO.getFollowers(
                    request.getTargetUserAlias(),
                    request.getLimit(),
                    request.getLastFollowerAlias());
            return new FollowersResponse(pageOfFollowers.getFirst(), pageOfFollowers.getSecond());
        } else {
            return new FollowersResponse("Session token expired. Please log out and sign back in.");
        }
    }

    public FolloweesCountResponse getFollowingCount(FolloweesCountRequest request) {
        if (request.getTargetUser() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a targetUser alias");
        }
        if (authDAO.checkTokenValidity(request.getAuthToken().getToken(), System.currentTimeMillis())) {
            int followingCount = userDAO.getNumberFollowees(request.getTargetUser().getAlias());
            return new FolloweesCountResponse(followingCount);
        } else {
            return new FolloweesCountResponse("Session token expired. Please log out and sign back in.");
        }
    }

    public FollowersCountResponse getFollowersCount(FollowersCountRequest request) {
        if (request.getTargetUser() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a targetUser alias");
        }
        if (authDAO.checkTokenValidity(request.getAuthToken().getToken(), System.currentTimeMillis())) {
            int followerCount = userDAO.getNumberFollowers(request.getTargetUser().getAlias());
            return new FollowersCountResponse(followerCount);
        } else {
            return new FollowersCountResponse("Session token expired. Please log out and sign back in.");
        }
    }
}
