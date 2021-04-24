package it.unipi.dii.inginf.lsmdb.rechype.user;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.InsertOneResult;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.MongoDriver;
import static com.mongodb.client.model.Filters.eq;
import org.bson.Document;

import javax.print.Doc;

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
            //serve una eccezione?
            //log
            System.out.println("mannaggia");
        }
        return false;
    }

    public boolean checkRegistration(String username, String password, String confPassword, String country){
        // DA FARE: controlli sui campi e conferma pass

        try(MongoCursor<Document> cursor =
                    MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS).find(eq("_id", username)).iterator()){
            if(cursor.hasNext()){
                if(username.equals(cursor.next().get("_id").toString())){
                    // Already exist this username
                    return false;
                }
            }
        }catch(MongoException me){
            //serve una eccezione?
            //log
            System.out.println("mannaggia");
        }

        // DA FARE: verificare la variabile di ritorno
        try {
            Document doc = new Document("_id", username).append("password", password).append("country", country);
            MongoCollection<Document> collection = MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS);
            collection.insertOne(doc);
        }catch(MongoException me){

        }



        return false;
    }


}
