package it.unipi.dii.inginf.lsmdb.rechype.persistence;
import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

public class Neo4jDriver {
    private final Driver driver;
    private static Neo4jDriver istance=new Neo4jDriver();

    private Neo4jDriver(){
        String uri=""; //config
        String user="";
        String pass="";
        driver= GraphDatabase.driver(uri, AuthTokens.basic(user, pass));
    }

    public Driver getDriver(){
        return driver;
    }

    public Neo4jDriver getInstance(){
        return istance;
    }

    public void closeDriver(){
        driver.close();
    }
}
