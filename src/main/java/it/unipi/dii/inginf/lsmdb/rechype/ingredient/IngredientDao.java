package it.unipi.dii.inginf.lsmdb.rechype.ingredient;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.oath.halodb.HaloDB;
import com.oath.halodb.HaloDBException;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.HaloDBDriver;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.MongoDriver;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class IngredientDao {

    // Return a list of ingredients given by the letters of the name
    public List<Ingredient> getIngredientByText(String ingredientName, int offset, int quantity) {
        List<Ingredient> returnList = new ArrayList<>();
        List<Document> returnDocList = new ArrayList<>();
        Pattern pattern = Pattern.compile(".*" + ingredientName + ".*", Pattern.CASE_INSENSITIVE);
        Bson filter = Filters.regex("_id", pattern);
        MongoCursor<Document> cursor  = MongoDriver.getObject().getCollection(MongoDriver.Collections.INGREDIENTS).find(filter).skip(offset).limit(quantity).iterator();

        while (cursor.hasNext()){
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
        while (cursor.hasNext()){
            Document doc = cursor.next();
            returnList.add(new Ingredient(doc));
        }
        return returnList;
    }

    public JSONObject getIngredientByKey(String key){
        try{
            HaloDB db = HaloDBDriver.getObject().getClient("ingredients");
            byte[] byteObj = db.get(key.getBytes(StandardCharsets.UTF_8));
            return new JSONObject(new String(byteObj));
        }catch(HaloDBException ex){
            LogManager.getLogger("ingredientsDao.class").fatal("HaloDB: caching failed");
            HaloDBDriver.getObject().closeConnection();
            System.exit(-1);
        }
        return new JSONObject();
    }

    private void cacheSearch(List<Document> ingredientsList){ //caching of ingredient's search
        for(int i=0; i<ingredientsList.size(); i++) {
            String idObj = ingredientsList.get(i).getString("_id");
            byte[] _id = idObj.getBytes(StandardCharsets.UTF_8); //key
            byte[] objToSave = ingredientsList.get(i).toJson().getBytes(StandardCharsets.UTF_8); //value
            try {
                HaloDBDriver.getObject().getClient("ingredients").put(_id, objToSave);
            }catch(Exception e){
                LogManager.getLogger("IngredientDao.class").fatal("HaloDB: caching failed");
                HaloDBDriver.getObject().closeConnection();
                System.exit(-1);
            }
        }
    }

}
