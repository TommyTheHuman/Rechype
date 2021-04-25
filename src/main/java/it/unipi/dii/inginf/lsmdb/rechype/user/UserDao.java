package it.unipi.dii.inginf.lsmdb.rechype.user;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.InsertOneResult;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.MongoDriver;
import static com.mongodb.client.model.Filters.eq;

import it.unipi.dii.inginf.lsmdb.rechype.persistence.Neo4jDriver;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;
import org.neo4j.driver.Session;

import javax.print.Doc;
import java.util.Date;

class UserDao {

    public boolean checkLogin(String username, String password){
        try(MongoCursor<Document> cursor =
        MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS).find(eq("_id", username)).iterator()){
            if(cursor.hasNext()){
                if(password.equals(cursor.next().get("password").toString())){
                    return true;
                }
            }
        }catch(MongoException me){

            System.out.println("mannaggia");
        }
        return false;
    }

    public String checkRegistration(String username, String password, String confPassword, String country, int age){
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0L;

        //infinite loop to check for double username
        while(true){
            try(MongoCursor<Document> cursor = MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS).find(eq("_id", username)).iterator()){
                //check for double username
                if(cursor.hasNext()){
                    // Already exist this username
                    return "usernameProb";
                }else{
                    break;
                }
            }catch(MongoException me){
                elapsedTime = (new Date()).getTime() - startTime;
                if(elapsedTime > 900){//configTime
                    LogManager.getLogger(UserDao.class.getName()).error("MongoDB: username retrievement failed");
                    return "Abort";
                }
                LogManager.getLogger(UserDao.class.getName()).error("Error searching username in users collection");
            }
        }

        Document doc = new Document("_id", username).append("password", password).append("country", country).append("age", age);
        startTime = System.currentTimeMillis();
        elapsedTime = 0L;

        //try on MongoDB
        while(true) {
            try {
                MongoCollection<Document> collection = MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS);
                collection.insertOne(doc);
                break;
            } catch (MongoException me) {
                elapsedTime = (new Date()).getTime() - startTime;
                if (elapsedTime > 900) {//configTime
                    LogManager.getLogger(UserDao.class.getName()).error("MongoDB: user insert failed");
                    return "Abort";
                }
                LogManager.getLogger(UserDao.class.getName()).error("Error inserting user in users collection");
            }
        }
        //try on Neo4j
        while (true) {
            try(Session session = Neo4jDriver.getObject().getDriver().session()) {
                MongoCollection<Document> collection = MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS);
                collection.insertOne(doc);
            } catch (MongoException me) {
                elapsedTime = (new Date()).getTime() - startTime;
                if (elapsedTime > 900) {//configTime
                    LogManager.getLogger(UserDao.class.getName()).error("MongoDB: user insert failed");
                    return "Abort";
                }
                LogManager.getLogger(UserDao.class.getName()).error("Error inserting user in users collection");
            }
        }

        return ok;

    }


}
