package it.unipi.dii.inginf.lsmdb.rechype.persistence;
import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

public class Neo4jDriver {
    private static Neo4jDriver obj=new Neo4jDriver();
    private final Driver driver;

    private Neo4jDriver(){
        String uri="bolt://localhost:11003"; //config
        String user="neo4j";
        String pass="rechype";
        driver= GraphDatabase.driver(uri, AuthTokens.basic(user, pass));
    }

    public Driver getDriver(){
        return driver;
    }

    public static Neo4jDriver getObject(){
        return obj;
    }

    public void closeDriver(){
        driver.close();
    }
}
