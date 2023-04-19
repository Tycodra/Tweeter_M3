package edu.byu.cs.tweeter.server.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import edu.byu.cs.tweeter.server.service.StatusService;

public class PostUpdateFeedMessagesHandler extends BaseHandler implements RequestHandler<SQSEvent, Void> {

    @Override
    public Void handleRequest(SQSEvent input, Context context) {
        StatusService service = new StatusService(getFactory());
        String msgBody = input.getRecords().get(0).getBody();
        service.postUpdateFeedMessage(msgBody);
        return null;
    }
}
