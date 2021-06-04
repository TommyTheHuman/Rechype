package it.unipi.dii.inginf.lsmdb.rechype.recipe;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.InsertOneResult;
import com.oath.halodb.HaloDB;
import com.oath.halodb.HaloDBException;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.HaloDBDriver;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.MongoDriver;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.Neo4jDriver;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.Value;
import org.neo4j.driver.exceptions.Neo4jException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.descending;
import static org.neo4j.driver.Values.parameters;

public class RecipeDao {

    /***
     * Adding a new recipe and creating the relations with its ingredients
     * @param doc
     * @return
     */
    public String addRecipe(Document doc){

        boolean already_tried = false;
        MongoCollection<Document> coll = null;
        InsertOneResult res = null;
        String id = null;
        while(true){
            // Add recipe to mongoDB
            try {
                if(!already_tried){
                    coll = MongoDriver.getObject().getCollection(MongoDriver.Collections.RECIPES);

                    //retrieving the inserted id for the current doc to store it in the key-value
                    res = coll.insertOne(doc);
                    id = res.getInsertedId().toString();
                    id = id.substring(19, id.length()-1);
                    doc.append("_id", id);
                } else {
                    coll = MongoDriver.getObject().getCollection(MongoDriver.Collections.RECIPES);
                    coll.deleteOne(eq("_id", res.getInsertedId()));
                    return "Abort";
                }
            } catch (MongoException me) {
                if(!already_tried) { //first time error
                    LogManager.getLogger("RecipeDao.class").error("MongoDB: recipe insert failed");
                    return "Abort";
                }else{ //second time error, consistency adjustment
                    LogManager.getLogger("RecipeDao.class").error("MongoDB[PARSE], recipe inconsistency: " + doc.toJson());
                    return "Abort";
                }
            }

            // Add recipes to neo4j along with the relation with ingredients and owner (the user)
            try (Session session = Neo4jDriver.getObject().getDriver().session()) { //try to add
                String Neo4jId = id;

                session.writeTransaction((TransactionWork<Void>) tx -> {
                    //creating the strings of the queries for adding all the ingredient's relation
                    String totalQueryMatch="";
                    String totalQueryCreate="";
                    JSONArray ingredientsJson=new JSONObject(doc.toJson()).getJSONArray("ingredients");
                    for(int i=0; i<ingredientsJson.length(); i++){
                        totalQueryMatch=totalQueryMatch + "MATCH(i"+i+":Ingredient) WHERE i"+i+".id=\""
                        +ingredientsJson.getJSONObject(i).getString("ingredient")+"\" ";
                        totalQueryCreate=totalQueryCreate + "CREATE (ee)-[:CONTAINS]->(i"+i+") ";
                    }
                    tx.run(
                            "MATCH (u:User) WHERE u.username=$owner " +
                            totalQueryMatch+
                            "CREATE (ee:Recipe { id:$id, name: $name, author: $author, pricePerServing: $pricePerServing, imageUrl: $imageUrl," +
                            "vegetarian: $vegetarian, vegan: $vegan, dairyFree: $dairyFree, glutenFree: $glutenFree} ) " +
                            "CREATE (u)-[rel:OWNS {since:date($date)}]->(ee) "+
                            totalQueryCreate,
                            parameters("id", Neo4jId,"name", doc.getString("name"), "author", doc.getString("author"), "pricePerServing", doc.get("pricePerServing"),
                            "imageUrl", doc.getString("image"), "vegetarian", doc.getBoolean("vegetarian"), "vegan", doc.getBoolean("vegan"), "dairyFree", doc.getBoolean("dairyFree"),
                            "glutenFree", doc.getBoolean("glutenFree"), "owner", doc.getString("author"), "date", java.time.LocalDate.now().toString()));

                    return null;
                });
                return "RecipeAdded";
            }catch(Neo4jException ne){ //fail, next cycle try to delete on MongoDB
                ne.printStackTrace();
                LogManager.getLogger("RecipeDao.class").error("Neo4j: recipe insert failed");
                already_tried=true;
            }
        }
    }

    public List<Recipe> getRecipesByText(String recipeName, int offset, int quantity, JSONObject filters){

        //create the case Insensitive pattern and perform the mongo query
        List<Bson> filtersList = new ArrayList<>();
        List<Recipe> returnList = new ArrayList<>();
        List<Document> returnDocList = new ArrayList<>();
        Pattern pattern = Pattern.compile(".*" + recipeName + ".*", Pattern.CASE_INSENSITIVE);
        Bson regexName = Filters.regex("name", pattern);
        filtersList.add(regexName);
        if(filters.getBoolean("GlutenFree")){
            Bson glutenFilter = Filters.eq("glutenFree", true);
            filtersList.add(glutenFilter);
        }
        if(filters.getBoolean("DairyFree")){
            Bson dairyFilter = Filters.eq("dairyFree", true);
            filtersList.add(dairyFilter);
        }
        if(filters.getBoolean("Vegetarian")){
            Bson vegetarianFilter = Filters.eq("vegetarian", true);
            filtersList.add(vegetarianFilter);
        }
        if(filters.getBoolean("Vegan")){
            Bson veganFilter = Filters.eq("vegan", true);
            filtersList.add(veganFilter);
        }

        if(filters.has(("Price"))) {
            Bson priceFilter = Filters.lte("pricePerServing", Recipe.symbolToPrice(filters.getString("Price")));
            filtersList.add(priceFilter);
        }
        MongoCursor<Document> recipeCursor;
        if(filters.getBoolean("RecipeSort")) {
            recipeCursor = MongoDriver.getObject().getCollection(MongoDriver.Collections.RECIPES)
                    .find(Filters.and(filtersList)).sort(Sorts.orderBy(Sorts.descending("likes"))).skip(offset).limit(quantity).iterator();
        }else{
            recipeCursor = MongoDriver.getObject().getCollection(MongoDriver.Collections.RECIPES)
                    .find(Filters.and(filtersList)).skip(offset).limit(quantity).iterator();
        }

        while (recipeCursor.hasNext()){
            Document doc = recipeCursor.next();
            Recipe recipe = new Recipe(doc);
            returnList.add(recipe);
            returnDocList.add(doc);
        }
        cacheSearch(returnDocList);
        return returnList;
    }

    public JSONObject getRecipeByKey(String key){
        try{
            byte[] byteObj = HaloDBDriver.getObject().getData("recipe", key.getBytes(StandardCharsets.UTF_8));
            return new JSONObject(new String(byteObj));
        }catch(HaloDBException ex){
            LogManager.getLogger("RecipeDao.class").fatal("HaloDB: caching failed");
            HaloDBDriver.getObject().closeConnection();
            System.exit(-1);
        }
        return new JSONObject();
    }

    public void cacheSearch(List<Document> recipesList){ //caching of recipe's search
        for(int i=0; i<recipesList.size(); i++) {
            String idObj = new JSONObject(recipesList.get(i).toJson()).getJSONObject("_id").getString("$oid");
            byte[] _id = idObj.getBytes(StandardCharsets.UTF_8); //key
            byte[] objToSave = recipesList.get(i).toJson().getBytes(StandardCharsets.UTF_8); //value
            try {
                HaloDBDriver.getObject().addData("recipe", _id, objToSave);
            }catch(Exception e){
                LogManager.getLogger("RecipeDao.class").fatal("HaloDB: caching failed");
                HaloDBDriver.getObject().closeConnection();
                System.exit(-1);
            }
        }
    }

    public void cacheAddedRecipe(Document doc){
        String idObj = doc.getString("_id");
        byte[] _id = idObj.getBytes(StandardCharsets.UTF_8); //key
        byte[] objToSave = doc.toJson().getBytes(StandardCharsets.UTF_8); //value
        try {
            HaloDBDriver.getObject().addData("recipe", _id, objToSave);
        }catch(Exception e){
            LogManager.getLogger("RecipeDao.class").fatal("HaloDB: caching failed");
            HaloDBDriver.getObject().closeConnection();
            System.exit(-1);
        }
    }

    public String addLike(String _id, String user){

        boolean already_tried = false;
        //cross-db consistency between neo4j and mongodb
        //the while will execute 2 iteration at most
        while(true){

            //_id in neo4j is the oid field in mongodb
            if(!already_tried) { //try to add to neo4j
                try (Session session = Neo4jDriver.getObject().getDriver().session()){
                    session.writeTransaction((TransactionWork<Void>) tx -> {
                        tx.run("MATCH (uu:User) WHERE uu.username = $username" +
                                        " MATCH (rr:Recipe) WHERE rr.id = $_id" +
                                        " CREATE (uu)-[rel:LIKES {since:date($date)}]->(rr)",
                                parameters("username", user, "_id", _id, "date", java.time.LocalDate.now().toString()));
                        return null;
                    });
                }catch(Neo4jException ne){
                    LogManager.getLogger("RecipeDao.class").error("Neo4j: like's relation insert failed");
                    return "Abort";
                }
            }
            //second try consists in deleting the relation from neo4j
            else{
                //try to delete the relation from neo4j in case the operation on mongo fails
                try (Session session = Neo4jDriver.getObject().getDriver().session()){
                    session.writeTransaction((TransactionWork<Void>) tx -> {
                        tx.run("MATCH (uu:User {username:$username})-[rel:LIKES]->(r:Recipe {id:$_id}) delete rel",
                                parameters("username", user, "_id", _id));
                        return null;
                    });
                }catch(Neo4jException ne){
                    LogManager.getLogger("RecipeDao.class").error("Neo4j[PARSE], like add inconsistency: _id: "+
                            _id+" username: "+user);
                    return "Abort";
                }
            }
            MongoCollection<Document> recipeColl=null;
            //try to add the redundancy on mongodb
            try {
                ObjectId objectId = new ObjectId(_id);
                recipeColl=MongoDriver.getObject().getCollection(MongoDriver.Collections.RECIPES);
                recipeColl.updateOne(eq("_id", objectId), Updates.inc("likes", 1));
                //the database are perfectly consistent
                return "LikeOk";
            }catch (MongoException me){
                LogManager.getLogger("RecipeDao.class").error("MongoDB: failed to insert like in recipes");
                already_tried=true;
            }
        }
    }

    public String removeLike(String _id, String username){
        boolean already_tried = false;
        //cross-db consistency between neo4j and mongodb
        //the while will execute 2 iteration at most
        while(true){

            //_id in neo4j is the oid field in mongodb
            if(!already_tried) { //try to delete on neo4j
                try (Session session = Neo4jDriver.getObject().getDriver().session()){
                    session.writeTransaction((TransactionWork<Void>) tx -> {
                        tx.run("MATCH (uu:User {username:$username})-[rel:LIKES]->(r:Recipe {id:$_id}) delete rel",
                                parameters("username", username, "_id", _id));
                        return null;
                    });
                }catch(Neo4jException ne){
                    LogManager.getLogger("RecipeDao.class").error("Neo4j: like's relation deletion failed");
                    return "Abort";
                }
            }
            //second try consists in deleting the relation from neo4j
            else{
                //try to add again the relation to neo4j in case the operation on mongo fails
                try (Session session = Neo4jDriver.getObject().getDriver().session()){
                    session.writeTransaction((TransactionWork<Void>) tx -> {
                        tx.run("MATCH (uu:User) WHERE uu.username = $username" +
                                        " MATCH (rr:Recipe) WHERE rr.id = $_id" +
                                        " CREATE (uu)-[rel:LIKES {since:date($date)}]->(rr)",
                                parameters("username", username, "_id", _id));
                        return null;
                    });
                    return "Abort";
                }catch(Neo4jException ne){
                    LogManager.getLogger("RecipeDao.class").error("Neo4j[PARSE], like delete inconsistency: _id: "+
                            _id+" username: "+username);
                    return "Abort";
                }
            }
            MongoCollection<Document> recipeColl=null;
            //try to update the redundancy on mongodb
            try {
                ObjectId objectId = new ObjectId(_id);
                recipeColl=MongoDriver.getObject().getCollection(MongoDriver.Collections.RECIPES);
                recipeColl.updateOne(eq("_id", objectId), Updates.inc("likes", -1));
                //the database are perfectly consistent
                return "LikeOk";
            }catch (MongoException me){
                LogManager.getLogger("RecipeDao.class").error("MongoDB: failed to delete like in recipes");
                already_tried=true;
            }
        }
    }


    public Document getRecipeById(String id) {

        Document recipe = new Document();
        try{
            MongoCursor<Document> cursor  = MongoDriver.getObject().getCollection(MongoDriver.Collections.RECIPES).find(eq("_id", new ObjectId(id))).iterator();
            if(cursor.hasNext()){
                recipe = cursor.next();
            }
        }catch (MongoException ex){
            LogManager.getLogger("RecipeDao.class").error("MongoDB: failed to get recipe by id");
        }

        return recipe;

    }

    /***
     * GLOBAL SUGGESTION
     * Retrieving best recipes: The recipes that have obtained more likes in the week
     * @return
     */
    public List<Document> getBestRecipes() {
        List<Document> recipes = new ArrayList<>();
        String todayDate = java.time.LocalDate.now().toString();
        try (Session session = Neo4jDriver.getObject().getDriver().session()) {
            session.readTransaction((TransactionWork<Void>) tx -> {
                Result res = tx.run(
                        "MATCH (:User)-[likes:LIKES]->(r:Recipe) " +
                                "WHERE date($date)-duration({days:7})<likes.since<=date($date)+duration({days:7}) " +
                                "return r AS RecipeNode, count(likes) AS totalLikes " +
                                "ORDER BY totalLikes DESC, RecipeNode.name ASC LIMIT 10",
                        parameters("date", todayDate));
                while (res.hasNext()) {
                    //building each recipe's document
                    Value recipe = res.next().get("RecipeNode");
                    Document doc = new Document();
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
            System.out.println("Neo4j was not able to retrieve the recipe's " +
                    "global suggestions");
        }
        return recipes;
    }


    // ANALYTICS

    // Get user with the higher number of likes by category if specified

    public List<Document> getRankingUserByLikeAndCategory(String category){
        MongoCollection<Document> collRecipe = MongoDriver.getObject().getCollection(MongoDriver.Collections.RECIPES);
        List<Bson> filters = new ArrayList<>();
        filters.add(nin("author", "Spoonacular", "PunkAPI", "CocktailDB"));
        if(category.equals("vegan")){
            filters.add(eq("vegan", true));
        }
        if(category.equals("vegetarian")){
            filters.add(eq("vegetarian", true));
        }
        if(category.equals("dairyFree")){
            filters.add(eq("dairyFree", true));
        }
        if(category.equals("glutenFree")){
            filters.add(eq("glutenFree", true));
        }

        Bson match = match(and(filters));
        Bson group = group("$author", sum("likes", "$likes"));
        Bson sort = sort(descending("likes"));
        Bson project = project(fields(excludeId(), computed("author", "$_id"), include("likes")));
        Bson limit = limit(20);

        List<Document> results = null;
        try{
            results = collRecipe.aggregate(Arrays.asList(match, group, sort, limit, project)).into(new ArrayList<>());
        } catch (MongoException ex){
            LogManager.getLogger("RecipeDao.class").error("MongoDB: fail analytics: Ranking user by like and category");
        }

        return results;
    }


    public List<Document> getUserRankingByLikeNumber(int minAge, int maxAge, String country) {
        MongoCollection<Document> collRecipe = MongoDriver.getObject().getCollection(MongoDriver.Collections.RECIPES);
        List<Bson> stages = new ArrayList<>();
        List<Bson> filters = new ArrayList<>();

        //LookUp stage --> attaches a user doc to a recipe doc
        stages.add(lookup("users", "author", "_id", "user"));

        if(minAge != -1){
            filters.add(lte("user.age", maxAge));
            filters.add(gte("user.age", minAge));
        }
        if(!country.equals("noCountry")) {
            filters.add(eq("user.country", country));
        }
        if(filters.size() > 0) {
            // MATCH on Age range and/or Country
            Bson match = match(and(filters));
            stages.add(match);
        }

        //unwind stage
        stages.add(unwind("$user"));

        //group stage
        stages.add(group("$user._id", sum("likes", "$likes")));

        stages.add(sort(descending("likes")));

        stages.add(limit(20));

        List<Document> results = null;
        try{
            results = collRecipe.aggregate(stages).into(new ArrayList<>());
        } catch (MongoException ex){
            ex.printStackTrace();
            LogManager.getLogger("RecipeDao.class").error("MongoDB: fail analytics: Ranking user by like's number");
        }

        return results;
    }

    public List<Document> getIngredientRanking(String nutrient, int minutes) {
        MongoCollection<Document> collRecipe = MongoDriver.getObject().getCollection(MongoDriver.Collections.RECIPES);
        List<Bson> stages = new ArrayList<>();
        List<Bson> filters = new ArrayList<>();

        if(minutes != -1){
            stages.add(match(lte("readyInMinutes", minutes)));
        }
        stages.add(unwind("$ingredients"));

        if(!nutrient.equals("noNutrient")) {

            stages.add(unwind("$nutrients"));

            filters.add(eq("nutrients.name", nutrient));
            int num;
            if(nutrient.equals("Fat")){
                num = 15;
                filters.add(lte("nutrients.amount", num));
            }
            if(nutrient.equals("Calories")){
                num = 250;
                filters.add(lte("nutrients.amount", num));
            }
            if(nutrient.equals("Protein")){
                num = 100;
                filters.add(gte("nutrients.amount", num));
            }
            stages.add(match(and(filters)));
        }

        stages.add(group("$ingredients.ingredient", sum("count", 1)));
        stages.add(sort(descending("count")));
        if(nutrient.equals("noNutrient")) {
           stages.add(skip(50));
        }
        stages.add(limit(20));
        List<Document> results = null;
        try{
            results = collRecipe.aggregate(stages).into(new ArrayList<>());
        } catch (MongoException ex){
            ex.printStackTrace();
            LogManager.getLogger("RecipeDao.class").error("MongoDB: fail analytics: Ranking user by level and healthScore");
        }

        return results;
    }
}
