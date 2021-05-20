package it.unipi.dii.inginf.lsmdb.rechype.recipe;

import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import org.json.JSONObject;

import java.util.List;

class RecipeServiceImpl implements RecipeService{

    private static RecipeDao recipeDao = new RecipeDao();

    public String addRecipe(Recipe recipe) {
        return recipeDao.addRecipe(recipe);
    }

    public List<Recipe> searchRecipe(String text, int offset, int quantity) {
        return recipeDao.getRecipesByText(text, offset, quantity);
    }

    public JSONObject getCachedRecipe(String key){
        return recipeDao.getRecipeByKey(key);
    }

    public String addLike(JSONObject _id, String user) {return recipeDao.updateRecipeLike(_id, user);}
}
