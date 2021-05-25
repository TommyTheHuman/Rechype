package it.unipi.dii.inginf.lsmdb.rechype.drink;

import org.bson.Document;
import org.json.JSONObject;

import java.util.List;

class DrinkServiceImpl implements DrinkService{
    DrinkDao drinkDao=new DrinkDao();

    public List<Drink> searchDrink(String text, int offset, int quantity){
    return drinkDao.getDrinksByText(text, offset, quantity); }

    public String addDrink(Document doc){ return drinkDao.addDrink(doc); }
    public void putDrinkInCache(Document drink){
        drinkDao.cacheAddedDrink(drink);
    }
    public String addLike(String _id, String user) { return drinkDao.addLike(_id, user); }
    public String removeLike(String _id, String user){ return drinkDao.removeLike(_id, user); }
    public JSONObject getCachedDrink(String key){
        return drinkDao.getDrinkByKey(key);
    }
    public Document searchDrinkById(String _id) { return drinkDao.getDrinkById(_id); }
}
