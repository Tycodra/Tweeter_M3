package edu.byu.cs.tweeter.client.presenter;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;

public class StoryPresenter extends PagedPresenter<Status>{
    public StoryPresenter(PagedView view) {
        super(view);
    }
    @Override
    public String getPresenterText() {
        return "GetStory";
    }
    @Override
    public void getItems(User user, int PAGE_SIZE, Status lastItem) {
        getStatusService().loadMoreStory(user, PAGE_SIZE, lastItem, this);
    }
}

