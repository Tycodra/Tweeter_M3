package edu.byu.cs.tweeter.client.presenter;

import edu.byu.cs.tweeter.client.model.service.FollowService;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.model.domain.User;

public class FollowersPresenter extends PagedPresenter<User> {

    @Override
    public String getPresenterText() {
        return "Get Followers";
    }

    public FollowersPresenter(PagedView view) {
        super(view);
    }

    @Override
    public void getItems(User user, int PAGE_SIZE, User lastItem) {
        getFollowService().loadMoreFollowers(user, PAGE_SIZE, lastItem, this);
    }
}