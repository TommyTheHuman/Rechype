package it.unipi.dii.inginf.lsmdb.rechype.recipe;

class RecipeServiceImpl implements RecipeService{

    private static RecipeDao recipeDao = new RecipeDao();

    public String addRecipe(Recipe recipe) {
        return recipeDao.addRecipe(recipe);
    }

}
