package edu.byu.cs.tweeter.client.presenter;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public abstract class AuthenticatePresenter extends BasePresenter{
    public interface View extends BaseView {
        void authenticationSuccessful(User user, AuthToken authToken);
        void displayErrorMessage(String message);
    }
    public AuthenticatePresenter(BaseView view) {
        super(view);
    }
}
