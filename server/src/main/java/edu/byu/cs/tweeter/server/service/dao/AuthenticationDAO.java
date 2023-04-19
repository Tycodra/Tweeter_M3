package edu.byu.cs.tweeter.server.service.dao;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.server.service.Dynamos.AuthBean;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

public class AuthenticationDAO implements AuthenticationDAOInterface{
    private static final String tableName = "Tweeter_Auth";
    private final DynamoDbTable<AuthBean> table;

    public AuthenticationDAO(DynamoDbEnhancedClient enhancedClient) {
        table = enhancedClient.table(tableName, TableSchema.fromBean(AuthBean.class));
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
        if ((timestamp - authBean.getTimestamp()) < 300000) {
            authBean.setTimestamp(timestamp);
            table.updateItem(authBean);
            return true;
        } else {
            return false;
        }
    }
}
