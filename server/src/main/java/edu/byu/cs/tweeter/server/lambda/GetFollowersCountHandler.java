package edu.byu.cs.tweeter.server.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import edu.byu.cs.tweeter.model.net.request.FollowersCountRequest;
import edu.byu.cs.tweeter.model.net.response.FollowersCountResponse;
import edu.byu.cs.tweeter.server.service.FollowService;

public class GetFollowersCountHandler extends BaseHandler implements RequestHandler<FollowersCountRequest, FollowersCountResponse> {

    @Override
    public FollowersCountResponse handleRequest(FollowersCountRequest request, Context context) {
        FollowService service = new FollowService(getFactory());
        return service.getFollowersCount(request);
    }
}
