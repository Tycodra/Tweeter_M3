package edu.byu.cs.tweeter.server.service.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.server.service.Dynamos.DataPage;
import edu.byu.cs.tweeter.server.service.Dynamos.StoryBean;
import edu.byu.cs.tweeter.util.FakeData;
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

public class StoryDAO implements StoryDAOInterface{
    private static final String tableName = "Tweeter_Story";
    private static final String authorUsername = "authorUsername";
    private static final String timestamp = "timestamp";
    private DynamoDbTable<StoryBean> table;
    private List<Status> fakeStatuses = FakeData.getInstance().getFakeStatuses();

    public StoryDAO(DynamoDbEnhancedClient enhancedClient) {
        table = enhancedClient.table(tableName, TableSchema.fromBean(StoryBean.class));
    }

    public Pair<List<Status>, Boolean> getStory(String authorUsername, int pageLimit, Status lastStatus) {
        Key key = Key.builder()
                .partitionValue(authorUsername)
                .build();
        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(key))
                .scanIndexForward(false)
                .limit(pageLimit);
        if (lastStatus != null) {
            String lastStatusUsername = authorUsername;
            String lastStatusTimestamp = String.valueOf(lastStatus.getTimestamp());

            Map<String, AttributeValue> startKey = new HashMap<>();
            startKey.put(
                    this.authorUsername,
                    AttributeValue.builder().s(lastStatusUsername).build());
            startKey.put(
                    this.timestamp,
                    AttributeValue.builder().s(lastStatusTimestamp).build());
            requestBuilder.exclusiveStartKey(startKey);
        }

        QueryEnhancedRequest request = requestBuilder.build();

        DataPage<StoryBean> result = new DataPage<>();
        PageIterable<StoryBean> pages = table.query(request);
        pages.stream()
                .limit(1)
                .forEach((Page<StoryBean> page) -> {
                    result.setHasMorePages(page.lastEvaluatedKey() != null);
                    page.items().forEach(status -> result.getValues().add(status));
                });
        List<Status> storyStatuses = new ArrayList<>(pageLimit);

        for (StoryBean status : result.getValues()) {
            storyStatuses.add(new Status(
                    status.getPost(),
                    new User(
                            status.getFirstName(),
                            status.getLastName(),
                            status.getAuthorUsername(),
                            status.getImageURL()),
                    status.getTimestamp(),
                    status.getUrls(),
                    status.getMentions()
            ));
        }
        return new Pair<>(storyStatuses, result.hasMorePages());
    }

    @Override
    public void addToStory(Status newStatus) {
        StoryBean storyBean = new StoryBean();
        storyBean.setImageURL(newStatus.getUser().getImageUrl());
        storyBean.setAuthorUsername(newStatus.getUser().getAlias());
        storyBean.setFirstName(newStatus.getUser().getFirstName());
        storyBean.setLastName(newStatus.getUser().getLastName());
        storyBean.setPost(newStatus.getPost());
        storyBean.setTimestamp(newStatus.getTimestamp());
        storyBean.setUrls(newStatus.getUrls());
        storyBean.setMentions(newStatus.getMentions());

        table.putItem(storyBean);
    }
}
