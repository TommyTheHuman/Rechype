package it.unipi.dii.inginf.lsmdb.rechype.ingredient;

import org.json.JSONObject;

import java.util.List;

public interface IngredientService {

    List<Ingredient> searchIngredients(String text, int offset, int quantity);

    List<Ingredient> getIngredientFromString(List<String> ingrName);

    JSONObject getCachedIngredient(String key);

}
