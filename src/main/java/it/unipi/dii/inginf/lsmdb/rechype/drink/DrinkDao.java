package it.unipi.dii.inginf.lsmdb.rechype.drink;

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
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.descending;
import static org.neo4j.driver.Values.parameters;

class DrinkDao {

    /***
     * Add new drink
     * @param doc
     * @return
     */
    public String addDrink(Document doc){

        boolean already_tried = false;
        MongoCollection<Document> coll = null;
        InsertOneResult res = null;
        String id = null;

        while(true){
            // Add drink to mongoDB
            try {
                if(!already_tried){
                    coll = MongoDriver.getObject().getCollection(MongoDriver.Collections.DRINKS);

                    //retrieving the inserted id for the current doc to store it in the key-value
                    res = coll.insertOne(doc);
                    id = res.getInsertedId().toString();
                    id = id.substring(19, id.length()-1);
                    doc.append("_id", id);
                } else {
                    coll = MongoDriver.getObject().getCollection(MongoDriver.Collections.DRINKS);
                    coll.deleteOne(eq("_id", res.getInsertedId()));
                    return "Abort";
                }
            } catch (MongoException me) {
                if(!already_tried) { //first time error
                    LogManager.getLogger("RecipeDao.class").error("MongoDB: drink insert failed");
                    return "Abort";
                }else{ //second time error, consistency adjustment
                    LogManager.getLogger("RecipeDao.class").error("MongoDB[PARSE], drink inconsistency: " + doc.toJson());
                    return "Abort";
                }
            }

            // Add some fields of recipe in neo4j
            try (Session session = Neo4jDriver.getObject().getDriver().session()) { //try to add
                String Neo4jId = id;
                session.writeTransaction((TransactionWork<Void>) tx -> {
                    //creating the string for adding all the ingredient's relation
                    String totalQueryMatch="";
                    String totalQueryCreate="";
                    JSONArray ingredientsJson=new JSONObject(doc.toJson()).getJSONArray("ingredients");
                    for(int i=0; i<ingredientsJson.length(); i++){
                        totalQueryMatch=totalQueryMatch+"MATCH(i"+i+":Ingredient) WHERE i"+i+".id=\""
                                +ingredientsJson.getJSONObject(i).getString("ingredient")+"\" ";
                        totalQueryCreate=totalQueryCreate+"CREATE (d)-[:CONTAINS]->(i"+i+") ";
                    }
                    tx.run(
                            "MATCH (u:User) WHERE u.username=$owner "+
                            totalQueryMatch+
                            "CREATE (d:Drink { id:$id, name: $name, author: $author, imageUrl: $imageUrl, " +
                            "tag: $tag}) "+
                            "CREATE (u)-[rel:OWNS {since:date($date)}]->(d) "+
                            totalQueryCreate,
                            parameters("id", Neo4jId,"name", doc.getString("name"), "author", doc.getString("author"),
                                    "imageUrl", doc.getString("image"), "tag", doc.getString("tag"), "owner", doc.getString("author"),
                                    "date", java.time.LocalDate.now().toString()));
                    return null;
                });
                return "DrinkAdded";
            }catch(Neo4jException ne){ //fail, next cycle try to delete on MongoDB
                LogManager.getLogger("DrinkDao.class").error("Neo4j: recipe insert failed");
                already_tried=true;
            }
        }
    }

    /***
     * Get a drinks given a part of drink's name
     * @param drinkName
     * @param offset
     * @param quantity
     * @param filters
     * @return
     */
    public List<Drink> getDrinksByText(String drinkName, int offset, int quantity, JSONObject filters){
        //create the case Insensitive pattern and perform the mongo query
        List<Drink> returnList = new ArrayList<>();
        List<Document> returnDocList = new ArrayList<>();
        Pattern pattern = Pattern.compile(".*" + drinkName + ".*", Pattern.CASE_INSENSITIVE);
        Bson nameFilter = Filters.regex("name", pattern);
        List<Bson> listFilters=new ArrayList<>();
        listFilters.add(nameFilter);
        if(filters.has("tag")){
            listFilters.add(Filters.eq("tag", filters.getString("tag")));
        }
        MongoCursor<Document> drinkCursor;
        if(filters.getBoolean("DrinkSort")){
            drinkCursor  = MongoDriver.getObject().getCollection(MongoDriver.Collections.DRINKS)
                    .find(Filters.and(listFilters)).sort(Sorts.orderBy(Sorts.descending("likes"))).skip(offset).limit(quantity).iterator();
        }else {
            drinkCursor = MongoDriver.getObject().getCollection(MongoDriver.Collections.DRINKS)
                    .find(Filters.and(listFilters)).skip(offset).limit(quantity).iterator();
        }
        while(drinkCursor.hasNext()){
            Document doc = drinkCursor.next();
            Drink drink = new Drink(doc);
            returnList.add(drink);
            returnDocList.add(doc);
        }
        cacheSearch(returnDocList);
        return returnList;
    }

    /***
     * caching the drink's search in a key-value DB
     * @param drinksList
     */
    public void cacheSearch(List<Document> drinksList){ //caching of drink's search
        for(int i=0; i<drinksList.size(); i++) {
            String idObj = new JSONObject(drinksList.get(i).toJson()).getJSONObject("_id").getString("$oid");
            byte[] _id = idObj.getBytes(StandardCharsets.UTF_8); //key
            byte[] objToSave = drinksList.get(i).toJson().getBytes(StandardCharsets.UTF_8); //value
            try {
                HaloDBDriver.getObject().addData("drink",_id, objToSave);
            }catch(HaloDBException ex){
                LogManager.getLogger("DrinkDao.class").fatal("HaloDB: caching failed");
                HaloDBDriver.getObject().closeConnection();
                System.exit(-1);
            }
        }
    }

    /***
     * Caching the drink just created
     * @param doc
     */
    public void cacheAddedDrink(Document doc){
        String idObj = doc.getString("_id");
        byte[] _id = idObj.getBytes(StandardCharsets.UTF_8); //key
        byte[] objToSave = doc.toJson().getBytes(StandardCharsets.UTF_8); //value
        try {
            HaloDBDriver.getObject().addData("drink",_id, objToSave);
        }catch(HaloDBException ex){
            LogManager.getLogger("DrinkDao.class").fatal("HaloDB: caching failed");
            HaloDBDriver.getObject().closeConnection();
            System.exit(-1);
        }
    }

    /***
     * Add a new like
     * @param _id
     * @param user
     * @return
     */
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
                                        " MATCH (d:Drink) WHERE d.id = $_id" +
                                        " CREATE (uu)-[rel:LIKES {since:date($date)}]->(d)",
                                parameters("username", user, "_id", _id, "date", java.time.LocalDate.now().toString()));
                        return null;
                    });
                }catch(Neo4jException ne){
                    LogManager.getLogger("DrinkDao.class").error("Neo4j: like's relation insert failed");
                    return "Abort";
                }
            }
            //second try consists in deleting the relation from neo4j
            else{
                //try to delete the relation from neo4j in case the operation on mongo fails
                try (Session session = Neo4jDriver.getObject().getDriver().session()){
                    session.writeTransaction((TransactionWork<Void>) tx -> {
                        tx.run("MATCH (uu:User {username:$username})-[rel:LIKES]->(d:Drink {id:$_id}) delete rel",
                                parameters("username", user, "_id", _id));
                        return null;
                    });
                }catch(Neo4jException ne){
                    LogManager.getLogger("DrinkDao.class").error("Neo4j[PARSE], like add inconsistency: _id: "+
                            _id+" username: "+user);
                    return "Abort";
                }
            }
            MongoCollection<Document> drinkColl=null;
            //try to add the redundancy on mongodb
            try {
                ObjectId objectId = new ObjectId(_id);
                drinkColl=MongoDriver.getObject().getCollection(MongoDriver.Collections.DRINKS);
                drinkColl.updateOne(eq("_id", objectId), Updates.inc("likes", 1));
                //the database are perfectly consistent
                return "LikeOk";
            }catch (MongoException me){
                LogManager.getLogger("DrinkDao.class").error("MongoDB: failed to insert like in drinks");
                already_tried=true;
            }
        }
    }

    /***
     * Remove like
     * @param _id
     * @param username
     * @return
     */
    public String removeLike(String _id, String username){
        boolean already_tried = false;
        //cross-db consistency between neo4j and mongodb
        //the while will execute 2 iteration at most
        while(true){

            //_id in neo4j is the oid field in mongodb
            if(!already_tried) { //try to delete on neo4j
                try (Session session = Neo4jDriver.getObject().getDriver().session()){
                    session.writeTransaction((TransactionWork<Void>) tx -> {
                        tx.run("MATCH (uu:User {username:$username})-[rel:LIKES]->(d:Drink {id:$_id}) delete rel",
                                parameters("username", username, "_id", _id));
                        return null;
                    });
                }catch(Neo4jException ne){
                    LogManager.getLogger("DrinkDao.class").error("Neo4j: like's relation deletion failed");
                    return "Abort";
                }
            }
            //second try consists in deleting the relation from neo4j
            else{
                //try to add again the relation to neo4j in case the operation on mongo fails
                try (Session session = Neo4jDriver.getObject().getDriver().session()){
                    session.writeTransaction((TransactionWork<Void>) tx -> {
                        tx.run("MATCH (uu:User) WHERE uu.username = $username" +
                                        " MATCH (d:Drink) WHERE rr.id = $_id" +
                                        " CREATE (uu)-[rel:LIKES {since:date($date)}]->(d)",
                                parameters("username", username, "_id", _id));
                        return null;
                    });
                }catch(Neo4jException ne){
                    LogManager.getLogger("DrinkDao.class").error("Neo4j[PARSE], like delete inconsistency: _id: "+
                            _id+" username: "+username);
                    return "Abort";
                }
            }
            MongoCollection<Document> drinkColl=null;
            //try to update the redundancy on mongodb
            try {
                ObjectId objectId = new ObjectId(_id);
                drinkColl=MongoDriver.getObject().getCollection(MongoDriver.Collections.DRINKS);
                drinkColl.updateOne(eq("_id", objectId), Updates.inc("likes", -1));
                //the database are perfectly consistent
                return "LikeOk";
            }catch (MongoException me){
                LogManager.getLogger("DrinkDao.class").error("MongoDB: failed to delete like in drinks");
                already_tried=true;
            }
        }
    }

    /***
     * Retrieving drinks from the key-value db
     * @param key
     * @return
     */
    public JSONObject getDrinkByKey(String key){
        try{
            byte[] byteObj = HaloDBDriver.getObject().getData("drink", key.getBytes(StandardCharsets.UTF_8));
            return new JSONObject(new String(byteObj));
        }catch(HaloDBException ex){
            LogManager.getLogger("DrinkDao.class").fatal("HaloDB: caching failed");
            HaloDBDriver.getObject().closeConnection();
            System.exit(-1);
        }
        return new JSONObject();
    }

    /***
     * Retrieve drink by id
     * @param id
     * @return
     */
    public Document getDrinkById(String id){
        try {
            Document drink;
            MongoCursor<Document> cursor = MongoDriver.getObject()
                    .getCollection(MongoDriver.Collections.DRINKS)
                    .find(eq("_id", new ObjectId(id))).iterator();

            drink = cursor.next();
            return drink;
        }catch(MongoException ex){

        }
        return null;
    }

    /***
     * GLOBAL SUGGESTION: best drinks, the drinks with the highest number of likes obtained in the week
     * @return
     */
    public List<Document> getBestDrinks(){
        List<Document> drinks = new ArrayList<>();
        String todayDate = java.time.LocalDate.now().toString();
        try (Session session = Neo4jDriver.getObject().getDriver().session()) {
            session.readTransaction((TransactionWork<Void>) tx -> {
                Result res = tx.run(
                        "MATCH (:User)-[likes:LIKES]->(d:Drink) " +
                                "WHERE date($date)-duration({days:7})<likes.since<=date($date)+duration({days:7}) " +
                                "return d AS DrinkNode, count(likes) AS totalLikes " +
                                "ORDER BY totalLikes DESC, DrinkNode.name ASC LIMIT 10",
                        parameters("date", todayDate));
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
            System.out.println("Neo4j was not able to retrieve the drink's " +
            "global suggestions");
        }
        return drinks;
    }

    public List<Document> mostUsedIngrByCategory(String category){
        MongoCollection<Document> collDrink = MongoDriver.getObject().getCollection(MongoDriver.Collections.DRINKS);

        List<Bson> stages = new ArrayList<>();
        List<Bson> filters = new ArrayList<>();

        if(category.equals("cocktail")){
            filters.add(eq("tag", "cocktail"));
        }
        if(category.equals("beer")){
            filters.add(eq("tag", "beer"));
        }
        if(category.equals("other")){
            filters.add(eq("tag", "other"));
        }

        stages.add(match(and(filters)));
        stages.add(unwind("$ingredients"));
        stages.add(group("$ingredients.ingredient", sum("count", 1)));
        stages.add(sort(descending("count")));
        stages.add(limit(10));
        List<Document> results = null;
        try{
            results = collDrink.aggregate(stages).into(new ArrayList<>());
        } catch (MongoException ex){
            ex.printStackTrace();
            LogManager.getLogger("DrinkDao.class").error("MongoDB: fail analytics: Most used ingredient");
        }

        return results;
    }

}
