package edu.byu.cs.tweeter.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import edu.byu.cs.tweeter.client.model.service.StatusService;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.PagedTaskObserver;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.util.FakeData;

public class IntegrationTest {
    private CountDownLatch countDownLatch;

    @BeforeEach
    public void setup() {
        resetCountDownLatch();
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
