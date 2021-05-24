package it.unipi.dii.inginf.lsmdb.rechype.drink;

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

class DrinkDao {

    List<Drink> getDrinksByText(String drinkName, int offset, int quantity){
        //create the case Insensitive pattern and perform the mongo query
        List<Drink> returnList = new ArrayList<>();
        List<Document> returnDocList = new ArrayList<>();
        Pattern pattern = Pattern.compile(".*" + drinkName + ".*", Pattern.CASE_INSENSITIVE);
        Bson filter = Filters.regex("name", pattern);
        MongoCursor<Document> drinkCursor  = MongoDriver.getObject().getCollection(MongoDriver.Collections.DRINKS)
        .find(filter).skip(offset).limit(quantity).iterator();
        while(drinkCursor.hasNext()){
            Document doc = drinkCursor.next();
            Drink drink = new Drink(doc);
            returnList.add(drink);
            returnDocList.add(doc);
        }
        cacheSearch(returnDocList);
        return returnList;
    }

    public void cacheSearch(List<Document> drinksList){ //caching of drink's search
        for(int i=0; i<drinksList.size(); i++) {
            String idObj = new JSONObject(drinksList.get(i).toJson()).getJSONObject("_id").getString("$oid");
            byte[] _id = idObj.getBytes(StandardCharsets.UTF_8); //key
            byte[] objToSave = drinksList.get(i).toJson().getBytes(StandardCharsets.UTF_8); //value
            try {
                HaloDBDriver.getObject().getClient("drinks").put(_id, objToSave);
            }catch(Exception e){
                LogManager.getLogger("DrinkDao.class").fatal("HaloDB: caching failed");
                HaloDBDriver.getObject().closeConnection();
                System.exit(-1);
            }
        }
    }

    public JSONObject getDrinkByKey(String key){
        try{
            HaloDB db = HaloDBDriver.getObject().getClient("drinks");
            byte[] byteObj = db.get(key.getBytes(StandardCharsets.UTF_8));
            return new JSONObject(new String(byteObj));
        }catch(HaloDBException ex){
            LogManager.getLogger("RecipeDao.class").fatal("HaloDB: caching failed");
            HaloDBDriver.getObject().closeConnection();
            System.exit(-1);
        }
        return new JSONObject();
    }

}
