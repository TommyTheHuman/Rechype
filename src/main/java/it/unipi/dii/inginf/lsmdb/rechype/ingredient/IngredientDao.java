package it.unipi.dii.inginf.lsmdb.rechype.ingredient;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.oath.halodb.HaloDB;
import com.oath.halodb.HaloDBException;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.HaloDBDriver;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.MongoDriver;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.Neo4jDriver;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.Neo4jException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.neo4j.driver.Values.parameters;

public class IngredientDao {

    // Return a list of ingredients given by the letters of the name
    public List<Ingredient> getIngredientByText(String ingredientName, int offset, int quantity) {
        List<Ingredient> returnList = new ArrayList<>();
        List<Document> returnDocList = new ArrayList<>();
        Pattern pattern = Pattern.compile(".*" + ingredientName + ".*", Pattern.CASE_INSENSITIVE);
        Bson filter = Filters.regex("_id", pattern);
        MongoCursor<Document> cursor = MongoDriver.getObject().getCollection(MongoDriver.Collections.INGREDIENTS).find(filter).skip(offset).limit(quantity).iterator();

        while (cursor.hasNext()) {
            Document doc = cursor.next();
            returnList.add(new Ingredient(doc));
            returnDocList.add(doc);
        }
        cacheSearch(returnDocList);
        return returnList;
    }

    // Return a list of Ingredients given a list of full names of Ingredients.
    public List<Ingredient> getIngredientFromString(List<String> ingredientName) {
        List<Ingredient> returnList = new ArrayList<>();

        Bson filter = Filters.in("id", ingredientName);
        MongoCursor<Document> cursor = MongoDriver.getObject().getCollection(MongoDriver.Collections.INGREDIENTS).find(filter).iterator();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            returnList.add(new Ingredient(doc));
        }
        return returnList;
    }

    public JSONObject getIngredientByKey(String key) {
        try {
            HaloDB db = HaloDBDriver.getObject().getClient("ingredients");
            byte[] byteObj = db.get(key.getBytes(StandardCharsets.UTF_8));
            return new JSONObject(new String(byteObj));
        } catch (HaloDBException ex) {
            LogManager.getLogger("ingredientsDao.class").fatal("HaloDB: caching failed");
            HaloDBDriver.getObject().closeConnection();
            System.exit(-1);
        }
        return new JSONObject();
    }

    private void cacheSearch(List<Document> ingredientsList) { //caching of ingredient's search
        for (int i = 0; i < ingredientsList.size(); i++) {
            String idObj = ingredientsList.get(i).getString("_id");
            byte[] _id = idObj.getBytes(StandardCharsets.UTF_8); //key
            byte[] objToSave = ingredientsList.get(i).toJson().getBytes(StandardCharsets.UTF_8); //value
            try {
                HaloDBDriver.getObject().getClient("ingredients").put(_id, objToSave);
            } catch (Exception e) {
                LogManager.getLogger("IngredientDao.class").fatal("HaloDB: caching failed");
                HaloDBDriver.getObject().closeConnection();
                System.exit(-1);
            }
        }
    }

    /***
     * GLOBAL SUGGESTION
     * retrieving "best ingredients": the most used ingredients in the week
     * @return
     */
    public List<Document> getBestIngredients() {
        List<Document> ingredients = new ArrayList<>();
        String todayDate = java.time.LocalDate.now().toString();
        try (Session session = Neo4jDriver.getObject().getDriver().session()) {
            session.readTransaction((TransactionWork<Void>) tx -> {
                Result res = tx.run(
                        "MATCH (i:Ingredient) " +
                                "OPTIONAL MATCH (:User)-[owns:OWNS]->(r:Recipe)-[con:CONTAINS]->(i) " +
                                "WHERE date($date)-duration({days:7})<owns.since<=date($date)+duration({days:7}) " +
                                "WITH i, count(con) as RecipesAdding " +
                                "OPTIONAL MATCH (:User)-[owns2:OWNS]->(d:Drink)-[con2:CONTAINS]->(i) " +
                                "WHERE date($date)-duration({days:7})<owns2.since<=date($date)+duration({days:7}) " +
                                "WITH i, count(con2)+RecipesAdding as totalAdding " +
                                "return i AS Ingredient, totalAdding " +
                                "ORDER BY totalAdding DESC, i.id ASC LIMIT 10",
                        parameters("date", todayDate));
                while (res.hasNext()) {
                    Record rec = res.next();
                    Value ingredient = rec.get("Ingredient");
                    Document doc = new Document();
                    doc.put("image", ingredient.get("imageUrl").asString());
                    doc.put("_id", ingredient.get("id").asString());
                    ingredients.add(doc);
                }
                return null;
            });

        }catch(Neo4jException ne){
            ne.printStackTrace();
            System.out.println("Neo4j was not able to retrieve the ingredient's " +
                    "global suggestions");
        }
        return ingredients;
    }
}

