package edu.byu.cs.tweeter.model.net.response;

public class FolloweesCountResponse extends Response {
    private int count;

    public FolloweesCountResponse(String message) {
        super(false, message);
    }

    public FolloweesCountResponse(int count) {
        super(true, null);
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
