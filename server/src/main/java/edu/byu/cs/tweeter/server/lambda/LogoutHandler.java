package edu.byu.cs.tweeter.server.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import edu.byu.cs.tweeter.model.net.request.LogoutRequest;
import edu.byu.cs.tweeter.model.net.response.LogoutResponse;
import edu.byu.cs.tweeter.server.service.UserService;

public class LogoutHandler extends BaseHandler implements RequestHandler<LogoutRequest, LogoutResponse> {
    @Override
    public LogoutResponse handleRequest(LogoutRequest request, Context context) {
        UserService userService = new UserService(getFactory());
        return userService.logout(request);
    }
}
