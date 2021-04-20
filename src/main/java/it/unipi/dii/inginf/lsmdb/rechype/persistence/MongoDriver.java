package it.unipi.dii.inginf.lsmdb.rechype.persistence;

//mettere un file di config per la configurazione degli ip e dei parametri

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 *
 */


public class MongoDriver {
    private final MongoClient client;
    private final MongoDatabase defaultDatabase;
    private static MongoDriver instance=new MongoDriver();

    private MongoDriver(){
        client = MongoClients.create("mongodb://localhost:27017");
        defaultDatabase = client.getDatabase(""); //default by config
    }

    public MongoCollection getCollection(Collections c){
        return defaultDatabase.getCollection(c.toString());
    }

    public MongoCollection getCollection(String DB, Collections c) {
        MongoCollection coll = null;
        try {
            MongoDatabase mongoDB = client.getDatabase(DB);
            coll = (MongoCollection) mongoDB.getCollection(c.name);
        }catch(MongoException me) {
            //logger
        }
        return coll;
    }

    public MongoDriver getInstance(){ return instance; }

    public void closeConnection(){
        client.close();
    }

    public enum Collections{
        RECIPES("recipes"),
        INGREDIENTS("ingredients"),
        PROFILES("profiles"),
        USERS("users");

        private String name;

        Collections(String n){
            this.name = n;
        }

        public String toString(){
            return this.name;
        }
    }
}
