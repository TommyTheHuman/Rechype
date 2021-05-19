package it.unipi.dii.inginf.lsmdb.rechype.user;

import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.oath.halodb.HaloDB;
import com.oath.halodb.HaloDBException;
import com.oath.halodb.HaloDBIterator;
import com.oath.halodb.Record;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.HaloDBDriver;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.MongoDriver;
import static com.mongodb.client.model.Filters.eq;
import static org.neo4j.driver.Values.parameters;

import it.unipi.dii.inginf.lsmdb.rechype.persistence.Neo4jDriver;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.exceptions.DiscoveryException;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.exceptions.SessionExpiredException;
import org.neo4j.driver.exceptions.TransientException;

import javax.print.Doc;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

class UserDao {

    public User checkLogin(String username, String password){
        try(MongoCursor<Document> cursor =
        MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS).find(eq("_id", username)).iterator()){
            if(cursor.hasNext()){
                Document doc = cursor.next();
                if(password.equals(doc.get("password").toString())){

                    String user = doc.get("_id").toString();
                    String country = doc.get("country").toString();
                    int age = Integer.parseInt(doc.get("age").toString());
                    int level = Integer.parseInt(doc.get("age").toString());

                    User userLogged = new User(user, country, age, level);

                    return userLogged;
                }
            }
        }catch(MongoException me){
            LogManager.getLogger("UserDao.class").error("MongoDB: an error occurred");
            System.exit(-1);
        }
        return null;
    }

    public JSONObject checkRegistration(String username, String password, String confPassword, String country, int age) {

        Document doc = new Document("_id", username).append("password", password).append("country", country).append("age", age).append("level", 0);
        JSONObject Json = new JSONObject(doc.toJson());

        try (MongoCursor<Document> cursor = MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS).find(eq("_id", username)).iterator()) {
            //check for double username or username not allow.
            if (cursor.hasNext() || username.equals("Spoonacular") || username.equals("admin") || username.equals("CocktailsDB") || username.equals("PunkAPI")) {
                Json.put("response", "usernameProb");
                return Json;
            }

        } catch (MongoException me) {
            LogManager.getLogger("UserDao.class").error("MongoDB: an error occurred");
        }


        boolean already_tried=false;
        MongoCollection<Document> coll=null;

        //try on MongoDB
        while(true){
            try {
                if(!already_tried) { //try to add
                    coll=MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS);
                    coll.insertOne(doc);
                }
                else{ //try to delete
                    coll=MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS);
                    coll.deleteOne(eq("_id", username));
                    Json.put("response", "Abort");
                    return Json;
                }

            } catch (MongoException me) {
                if(!already_tried) { //first time error
                    LogManager.getLogger("UserDao.class").error("MongoDB: user insert failed");
                    Json.put("response", "Abort");
                    return Json;
                }else{ //second time error, consistency adjustment
                    LogManager.getLogger("UserDao.class").error("MongoDB[PARSE], registration inconsistency: "+doc.toJson());
                    Json.put("response", "Abort");
                    return Json;
                }
            }
            //try Neo4j
            try (Session session = Neo4jDriver.getObject().getDriver().session()) { //try to add
                session.writeTransaction((TransactionWork<Void>) tx -> {
                    tx.run("CREATE (ee:Person { username: $username, country: $country, level: $level })", parameters("username", username, "country", country, "level", 0));
                    return null;
                });
                Json.put("response", "RegOk");
                return Json;
            }catch(Neo4jException ne){ //fail, next cycle try to delete on MongoDB
                LogManager.getLogger("UserDao.class").error("Neo4j: user insert failed");
                already_tried=true;
            }
        }
    }

    public List<User> getUsersByText(String userName, int offset, int quantity){
        //List of users and list of documents for caching
        List<User> returnList = new ArrayList<>();
        List<Document> returnDocList = new ArrayList<>();
        //create the case Insesitive pattern and perform the mongo query
        Pattern pattern = Pattern.compile(".*" + userName + ".*", Pattern.CASE_INSENSITIVE);
        Bson filter = Filters.regex("_id", pattern);
        MongoCursor<Document> cursor  = MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS).find(filter)
        .skip(offset).limit(quantity).iterator();
        while (cursor.hasNext()){

            Document doc = cursor.next();
            returnDocList.add(doc);
            returnList.add(new User(doc));

        }
        cacheSearch(returnDocList);
        return returnList;
    }

    public JSONObject getUserByKey(String key){
        try{
            HaloDB db = HaloDBDriver.getObject().getClient("users");
            byte[] byteObj = db.get(key.getBytes(StandardCharsets.UTF_8));
            return new JSONObject(new String(byteObj));
        }catch(HaloDBException ex){
            LogManager.getLogger("UserDao.class").fatal("HaloDB: caching failed");
            HaloDBDriver.getObject().closeConnection();
            System.exit(-1);
        }
        return new JSONObject();
    }

    private void cacheSearch(List<Document> userList){ //caching of user's search
        for(int i=0; i<userList.size(); i++) {
            byte[] username = userList.get(i).getString("_id").getBytes(StandardCharsets.UTF_8); //key
            byte[] objToSave = userList.get(i).toJson().getBytes(StandardCharsets.UTF_8); //value
            try{
                HaloDBDriver.getObject().getClient("users").put(username, objToSave);
            }catch(Exception e){
                LogManager.getLogger("UserDao.class").fatal("HaloDB: caching failed");
                HaloDBDriver.getObject().closeConnection();
                System.exit(-1);
            }
        }
    }

}
