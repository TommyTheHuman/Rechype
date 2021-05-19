package it.unipi.dii.inginf.lsmdb.rechype.persistence;
import org.neo4j.driver.*;

import java.util.concurrent.TimeUnit;

/**
 * Class that defines and configures the connection to the neo4j cluster
 */

public class Neo4jDriver {
    private static Neo4jDriver obj=new Neo4jDriver();
    private final Driver driver;
    public static final String MAX_TRANSACTION_RETRY_TIME = "max.transaction.retry.time";
    private final long timer;

    private Neo4jDriver(){
        timer=DBConfigurations.getObject().timerNeo4j;
        Config.ConfigBuilder builder = Config.builder();
        builder.withMaxTransactionRetryTime(DBConfigurations.getObject().timerNeo4j, TimeUnit.MILLISECONDS); //config + exception
        Config config=builder.build();
        driver= GraphDatabase.driver(DBConfigurations.getObject().Neo4jUri, AuthTokens.basic(DBConfigurations.getObject().Neo4jUser,
        DBConfigurations.getObject().Neo4jPassword), config);
    }

    public Driver getDriver(){return driver;}

    public static Neo4jDriver getObject(){
        return obj;
    }

    public void closeConnection(){
        driver.close();
    }

}
