package it.unipi.dii.inginf.lsmdb.rechype.user;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.MongoDriver;
import static com.mongodb.client.model.Filters.eq;

import org.bson.Document;

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

    public boolean checkRegistration(String username, String password, String confPassword, String country, int age){
        boolean ok=true;
        try(MongoCursor<Document> cursor =
                    MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS).find(eq("_id", username)).iterator()){
            if(cursor.hasNext()){
                if(username.equals(cursor.next().get("_id").toString())){
                    // Already exist this username
                    ok = false;
                }
            }
        }catch(MongoException me){

        }
        // DA FARE: verificare la variabile di ritorno
        try {
            Document doc = new Document("_id", username).append("password", password).append("country", country).append("age", age);
            MongoCollection<Document> collection = MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS);
            collection.insertOne(doc);
        }catch(MongoException me){

        }
        return ok;

    }


}
