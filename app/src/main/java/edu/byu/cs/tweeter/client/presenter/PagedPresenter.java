package edu.byu.cs.tweeter.client.presenter;

import java.util.List;

import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.GetUserObserver;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.PagedTaskObserver;
import edu.byu.cs.tweeter.model.domain.User;

public abstract class PagedPresenter<T> extends BasePresenter implements GetUserObserver, PagedTaskObserver<T> {
    public boolean isLoading() {
        return isLoading;
    }

    public boolean hasMorePages() {
        return hasMorePages;
    }

    public interface PagedView<T> extends BaseView {
        void setLoadingFooter(boolean loadingFooterStatus);
        void displayUser(User user);
        void displayMessage(String message);
        void addMoreItems(List<T> itemsList);
    }
    protected static final int PAGE_SIZE = 10;
    protected boolean isLoading = false;
    protected boolean hasMorePages;
    protected T lastItem;

    public PagedPresenter(BaseView view) {
        super(view);
    }
    public void loadMoreItems(User user) {
        if (!isLoading) {
            isLoading = true;
            getPagedView().setLoadingFooter(isLoading);
            getItems(user, PAGE_SIZE, lastItem);
        }
    }

    public PagedView<T> getPagedView() {
        return (PagedView<T>)baseView;
    }

    @Override
    public void displayUser(User user) {
        getPagedView().displayUser(user);
    }
    public void getUser(String userAlias) {
        getUserService().getUser(userAlias, this);
    }
    public abstract void getItems(User user, int PAGE_SIZE, T lastItem);

    @Override
    public void handleSuccess(List<T> itemsList, boolean hasMorePages) {
            lastItem = (itemsList.size() > 0) ? (T) itemsList.get(itemsList.size() - 1) : null;
            this.hasMorePages = hasMorePages;
            isLoading = false;
            getPagedView().setLoadingFooter(isLoading);
            getPagedView().addMoreItems(itemsList);
    }

    @Override
    public void handleFailure(String message) {
        isLoading = false;
        getPagedView().setLoadingFooter(isLoading);
        super.handleFailure(message);
    }

    @Override
    public void handleException(String message) {
        isLoading = false;
        getPagedView().setLoadingFooter(isLoading);
        super.handleException(message);
    }
}
