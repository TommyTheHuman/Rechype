package it.unipi.dii.inginf.lsmdb.rechype.profile;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.MongoDriver;
import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Updates.push;
import static com.mongodb.client.model.Updates.pull;
import static com.mongodb.client.model.Filters.eq;

class ProfileDAO {

    //retrieving a profile's entity specifying the username
    public Profile getProfileByUsername(String username){
        try(MongoCursor<Document> cursor = MongoDriver.getObject().getCollection(MongoDriver.Collections.PROFILES).find(eq("_id", username)).iterator()){
            Document doc = cursor.next();
            Profile retProf = new Profile(doc);
            return retProf;
        }catch(MongoException me){
            LogManager.getLogger("UserDao.class").error("MongoDB: an error occurred");
            System.exit(-1);
        }
        return null;
    }

    //it inserts the profile's entity along with its vectors of objects "meals" and "fridge"
    public Boolean insertProfile(String username){

        List<Document> meals = new ArrayList<>();
        List<Document> fridge = new ArrayList<>();

        Document doc = new Document().append("_id", username).append("meals", meals).append("fridge", fridge);
        try {
            MongoCollection<Document> coll=MongoDriver.getObject().getCollection(MongoDriver.Collections.PROFILES);
            coll.insertOne(doc);
            return true;

        } catch (MongoException me) {
            LogManager.getLogger("UserDao.class").error("MongoDB: an error occurred");
        }
        return false;
    }

    //delete a profile's entity
    public Boolean deleteProfile(String username){
        MongoCollection<Document> coll=null;
        try{
            coll=MongoDriver.getObject().getCollection(MongoDriver.Collections.PROFILES);
            coll.deleteOne(eq("_id", username));
            return true;
        }catch(MongoException ex){
            LogManager.getLogger("ProfileDAO.class").error("MongoDB[PARSE], profile deletion failed: "+username);
            return false;
        }
    }

    // add meal in user profile
    public String addMealToProfile(String title, String type, List<Document> recipes, List<Document> drinks, String username){

        List<String> existingTitle = new ArrayList<>();

        try(MongoCursor<Document> cursor = MongoDriver.getObject().getCollection(MongoDriver.Collections.PROFILES).find(eq("_id", username)).iterator()){
            Document doc = cursor.next();
            List<Document> existingMeals = (List<Document>) doc.get("meals");
            for(int i=0; i<existingMeals.size(); i++){
                existingTitle.add(existingMeals.get(i).getString("title"));
            }
        }catch(MongoException ex){
            LogManager.getLogger("ProfileDao.class").error("MongoDB: meal insert failed on check other meal's title");
        }

        if(existingTitle.contains(title)){
            return "DuplicateTitle";
        }

        MongoCollection<Document>  coll = null;
        Document doc = new Document().append("title", title).append("type", type).append("recipes", recipes).append("drinks", drinks);
        try{
            coll = MongoDriver.getObject().getCollection(MongoDriver.Collections.PROFILES);
            coll.updateOne(eq("_id", username), push("meals", doc));
        }catch(MongoException ex){
            LogManager.getLogger("ProfileDao.class").error("MongoDB: meal insert failed");
            return "Abort";
        }
        return "AddOK";
    }

    public Boolean deleteMealFromProfile(String title, String username){
        MongoCollection<Document> coll = null;
        try{
            coll = MongoDriver.getObject().getCollection(MongoDriver.Collections.PROFILES);
            coll.updateOne(eq("_id", username), pull("meals", eq("title", title)));
        }catch(MongoException ex){
            LogManager.getLogger("ProfileDao.class").error("MongoDB: meal insert failed");
            return false;
        }
        return true;
    }

}
