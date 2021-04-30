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

            LogManager.getLogger(UserDao.class.getName()).error("MongoDB: error occurred");
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
                if (elapsedTime > 900) { //MongoDriver.getObject().getTimer()
                    LogManager.getLogger(UserDao.class.getName()).error("MongoDB: user insert failed");
                    return "Abort";
                }
                LogManager.getLogger(UserDao.class.getName()).error("Error inserting user in users collection");
            }
        }
        try(Session session = Neo4jDriver.getObject().getDriver().session()) {
            session.writeTransaction((TransactionWork<Void>) tx->{
                tx.run("CREATE (ee:Person { username: $username, country: $country, level: $level })", parameters("username", username, "country", country, "level", 0));
                return  null;
            });


        }catch (TransientException | DiscoveryException | SessionExpiredException ex){
            //timer evaluation, dovremmo fare un timer generale e lasciare quelli di default
            System.out.println("primo");
        }
        catch (Neo4jException ne){
            //another error occurs it's not necessary to continue
            System.out.println("secondo");
        }

        return "registrato";

    }


}
