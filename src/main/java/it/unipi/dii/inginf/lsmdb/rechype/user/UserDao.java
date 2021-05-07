package it.unipi.dii.inginf.lsmdb.rechype.user;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.InsertOneResult;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.MongoDriver;
import static com.mongodb.client.model.Filters.eq;
import static org.neo4j.driver.Values.parameters;

import it.unipi.dii.inginf.lsmdb.rechype.persistence.Neo4jDriver;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.exceptions.DiscoveryException;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.exceptions.SessionExpiredException;
import org.neo4j.driver.exceptions.TransientException;

import javax.print.Doc;
import java.io.IOException;
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
            LogManager.getLogger("UserDao.class").error("MongoDB: an error occurred");
            System.exit(-1);
        }
        return false;
    }

    public String checkRegistration(String username, String password, String confPassword, String country, int age) {

        try (MongoCursor<Document> cursor = MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS).find(eq("_id", username)).iterator()) {
            //check for double username
            if (cursor.hasNext())
                return "usernameProb";

        } catch (MongoException me) {
            LogManager.getLogger("UserDao.class").error("MongoDB: an error occurred");
        }

        Document doc = new Document("_id", username).append("password", password).append("country", country).append("age", age);
        boolean already_tried=false;
        MongoCollection<Document> coll=null;
        System.out.println(doc.toJson());

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
                    return "Abort";
                }

            } catch (MongoException me) {
                if(!already_tried) { //first time error
                    LogManager.getLogger("UserDao.class").error("MongoDB: user insert failed");
                    return "Abort";
                }else{ //second time error, consistency adjustment
                    LogManager.getLogger("UserDao.class").error("MongoDB[PARSE], registration inconsistency: "+doc.toJson());
                    return "Abort";
                }
            }
            //try Neo4j
            try (Session session = Neo4jDriver.getObject().getDriver().session()) { //try to add
                session.writeTransaction((TransactionWork<Void>) tx -> {
                    tx.run("CREATE (ee:Person { username: $username, country: $country, level: $level })", parameters("username", username, "country", country, "level", 0));
                    return null;
                });
                return "regOk";
            }catch(Neo4jException ne){ //fail, next cycle try to delete on MongoDB
                LogManager.getLogger("UserDao.class").error("Neo4j: user insert failed");
                already_tried=true;
            }
        }
    }
}
