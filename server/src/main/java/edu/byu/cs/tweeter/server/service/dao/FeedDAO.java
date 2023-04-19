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
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteResult;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

public class FeedDAO implements FeedDAOInterface{
    private static final String tableName = "Tweeter_Feed";
    private final DynamoDbTable<FeedBean> table;
    DynamoDbEnhancedClient enhancedClient;

    public FeedDAO(DynamoDbEnhancedClient enhancedClient) {
        this.enhancedClient = enhancedClient;
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
            String lastStatusTimestamp = String.valueOf(lastStatus.getTimestamp());

            Map<String, AttributeValue> startKey = new HashMap<>();
            startKey.put(
                    "username",
                    AttributeValue.builder().s(feedOwnerUsername).build());
            startKey.put(
                    "timestamp",
                    AttributeValue.builder().n(lastStatusTimestamp).build());
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

    @Override
    public void batchAddStatus(List<String> followers, Status status) {
        WriteBatch.Builder<FeedBean> feedWriteBuilder = WriteBatch
                .builder(FeedBean.class)
                .mappedTableResource(table);

        List<String> troubleFollowers = new ArrayList<>();

        String posterUsername = status.getUser().getAlias();
        String posterFirstName = status.getUser().getFirstName();
        String posterLastName = status.getUser().getLastName();
        String posterImageUrl = status.getUser().getImageUrl();
        String post = status.getPost();
        long timestamp = status.getTimestamp();
        List<String> mentions = status.getMentions();
        List<String> urls = status.getUrls();
        int counter = 0;

        for (String followerUsername : followers) {

            FeedBean feedBean = new FeedBean();
            feedBean.setUsername(followerUsername);
            feedBean.setPosterUsername(posterUsername);
            feedBean.setPosterFirstName(posterFirstName);
            feedBean.setPosterLastName(posterLastName);
            feedBean.setPosterImageURL(posterImageUrl);
            feedBean.setPost(post);
            feedBean.setTimestamp(timestamp);
            feedBean.setMentions(mentions);
            feedBean.setUrls(urls);

            feedWriteBuilder.addPutItem(builder -> builder.item(feedBean));
            counter += 1;

            if (counter == 16) {
                troubleFollowers.addAll(addBatch(feedWriteBuilder));
                feedWriteBuilder = WriteBatch.builder(FeedBean.class)
                        .mappedTableResource(table);
                counter = 0;
            }
        }
        if (counter > 0) {
            troubleFollowers.addAll(addBatch(feedWriteBuilder));
        }

        if (troubleFollowers.size() > 0) {
            System.out.println("***************** " + troubleFollowers.size() + " feeds were not updated with the rest**********");
            batchAddStatus(troubleFollowers, status);
        }
    }

    private List<String> addBatch(WriteBatch.Builder<FeedBean> feedWriteBuilder) {
        List<String> troubleFollowers = new ArrayList<>();
        BatchWriteItemEnhancedRequest batchWriteItemEnhancedRequest = BatchWriteItemEnhancedRequest.builder()
                .writeBatches(feedWriteBuilder.build()).build();

        try {
            BatchWriteResult feedResult = enhancedClient.batchWriteItem(batchWriteItemEnhancedRequest);
            // just hammer dynamodb again with anything that didn't get written this time
            if (feedResult.unprocessedPutItemsForTable(table).size() > 0) {
                for (FeedBean feedBean : feedResult.unprocessedPutItemsForTable(table)) {
                    troubleFollowers.add(feedBean.getUsername());
                }
            }

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return troubleFollowers;
    }
}
