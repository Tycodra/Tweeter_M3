package edu.byu.cs.tweeter.client.presenter;

import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.CountObserver;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.IsFollowerObserver;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.StringNotificationObserver;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.SimpleNotificationObserver;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;

public class MainPresenter extends BasePresenter{
    public interface MainView extends BasePresenter.BaseView {
        void isFollower(boolean isFollower);
        void displayMessage(String message);
        void unfollow();
        void follow();
        void enableFollowButton(boolean enableButton);
        void logout();
        void cancelPostToast();
        void setFollowerCount(String string);
        void setFollowingCount(String string);
    }

    public MainPresenter(MainView view) {
        super(view);
    }
    public MainView getMainView() {
        return (MainView)baseView;
    }
    public void updateFollows(User selectedUser) {
        getFollowService().updateFollowers(selectedUser, new UpdateFollowersObserver(getMainView()));
        getFollowService().updateFollowing(selectedUser, new UpdateFollowingObserver(getMainView()));
    }

    public void isFollower(User selectedUser) {
        getFollowService().isFollower(selectedUser, new IsFollower(getMainView()));
    }
    public void unfollow(User selectedUser) {
        getFollowService().unfollow(selectedUser, new UnfollowObserver(getMainView()));
    }
    public void follow(User selectedUser) {
        getFollowService().follow(selectedUser, new FollowObserver(getMainView()));
    }
    public void postStatus(Status newStatus) {
        getMainView().displayMessage("Posting Status...");
        getStatusService().postStatus(newStatus, new PostStatusObserver(getMainView()));
    }
    public void logout() {
        getUserService().logout(new LogoutObserver(getMainView()));
    }

    public class UpdateFollowingObserver extends BasePresenter implements CountObserver {
        public UpdateFollowingObserver(MainView view) {
            super(view);
        }
        @Override
        public String getPresenterText() {
            return "get Following";
        }
        @Override
        public void handleSuccess(int count) {
            String followingCount = String.valueOf(count);
            getMainView().setFollowingCount("Following: " + followingCount);
        }
    }

    public class UpdateFollowersObserver extends BasePresenter implements CountObserver {
        public UpdateFollowersObserver(MainView view) {
            super(view);
        }
        @Override
        public String getPresenterText() {
            return "get Followers";
        }
        @Override
        public void handleSuccess(int count) {
            String followerCount = String.valueOf(count);
            getMainView().setFollowerCount("Followers: " + followerCount);
        }
    }

    public class PostStatusObserver extends BasePresenter implements StringNotificationObserver {
        public PostStatusObserver(MainView view) {
            super(view);
        }
        @Override
        public void handleSuccess(String message) {
            getMainView().displayMessage(message);
        }
        @Override
        public String getPresenterText() {
            return "Post Status";
        }

    }

    public class LogoutObserver extends BasePresenter implements SimpleNotificationObserver {
        public LogoutObserver(MainView view) {
            super(view);
        }
        @Override
        public String getPresenterText() {
            return "Logout";
        }
        @Override
        public void handleSuccess() {
            getMainView().logout();
        }
    }

    public class UnfollowObserver extends BasePresenter implements SimpleNotificationObserver {
        public UnfollowObserver(MainView view) {
            super(view);
        }
        @Override
        public void handleSuccess() {
            getMainView().unfollow();
            getMainView().enableFollowButton(true);
        }
        @Override
        public String getPresenterText() {
            return "Unfollow";
        }
    }

    public class FollowObserver extends BasePresenter implements SimpleNotificationObserver {
        public FollowObserver(MainView view) {
            super(view);
        }
        @Override
        public void handleSuccess() {
            getMainView().follow();
            getMainView().enableFollowButton(true);
        }
        @Override
        public String getPresenterText() {
            return "Follow";
        }
    }

    public class IsFollower extends BasePresenter implements IsFollowerObserver {
        public IsFollower(MainView view) {
            super(view);
        }
        @Override
        public void setIsFollowerButton(boolean isFollower) {
            getMainView().isFollower(isFollower);
        }
        @Override
        public String getPresenterText() {
            return "IsFollower";
        }
    }

    @Override
    public String getPresenterText() {
        return null;
    }
}
