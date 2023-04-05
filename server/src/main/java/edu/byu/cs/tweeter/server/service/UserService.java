package edu.byu.cs.tweeter.server.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

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

        String hashedPassword;
        try {
            hashedPassword = hashPassword(request.getPassword(), request.getUsername());
        } catch (NoSuchAlgorithmException ex) {
            return new LoginResponse("[Server Error] unable to verify password");
        }

        if (!userDAO.verifyLogin(request.getUsername(), hashedPassword)) {
            return new LoginResponse("Invalid username or password");
        }

        User user = userDAO.getUser(request.getUsername());

        try {
            String token = generateAuthToken(request.getUsername(), request.getPassword());
            long timestamp = System.currentTimeMillis();

            AuthToken authToken = authDAO.addAuthToken(token, timestamp);

            return new LoginResponse(user, authToken);
        } catch (NoSuchAlgorithmException ex) {
            return new LoginResponse("[Server Error] unable to establish session token");
        }
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

        if (!userDAO.verifyUsernameAvailability(request.getUsername())) {
            return new RegisterResponse("Username already taken");
        }

        String hashedPassword;
        try {
            hashedPassword = hashPassword(request.getPassword(), request.getUsername());
        } catch (NoSuchAlgorithmException ex) {
            return new RegisterResponse("[Server Error] unable to verify password");
        }

        AmazonS3 s3 = AmazonS3ClientBuilder
                .standard()
                .withRegion("us-west-2")
                .build();

        byte[] byteArray = Base64.getDecoder().decode(request.getImage());

        ObjectMetadata data = new ObjectMetadata();

        data.setContentLength(byteArray.length);

        data.setContentType("image/jpeg");

        PutObjectRequest putRequest = new PutObjectRequest("tycolax-cs340-tweeter", request.getUsername(), new ByteArrayInputStream(byteArray), data).withCannedAcl(CannedAccessControlList.PublicRead);

        s3.putObject(putRequest);

        String link = "https://tycolax-cs340-tweeter.s3.us-west-2.amazonaws.com/" + request.getUsername();

        User user = userDAO.registerUser(request.getUsername(),
                hashedPassword,
                request.getFirstName(),
                request.getLastName(),
                link,
                0,
                0);

        try {
            String token = generateAuthToken(request.getUsername(), request.getPassword());
            long timestamp = System.currentTimeMillis();

            AuthToken authToken = authDAO.addAuthToken(token, timestamp);

            return new RegisterResponse(user, authToken);
        } catch (NoSuchAlgorithmException ex) {
            return new RegisterResponse("[Server Error] unable to establish session token");
        }
    }

    public LogoutResponse logout(LogoutRequest request) {
        System.out.println("In logout");
        if (request.getAuthToken() == null) {
            System.out.println("Missing an authToken in logout");
            throw new RuntimeException("[Bad Request] Missing an authToken");
        }
        authDAO.removeAuthToken(request.getAuthToken().getToken());
        return new LogoutResponse();
    }

    public GetUserResponse getUser(GetUserRequest request) {
        if(request.getUsername() == null){
            throw new RuntimeException("[Bad Request] Missing a username");
        }
        User user = userDAO.getUser(request.getUsername());
        return new GetUserResponse(user);
    }

    private String generateAuthToken(String username, String password) throws NoSuchAlgorithmException{
        String saltedToken = username + password;
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(saltedToken.getBytes(StandardCharsets.UTF_8));
        return convertToHex(md.digest());
    }
    private String hashPassword(String password, String username) throws NoSuchAlgorithmException {
        String saltedPassword = password + username;
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(saltedPassword.getBytes(StandardCharsets.UTF_8));
        return convertToHex(md.digest());
    }
    private String convertToHex(byte[] messageDigest) {
        BigInteger bigint = new BigInteger(1, messageDigest);
        String hexText = bigint.toString(16);
        while(hexText.length() < 32) {
            hexText = "0".concat(hexText);
        }
        return hexText;
    }
}
