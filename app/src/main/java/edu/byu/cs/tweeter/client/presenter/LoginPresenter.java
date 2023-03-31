package edu.byu.cs.tweeter.client.presenter;

import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.AuthenticateUserObserver;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class LoginPresenter extends AuthenticatePresenter implements AuthenticateUserObserver {
    public LoginPresenter(View view) {
        super(view);
    }
    public View getLoginView() {
        return (View)baseView;
    }

    public void initiateLogin(String username, String password) {
        String validationMessage = validateLogin(username, password);

        if (validationMessage == null) {
            getLoginView().displayMessage("Logging in ....");
            getUserService().login(username, password, this);
        }
        else {
            getLoginView().displayErrorMessage(validationMessage);
        }
    }
    @Override
    public void handleSuccess(User user, AuthToken authToken) {
        System.out.println(user.getFirstName());
        getLoginView().authenticationSuccessful(user, authToken);
    }
    @Override
    public String getPresenterText() {
        return "Login";
    }

    public String validateLogin(String username, String password) {
        if (username.length() > 0 && username.charAt(0) != '@') {
            return "Alias must begin with @.";
        }
        if (username.length() < 2) {
            return "Alias must contain 1 or more characters after the @.";
        }
        if (password.length() == 0) {
            return "Password cannot be empty.";
        }
        return null;
    }
}
