package it.unipi.dii.inginf.lsmdb.rechype.drink;

import it.unipi.dii.inginf.lsmdb.rechype.recipe.Recipe;
import org.bson.Document;
import org.json.JSONObject;

import java.util.List;

public interface DrinkService {

    //String addRecipe(Document doc);
    List<Drink> searchDrink(String text, int offset, int quantity);
    JSONObject getCachedDrink(String key);
    //void putRecipeInCache(Document recipe);
    String addLike(String _id, String username);
    String removeLike(String _id, String username);
}
