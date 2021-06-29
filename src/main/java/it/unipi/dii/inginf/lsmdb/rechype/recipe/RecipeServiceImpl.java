package it.unipi.dii.inginf.lsmdb.rechype.recipe;

import org.bson.Document;
import org.json.JSONObject;

import java.util.List;

class RecipeServiceImpl implements RecipeService{

    private static RecipeDao recipeDao = new RecipeDao();

    public String addRecipe(Document doc) {
        return recipeDao.addRecipe(doc);
    }

    public List<Recipe> searchRecipe(String text, int offset, int quantity, JSONObject filters) {
        return recipeDao.getRecipesByText(text, offset, quantity, filters);
    }

    public JSONObject getCachedRecipe(String key){
        return recipeDao.getRecipeByKey(key);
    }

    public void putRecipeInCache(Document recipe){
        recipeDao.cacheAddedRecipe(recipe);
    }
    public String addLike(String _id, String user) { return recipeDao.addLike(_id, user); }
    public String removeLike(String _id, String user){ return recipeDao.removeLike(_id, user); }
    public Document searchRecipeById(String id){ return recipeDao.getRecipeById(id);}
    public List<Document> getBestRecipes(){ return recipeDao.getBestRecipes(); }
    public List<Document> getPriceDistribution() { return recipeDao.recipeDistributionByPrice(); }
    public List<Document> getMostUsedIngr(String category) {return recipeDao.mostUsedIngrByCategory(category); }
}
