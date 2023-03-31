package edu.byu.cs.tweeter.server.service.Dynamos;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class FollowsBean {
    private String followerUsername;
    private String followerFirstName;
    private String followerLastName;
    private String followerImageUrl;
    private String followeeUsername;
    private String followeeFirstName;
    private String followeeLastName;
    private String followeeImageUrl;

    @DynamoDbPartitionKey
    @DynamoDbSecondarySortKey(indexNames = "followee-follower-index")
    public String getFollowerUsername() {
        return followerUsername;
    }

    public void setFollowerUsername(String followerUsername) {
        this.followerUsername = followerUsername;
    }

    public String getFollowerFirstName() {
        return followerFirstName;
    }

    public void setFollowerFirstName(String followerFirstName) {
        this.followerFirstName = followerFirstName;
    }

    public String getFollowerLastName() {
        return followerLastName;
    }

    public void setFollowerLastName(String followerLastName) {
        this.followerLastName = followerLastName;
    }

    public String getFollowerImageUrl() {
        return followerImageUrl;
    }

    public void setFollowerImageUrl(String followerImageUrl) {
        this.followerImageUrl = followerImageUrl;
    }

    @DynamoDbSortKey
    @DynamoDbSecondaryPartitionKey(indexNames = "followee-follower-index")
    public String getFolloweeUsername() {
        return followeeUsername;
    }

    public void setFolloweeUsername(String followeeUsername) {
        this.followeeUsername = followeeUsername;
    }

    public String getFolloweeFirstName() {
        return followeeFirstName;
    }

    public void setFolloweeFirstName(String followeeFirstName) {
        this.followeeFirstName = followeeFirstName;
    }

    public String getFolloweeLastName() {
        return followeeLastName;
    }

    public void setFolloweeLastName(String followeeLastName) {
        this.followeeLastName = followeeLastName;
    }

    public String getFolloweeImageUrl() {
        return followeeImageUrl;
    }

    public void setFolloweeImageUrl(String followeeImageUrl) {
        this.followeeImageUrl = followeeImageUrl;
    }
}
