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

    public byte[] getCachedImage(String key){
        return recipeDao.getImgByKey(key);
    }

    public void putRecipeInCache(Document recipe){
        recipeDao.cacheAddedRecipe(recipe);
    }
    public String addLike(String _id, String user) { return recipeDao.addLike(_id, user); }
    public String removeLike(String _id, String user){ return recipeDao.removeLike(_id, user); }
    public Document searchRecipeById(String id){ return recipeDao.getRecipeById(id);}
    public List<Document> getUserByLikeAndCategory(String category) { return recipeDao.getRankingUserByLikeAndCategory(category);}
    public List<Document> getUserByLikeNumber(int minAge, int maxAge, String country) {return recipeDao.getUserRankingByLikeNumber(minAge, maxAge, country);}
    public List<Document> getPopularIngredient(String nutrient, int minutes) {return recipeDao.getIngredientRanking(nutrient, minutes);}
    public List<Document> getBestRecipes(){ return recipeDao.getBestRecipes(); }
}
