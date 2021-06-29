package it.unipi.dii.inginf.lsmdb.rechype.recipe;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.InsertOneResult;
import com.oath.halodb.HaloDB;
import com.oath.halodb.HaloDBException;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.IngredientService;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.IngredientServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.HaloDBDriver;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.MongoDriver;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.Neo4jDriver;
import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import org.apache.commons.io.IOUtils;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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

            // Add recipe to neo4j
            try (Session session = Neo4jDriver.getObject().getDriver().session()) { //try to add
                String Neo4jId = id;

                session.writeTransaction((TransactionWork<Void>) tx -> {
                    tx.run(
                            "MATCH (u:User) WHERE u.username=$owner " +
                            "CREATE (ee:Recipe { id:$id, name: $name } ) " +
                            "CREATE (u)-[rel:OWNS {since:date($date)}]->(ee) ",
                            parameters("id", Neo4jId,"name", doc.getString("name"),
                            "owner", doc.getString("author"), "date", java.time.LocalDate.now().toString()));

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

    /***
     * retrieve a list of recipes with different filters
     * @param recipeName
     * @param offset
     * @param quantity
     * @param filters
     * @return
     */
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

            Bson lvlFilter1;
            Bson lvlFilter2;
            if(Recipe.symbolToPrice(filters.getString("Price"))==0) {
                lvlFilter1 = Filters.gte("pricePerServing", Recipe.symbolToPrice(filters.getString("Price")));
            }else {
                lvlFilter1=Filters.gt("pricePerServing", Recipe.symbolToPrice(filters.getString("Price")));
            }
            filtersList.add(lvlFilter1);
            if(Recipe.symbolToPrice(filters.getString("Price"))!=1500) {
                lvlFilter2 = Filters.lte("pricePerServing", Recipe.symbolToPrice(filters.getString("Price")) + 500);
                filtersList.add(lvlFilter2);
            }
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

    /***
     * get recipe by key
     * @param key
     * @return
     */
    public byte[] getImgByKey(String key){
        try{
            byte[] byteObj = HaloDBDriver.getObject().getData("recipe", key.getBytes(StandardCharsets.UTF_8));
            return byteObj;
        }catch(HaloDBException ex){
            LogManager.getLogger("RecipeDao.class").fatal("HaloDB: caching failed");
            HaloDBDriver.getObject().closeConnection();
            System.exit(-1);
        }
        return null;
    }

    /***
     * caching the recipe's search in a key-value DB
     * @param recipesList
     */
    public void cacheSearch(List<Document> recipesList){ //caching of recipe's search
        for(int i=0; i<recipesList.size(); i++) {
            cacheAddedRecipe(recipesList.get(i));
        }
    }

    /***
     * Cache the recipe just created
     * @param doc
     */
    public boolean cacheAddedRecipe(Document doc){
        String idObj;
        if(doc.get("_id") instanceof String)
            idObj=doc.getString("_id");
        else
            idObj = new JSONObject(doc.toJson()).getJSONObject("_id").getString("$oid");
        byte[] _id = idObj.getBytes(StandardCharsets.UTF_8); //key
        InputStream imgStream;
        String stringUrl=doc.getString("image");
        byte[] objToSave;
        try {
            imgStream = new URL(stringUrl).openStream();
            objToSave=imgStream.readAllBytes();
            imgStream.close();
        }catch(IOException ie){
            return false;
        }
        try {
            HaloDBDriver.getObject().addData("recipe", _id, objToSave);
            //byte[] c=HaloDBDriver.getObject().getData("recipe", _id);
            return true;
        }catch(HaloDBException ex){
            LogManager.getLogger("RecipeDao.class").fatal("HaloDB: caching failed");
            HaloDBDriver.getObject().closeConnection();
            System.exit(-1);
            return false;
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




    public List<Document> recipeDistributionByPrice() {
        MongoCollection<Document> collRecipe = MongoDriver.getObject().getCollection(MongoDriver.Collections.RECIPES);

        List<Bson> stages = new ArrayList<>();

        Document priceSwitch = Document.parse("{\n" +
                "            $switch: {\n" +
                "              branches: [\n" +
                "                  \t{ case: {$and:[{ $gte: [ \"$pricePerServing\", 0 ] }, { $lte: [ \"$pricePerServing\", 500 ] }]}, then: 1 },\n" +
                "                  \t{ case: {$and:[{ $gt: [ \"$pricePerServing\", 500 ] }, { $lte: [ \"$pricePerServing\", 1000 ] }]}, then: 2 },\n" +
                "\t\t        { case: {$and:[{ $gt: [ \"$pricePerServing\", 1000 ] }, { $lte: [ \"$pricePerServing\", 1500 ] }]}, then: 3 },\n" +
                "\t                { case: {$and:[{ $gt: [ \"$pricePerServing\", 1500 ] }]}, then: 4 }\n" +
                "              ]\n" +
                "            }\n" +
                "          }");

        stages.add(project(fields(
                excludeId(),
                computed("priceRange", priceSwitch)
                )
        ));

        stages.add(group("$priceRange", sum("count", 1)));

        List<Document> results = null;
        try{
            results = collRecipe.aggregate(stages).into(new ArrayList<>());
            //results = collRecipe.aggregate(stages).into(new ArrayList<>());
        } catch (MongoException ex){
            ex.printStackTrace();
            LogManager.getLogger("RecipeDao.class").error("MongoDB: fail analytics: Ranking user by level and healthScore");
        }

        return results;
    }


    public List<Document> mostUsedIngrByCategory(String category){
        MongoCollection<Document> collRecipe = MongoDriver.getObject().getCollection(MongoDriver.Collections.RECIPES);

        List<Bson> stages = new ArrayList<>();
        List<Bson> filters = new ArrayList<>();

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

        stages.add(match(and(filters)));
        stages.add(unwind("$ingredients"));
        stages.add(group("$ingredients.ingredient", sum("count", 1)));
        stages.add(sort(descending("count")));
        stages.add(limit(10));
        List<Document> results = null;
        try{
            results = collRecipe.aggregate(stages).into(new ArrayList<>());
        } catch (MongoException ex){
            ex.printStackTrace();
            LogManager.getLogger("RecipeDao.class").error("MongoDB: fail analytics: Most used ingredient");
        }

        return results;
    }

}
