package it.unipi.dii.inginf.lsmdb.rechype.recipe;

class RecipeServiceImpl implements RecipeService{

    private static RecipeDao recipeDao = new RecipeDao();

    public boolean addRecipe(Recipe recipe) {
        return recipeDao.addRecipe(recipe);
    }

}
