package edu.byu.cs.tweeter.client.presenter;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;

public class FeedPresenter extends PagedPresenter<Status>{
    @Override
    public String getPresenterText() {
        return "Get Feed";
    }
    public FeedPresenter(PagedView view) {
        super(view);
    }
    @Override
    public void getItems(User user, int PAGE_SIZE, Status lastItem) {
        getStatusService().loadMoreFeed(user, PAGE_SIZE, lastItem, this);
    }
}

