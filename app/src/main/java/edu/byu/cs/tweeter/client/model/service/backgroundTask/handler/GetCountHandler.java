package edu.byu.cs.tweeter.client.model.service.backgroundTask.handler;

import android.os.Bundle;

import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetFollowingCountTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.CountObserver;

public class GetCountHandler extends BackgroundTaskHandler<CountObserver> {
    public GetCountHandler(CountObserver observer) {
        super(observer);
    }

    @Override
    protected void handleSuccess(Bundle data, CountObserver observer) {
        int count = data.getInt(GetFollowingCountTask.COUNT_KEY);
        observer.handleSuccess(count);
    }
}
