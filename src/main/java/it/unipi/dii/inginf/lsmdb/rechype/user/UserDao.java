package it.unipi.dii.inginf.lsmdb.rechype.user;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCursor;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.MongoDriver;
import static com.mongodb.client.model.Filters.eq;
import org.bson.Document;

class UserDao {

    public boolean checkPassword(String username, String password){
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


}
