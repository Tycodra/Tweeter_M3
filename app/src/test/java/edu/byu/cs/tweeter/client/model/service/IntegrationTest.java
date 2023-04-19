package edu.byu.cs.tweeter.client.model.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

import edu.byu.cs.tweeter.client.model.service.net.ServerFacade;
import edu.byu.cs.tweeter.client.model.service.net.TweeterRequestException;
import edu.byu.cs.tweeter.client.presenter.MainPresenter;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.net.TweeterRemoteException;
import edu.byu.cs.tweeter.model.net.request.FollowersCountRequest;
import edu.byu.cs.tweeter.model.net.request.FollowersRequest;
import edu.byu.cs.tweeter.model.net.request.RegisterRequest;
import edu.byu.cs.tweeter.model.net.response.FollowersResponse;
import edu.byu.cs.tweeter.model.net.response.RegisterResponse;

public class IntegrationTest {

    private ServerFacade mockFacade;
    private MainPresenter.MainView mockView;
    private MainPresenter mainPresenterSpy;


    @BeforeEach
    public void setup() {
        mockFacade = new ServerFacade();
        mockView = Mockito.mock(MainPresenter.MainView.class);
        mainPresenterSpy = Mockito.spy(new MainPresenter(mockView));
    }

    /**
     * Verify that for a successful request we get an accurate response.
     * Since we know we always get Allen Anderson, we can compare our response
     * with the knowledge of what we should be receiving.
     * @throws IOException
     * @throws TweeterRemoteException
     */
    @Test
    public void testRegister_validRequest_correctResponse() throws IOException, TweeterRemoteException {
        RegisterRequest request = new RegisterRequest(
                "username",
                "password",
                "firstName",
                "lastName",
                "image");
        RegisterResponse response = mockFacade.register(request, "/register");

        Assertions.assertTrue(response.isSuccess());
        Assertions.assertNull(response.getMessage());
        Assertions.assertNotNull(response.getUser());
        Assertions.assertEquals("Allen", response.getUser().getFirstName());
        Assertions.assertNotNull(response.getAuthToken());
    }

    /**
     * Verify that for invalid request we will correctly throw an exception.
     * These happen if any of the parameters are left null.
     * @throws IOException
     * @throws TweeterRemoteException
     */
    @Test
    public void testRegister_invalidRequest_correctResponse() throws IOException, TweeterRemoteException {
        RegisterRequest request = new RegisterRequest(
                null,
                "password",
                null,
                "lastName",
                null);
        Assertions.assertThrows(
                TweeterRequestException.class,
                () -> { RegisterResponse response = mockFacade.register(request, "/register"); });
    }

//    /**
//     * Verify that for a successful request, the method returns the
//     * expected response object. Looking at fakeData we can see who
//     * should be the 10th user in the list.
//     * @throws IOException
//     * @throws TweeterRemoteException
//     */
//    @Test
//    public void testGetFollowers_validRequest_correctResponse() throws IOException, TweeterRemoteException {
//        FollowersRequest request = new FollowersRequest(new AuthToken(), "bob", 10, null);
//        FollowersResponse response = mockFacade.getFollowers(request, "/getfollowers");
//
//        Assertions.assertTrue(response.isSuccess());
//        Assertions.assertNull(response.getMessage());
//        Assertions.assertEquals(10, response.getFollowers().size());
//        Assertions.assertEquals("@elizabeth", response.getFollowers().get(9).getAlias());
//    }

    /**
     * Currently, an alias that isn't in the database will still pass, but
     * we catch limits when they are less than 0. So the alias passes, but
     * the limit will throw an exception.
     * @throws IOException
     * @throws TweeterRemoteException
     */
    @Test
    public void testGetFollowers_invalidRequest_correctResponse() throws IOException, TweeterRemoteException {
        FollowersRequest request = new FollowersRequest(new AuthToken(), "@randomAlias", -10, null);
        Assertions.assertThrows(
                TweeterRequestException.class,
                () -> {
                    FollowersResponse response = mockFacade.getFollowers(request, "/getfollowers");
                }
        );
    }

//    /**
//     * Verify that for successful request, the server returns the
//     * correct count of followers. At this point there is no difference
//     * between users, but we do know that it should be returning 21
//     * for any targetUser that is provided.
//     * @throws IOException
//     * @throws TweeterRemoteException
//     */
//    @Test
//    public void testGetFollowersCount_validRequest_correctResponse() throws IOException, TweeterRemoteException {
//        User testUser = new User("Allen", "Anderson", "image");
//        FollowersCountRequest request = new FollowersCountRequest(testUser, new AuthToken());
//        FollowersCountResponse response = mockFacade.getFollowersCount(request, "/getfollowerscount");
//
//        Assertions.assertTrue(response.isSuccess());
//        Assertions.assertNull(response.getMessage());
//        Assertions.assertEquals(21, response.getCount());
//    }

    /**
     * Verify that this fails if the target user is left null in the request object
     * and the server will throw a TweeterRequestException because of this.
     */
    @Test
    public void testGetFollowersCount_invalidRequest_correctResponse() {
        FollowersCountRequest request = new FollowersCountRequest(null, new AuthToken());

        Assertions.assertThrows(
                TweeterRequestException.class,
                () -> {
                    mockFacade.getFollowersCount(request, "/getfollowerscount");
                }
        );
    }
}
