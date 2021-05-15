package it.unipi.dii.inginf.lsmdb.rechype.ingredient;

public class IngredientServiceFactory {

    private IngredientServiceFactory(){}

    public static IngredientServiceFactory create(){ return new IngredientServiceFactory(); }

    public IngredientService getService(){ return new IngredientServiceImpl(); }


}
