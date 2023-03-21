package edu.byu.cs.tweeter.client.presenter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import edu.byu.cs.tweeter.client.model.service.StatusService;
import edu.byu.cs.tweeter.model.domain.Status;

public class MainPresenterUnitTest {
    private MainPresenter.MainView mockView;
    private StatusService mockStatusService;
    private MainPresenter mainPresenterSpy;

    private Status status;

    @BeforeEach
    public void setup() {
        // Create mocks
        mockView = Mockito.mock(MainPresenter.MainView.class);
        mockStatusService = Mockito.mock(StatusService.class);

        mainPresenterSpy = Mockito.spy(new MainPresenter(mockView));

        status = new Status();

        Mockito.when(mainPresenterSpy.getStatusService()).thenReturn(mockStatusService);
        // OR Mockito.doReturn(mockStatusService).when(mainPresenterSpy.getStatusService());
    }
    @Test
    public void testPostStatus_HandleSuccess() {
        Answer<Void> answer = new Answer<>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                status = invocation.getArgument(0, Status.class);
                MainPresenter.PostStatusObserver observer = invocation.getArgument(1, MainPresenter.PostStatusObserver.class);
                observer.handleSuccess("Successfully Posted!");
                assertEquals("postSuccessful", status.post, "failure message: Successful");
                return null;
            }
        };

        Mockito.doAnswer(answer).when(mockStatusService).postStatus(any(), any());
        status.post = "postSuccessful";
        mainPresenterSpy.postStatus(status);

        Mockito.verify(mockView).displayMessage("Posting Status...");
//        Mockito.verify(mockView).cancelPostToast();
        Mockito.verify(mockView).displayMessage("Successfully Posted!");
    }

    @Test
    public void testPostStatus_HandleFailure() {
        Answer<Void> answer = new Answer<>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                status = invocation.getArgument(0, Status.class);
                MainPresenter.PostStatusObserver observer = invocation.getArgument(1, MainPresenter.PostStatusObserver.class);
                assertEquals("postFailure", status.post, "failure message: Failure");
                observer.handleFailure("error");
                return null;
            }
        };

        Mockito.doAnswer(answer).when(mockStatusService).postStatus(any(), any());
        status.post = "postFailure";
        mainPresenterSpy.postStatus(status);

        Mockito.verify(mockView).displayMessage("Posting Status...");
        Mockito.verify(mockView).displayMessage("Failed to Post Status: error");
    }

    @Test
    public void testPostStatus_HandleException() {
        Answer<Void> answer = new Answer<>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                status = invocation.getArgument(0, Status.class);
                MainPresenter.PostStatusObserver observer = invocation.getArgument(1, MainPresenter.PostStatusObserver.class);
                assertEquals("postException", status.post, "failure message: Exception");
                observer.handleException("exception");
                return null;
            }
        };

        Mockito.doAnswer(answer).when(mockStatusService).postStatus(any(), any());
        status.post = "postException";
        mainPresenterSpy.postStatus(status);

        Mockito.verify(mockView).displayMessage("Posting Status...");
        Mockito.verify(mockView).displayMessage("Failed to Post Status because of exception: exception");
    }
}
