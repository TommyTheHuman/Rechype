package it.unipi.dii.inginf.lsmdb.rechype.ingredient;

import org.bson.Document;
import org.json.JSONObject;

import java.util.List;

class IngredientServiceImpl implements IngredientService{

    private static IngredientDao ingredientDao = new IngredientDao();

    @Override
    public List<Ingredient> searchIngredients(String text, int offset, int quantity) {
        return ingredientDao.getIngredientByText(text, offset, quantity);
    }

    public JSONObject getCachedIngredient(String ingrName){ return ingredientDao.getIngredientByKey(ingrName); }

    public List<Document> getBestIngredients() {return ingredientDao.getBestIngredients();}
}
