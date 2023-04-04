package edu.byu.cs.tweeter.server.service.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.server.service.Dynamos.DataPage;
import edu.byu.cs.tweeter.server.service.Dynamos.FeedBean;
import edu.byu.cs.tweeter.util.Pair;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class FeedDAO implements FeedDAOInterface{
    private static final String tableName = "Tweeter_Feed";
    private DynamoDbTable<FeedBean> table;

    public FeedDAO(DynamoDbEnhancedClient enhancedClient) {
        table = enhancedClient.table(tableName, TableSchema.fromBean(FeedBean.class));
    }

    @Override
    public Pair<List<Status>, Boolean> getFeed(String feedOwnerUsername, int pageLimit, Status lastStatus) {
        Key key = Key.builder()
                .partitionValue(feedOwnerUsername)
                .build();
        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(key))
                .scanIndexForward(false)
                .limit(pageLimit);
        if (lastStatus != null) {
            String lastUsername = feedOwnerUsername;
            String lastStatusTimestamp = String.valueOf(lastStatus.getTimestamp());

            Map<String, AttributeValue> startKey = new HashMap<>();
            startKey.put(
                    "username",
                    AttributeValue.builder().s(lastUsername).build());
            startKey.put(
                    "timestamp",
                    AttributeValue.builder().s(lastStatusTimestamp).build());
            requestBuilder.exclusiveStartKey(startKey);
        }

        QueryEnhancedRequest request = requestBuilder.build();

        DataPage<FeedBean> result = new DataPage<>();
        PageIterable<FeedBean> pages = table.query(request);
        pages.stream()
                .limit(1)
                .forEach((Page<FeedBean> page) -> {
                    result.setHasMorePages(page.lastEvaluatedKey() != null);
                    page.items().forEach(status -> result.getValues().add(status));
                });
        List<Status> feedStatuses = new ArrayList<>(pageLimit);

        for (FeedBean status : result.getValues()) {
            feedStatuses.add(new Status(
                    status.getPost(),
                    new User(
                            status.getPosterFirstName(),
                            status.getPosterLastName(),
                            status.getPosterUsername(),
                            status.getPosterImageURL()),
                    status.getTimestamp(),
                    status.getUrls(),
                    status.getMentions()
            ));
        }
        return new Pair<>(feedStatuses, result.hasMorePages());
    }

    @Override
    public void addStatus(String feedOwnerUsername, Status newStatus) {
        FeedBean feedBean = new FeedBean();
        feedBean.setUsername(feedOwnerUsername);
        feedBean.setPosterImageURL(newStatus.getUser().getImageUrl());
        feedBean.setPosterUsername(newStatus.getUser().getAlias());
        feedBean.setPosterFirstName(newStatus.getUser().getFirstName());
        feedBean.setPosterLastName(newStatus.getUser().getLastName());
        feedBean.setPost(newStatus.getPost());
        feedBean.setTimestamp(newStatus.getTimestamp());
        feedBean.setUrls(newStatus.getUrls());
        feedBean.setMentions(newStatus.getMentions());

        table.putItem(feedBean);
    }
}
