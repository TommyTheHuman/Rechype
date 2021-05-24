package it.unipi.dii.inginf.lsmdb.rechype.drink;


public class DrinkServiceFactory {

    private DrinkServiceFactory(){}

    public static DrinkServiceFactory create(){
        return new DrinkServiceFactory();
    }

    public DrinkService getService(){
        return new DrinkServiceImpl();
    }
}
