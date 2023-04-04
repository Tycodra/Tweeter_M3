package edu.byu.cs.tweeter.model.net.response;

public class FollowersCountResponse extends Response {
    private int count;

    public FollowersCountResponse(String message) {
        super(false, message);
    }
    public FollowersCountResponse(int count) {
        super(true, null);
        this.count = count;
    }
    public int getCount() {
        return count;
    }
}
