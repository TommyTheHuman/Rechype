package it.unipi.dii.inginf.lsmdb.rechype.user;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.oath.halodb.HaloDB;
import com.oath.halodb.HaloDBException;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.HaloDBDriver;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.MongoDriver;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.Neo4jDriver;
import org.apache.logging.log4j.LogManager;
import org.bson.BsonArray;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.Neo4jException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Accumulators.first;
import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.inc;
import static org.neo4j.driver.Values.parameters;

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
                    tx.run("CREATE (ee:User { username: $username, country: $country, level: $level , age:$age})",
                    parameters("username", username, "country", country, "level", 0, "age", age));
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
     * retrieving the user's entity by mongodb using the _id (username)
     * @param id
     * @return
     */
    public Document getUserById(String id){
        Document user;
        MongoCursor<Document> cursor  = MongoDriver.getObject()
                .getCollection(MongoDriver.Collections.USERS)
                .find(eq("_id", id)).iterator();

        user = cursor.next();

        return user;
    }

    /***
     * retrieve the users from mongoDb and cache the search into the key-value DB
     * @param userName
     * @param offset
     * @param quantity
     * @return
     */
    public List<User> getUsersByText(String userName, int offset, int quantity, JSONObject filters){
        //List of users to build gui and list of documents to caching
        List<User> returnList = new ArrayList<>();
        List<Document> returnDocList = new ArrayList<>();

        List<Bson> listFilters = new ArrayList<>();
        //create the case insensitive pattern and perform the mongo query
        Pattern pattern = Pattern.compile(".*" + userName + ".*", Pattern.CASE_INSENSITIVE);
        Bson nameRegex = Filters.regex("_id", pattern);
        listFilters.add(nameRegex);
        if(filters.has("Age")){
            Bson ageFilter = Filters.gte("age", Integer.parseInt(filters.getString("Age")));
            listFilters.add(ageFilter);
        }
        if(filters.has("Level")){
            Bson lvlFilter = Filters.gte("level", User.levelToInt(filters.getString("Level")));
            listFilters.add(lvlFilter);
        }



        MongoCursor<Document> cursor  = MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS).find(Filters.and(listFilters))
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
            byte[] byteObj = HaloDBDriver.getObject().getData("user", key.getBytes(StandardCharsets.UTF_8));
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
                HaloDBDriver.getObject().addData("user", username, objToSave);
            }catch(Exception e){
                LogManager.getLogger("UserDao.class").fatal("HaloDB: caching failed");
                HaloDBDriver.getObject().closeConnection();
                System.exit(-1);
            }
        }
    }

    /***
     * simple delete of a user's entity from both databases (cross-db consistency)
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
        }catch(MongoException ex){ //something goes wrong, a software will parse and solve
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
        }catch(Neo4jException ne){ //something goes wrong, a software will parse and solve
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
            //increasing the level if the recipe is created and not added to favourites
            if(user.getUsername().equals(recipe.getString("author")))
                listUpdates.add(Updates.inc("level", 1));
            collUser.updateOne(find, Updates.combine(listUpdates));
        } catch (MongoException me) {
            me.printStackTrace();
            if(recipe.getBoolean("NoParse")==null) //if no parse is needed the log specify it
                LogManager.getLogger("UserDao.class").error("MongoDB[PARSE]: insert nested recipe(creation) in user failed" + recipe.toJson());
            else
                LogManager.getLogger("UserDao.class").error("MongoDB: insert nested recipe(favourite) in user failed");
            return "Abort";
        }

        //if the recipe is created by the user the level on neo4j entity is updated
        if(user.getUsername().equals(recipe.getString("author"))){
            try (Session session = Neo4jDriver.getObject().getDriver().session()) { //try to add
                session.writeTransaction((TransactionWork<Void>) tx -> {
                    tx.run(
                            "MATCH (u:User) WHERE u.username=$username " +
                                "SET u.level=u.level+1 ",
                    parameters("username", user.getUsername()));
                    return null;
                });
            }catch(Neo4jException ne){
                LogManager.getLogger("UserDao.class").error("Neo4j[PARSE]: user level increment failed"+user.getUsername());
            }
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
        catch(Neo4jException ne){ //something goes wrong, a software will parse and solve
            LogManager.getLogger("UserDao.class").error("Neo4j: like check failed");
            return false;
        }
    }

    /***
     * The function removes the user from mongodb and neo4j (cross-db consistency),
     * the user's recipes are removed and all the relations on neo4j are removed (likes to recipes)
     * If the first query on mongo fails (user entity deletion) all the function failed and the ban itself fails.
     * If the second or third query fails is not a problem because the functionalities for the ban's user are interrupted.
     * @param username
     * @return
     */
    public String banUser(String username){
        MongoCollection<Document> coll=null;

        //deleting the user's entity on mongodb
        try {
            coll = MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS);
            DeleteResult result=coll.deleteOne(eq("_id", username));
            if(result.getDeletedCount() == 0)
                return "Abort";
        }catch(MongoException ex){ //deletion failed
            LogManager.getLogger("UserDao.class").error("MongoDB: user ban failed");
            return "Abort";
        }

        //try Neo4j
        try (Session session = Neo4jDriver.getObject().getDriver().session()) {
            List<ObjectId> idRecipesList=new ArrayList<>();
            List<ObjectId> idDrinksList=new ArrayList<>();
            session.writeTransaction((TransactionWork<Void>) tx -> {
                Result res=tx.run(
                    "MATCH (u:User) WHERE u.username=$username "+
                    "OPTIONAL MATCH (u)-[rel1:LIKES]->(r:Recipe) "+
                    "OPTIONAL MATCH (u)-[rel2:LIKES]->(d:Drink) "+
                    "WITH collect(r.id) AS RecipeIds, collect(d.id) AS DrinkIds, u " +
                    "DETACH DELETE u " +
                    "return RecipeIds, DrinkIds",
                parameters("username", username));
                while(res.hasNext()){
                    Record rec=res.next();
                    for(int i=0; i<rec.get("RecipeIds").size();i++)
                        idRecipesList.add(new ObjectId(rec.get("RecipeIds").get(i).asString()));
                    for(int i=0; i<rec.get("DrinkIds").size();i++){
                        idDrinksList.add(new ObjectId(rec.get("DrinkIds").get(i).asString()));
                    }
                }
                return null;
            });

            //update recipe's like and drink's like on mongodb
            try{
                coll = MongoDriver.getObject().getCollection(MongoDriver.Collections.RECIPES);
                List<Bson> filtersList=new ArrayList<>();
                for(int i=0; i<idRecipesList.size(); i++){
                    filtersList.add(Filters.eq("_id", idRecipesList.get(i)));
                }
                coll.updateMany(Filters.in("_id", idRecipesList), inc("likes", -1));
            }catch(MongoException me){
                LogManager.getLogger("UserDao.class").error("MongoDB[PARSE], user ban inconsistency on recipes: "+username);
            }

            try{
                coll = MongoDriver.getObject().getCollection(MongoDriver.Collections.DRINKS);
                List<Bson> filtersList=new ArrayList<>();
                for(int i=0; i<idDrinksList.size(); i++){
                    filtersList.add(Filters.eq("_id", idDrinksList.get(i)));
                }
                coll.updateMany(Filters.in("_id", idDrinksList), inc("likes", -1));
            }catch(MongoException me){
                LogManager.getLogger("UserDao.class").error("MongoDB[PARSE], user ban inconsistency on drinks: "+username);
            }
        }catch(Neo4jException ne){
            ne.printStackTrace();
            //if neo4j query fails the like redundancy on each recipe is not updated so we need to parse
            LogManager.getLogger("UserDao.class").error("MongoDB[PARSE], user ban inconsistency on likes: "+username);
            LogManager.getLogger("UserDao.class").error("Neo4j[PARSE], user ban inconsistency on user deletion: "+username);
        }
        return "BanOk";
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
            LogManager.getLogger("UserDao.class").error("MongoDB: an error occurred in getting nested recipe.");
            return null;
        }
        returnRecipeList = (List<Document>) doc.get("recipes");
        return returnRecipeList;
    }

    public Document getUserRecipeAndDrinks(String user){
        Bson filter = Filters.in("_id", user);
        Document doc;
        try {
            MongoCursor<Document> cursor = MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS).find(filter).iterator();
            doc = cursor.next();
        }catch(MongoException ex){
            LogManager.getLogger("UserDao.class").error("MongoDB: an error occurred in getting nested recipe.");
            return null;
        }
        return doc;
    }


    public String followUser(String myName, String userName, String btnStatus) {

        if(btnStatus.equals("Follow")) {
            try (Session session = Neo4jDriver.getObject().getDriver().session()) {
                System.out.println();
                session.writeTransaction((TransactionWork<Void>) tx -> {
                    tx.run("MATCH (uu:User) WHERE uu.username = $myName" +
                                    " MATCH (uu2:User) WHERE uu2.username = $userName" +
                                    " CREATE (uu)-[rel:FOLLOWS {since:date($date)}]->(uu2)",
                            parameters("myName", myName, "userName", userName, "date", java.time.LocalDate.now().toString()));
                    return null;
                });
            } catch (Neo4jException ne) {
                ne.printStackTrace();
                LogManager.getLogger("RecipeDao.class").error("Neo4j: follows relation insert failed");
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
        } catch (Neo4jException ne) { //something goes wrong, a software will parse and solve
            LogManager.getLogger("UserDao.class").error("Neo4j: follow check failed");
            return false;
        }
    }

    /***
     * SUGGESTION: finding the recipe's with the highest number of likes in the week in the follower set of the user
     * NB: the like that are put from the current user are not considered
     * @param username
     * @return
     */
    public List<Document> getSuggestedRecipes(String username){
        String todayDate=java.time.LocalDate.now().toString();
        List<Document> recipes=new ArrayList<>();
        try (Session session = Neo4jDriver.getObject().getDriver().session()) {
            session.readTransaction((TransactionWork<Void>) tx -> {
                Result res = tx.run(
                        "MATCH (u:User {username: $username })-[rel:FOLLOWS]->(u2:User) " +
                            "MATCH (u2)-[relLikes:LIKES]->(r:Recipe) " +
                            "WHERE date($date)-duration({days:7})<relLikes.since<=date($date)+duration({days:7}) " +
                            "AND r.author<>$username " +
                            "WITH r AS RecipeNode, count(relLikes) AS likesNumber "+
                            "RETURN RecipeNode, sum(likesNumber) as totalLikes "+
                            "ORDER BY totalLikes DESC, RecipeNode.name ASC LIMIT 10",
                        parameters("username", username, "date", todayDate));
                while(res.hasNext()){
                    //building each recipe's document
                    Record rec=res.next();
                    Value recipe=rec.get("RecipeNode");
                    Document doc=new Document();
                    doc.put("author", recipe.get("author").asString());
                    doc.put("dairyFree", recipe.get("dairyFree").asBoolean());
                    doc.put("glutenFree", recipe.get("glutenFree").asBoolean());
                    doc.put("vegan", recipe.get("vegan").asBoolean());
                    doc.put("vegetarian", recipe.get("vegetarian").asBoolean());
                    doc.put("_id", new ObjectId(recipe.get("id").asString()).toString());
                    doc.put("image", recipe.get("imageUrl").asString());
                    doc.put("name", recipe.get("name").asString());
                    doc.put("pricePerServing", recipe.get("pricePerServing").asDouble());
                    recipes.add(doc);
                }
                return null;
            });
        }catch(Neo4jException ne){
            ne.printStackTrace();
            System.out.println("Neo4j was not able to retrieve the recipe's suggestions");
        }
        return recipes;
    }

    /***
     * SUGGESTION: finding the drink's with the highest number of likes in the week in the follower set of the user
     * @param username
     * @return
     */
    public List<Document> getSuggestedDrinks(String username){
        String todayDate=java.time.LocalDate.now().toString();
        List<Document> drinks=new ArrayList<>();
        try (Session session = Neo4jDriver.getObject().getDriver().session()) {
            session.readTransaction((TransactionWork<Void>) tx -> {
                Result res = tx.run(
                        "MATCH (u:User {username: $username })-[rel:FOLLOWS]->(u2:User) " +
                                "MATCH (u2)-[relLikes:LIKES]->(d:Drink) " +
                                "WHERE date($date)-duration({days:7})<relLikes.since<=date($date)+duration({days:7}) " +
                                "AND d.author<>$username " +
                                "WITH d AS DrinkNode, count(relLikes) AS likesNumber "+
                                "RETURN DrinkNode, sum(likesNumber) as totalLikes "+
                                "ORDER BY totalLikes DESC, DrinkNode.name ASC LIMIT 10",
                        parameters("username", username, "date", todayDate));
                while(res.hasNext()){
                    //building each recipe's document
                    Value drink=res.next().get("DrinkNode");
                    Document doc=new Document();
                    doc.put("author", drink.get("author").asString());
                    doc.put("_id", new ObjectId(drink.get("id").asString()).toString());
                    doc.put("image", drink.get("imageUrl").asString());
                    doc.put("name", drink.get("name").asString());
                    doc.put("tag", drink.get("tag").asString());
                    drinks.add(doc);
                }
                return null;

            });
        }catch(Neo4jException ne){
            ne.printStackTrace();
            System.out.println("Neo4j was not able to retrieve the drink's suggestions");
        }
        return drinks;
    }

    /***
     * retrieving the users followed by the user's followed and returning the best ones (users with the highest number
     * of followers)
     * @param username
     * @return
     */
    public List<Document> getSuggestedUsers(String username) {
        List<Document> users = new ArrayList<>();
        try (Session session = Neo4jDriver.getObject().getDriver().session()) {
            session.readTransaction((TransactionWork<Void>) tx -> {
                Result res = tx.run(
                        "MATCH (u1:User)-[:FOLLOWS]->(:User)-[:FOLLOWS]->(newUser:User) " + //matching all the followed of the followed of the user
                                "WHERE u1.username=$username AND newUser.username<>$username AND" +
                                "(NOT ((u1)-[:FOLLOWS]->(newUser))) " + //excluding the already followed
                                "MATCH (:User)-[rel:FOLLOWS]->(newUser) " + //counting the number of new user's followers*/
                                "return newUser, count(rel) AS totalFollowers "+ //...
                                "ORDER BY totalFollowers DESC, newUser.username ASC LIMIT 10",//...
                        parameters("username", username));
                while (res.hasNext()) {
                    Record rec=res.next();
                    Value user = rec.get("newUser");
                    Document doc = new Document();
                    doc.put("country", user.get("country").asString());
                    doc.put("level", user.get("level").asInt());
                    doc.put("_id", user.get("username").asString());
                    doc.put("age", user.get("age").asInt());
                    users.add(doc);
                }
                return null;
            });
        }catch(Neo4jException ne){
            ne.printStackTrace();
            System.out.println("Neo4j was not able to retrieve the user's suggestions");
        }
        return users;
    }

    /***
     * GLOBAL SUGGESTION
     * retrieving "best users": the user's that have created the highest number of recipes/drink in the week and that have
     * received the highest number of likes on those recipes in the week. The user MUST create at least one recipe and one
     * drink (globally, not in the week) to appear in the "best users".
     * @return
     */
    public List<Document> getBestUsers() {
        String todayDate = java.time.LocalDate.now().toString();
        List<Document> users = new ArrayList<>();
        try (Session session = Neo4jDriver.getObject().getDriver().session()) {
            session.readTransaction((TransactionWork<Void>) tx -> {
                Result res = tx.run(
                        "MATCH (u:User)-[:OWNS]->(r:Recipe) " + //user must have at least one recipe
                                "OPTIONAL MATCH (:User)-[owns1:OWNS]->(r)<-[likes1:LIKES]-(:User) "+ //optional matching on last week's recipes
                                "WHERE date($date)-duration({days:7})<owns1.since<=date($date)+duration({days:7}) " +
                                "WITH u, count(likes1) as RecipesLikes " + //counting likes of last week's recipes
                                "MATCH (u)-[:OWNS]->(d:Drink) " + //same
                                "OPTIONAL MATCH (:User)-[owns2:OWNS]->(d)<-[likes2:LIKES]-(:User) " +
                                "WHERE date($date)-duration({days:7})<owns2.since<=date($date)+duration({days:7}) " +
                                "WITH u, count(likes2)+RecipesLikes as totalLikes " + //counting total likes of recipes and drinks for a user
                                "return u AS User, totalLikes " +
                                "ORDER BY totalLikes DESC, User.username ASC LIMIT 10",
                        parameters("date", todayDate));
                while (res.hasNext()) {
                    Record rec = res.next();
                    Value user = rec.get("User");
                    Document doc = new Document();
                    doc.put("country", user.get("country").asString());
                    doc.put("level", user.get("level").asInt());
                    doc.put("_id", user.get("username").asString());
                    doc.put("age", user.get("age").asInt());
                    users.add(doc);
                }
                return null;
            });

        } catch (Neo4jException ne) {
            ne.printStackTrace();
            System.out.println("Neo4j was not able to retrieve user's global suggestion");
        }
        return users;
    }

    public List<Document> getHealthRankByLevel(String level){
        MongoCollection<Document> collRecipe = MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS);
        List<Bson> stages = new ArrayList<>();
        List<Bson> filters = new ArrayList<>();
        if(!level.equals("noLevel")) {
            int lvl = User.levelToInt(level);
            filters.add(gte("level", lvl));
        }
        if(filters.size() > 0){
            stages.add(match(and(filters)));
        }

        stages.add(lookup("recipes", "_id", "author", "recipe"));

        stages.add(unwind("$recipe"));

        stages.add(unwind("$recipe.nutrients"));

        stages.add(match(eq("recipe.nutrients.name", "Calories")));

        stages.add(match(gt("recipe.nutrients.amount", 0)));

        Document healthIndex = Document.parse("{$divide:[\"$recipe.weightPerServing\", \"$recipe.nutrients.amount\"]}");

        stages.add(project(fields(
                excludeId(),
                include("_id"),
                computed("healthIndex", healthIndex)
                )
        ));

        stages.add(match(lte("healthIndex", 0.8)));

        stages.add(group("$_id", sum("count", 1)));

        stages.add(sort(descending("count")));

        List<Document> results = null;
        try{
            results = collRecipe.aggregate(stages).into(new ArrayList<>());
        } catch (MongoException ex){
            ex.printStackTrace();
            LogManager.getLogger("RecipeDao.class").error("MongoDB: fail analytics: Ranking user by level and healthScore");
        }

        return results;
    }



    public List<Document> getDrinkRecipe(String username) {

        List<Document> returnDrinkList = new ArrayList<>();
        Bson filter = Filters.in("_id", username);
        Document doc;
        try {
            MongoCursor<Document> cursor = MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS).find(filter).iterator();
            doc = cursor.next();
        }catch(MongoException ex){
            LogManager.getLogger("UserDao.class").error("MongoDB: an error occurred in getting nested recipe.");
            return null;
        }
        returnDrinkList = (List<Document>) doc.get("drinks");
        return returnDrinkList;


    }

    public List<Document> mostSavedRecipes(String category) {
        MongoCollection<Document> collRecipe = MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS);
        List<Bson> stages = new ArrayList<>();
        List<Bson> filters = new ArrayList<>();

        if(category.equals("drinks")){
            stages.add(unwind("$drinks"));
            stages.add(group("$drinks._id", first("name", "$drinks.name"), sum("count", 1)));
        }else{
            stages.add(unwind("$recipes"));
            stages.add(group("$recipes._id", first("name", "$recipes.name"), sum("count", 1)));
        }

        stages.add(sort(descending("count")));
        stages.add(project(fields(excludeId(), include("name"), include("count"))));

        List<Document> results = null;
        try{
            results = collRecipe.aggregate(stages).into(new ArrayList<>());
        } catch (MongoException ex){
            ex.printStackTrace();
            LogManager.getLogger("RecipeDao.class").error("MongoDB: fail analytics: Ranking user by like's number");
        }

        return results;
    }
}
