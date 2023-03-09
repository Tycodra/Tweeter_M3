package edu.byu.cs.tweeter.client.presenter;

import edu.byu.cs.tweeter.model.domain.User;

public class FollowingPresenter extends PagedPresenter<User>{
    public FollowingPresenter(PagedView view) {
        super(view);
    }

    @Override
    public String getPresenterText() {
        return "Get Following";
    }

    @Override
    public void getItems(User user, int PAGE_SIZE, User lastItem) {
        getFollowService().loadMoreFollowees(user, PAGE_SIZE, lastItem, this);
    }
}