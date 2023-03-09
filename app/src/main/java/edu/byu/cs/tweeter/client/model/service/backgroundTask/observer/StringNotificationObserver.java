package edu.byu.cs.tweeter.client.model.service.backgroundTask.observer;

public interface StringNotificationObserver extends ServiceObserver{
    void handleSuccess(String message);
}
