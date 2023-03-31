package edu.byu.cs.tweeter.server.service.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.server.service.Dynamos.DataPage;
import edu.byu.cs.tweeter.server.service.Dynamos.FollowsBean;
import edu.byu.cs.tweeter.util.FakeData;
import edu.byu.cs.tweeter.util.Pair;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

/**
 * A DAO for accessing 'following' data from the database.
 */
public class FollowDAO implements FollowDAOInterface {
    private static final String tableName = "Tweeter_Follows";
    private static final String indexName = "followee-follower-index";
    private final String followerUsername = "followerUsername";
    private final String followeeUsername = "followeeUsername";
    private DynamoDbTable<FollowsBean> table;
    private DynamoDbIndex<FollowsBean> index;

    public FollowDAO(DynamoDbEnhancedClient enhancedClient) {
        table = enhancedClient.table(tableName, TableSchema.fromBean(FollowsBean.class));
        index = enhancedClient.table(tableName, TableSchema.fromBean(FollowsBean.class)).index(indexName);
    }

    /**
     * Gets the users from the database that the user specified in the request is following. Uses
     * information in the request object to limit the number of followees returned and to return the
     * next set of followees after any that were returned in a previous request.
     */
    @Override
    public Pair<List<User>, Boolean> getFollowees(String followerUsername, int pageLimit, String lastFolloweeUsername) {
        Key key = Key.builder()
                .partitionValue(followerUsername)
                .build();

        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(key))
                .scanIndexForward(true)
                .limit(pageLimit);

        if ((lastFolloweeUsername != null) && (lastFolloweeUsername.length() > 0)) {
            Map<String, AttributeValue> startKey = new HashMap<>();
            startKey.put(this.followerUsername,
                    AttributeValue.builder().s(followerUsername).build());
            startKey.put(this.followeeUsername,
                    AttributeValue.builder().s(lastFolloweeUsername).build());

            requestBuilder.exclusiveStartKey(startKey);
        }

        QueryEnhancedRequest request = requestBuilder.build();

        DataPage<FollowsBean> result = new DataPage<>();
        PageIterable<FollowsBean> pages = table.query(request);
        pages.stream()
                .limit(1)
                .forEach((Page<FollowsBean> page) -> {
                    result.setHasMorePages(((Page<?>) page).lastEvaluatedKey() != null);
                    page.items().forEach(follow -> result.getValues().add(follow));
                });

        List<User> followeeUsers = new ArrayList<>();
        for (FollowsBean follow : result.getValues()) {
            followeeUsers.add(new User(
                    follow.getFolloweeFirstName(),
                    follow.getFolloweeLastName(),
                    follow.getFolloweeUsername(),
                    follow.getFollowerImageUrl()));
        }
        boolean hasMorePage = result.hasMorePages();

        return new Pair<>(followeeUsers, hasMorePage);

//        List<User> allFollowees = getDummyFollowees();
//        List<User> responseFollowees = new ArrayList<>(request.getLimit());
//
//        boolean hasMorePages = false;
//
//        if(request.getLimit() > 0) {
//            if (allFollowees != null) {
//                int followeesIndex = getFolloweesStartingIndex(request.getLastFolloweeAlias(), allFollowees);
//
//                for(int limitCounter = 0; followeesIndex < allFollowees.size() && limitCounter < request.getLimit(); followeesIndex++, limitCounter++) {
//                    responseFollowees.add(allFollowees.get(followeesIndex));
//                }
//
//                hasMorePages = followeesIndex < allFollowees.size();
//            }
//        }
//
//        return new FolloweesResponse(responseFollowees, hasMorePages);
    }

    @Override
    public Pair<List<User>, Boolean> getFollowers(String followeeUsername, int pageLimit, String lastFollowerUsername) {
        Key key = Key.builder()
                .partitionValue(followeeUsername)
                .build();
        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(key))
                .scanIndexForward(true)
                .limit(pageLimit);

        if ((lastFollowerUsername != null) && (lastFollowerUsername.length() > 0)) {
            Map<String, AttributeValue> startKey = new HashMap<>();
            startKey.put(this.followeeUsername, AttributeValue.builder().s(followeeUsername).build());
            startKey.put(this.followerUsername, AttributeValue.builder().s(lastFollowerUsername).build());

            requestBuilder.exclusiveStartKey(startKey);
        }

        QueryEnhancedRequest request = requestBuilder.build();

        DataPage<FollowsBean> result = new DataPage<>();

        SdkIterable<Page<FollowsBean>> sdkIterable = index.query(request);
        PageIterable<FollowsBean> pages = PageIterable.create(sdkIterable);
        pages.stream()
                .limit(1)
                .forEach((Page<FollowsBean> page) -> {
                    result.setHasMorePages(page.lastEvaluatedKey() != null);
                    page.items().forEach(follow -> result.getValues().add(follow));
                });

        List<User> followeeUsers = new ArrayList<>();
        for (FollowsBean follow : result.getValues()) {
            followeeUsers.add(new User(
                    follow.getFolloweeFirstName(),
                    follow.getFolloweeLastName(),
                    follow.getFolloweeUsername(),
                    follow.getFollowerImageUrl()));
        }
        boolean hasMorePage = result.hasMorePages();

        return new Pair<>(followeeUsers, hasMorePage);

//        List<User> allFollowers = getDummyFollowers();
//        List<User> responseFollowers = new ArrayList<>(request.getLimit());
//
//        boolean hasMorePages = false;
//
//        if (request.getLimit() > 0) {
//            if (allFollowers != null) {
//                int followersIndex = getFollowersStartingIndex(request.getLastFollowerAlias(), allFollowers);
//
//                for (int limitCounter = 0; followersIndex < allFollowers.size() && limitCounter < request.getLimit(); followersIndex ++, limitCounter++) {
//                    responseFollowers.add(allFollowers.get(followersIndex));
//                }
//
//                hasMorePages = followersIndex < allFollowers.size();
//            }
//        }
//
//        return new FollowersResponse(responseFollowers, hasMorePages);
    }

    @Override
    public boolean isFollower(String followerUsername, String followeeUsername) {
        Key key = Key.builder()
                .partitionValue(followerUsername)
                .sortValue(followeeUsername)
                .build();
        try {
            FollowsBean followsBean = table.getItem(key);
            return followsBean != null;
        } catch (DynamoDbException ex) {
            System.out.println("Failed to get isFollower because of " + ex.getMessage());
        }
        return false;
    }

    @Override
    public void addFollow(User follower, User followee) {
        FollowsBean followsBean = new FollowsBean();

        followsBean.setFollowerFirstName(follower.getFirstName());
        followsBean.setFollowerLastName(follower.getLastName());
        followsBean.setFollowerUsername(follower.getAlias());
        followsBean.setFollowerImageUrl(follower.getImageUrl());

        followsBean.setFolloweeFirstName(followee.getFirstName());
        followsBean.setFolloweeLastName(followee.getLastName());
        followsBean.setFolloweeUsername(followee.getAlias());
        followsBean.setFolloweeImageUrl(followee.getImageUrl());

        try {
            table.putItem(followsBean);
        } catch (DynamoDbException ex) {
            System.out.println("Failed to add because " + ex.getMessage());
        }
    }

    @Override
    public void removeFollow(String followerUsername, String followeeUsername) {
        try {
            Key key = Key.builder()
                    .partitionValue(followerUsername)
                    .sortValue(followeeUsername)
                    .build();
            table.deleteItem(key);
        } catch (DynamoDbException ex) {
            System.out.println(
                    "Failed to remove item- P:" +
                    followerUsername +
                    " S:" +
                    followeeUsername +
                    " because of " +
                    ex.getLocalizedMessage());
        }
    }
    /**
     * Determines the index for the first followee in the specified 'allFollowees' list that should
     * be returned in the current request. This will be the index of the next followee after the
     * specified 'lastFollowee'.
     *
     * @param lastFolloweeAlias the alias of the last followee that was returned in the previous
     *                          request or null if there was no previous request.
     * @param allFollowees the generated list of followees from which we are returning paged results.
     * @return the index of the first followee to be returned.
     */
    private int getFolloweesStartingIndex(String lastFolloweeAlias, List<User> allFollowees) {

        int followeesIndex = 0;

        if(lastFolloweeAlias != null) {
            // This is a paged request for something after the first page. Find the first item
            // we should return
            for (int i = 0; i < allFollowees.size(); i++) {
                if(lastFolloweeAlias.equals(allFollowees.get(i).getAlias())) {
                    // We found the index of the last item returned last time. Increment to get
                    // to the first one we should return
                    followeesIndex = i + 1;
                    break;
                }
            }
        }

        return followeesIndex;
    }

    private int getFollowersStartingIndex(String lastFollowerAlias, List<User> allFollowers) {
        int followersIndex = 0;

        if (lastFollowerAlias != null) {
            // This is a paged request for something after the first page. Find the first item
            // we should return
            for (int i = 0; i < allFollowers.size(); i++) {
                if (lastFollowerAlias.equals(allFollowers.get(i).getAlias())) {
                    // We found the index of the last item return last time. Increment to get
                    // to the first one we should return
                    followersIndex = i + 1;
                    break;
                }
            }
        }
        return followersIndex;
    }

    /**
     * Returns the list of dummy followee data. This is written as a separate method to allow
     * mocking of the followees.
     *
     * @return the followees.
     */
    List<User> getDummyFollowees() {
        return getFakeData().getFakeUsers();
    }

    /**
     * Returns the list of dummy followers data. This is written as a separate method to allow
     * mocking of the followers.
     *
     * @return the followers.
     */
    List<User> getDummyFollowers() {
        return getFakeData().getFakeUsers();
    }

    /**
     * Returns the {@link FakeData} object used to generate dummy followees.
     * This is written as a separate method to allow mocking of the {@link FakeData}.
     *
     * @return a {@link FakeData} instance.
     */
    FakeData getFakeData() {
        return FakeData.getInstance();
    }
}
