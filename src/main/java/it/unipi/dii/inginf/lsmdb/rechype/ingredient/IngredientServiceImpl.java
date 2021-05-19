package it.unipi.dii.inginf.lsmdb.rechype.ingredient;

import java.util.List;

public class IngredientServiceImpl implements IngredientService{

    private static IngredientDao ingredientDao = new IngredientDao();

    @Override
    public List<Ingredient> searchIngredients(String text, int offset, int quantity) {
        return ingredientDao.getIngredientByText(text, offset, quantity);
    }

    public List<Ingredient> getIngredientFromString(List<String> ingrName){ return ingredientDao.getIngredientFromString(ingrName); }
}
