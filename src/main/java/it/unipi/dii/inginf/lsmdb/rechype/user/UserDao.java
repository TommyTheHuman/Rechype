package it.unipi.dii.inginf.lsmdb.rechype.user;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.oath.halodb.HaloDB;
import com.oath.halodb.HaloDBException;
import static com.mongodb.client.model.Aggregates.*;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.HaloDBDriver;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.MongoDriver;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.eq;
import static org.neo4j.driver.Values.parameters;

import it.unipi.dii.inginf.lsmdb.rechype.persistence.Neo4jDriver;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.Recipe;
import org.apache.logging.log4j.LogManager;
import org.bson.BsonArray;
import org.bson.BsonBinary;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionWork;
import static com.mongodb.client.model.Updates.push;
import static com.mongodb.client.model.Updates.addToSet;
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

    /***
     * performing the login on the mongoDB user's collection, if something goes wrong the user is not
     * logged into the service
     * @param username
     * @param password
     * @return
     */
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

    /***
     * Performing the registration of a user writing the corresponding entity in both Neo4j
     * and MongoDB Databases. If something goes wrong during the registration the cross-db consistency
     * is handled in this way:
     * try on mongo->fails->abort
     * try on mongo->success->try on neo4j->fails->try delete on mongo->success
     * try on mongo->success->try on neo4j->fails->try delete on mongo->fails->parsing
     * @param username
     * @param password
     * @param confPassword
     * @param country
     * @param age
     * @return
     */
    public JSONObject checkRegistration(String username, String password, String confPassword, String country, int age) {

        Document doc = new Document("_id", username).append("password", password).append("country", country)
        .append("age", age).append("level", 0).append("recipes", new BsonArray()).append("drinks", new BsonArray());
        JSONObject Json = new JSONObject(doc.toJson());

        try (MongoCursor<Document> cursor = MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS).find(eq("_id", username)).iterator()) {
            //check for double username or username not allow.
            if (cursor.hasNext() || username.equals("Spoonacular") || username.equals("admin") || username.equals("CocktailsDB") || username.equals("PunkAPI")) {
                Json.put("response", "usernameProb");
                return Json;
            }

        } catch (MongoException me) {
            me.printStackTrace();
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
                    tx.run("CREATE (ee:User { username: $username, country: $country, level: $level })", parameters("username", username, "country", country, "level", 0));
                    return null;
                });
                Json.put("response", "RegOk");
                return Json;
            }catch(Neo4jException ne){ //fail, next cycle will try to delete on MongoDB
                LogManager.getLogger("UserDao.class").error("Neo4j: user insert failed");
                already_tried=true;
            }
        }
    }

    /***
     * retrieve the users from mongoDb and cache the search into the key-value DB
     * @param userName
     * @param offset
     * @param quantity
     * @return
     */
    public List<User> getUsersByText(String userName, int offset, int quantity){
        //List of users to build gui and list of documents to caching
        List<User> returnList = new ArrayList<>();
        List<Document> returnDocList = new ArrayList<>();

        //create the case insensitive pattern and perform the mongo query
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

    /***
     * retrieving the user's entity from the key-value DB
     * @param key
     * @return
     */
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

    /***
     * caching the user's search in a key-value DB
     * @param userList
     */
    private void cacheSearch(List<Document> userList){
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

    /***
     * deleting a user's entity from both databases (cross-db consistency)
     * @param username
     * @return
     */
    public Boolean deleteUser(String username){
        MongoCollection<Document> coll=null;
        Boolean neo4j, mongo;

        //delete on MongoDB
        try {
            coll = MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS);
            coll.deleteOne(eq("_id", username));
            mongo=true;
        }catch(MongoException ex){ //somethind goes wrong, a software will parse and solve
            LogManager.getLogger("UserDao.class").error("MongoDB[PARSE], user deletion inconsistency: "+username);
            mongo=false;
        }

        //try Neo4j
        try (Session session = Neo4jDriver.getObject().getDriver().session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User { username: $username }) delete u", parameters("username", username));
                return null;
            });
            neo4j=true;
        }catch(Neo4jException ne){ //somethind goes wrong, a software will parse and solve
            LogManager.getLogger("UserDao.class").error("Neo4j[PARSE], user deletion inconsistency: "+username);
            neo4j=false;
        }
        return neo4j && mongo;
    }

    /***
     * Adding new nested recipe to user, it can be called during the creation of a new recipe or during the
     * adding to favourites. Type discriminates the case of recipe or drink
     * @param recipe
     * @return
     */
    public String addNestedRecipe(Document recipe, User user, String type){
        MongoCollection<Document> collUser = null;
        Document userRecipe = new Document();
        if(type.equals("recipe"))
            userRecipe.append("_id", recipe.get("_id").toString()).append("name", recipe.getString("name")).append("author", recipe.getString("author"))
                    .append("pricePerServing", recipe.get("pricePerServing")).append("image", recipe.getString("image"))
                    .append("vegetarian", recipe.getBoolean("vegetarian")).append("vegan", recipe.getBoolean("vegan")).append("dairyFree", recipe.getBoolean("dairyFree"))
                    .append("glutenFree", recipe.getBoolean("glutenFree"));
        else
            userRecipe.append("_id", recipe.get("_id").toString()).append("name", recipe.getString("name")).append("author", recipe.getString("author"))
            .append("tag", recipe.getString("tag")).append("image", recipe.getString("image"));
        try {
            collUser = MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS);
            List<Bson> listUpdates = new ArrayList<>();
            Bson find;
            if(type.equals("recipe")) {
                find = eq("_id", user.getUsername());
                listUpdates.add(Updates.push("recipes", userRecipe));
            }
            else {
                find = eq("_id", user.getUsername());
                listUpdates.add(Updates.push("drinks", userRecipe));
            }
            //increasing the level if the recipe is created
            //a user cannot add his recipes to favourites because they are already there
            //so the check on the author username is enough
            if(user.getUsername().equals(recipe.getString("author")))
                listUpdates.add(Updates.inc("level", 1));
            collUser.updateOne(find, Updates.combine(listUpdates));
        } catch (MongoException me) {
            me.printStackTrace();
            if(recipe.getBoolean("NoParse")==null) //if no parse is needed the log does not specify it
                LogManager.getLogger("UserDao.class").error("MongoDB[PARSE]: insert nested recipe(creation) in user failed" + recipe.toJson());
            else
                LogManager.getLogger("UserDao.class").error("MongoDB: insert nested recipe(favourite) in user failed");
            return "Abort";
        }
        return "RecipeOk";
    }

    /***
     * check if recipe or drink has been liked by user
     * @param username
     * @param _id
     * @return
     */
    public boolean checkRecipeLike(String username, String _id, String type){
        //accessing neo4j and check the relationship between the user and the recipe entity
        try (Session session = Neo4jDriver.getObject().getDriver().session()) {
            return session.readTransaction((TransactionWork<Boolean>) tx -> {
                    Result res=null;
                    if(type.equals("recipe"))
                        res=tx.run("MATCH (u:User {username: $username })-[rel:LIKES]->(r:Recipe {id:$_id}) " +
                                        "return rel",
                                parameters("username", username, "_id", _id));
                    else
                        res=tx.run("MATCH (u:User {username: $username })-[rel:LIKES]->(d:Drink {id:$_id}) " +
                                        "return rel",
                                parameters("username", username, "_id", _id));
                    if ((res.hasNext())) {
                        return true;
                    }
                    return false;
            });
        }
        catch(Neo4jException ne){ //somethind goes wrong, a software will parse and solve
            LogManager.getLogger("UserDao.class").error("Neo4j: like check failed");
            return false;
        }
    }

    /***
     * check if user has saved the recipe/drink with the specified id, type refers to recipe/drink
     * @param username
     * @param _id
     * @return
     */
    public boolean checkSavedRecipe(String username, String _id, String type){
        boolean ret=false;
        //accessing mongodb and check the recipe's array of the user
        MongoCollection coll=MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS);
        try(MongoCursor<Document> cursor  =coll.find(eq("_id", username)).cursor();){
            Document doc = cursor.next();
            List<Document> recipes;
            if(type.equals("recipe")) {
                recipes = (List<Document>) doc.get("recipes");
            }
            else {
                recipes = (List<Document>) doc.get("drinks");
            }
            for(int i=0; i<recipes.size(); i++){
                if(recipes.get(i).getString("_id").equals(_id))
                    ret=true;
            }
        }
        return ret;
    }

    /***
     * removing the nested recipe/drink (_id) on user's entity in mongodb
     * @param user
     * @param _id
     * @return
     */
    public String removeNestedRecipe(String user, String _id, String type){
        MongoCollection collUser;
        try {
            //deleting nested recipe from user entity
            collUser = MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS);
            if(type.equals("recipe"))
                collUser.updateOne(eq("_id", user), Updates.pull("recipes", eq("_id", _id)));
            else
                collUser.updateOne(eq("_id", user), Updates.pull("drinks", eq("_id", _id)));
        } catch (MongoException me) {
            me.printStackTrace();
            LogManager.getLogger("UserDao.class").error("MongoDB: deletion of nested recipe in user failed");
            return "Abort";
        }
        return "RecipeOk";
    }

    /***
     * Get nested recipe from user
     * @param user
     * @return List<Recipe>
     */
    public List<Document> getUserRecipe(String user){
        List<Document> returnRecipeList = new ArrayList<>();
        Bson filter = Filters.in("_id", user);
        Document doc;
        try {
            MongoCursor<Document> cursor = MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS).find(filter).iterator();
            doc = cursor.next();
        }catch(MongoException ex){
            LogManager.getLogger("UserDao.class").error("MongoDB: an error occured in getting nested recipe.");
            return null;
        }
        returnRecipeList = (List<Document>) doc.get("recipes");
        return returnRecipeList;
    }


    public String followUser(String myName, String userName, String btnStatus) {

        if(btnStatus.equals("Follow")) {
            try (Session session = Neo4jDriver.getObject().getDriver().session()) {
                System.out.println();
                session.writeTransaction((TransactionWork<Void>) tx -> {
                    tx.run("MATCH (uu:User) WHERE uu.username = $myName" +
                                    " MATCH (uu2:User) WHERE uu2.username = $userName" +
                                    " CREATE (uu)-[rel:FOLLOWS {since:$date}]->(uu2)",
                            parameters("myName", myName, "userName", userName, "date", java.time.LocalDate.now().toString()));
                    return null;
                });
            } catch (Neo4jException ne) {
                ne.printStackTrace();
                LogManager.getLogger("RecipeDao.class").error("Neo4j: follow's relation insert failed");
                return "Abort";
            }
            return "followOk";
        }else{
            try (Session session = Neo4jDriver.getObject().getDriver().session()){
                session.writeTransaction((TransactionWork<Void>) tx -> {
                    tx.run("MATCH (uu:User {username:$myName})-[rel:FOLLOWS]->(uu2:User {username:$userName}) delete rel",
                            parameters("myName", myName, "userName", userName));
                    return null;
                });
            }catch(Neo4jException ne){
                LogManager.getLogger("RecipeDao.class").error("Neo4j: like's relation deletion failed");
                return "Abort";
            }
            return "followDelOk";
        }
    }

    public boolean checkUserFollow(String myName, String userName) {
        //accessing neo4j and check the relationship between the user and the recipe entity
        try (Session session = Neo4jDriver.getObject().getDriver().session()) {
            return session.readTransaction((TransactionWork<Boolean>) tx -> {
                Result res = tx.run("MATCH (u:User {username: $myName })-[rel:FOLLOWS]->(u2:User {username: $userName}) " +
                                "return rel",
                        parameters("myName", myName, "userName", userName));
                if ((res.hasNext())) {
                    return true;
                }
                return false;
            });
        } catch (Neo4jException ne) { //somethind goes wrong, a software will parse and solve
            LogManager.getLogger("UserDao.class").error("Neo4j: follow check failed");
            return false;
        }
    }

}
