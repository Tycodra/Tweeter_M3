package edu.byu.cs.tweeter.server.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import edu.byu.cs.tweeter.model.net.request.FolloweesCountRequest;
import edu.byu.cs.tweeter.model.net.response.FolloweesCountResponse;
import edu.byu.cs.tweeter.server.service.FollowService;

public class GetFollowingCountHandler extends BaseHandler implements RequestHandler<FolloweesCountRequest, FolloweesCountResponse> {

    @Override
    public FolloweesCountResponse handleRequest(FolloweesCountRequest request, Context context) {
        FollowService service = new FollowService(getFactory());
        return service.getFollowingCount(request);
    }
}
