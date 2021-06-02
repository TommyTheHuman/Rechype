package it.unipi.dii.inginf.lsmdb.rechype.profile;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

public class Profile {
    private String username;
    private JSONArray meals;
    private JSONArray fridge;

    public Profile(Document doc){
        JSONObject json = new JSONObject(doc.toJson());
        meals = json.getJSONArray("meals");
        fridge = json.getJSONArray("fridge");
        username = json.getString("_id");



        //JSONArray recipes = meals.getJSONArray("recipes");
    }

    public JSONArray getFridge() {
        return fridge;
    }

    public JSONArray getMeals() {
        return meals;
    }

    public String getUsername() {
        return username;
    }
}
