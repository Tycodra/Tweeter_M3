package edu.byu.cs.tweeter.server.service;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.GetUserRequest;
import edu.byu.cs.tweeter.model.net.request.LoginRequest;
import edu.byu.cs.tweeter.model.net.request.LogoutRequest;
import edu.byu.cs.tweeter.model.net.request.RegisterRequest;
import edu.byu.cs.tweeter.model.net.response.GetUserResponse;
import edu.byu.cs.tweeter.model.net.response.LoginResponse;
import edu.byu.cs.tweeter.model.net.response.LogoutResponse;
import edu.byu.cs.tweeter.model.net.response.RegisterResponse;
import edu.byu.cs.tweeter.server.service.dao.AuthenticationDAOInterface;
import edu.byu.cs.tweeter.server.service.dao.DAOFactory;
import edu.byu.cs.tweeter.server.service.dao.UserDAOInterface;
import edu.byu.cs.tweeter.util.FakeData;

public class UserService {
    private AuthenticationDAOInterface authDAO;
    private UserDAOInterface userDAO;

    public UserService(DAOFactory factory) {
        this.authDAO = factory.getAuthDAO();
        this.userDAO = factory.getUserDAO();
    }
    public LoginResponse login(LoginRequest request) {
        if(request.getUsername() == null){
            throw new RuntimeException("[Bad Request] Missing a username");
        } else if(request.getPassword() == null) {
            throw new RuntimeException("[Bad Request] Missing a password");
        }

        // TODO: Generates dummy data. Replace with a real implementation.
        User user = userDAO.getUser(request.getUsername());
        AuthToken authToken = authDAO.getAuthToken();
        return new LoginResponse(user, authToken);
    }

    public RegisterResponse register(RegisterRequest request) {
        if(request.getUsername() == null){
            throw new RuntimeException("[Bad Request] Missing a username");
        } else if(request.getPassword() == null) {
            throw new RuntimeException("[Bad Request] Missing a password");
        } else if(request.getFirstName() == null) {
            throw new RuntimeException("[Bad Request] Missing a first name");
        } else if(request.getLastName() == null) {
            throw new RuntimeException("[Bad Request] Missing a last name");
        } else if(request.getImage() == null) {
            throw new RuntimeException("[Bad Request] Missing an image");
        }

        User user = userDAO.getUser(request.getUsername());
        AuthToken authToken = authDAO.getAuthToken();
        return new RegisterResponse(user, authToken);
    }

    public LogoutResponse logout(LogoutRequest request) {
        if (request.getAuthToken() == null) {
            throw new RuntimeException("[Bad Request] Missing an authToken");
        }
        return new LogoutResponse();
    }

    public GetUserResponse getUser(GetUserRequest request) {
        if(request.getUsername() == null){
            throw new RuntimeException("[Bad Request] Missing a username");
        }
        User user = userDAO.getUser(request.getUsername());
        return new GetUserResponse(user);
    }
}
