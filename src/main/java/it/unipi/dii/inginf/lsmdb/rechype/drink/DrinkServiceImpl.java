package it.unipi.dii.inginf.lsmdb.rechype.drink;

import org.bson.Document;
import org.json.JSONObject;

import java.util.List;
import java.util.ListResourceBundle;

class DrinkServiceImpl implements DrinkService{
    DrinkDao drinkDao=new DrinkDao();

    public List<Drink> searchDrink(String text, int offset, int quantity, JSONObject filters){return drinkDao.getDrinksByText(text, offset, quantity, filters); }
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
    public List<Document> getBestDrinks() { return drinkDao.getBestDrinks(); }
    public List<Document> getUserByLikeAndCategory(String category){ return drinkDao.getRankingUserAndCategory(category); }
    public List<Document> getUserByLikeAndNationAndAge(int min, int max, String country){ return drinkDao.getRankingUserAndNation(min, max, country); }

}
