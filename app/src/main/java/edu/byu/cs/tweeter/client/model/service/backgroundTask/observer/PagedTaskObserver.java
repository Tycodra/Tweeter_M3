package edu.byu.cs.tweeter.client.model.service.backgroundTask.observer;

import android.os.Bundle;

import java.util.List;

import edu.byu.cs.tweeter.model.domain.Status;

public interface PagedTaskObserver<T> extends ServiceObserver{
    void handleSuccess(List<T> itemsList, boolean hasMorePages);
}
