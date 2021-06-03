package it.unipi.dii.inginf.lsmdb.rechype.recipe;

public class RecipeServiceFactory {

    private RecipeServiceFactory(){}

    public static RecipeServiceFactory create(){
        return new RecipeServiceFactory();
    }

    public RecipeService getService(){
        return new RecipeServiceImpl();
    }
}

