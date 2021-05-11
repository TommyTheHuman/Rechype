package it.unipi.dii.inginf.lsmdb.rechype.ingredient;

import java.util.List;

public class IngredientServiceImpl implements IngredientService{

    private static IngredientDao ingredientDao = new IngredientDao();

    @Override
    public List<Ingredient> searchIngredients(String text) {
        return ingredientDao.getIngredientByText(text);
    }
}
