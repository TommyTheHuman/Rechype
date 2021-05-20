package it.unipi.dii.inginf.lsmdb.rechype.recipe;

import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import org.bson.Document;
import org.json.JSONObject;

import java.util.List;

public interface RecipeService {

    String addRecipe(org.bson.Document doc, Recipe recipe);
    List<Recipe> searchRecipe(String text, int offset, int quantity);
    JSONObject getCachedRecipe(String key);
    void putRecipeInCache(Document recipe);

}
