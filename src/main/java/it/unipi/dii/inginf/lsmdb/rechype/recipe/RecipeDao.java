package it.unipi.dii.inginf.lsmdb.rechype.recipe;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.oath.halodb.HaloDB;
import com.oath.halodb.HaloDBException;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.HaloDBDriver;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.MongoDriver;
import static com.mongodb.client.model.Filters.eq;
import static org.neo4j.driver.Values.parameters;

import it.unipi.dii.inginf.lsmdb.rechype.persistence.Neo4jDriver;
import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.exceptions.Neo4jException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RecipeDao {

    public String addRecipe(Recipe recipe){

        Document doc = new Document().append("name", recipe.getName()).append("author", recipe.getAuthor())
                .append("vegetarian", recipe.isVegetarian()).append("glutenFree", recipe.isGlutenFree())
                .append("dairyFree", recipe.isDairyFree()).append("pricePerServing", recipe.getPricePerServing()).append("weightPerServing", recipe.getWeightPerServing())
                .append("servings", recipe.getServings()).append("image", recipe.getImage())
                .append("description", recipe.getDescription()).append("readyInMinutes", recipe.getReadyInMinute())
                .append("method", recipe.getMethod()).append("likes", recipe.getLikes());

        boolean already_tried = false;
        MongoCollection<Document> coll = null;
        System.out.println(doc.toJson());
        InsertOneResult res = null;

        while(true){
            try {
                if(!already_tried){
                    coll = MongoDriver.getObject().getCollection(MongoDriver.Collections.RECIPES);
                    res = coll.insertOne(doc);
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

            try (Session session = Neo4jDriver.getObject().getDriver().session()) { //try to add
                session.writeTransaction((TransactionWork<Void>) tx -> {
                    tx.run("CREATE (ee:Recipe { name: $name, author: $author, pricePerServing: $pricePerServing, imageUrl: $imageUrl," +
                            "vegetarian: $vegetarian, vegan: $vegan, dairyFree: $dairyFree, glutenFree: $glutenFree, likes: $likes})",
                            parameters("name", recipe.getName(), "author", recipe.getAuthor(), "pricePerServing", recipe.getPricePerServing(),
                            "imageUrl", recipe.getImage(), "vegetarian", recipe.isVegetarian(), "vegan", recipe.isVegan(), "dairyFree", recipe.isDairyFree(),
                            "glutenFree", recipe.isGlutenFree(), "likes", recipe.getLikes()));
                    return null;
                });
                return "recipeAdded";
            }catch(Neo4jException ne){ //fail, next cycle try to delete on MongoDB
                LogManager.getLogger("RecipeDao.class").error("Neo4j: recipe insert failed");
                already_tried=true;
            }
        }
    }

    public List<Recipe> getRecipesByText(String recipeName, int offset, int quantity){

        //create the case Insensitive pattern and perform the mongo query
        List<Recipe> returnList = new ArrayList<>();
        List<Document> returnDocList = new ArrayList<>();
        Pattern pattern = Pattern.compile(".*" + recipeName + ".*", Pattern.CASE_INSENSITIVE);
        Bson filter = Filters.regex("name", pattern);
        MongoCursor<Document> recipeCursor  = MongoDriver.getObject().getCollection(MongoDriver.Collections.RECIPES).find(filter).skip(offset).limit(quantity).iterator();
        while (recipeCursor.hasNext()){
            Document doc = recipeCursor.next();
            Recipe user = new Recipe(doc);
            returnList.add(user);
            returnDocList.add(doc);
        }
        cacheSearch(returnDocList);
        return returnList;
    }

    public JSONObject getRecipeByKey(String key){
        try{
            HaloDB db = HaloDBDriver.getObject().getClient("recipes");
            byte[] byteObj = db.get(key.getBytes(StandardCharsets.UTF_8));
            return new JSONObject(new String(byteObj));
        }catch(HaloDBException ex){
            LogManager.getLogger("UserDao.class").fatal("HaloDB: caching failed");
            HaloDBDriver.getObject().closeConnection();
            System.exit(-1);
        }
        return new JSONObject();
    }

    private void cacheSearch(List<Document> recipesList){ //caching of recipe's search
        for(int i=0; i<recipesList.size(); i++) {
            String idObj = new JSONObject(recipesList.get(i).toJson()).getJSONObject("_id").getString("$oid");
            byte[] _id = idObj.getBytes(StandardCharsets.UTF_8); //key
            byte[] objToSave = recipesList.get(i).toJson().getBytes(StandardCharsets.UTF_8); //value
            try {
                HaloDBDriver.getObject().getClient("recipes").put(_id, objToSave);
            }catch(Exception e){
                LogManager.getLogger("RecipeDao.class").fatal("HaloDB: caching failed");
                HaloDBDriver.getObject().closeConnection();
                System.exit(-1);
            }
        }
    }

    public String updateRecipeLike(JSONObject _id, String user){

        String ret = null;

        try (Session session = Neo4jDriver.getObject().getDriver().session()) { //try to add
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (uu:User) WHERE uu.name = $username" +
                            "MATCH (rr:Recipe) WHERE rr.name = $title" +
                                " CREATE (uu)-[r:LIKES]->(rr)",
                        parameters("username", user, "title", _id.getString("$oid")));
                return null;
            });
        }catch(Neo4jException ne){ //fail, next cycle try to delete on MongoDB
            LogManager.getLogger("RecipeDao.class").error("Neo4j: recipe insert failed");
        }

        try (MongoCursor<Document> cursor = MongoDriver.getObject().getCollection(MongoDriver.Collections.RECIPES).find(eq("_id", _id)).iterator()) {
            //check for double username or username not allow.


        } catch (MongoException me) {
            LogManager.getLogger("UserDao.class").error("MongoDB: an error occurred");
        }



        return "ciao";
    }


}
