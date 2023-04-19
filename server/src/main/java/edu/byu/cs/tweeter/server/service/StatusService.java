package edu.byu.cs.tweeter.server.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    AmazonSQS sqs;
    public StatusService(DAOFactory factory) {
        this.feedDAO = factory.getFeedDAO();
        this.storyDAO = factory.getStoryDAO();
        this.authDAO = factory.getAuthDAO();
        this.followDAO = factory.getFollowDAO();
        sqs = AmazonSQSClientBuilder.defaultClient();
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

            Gson gson = new Gson();
            String messageBody = gson.toJson(request.getStatus());
            String queueUrl = "https://sqs.us-west-2.amazonaws.com/186990554695/PostStatusQueue";

            SendMessageRequest send_msg_request = new SendMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withMessageBody(messageBody);

            sqs.sendMessage(send_msg_request);

            return new PostStatusResponse();
        } else {
            return new PostStatusResponse("Session token expired. Please log out and sign back in.");
        }
    }

    public void postUpdateFeedMessage(String msgBody) {
        Gson gson = new Gson();
        Status newStatus = gson.fromJson(msgBody, Status.class);

        String statusAuthor = newStatus.getUser().getAlias();
        int pageLimit = 200;
        String lastFollowerUsername = null;
        boolean hasMorePages = true;
        int requestsSent = 0;

        while (hasMorePages) {
            Pair<List<String>, Boolean> pageOfFollowersUsernames = followDAO.getFollowersUsernames(
                    statusAuthor,
                    pageLimit,
                    lastFollowerUsername);
            List<String> followers = pageOfFollowersUsernames.getFirst();
            hasMorePages = pageOfFollowersUsernames.getSecond();
            if (hasMorePages) {
                lastFollowerUsername = followers.get(pageLimit-1);
            }

            if (followers.size() > 0) {
                Map<String, Object> map = new HashMap<>();
                map.put("followers", followers);
                map.put("status", newStatus);

                String messageBody = gson.toJson(map);

                String queueUrl = "https://sqs.us-west-2.amazonaws.com/186990554695/UpdateFeedQueue";

                SendMessageRequest send_msg_request = new SendMessageRequest()
                        .withQueueUrl(queueUrl)
                        .withMessageBody(messageBody);

                sqs.sendMessage(send_msg_request);
                requestsSent += 1;
            }
        }
    }

    public void updateFeeds(String msgBody) {
        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> map = gson.fromJson(msgBody, mapType);

        @SuppressWarnings("unchecked")
        List<String> followers = (List<String>)map.get("followers");
        Status status = gson.fromJson(gson.toJson(map.get("status")), Status.class);

        if (followers.size() > 0) {
            feedDAO.batchAddStatus(followers, status);
        }
    }
}
