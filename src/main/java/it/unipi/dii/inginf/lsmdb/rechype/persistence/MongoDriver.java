package it.unipi.dii.inginf.lsmdb.rechype.persistence;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.logging.log4j.LogManager;

/**
 * Class that defines and configures the connection to the MongoDb cluster
 */


public class MongoDriver {
    private static final MongoDriver obj = new MongoDriver();
    private MongoClient client;
    private MongoDatabase defaultDatabase;

    private MongoDriver(){
            client = MongoClients.create(DBConfigurations.getObject().MongoUri);
            defaultDatabase = client.getDatabase(DBConfigurations.getObject().defaultDBMongo);
    }

    public MongoCollection getCollection(Collections c){
        return defaultDatabase.getCollection(c.toString());
    }

    //this function allows to access to databases different from the default one
    public MongoCollection getCollection(String DB, Collections c) {
        MongoCollection coll = null;
        try {
            MongoDatabase mongoDB = client.getDatabase(DB);
            coll = (MongoCollection) mongoDB.getCollection(c.name);
        }catch(MongoException me) {
            LogManager.getLogger(MongoDriver.class.getName()).fatal("MongoDB: collection not retrieved");
        }
        return coll;
    }

    public static MongoDriver getObject(){ return obj; }

    public void closeConnection(){
        client.close();
    }

    public enum Collections{
        RECIPES("recipes"),
        INGREDIENTS("ingredients"),
        PROFILES("profiles"),
        USERS("users"),
        DRINKS("drinks");

        private String name;

        Collections(String n){
            this.name = n;
        }

        public String toString(){
            return this.name;
        }
    }
}
