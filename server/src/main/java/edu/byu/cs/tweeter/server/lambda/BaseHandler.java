package edu.byu.cs.tweeter.server.lambda;

import edu.byu.cs.tweeter.server.service.dao.DAOFactory;
import edu.byu.cs.tweeter.server.service.dao.DynamoDAO;

public class BaseHandler {
    DAOFactory factory = new DynamoDAO();

    public DAOFactory getFactory() {
        return factory;
    }

    public void setFactory(DAOFactory factory) {
        this.factory = factory;
    }
}
