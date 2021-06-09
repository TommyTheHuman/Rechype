package it.unipi.dii.inginf.lsmdb.rechype.profile;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.MongoDriver;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.*;

class ProfileDAO {
    /***
     * retrieving a profile's entity specifying the username
     * @param username
     * @return
     */
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

    /***
     * it inserts the profile's entity along with its vectors of objects "meals" and "fridge"
     * @param username
     * @return
     */
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

    /***
     * delete a profile's entity
     * @param username
     * @return
     */
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

    /***
     * add meal in user profile
     * @param title
     * @param type
     * @param recipes
     * @param drinks
     * @param username
     * @return
     */
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

    /***
     * Delete a meal from the array inside user's profile document.
     * @param title
     * @param username
     * @return
     */
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

    /***
     * Add one or more ingredient/s to the user's fridge.
     * @param ingredients
     * @param username
     * @return
     */
    public Boolean addIngredientToFridge(List<Document> ingredients, String username){
        MongoCollection<Document> coll = null;
        try{
            coll = MongoDriver.getObject().getCollection(MongoDriver.Collections.PROFILES);
            coll.updateOne(eq("_id", username), pushEach("fridge", ingredients));
        }catch(MongoException ex){
            LogManager.getLogger("ProfileDao.class").error("MongoDB: ingredients insert failed");
            return false;
        }
        return true;
    }

    /***
     * Delete an ingredient from user's fridge
     * @param username
     * @param ingredient
     * @return
     */
    public boolean deleteIngredientFromProfile(String username, String ingredient) {

        MongoCollection<Document> coll = null;
        try{
            coll = MongoDriver.getObject().getCollection(MongoDriver.Collections.PROFILES);
            coll.updateOne(eq("_id", username), pull("fridge", eq("name", ingredient)));
        }catch(MongoException ex){
            LogManager.getLogger("ProfileDao.class").error("MongoDB: ingredients deletion failed");
            return false;
        }
        return true;
    }

}
