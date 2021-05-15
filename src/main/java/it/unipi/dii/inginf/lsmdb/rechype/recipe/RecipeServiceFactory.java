package it.unipi.dii.inginf.lsmdb.rechype.recipe;

import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;

public class RecipeServiceFactory {

    private RecipeServiceFactory(){}

    public static RecipeServiceFactory create(){
        return new RecipeServiceFactory();
    }

    public RecipeService getService(){
        return new RecipeServiceImpl();
    }
}

