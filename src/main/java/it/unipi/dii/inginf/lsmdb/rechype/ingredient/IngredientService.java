package it.unipi.dii.inginf.lsmdb.rechype.ingredient;

import java.util.List;

public interface IngredientService {

    List<Ingredient> searchIngredients(String text, int offset, int quantity);
    byte[] getCachedImage(String key);
    List<Ingredient> searchIngredientsList(List<String> ingredientsList);

}
