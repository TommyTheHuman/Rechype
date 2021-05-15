package it.unipi.dii.inginf.lsmdb.rechype.ingredient;

import java.util.List;

public interface IngredientService {

    List<Ingredient> searchIngredients(String text);

    List<Ingredient> getIngredientFromString(List<String> ingrName);

}
