package edu.byu.cs.tweeter.client;

import edu.byu.cs.tweeter.client.model.service.net.ServerFacade;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;

public class PostStatusTest {
    //Login a user through the server facade to avoid threading
    //Post a status from the user to the server by calling the "post status" operation on the relevant presenter
    //Mock the view and verify that the "Successfully Posted!" message was displayed to the user.
    //Retrieve the user's story from the server to verify that the new status was correctly appended to the user's story, and that all status details are correct
    //It would be okay to remove the feed queue stuff for this

    private ServerFacade mockFacade = new ServerFacade();
    User currentUser;
    Status testStatus;
}
