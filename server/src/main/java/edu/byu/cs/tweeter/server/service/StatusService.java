package edu.byu.cs.tweeter.server.service;

import java.util.List;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FeedRequest;
import edu.byu.cs.tweeter.model.net.request.PostStatusRequest;
import edu.byu.cs.tweeter.model.net.request.StoryRequest;
import edu.byu.cs.tweeter.model.net.response.FeedResponse;
import edu.byu.cs.tweeter.model.net.response.PostStatusResponse;
import edu.byu.cs.tweeter.model.net.response.StoryResponse;
import edu.byu.cs.tweeter.server.service.dao.AuthenticationDAOInterface;
import edu.byu.cs.tweeter.server.service.dao.DAOFactory;
import edu.byu.cs.tweeter.server.service.dao.FeedDAOInterface;
import edu.byu.cs.tweeter.server.service.dao.FollowDAOInterface;
import edu.byu.cs.tweeter.server.service.dao.StoryDAOInterface;
import edu.byu.cs.tweeter.util.Pair;

public class StatusService {
    FeedDAOInterface feedDAO;
    StoryDAOInterface storyDAO;
    AuthenticationDAOInterface authDAO;
    FollowDAOInterface followDAO;
    public StatusService(DAOFactory factory) {
        this.feedDAO = factory.getFeedDAO();
        this.storyDAO = factory.getStoryDAO();
        this.authDAO = factory.getAuthDAO();
        this.followDAO = factory.getFollowDAO();
    }
    public FeedResponse getFeed(FeedRequest request) {
        if(request.getUserAlias() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a user alias");
        } else if (request.getLimit() <= 0) {
            throw new RuntimeException("[Bad Request] Request needs to have a positive limit");
        }
        if (authDAO.checkTokenValidity(request.getAuthToken().getToken(), System.currentTimeMillis())) {
            Pair<List<Status>, Boolean> pageOfFeed = feedDAO.getFeed(
                    request.getUserAlias(),
                    request.getLimit(),
                    request.getLastStatus());
            return new FeedResponse(pageOfFeed.getFirst(), pageOfFeed.getSecond());
        } else {
            return new FeedResponse("Session token expired. Please log out and sign back in.");
        }
    }

    public StoryResponse getStory(StoryRequest request) {
        if(request.getUserAlias() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a user alias");
        } else if (request.getLimit() <= 0) {
            throw new RuntimeException("[Bad Request] Request needs to have a positive limit");
        }
        if (authDAO.checkTokenValidity(request.getAuthToken().getToken(), System.currentTimeMillis())) {
            Pair<List<Status>, Boolean> pageOfStatuses = storyDAO.getStory(
                    request.getUserAlias(),
                    request.getLimit(),
                    request.getLastStatus());
            return new StoryResponse(pageOfStatuses.getFirst(), pageOfStatuses.getSecond());
        } else {
            return new StoryResponse("Session token expired. Please log out and sign back in.");
        }    }

    public PostStatusResponse postStatus(PostStatusRequest request) {
        if(request.getStatus() == null) {
            throw new RuntimeException("[Bad Request] Missing a status");
        } else if (request.getAuthToken() == null) {
            throw new RuntimeException("[Bad Request] Missing an authToken");
        }
        if (authDAO.checkTokenValidity(request.getAuthToken().getToken(), System.currentTimeMillis())) {
            storyDAO.addToStory(request.getStatus());

            String statusAuthor = request.getStatus().getUser().getAlias();
            Pair<List<User>, Boolean> pageOfFollowers = followDAO.getFollowers(statusAuthor, 10, null);
            List<User> followers = pageOfFollowers.getFirst();
            while (pageOfFollowers.getSecond()) {
                String lastFollowerUsername = followers.get(followers.size()-1).getAlias();
                pageOfFollowers = followDAO.getFollowers(statusAuthor, 10, lastFollowerUsername);
                followers.addAll(pageOfFollowers.getFirst());
            }
            for (User follower : followers) {
                feedDAO.addStatus(follower.getAlias(), request.getStatus());
            }

            return new PostStatusResponse();
        } else {
            return new PostStatusResponse("Session token expired. Please log out and sign back in.");
        }
    }
}
