package it.unipi.dii.inginf.lsmdb.rechype.ingredient;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.MongoDriver;
import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class IngredientDao {


    public List<Ingredient> getIngredientByText(String ingredientName) {
        List<Ingredient> returnList = new ArrayList<>();
        Pattern pattern = Pattern.compile(".*" + ingredientName + ".*", Pattern.CASE_INSENSITIVE);
        Bson filter = Filters.regex("_id", pattern);
        MongoCursor<Document> cursor  = MongoDriver.getObject().getCollection(MongoDriver.Collections.INGREDIENTS).find(filter).iterator();

        while (cursor.hasNext()){
            Document doc = cursor.next();
            returnList.add(new Ingredient(doc));
        }
        return returnList;
    }
}
