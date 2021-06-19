package it.unipi.dii.inginf.lsmdb.rechype.ingredient;

import org.bson.Document;
import org.json.JSONObject;

import java.util.List;

public interface IngredientService {

    List<Ingredient> searchIngredients(String text, int offset, int quantity);
    byte[] getCachedImage(String key);
    List<Document> getBestIngredients();
    List<Ingredient> searchIngredientsList(List<String> ingredientsList);

}
