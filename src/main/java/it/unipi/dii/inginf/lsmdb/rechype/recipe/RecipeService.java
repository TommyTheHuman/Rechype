package it.unipi.dii.inginf.lsmdb.rechype.recipe;

import it.unipi.dii.inginf.lsmdb.rechype.user.User;

import java.util.List;

public interface RecipeService {

    String addRecipe(Recipe recipe);
    List<Recipe> searchRecipe(String text, int offset, int quantity);

}
