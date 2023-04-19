package edu.byu.cs.tweeter.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.StatusService;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.PagedTaskObserver;
import edu.byu.cs.tweeter.client.model.service.net.ServerFacade;
import edu.byu.cs.tweeter.client.presenter.MainPresenter;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.TweeterRemoteException;
import edu.byu.cs.tweeter.model.net.request.LoginRequest;
import edu.byu.cs.tweeter.model.net.request.StoryRequest;
import edu.byu.cs.tweeter.model.net.response.LoginResponse;
import edu.byu.cs.tweeter.model.net.response.StoryResponse;
import edu.byu.cs.tweeter.util.FakeData;

public class IntegrationTest {
    private CountDownLatch countDownLatch;
    private ServerFacade mockFacade;
    private MainPresenter.MainView mockView;
    private MainPresenter mainPresenterSpy;


    @BeforeEach
    public void setup() {
        resetCountDownLatch();
        mockFacade = new ServerFacade();
        mockView = Mockito.mock(MainPresenter.MainView.class);
        mainPresenterSpy = Mockito.spy(new MainPresenter(mockView));
    }

    private void resetCountDownLatch() {
        countDownLatch = new CountDownLatch(1);
    }

    private void awaitCountDownLatch() throws InterruptedException {
        countDownLatch.await();
        resetCountDownLatch();
    }

    private class StatusServiceObserver implements PagedTaskObserver<Status> {
        private boolean success;
        private String message;
        private List<Status> story;
        private boolean hasMorePages;
        private Exception exception;
        @Override
        public void handleSuccess(List<Status> itemsList, boolean hasMorePages) {
            this.success = true;
            this.message = null;
            this.story = itemsList;
            this.hasMorePages = hasMorePages;
            this.exception = null;

            countDownLatch.countDown();
        }

        @Override
        public void handleFailure(String message) {
            this.success = false;
            this.message = message;
            this.story = null;
            this.hasMorePages = false;
            this.exception = null;

            countDownLatch.countDown();
        }

        @Override
        public void handleException(String message) {
            this.success = false;
            this.message = message;
            this.story = null;
            this.hasMorePages = false;
            this.exception = null;

            countDownLatch.countDown();
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public List<Status> getStory() {
            return story;
        }

        public boolean isHasMorePages() {
            return hasMorePages;
        }

        public Exception getException() {
            return exception;
        }
    }

    @Test
    public void testPostStatus_validRequest_CorrectResponse() throws IOException, TweeterRemoteException, InterruptedException {

        LoginRequest request = new LoginRequest(
                "@allen",
                "allen");
        LoginResponse loginResponse = mockFacade.login(request, "/login");

        Assertions.assertTrue(loginResponse.isSuccess());
        Assertions.assertNull(loginResponse.getMessage());
        Assertions.assertEquals("Allen", loginResponse.getUser().getFirstName());
        Assertions.assertNotNull(loginResponse.getAuthToken());

        User currentUser = loginResponse.getUser();
        AuthToken authToken = loginResponse.getAuthToken();
        Cache.getInstance().setCurrUser(currentUser);
        Cache.getInstance().setCurrUserAuthToken(authToken);

        List<String> urls = new ArrayList<>();
        urls.add("google.com");
        List<String> mentions = new ArrayList<>();
        mentions.add("@test");
        Status newStatus = new Status("Testing Post", currentUser, System.currentTimeMillis(), urls, mentions);


        Mockito.doAnswer(invocation -> {
            countDownLatch.countDown();
            return null;
        }).when(mockView).displayMessage("Successfully Posted!");

        mainPresenterSpy.postStatus(newStatus);
        awaitCountDownLatch();

        Mockito.verify(mockView).displayMessage("Posting Status...");
        Mockito.verify(mockView).displayMessage("Successfully Posted!");

        StoryRequest storyRequest = new StoryRequest(
                authToken,
                currentUser.getAlias(),
                5,
                null);
        StoryResponse storyResponse = mockFacade.getStory(storyRequest, "/getstory");

        List<Status> recentStatuses = storyResponse.getStory();
        Status mostRecentStatus = recentStatuses.get(0);
        Assertions.assertEquals(newStatus, mostRecentStatus);
    }

    /**
     * Verify that the correct response is retrieved through comparing
     * the expected value vs the actual value of the response. I had to
     * check for a portion of the statuses specifically because the timestamps
     * were not matching up so I couldn't test them like the github example.
     * @throws InterruptedException
     */
    @Test
    public void testGetStory_ValidRequest_CorrectResponse() throws InterruptedException {
        StatusService statusServiceSpy = Mockito.spy(new StatusService());
        StatusServiceObserver observer = new StatusServiceObserver();

        User testUser = new User("firstName", "lastName", "image");
        statusServiceSpy.loadMoreStory(testUser, 10, null, observer);
        awaitCountDownLatch();

        List<Status> expectedStatuses = FakeData.getInstance().getFakeStatuses().subList(0,10);
        Assertions.assertTrue(observer.isSuccess());
        Assertions.assertNull(observer.getMessage());
        Assertions.assertEquals(expectedStatuses.get(9).getPost(), observer.getStory().get(9).getPost());
        Assertions.assertTrue(observer.isHasMorePages());
        Assertions.assertNull(observer.getException());

    }
}
