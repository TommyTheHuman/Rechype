package it.unipi.dii.inginf.lsmdb.rechype.drink;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.oath.halodb.HaloDB;
import com.oath.halodb.HaloDBException;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.HaloDBDriver;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.MongoDriver;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.Neo4jDriver;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.exceptions.Neo4jException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.eq;
import static org.neo4j.driver.Values.parameters;

class DrinkDao {

    List<Drink> getDrinksByText(String drinkName, int offset, int quantity){
        //create the case Insensitive pattern and perform the mongo query
        List<Drink> returnList = new ArrayList<>();
        List<Document> returnDocList = new ArrayList<>();
        Pattern pattern = Pattern.compile(".*" + drinkName + ".*", Pattern.CASE_INSENSITIVE);
        Bson filter = Filters.regex("name", pattern);
        MongoCursor<Document> drinkCursor  = MongoDriver.getObject().getCollection(MongoDriver.Collections.DRINKS)
        .find(filter).skip(offset).limit(quantity).iterator();
        while(drinkCursor.hasNext()){
            Document doc = drinkCursor.next();
            Drink drink = new Drink(doc);
            returnList.add(drink);
            returnDocList.add(doc);
        }
        cacheSearch(returnDocList);
        return returnList;
    }

    public void cacheSearch(List<Document> drinksList){ //caching of drink's search
        for(int i=0; i<drinksList.size(); i++) {
            String idObj = new JSONObject(drinksList.get(i).toJson()).getJSONObject("_id").getString("$oid");
            byte[] _id = idObj.getBytes(StandardCharsets.UTF_8); //key
            byte[] objToSave = drinksList.get(i).toJson().getBytes(StandardCharsets.UTF_8); //value
            try {
                HaloDBDriver.getObject().getClient("drinks").put(_id, objToSave);
            }catch(Exception e){
                LogManager.getLogger("DrinkDao.class").fatal("HaloDB: caching failed");
                HaloDBDriver.getObject().closeConnection();
                System.exit(-1);
            }
        }
    }

    public String addLike(String _id, String user){

        boolean already_tried = false;
        //cross-db consistency between neo4j and mongodb
        //the while will execute 2 iteration at most
        while(true){

            //_id in neo4j is the oid field in mongodb
            if(!already_tried) { //try to add to neo4j
                try (Session session = Neo4jDriver.getObject().getDriver().session()){
                    session.writeTransaction((TransactionWork<Void>) tx -> {
                        tx.run("MATCH (uu:User) WHERE uu.username = $username" +
                                        " MATCH (d:Drink) WHERE rr.id = $_id" +
                                        " CREATE (uu)-[rel:LIKES {since:$date}]->(d)",
                                parameters("username", user, "_id", _id, "date", java.time.LocalDate.now().toString()));
                        return null;
                    });
                }catch(Neo4jException ne){
                    LogManager.getLogger("DrinkDao.class").error("Neo4j: like's relation insert failed");
                    return "Abort";
                }
            }
            //second try consists in deleting the relation from neo4j
            else{
                //try to delete the relation from neo4j in case the operation on mongo fails
                try (Session session = Neo4jDriver.getObject().getDriver().session()){
                    session.writeTransaction((TransactionWork<Void>) tx -> {
                        tx.run("MATCH (uu:User {username:$username})-[rel:LIKES]->(d:Drink {id:$_id}) delete rel",
                                parameters("username", user, "_id", _id));
                        return null;
                    });
                }catch(Neo4jException ne){
                    LogManager.getLogger("DrinkDao.class").error("Neo4j[PARSE], like add inconsistency: _id: "+
                            _id+" username: "+user);
                    return "Abort";
                }
            }
            MongoCollection<Document> drinkColl=null;
            //try to add the redundancy on mongodb
            try {
                ObjectId objectId = new ObjectId(_id);
                drinkColl=MongoDriver.getObject().getCollection(MongoDriver.Collections.DRINKS);
                drinkColl.updateOne(eq("_id", objectId), Updates.inc("likes", 1));
                //the database are perfectly consistent
                return "LikeOk";
            }catch (MongoException me){
                LogManager.getLogger("DrinkDao.class").error("MongoDB: failed to insert like in drinks");
                already_tried=true;
            }
        }
    }

    public String removeLike(String _id, String username){
        boolean already_tried = false;
        //cross-db consistency between neo4j and mongodb
        //the while will execute 2 iteration at most
        while(true){

            //_id in neo4j is the oid field in mongodb
            if(!already_tried) { //try to delete on neo4j
                try (Session session = Neo4jDriver.getObject().getDriver().session()){
                    session.writeTransaction((TransactionWork<Void>) tx -> {
                        tx.run("MATCH (uu:User {username:$username})-[rel:LIKES]->(d:Drink {id:$_id}) delete rel",
                                parameters("username", username, "_id", _id));
                        return null;
                    });
                }catch(Neo4jException ne){
                    LogManager.getLogger("DrinkDao.class").error("Neo4j: like's relation deletion failed");
                    return "Abort";
                }
            }
            //second try consists in deleting the relation from neo4j
            else{
                //try to add again the relation to neo4j in case the operation on mongo fails
                try (Session session = Neo4jDriver.getObject().getDriver().session()){
                    session.writeTransaction((TransactionWork<Void>) tx -> {
                        tx.run("MATCH (uu:User) WHERE uu.username = $username" +
                                        " MATCH (d:Drink) WHERE rr.id = $_id" +
                                        " CREATE (uu)-[rel:LIKES {since:$date}]->(d)",
                                parameters("username", username, "_id", _id));
                        return null;
                    });
                }catch(Neo4jException ne){
                    LogManager.getLogger("DrinkDao.class").error("Neo4j[PARSE], like delete inconsistency: _id: "+
                            _id+" username: "+username);
                    return "Abort";
                }
            }
            MongoCollection<Document> drinkColl=null;
            //try to update the redundancy on mongodb
            try {
                ObjectId objectId = new ObjectId(_id);
                drinkColl=MongoDriver.getObject().getCollection(MongoDriver.Collections.DRINKS);
                drinkColl.updateOne(eq("_id", objectId), Updates.inc("likes", -1));
                //the database are perfectly consistent
                return "LikeOk";
            }catch (MongoException me){
                LogManager.getLogger("DrinkDao.class").error("MongoDB: failed to delete like in drinks");
                already_tried=true;
            }
        }
    }


    public JSONObject getDrinkByKey(String key){
        try{
            HaloDB db = HaloDBDriver.getObject().getClient("drinks");
            byte[] byteObj = db.get(key.getBytes(StandardCharsets.UTF_8));
            return new JSONObject(new String(byteObj));
        }catch(HaloDBException ex){
            LogManager.getLogger("DrinkDao.class").fatal("HaloDB: caching failed");
            HaloDBDriver.getObject().closeConnection();
            System.exit(-1);
        }
        return new JSONObject();
    }

}
