package edu.byu.cs.tweeter.client.model.service;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.BackgroundTaskUtils;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetUserTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.LoginTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.LogoutTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.RegisterTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.handler.AuthenticateUserTaskHandler;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.handler.GetUserHandler;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.handler.SimpleNotificationHandler;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.AuthenticateUserObserver;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.GetUserObserver;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.SimpleNotificationObserver;

public class UserService extends BackgroundTaskUtils {
    public void getUser(String userAlias, GetUserObserver getUserObserver) {
        GetUserTask getUserTask = new GetUserTask(Cache.getInstance().getCurrUserAuthToken(), userAlias, new GetUserHandler(getUserObserver));
        runTask(getUserTask);
    }
    public void login(String username, String password, AuthenticateUserObserver observer) {
        LoginTask loginTask = new LoginTask(username, password, new AuthenticateUserTaskHandler(observer));
        runTask(loginTask);
    }
    public void register(String firstName, String lastName, String alias, String password, String imageToUpload, AuthenticateUserObserver observer) {
        RegisterTask registerTask = new RegisterTask(firstName, lastName,
                                    alias, password, imageToUpload,
                                    new AuthenticateUserTaskHandler(observer));
        runTask(registerTask);
    }
    public void logout(SimpleNotificationObserver observer) {
        LogoutTask logoutTask = new LogoutTask(Cache.getInstance().getCurrUserAuthToken(), new SimpleNotificationHandler(observer));
        runTask(logoutTask);
    }
}