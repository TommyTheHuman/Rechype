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

    public byte[] getCachedImage(String ingrName){ return ingredientDao.getImgByKey(ingrName); }

    public List<Document> getBestIngredients() {return ingredientDao.getBestIngredients();}

    public List<Ingredient> searchIngredientsList(List<String> ingredientsList){
        return ingredientDao.searchIngredientsList(ingredientsList);
    }
}
