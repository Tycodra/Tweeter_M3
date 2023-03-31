package edu.byu.cs.tweeter.server.service.dao;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.server.service.Dynamos.AuthBean;
import edu.byu.cs.tweeter.util.FakeData;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

public class AuthenticationDAO implements AuthenticationDAOInterface{
    private static final String tableName = "Tweeter_Auth";
    private final DynamoDbTable<AuthBean> table;

    public AuthenticationDAO(DynamoDbEnhancedClient enhancedClient) {
        table = enhancedClient.table(tableName, TableSchema.fromBean(AuthBean.class));
    }
    public AuthToken getAuthToken() {
        return getDummyAuthToken();
    }

    @Override
    public AuthToken addAuthToken(String authToken, long timestamp) {
        AuthBean authBean = new AuthBean();
        authBean.setAuthToken(authToken);
        authBean.setTimestamp(timestamp);

        table.putItem(authBean);
        return new AuthToken(authToken, String.valueOf(timestamp));
    }

    @Override
    public void removeAuthToken(String authToken) {
        System.out.println("In removeAuthToken");
        try {
            Key key = Key.builder()
                    .partitionValue(authToken)
                    .build();

            table.deleteItem(key);
            System.out.println("deletedItem: " + authToken);
        } catch (DynamoDbException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    @Override
    public boolean checkTokenValidity(String authToken, long timestamp) {
        Key key = Key.builder()
                .partitionValue(authToken)
                .build();
        AuthBean authBean = table.getItem(key);
        if ((timestamp - authBean.getTimestamp()) < 60000) {
            authBean.setTimestamp(timestamp);
            table.putItem(authBean);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the dummy auth token to be returned by the login operation.
     * This is written as a separate method to allow mocking of the dummy auth token.
     *
     * @return a dummy auth token.
     */
    private AuthToken getDummyAuthToken() {
        return getFakeData().getAuthToken();
    }

    /**
     * Returns the {@link FakeData} object used to generate dummy users and auth tokens.
     * This is written as a separate method to allow mocking of the {@link FakeData}.
     *
     * @return a {@link FakeData} instance.
     */
    private FakeData getFakeData() {
        return FakeData.getInstance();
    }
}
