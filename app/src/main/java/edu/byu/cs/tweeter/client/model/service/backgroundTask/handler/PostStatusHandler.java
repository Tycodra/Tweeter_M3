package edu.byu.cs.tweeter.client.model.service.backgroundTask.handler;

import android.os.Bundle;

import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.StringNotificationObserver;

public class PostStatusHandler extends BackgroundTaskHandler<StringNotificationObserver> {
    public PostStatusHandler(StringNotificationObserver observer) {
        super(observer);
    }
    @Override
    protected void handleSuccess(Bundle data, StringNotificationObserver observer) {
        observer.handleSuccess("Successfully Posted!");
    }
}