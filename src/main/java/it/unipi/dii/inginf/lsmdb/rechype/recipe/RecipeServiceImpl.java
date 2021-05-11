package it.unipi.dii.inginf.lsmdb.rechype.recipe;

import it.unipi.dii.inginf.lsmdb.rechype.user.User;

import java.util.List;

class RecipeServiceImpl implements RecipeService{

    private static RecipeDao recipeDao = new RecipeDao();

    public String addRecipe(Recipe recipe) {
        return recipeDao.addRecipe(recipe);
    }
    public List<Recipe> searchRecipe(String text) {
        return recipeDao.getRecipesByText(text);
    }
}
