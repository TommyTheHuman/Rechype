package it.unipi.dii.inginf.lsmdb.rechype.drink;

import it.unipi.dii.inginf.lsmdb.rechype.recipe.Recipe;
import org.json.JSONObject;

import java.util.List;

class DrinkServiceImpl implements DrinkService{
    DrinkDao drinkDao=new DrinkDao();

    public List<Drink> searchDrink(String text, int offset, int quantity){
        return drinkDao.getDrinksByText(text, offset, quantity);
    }

    public JSONObject getCachedDrink(String key){
        return drinkDao.getDrinkByKey(key);
    }
}
