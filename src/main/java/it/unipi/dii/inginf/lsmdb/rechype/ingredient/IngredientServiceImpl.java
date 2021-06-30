package it.unipi.dii.inginf.lsmdb.rechype.ingredient;

import java.util.List;

class IngredientServiceImpl implements IngredientService{

    private static IngredientDao ingredientDao = new IngredientDao();

    @Override
    public List<Ingredient> searchIngredients(String text, int offset, int quantity) {
        return ingredientDao.getIngredientByText(text, offset, quantity);
    }

    public byte[] getCachedImage(String ingrName){ return ingredientDao.getImgByKey(ingrName); }

    public List<Ingredient> searchIngredientsList(List<String> ingredientsList){
        return ingredientDao.searchIngredientsList(ingredientsList);
    }
}
