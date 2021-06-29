package it.unipi.dii.inginf.lsmdb.rechype.ingredient;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.oath.halodb.HaloDB;
import com.oath.halodb.HaloDBException;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.HaloDBDriver;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.MongoDriver;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.Neo4jDriver;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.Neo4jException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.neo4j.driver.Values.parameters;

public class IngredientDao {

    /***
     * Return a list of ingredients given by the letters of the name
     * @param ingredientName
     * @param offset
     * @param quantity
     * @return
     */
    public List<Ingredient> getIngredientByText(String ingredientName, int offset, int quantity) {
        List<Ingredient> returnList = new ArrayList<>();
        List<Document> returnDocList = new ArrayList<>();
        Pattern pattern = Pattern.compile(".*" + ingredientName + ".*", Pattern.CASE_INSENSITIVE);
        Bson filter = Filters.regex("_id", pattern);
        MongoCursor<Document> cursor = MongoDriver.getObject().getCollection(MongoDriver.Collections.INGREDIENTS)
        .find(filter).skip(offset).limit(quantity).iterator();

        while (cursor.hasNext()) {
            Document doc = cursor.next();
            returnList.add(new Ingredient(doc));
            returnDocList.add(doc);
        }
        cacheSearch(returnDocList);
        return returnList;
    }

    /***
     * retrieve cached ingredient
     * @param key
     * @return
     */
    public byte[] getImgByKey(String key) {
        try {
            byte[] byteObj = HaloDBDriver.getObject().getData("ingredient", key.getBytes(StandardCharsets.UTF_8));
            return byteObj;
        } catch (HaloDBException ex) {
            LogManager.getLogger("ingredientsDao.class").fatal("HaloDB: caching failed");
            HaloDBDriver.getObject().closeConnection();
            System.exit(-1);
        }
        return null;
    }

    /***
     * caching the ingredient's search in a key-value DB
     * @param ingredientsList
     */
    private void cacheSearch(List<Document> ingredientsList) { //caching of ingredient's search
        for (int i = 0; i < ingredientsList.size(); i++) {
            cacheSingleIngredient(ingredientsList.get(i));
        }
    }

    private boolean cacheSingleIngredient(Document doc){
        String idObj = doc.getString("_id");
        byte[] _id = idObj.getBytes(StandardCharsets.UTF_8); //key
        InputStream imgStream;
        String stringUrl;
        if(doc.containsKey("image")){
            stringUrl="https://spoonacular.com/cdn/ingredients_100x100/"+doc.getString("image");
        }
        else{
            String imageName=idObj.replace(" ", "-");
            stringUrl="https://spoonacular.com/cdn/ingredients_100x100/"+imageName+".jpg";
        }
        byte[] objToSave;
        try {
            imgStream = new URL(stringUrl).openStream();
            objToSave = imgStream.readAllBytes();
        }catch(IOException ie){
            return false;
        }
        try {
            HaloDBDriver.getObject().addData("ingredient", _id, objToSave);
            return true;
        }catch(HaloDBException ex){
            LogManager.getLogger("IngredientDao.class").fatal("HaloDB: caching failed");
            HaloDBDriver.getObject().closeConnection();
            System.exit(-1);
            return false;
        }
    }

    List<Ingredient> searchIngredientsList(List<String> ingredientsList){
        List<Ingredient> returnList = new ArrayList<>();
        List<Document> docList = new ArrayList<>();
        Bson filter = Filters.in("_id", ingredientsList);
        MongoCursor<Document> cursor = MongoDriver.getObject().getCollection(MongoDriver.Collections.INGREDIENTS)
        .find(filter).iterator();

        while (cursor.hasNext()) {
            Document doc = cursor.next();
            docList.add(doc);
            returnList.add(new Ingredient(new JSONObject(doc.toJson())));
        }
        cacheSearch(docList);
        return returnList;
    }
}

