package it.unipi.dii.inginf.lsmdb.rechype.profile;

import it.unipi.dii.inginf.lsmdb.rechype.recipe.Recipe;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

public class Profile {
    private String username;

    public Profile(Document doc){
        JSONObject json = new JSONObject(doc.toJson());
        JSONArray meals = json.getJSONArray("meals");
        username = json.getString("_id");

        for(int i = 0; i < meals.length(); i++){
            JSONArray recipes = meals.getJSONObject(i).getJSONArray("recipes");
            for(int j = 0; j < recipes.length(); j++){
                Document recipeDoc = Document.parse(recipes.getJSONObject(j).toString());
                Recipe recipe = new Recipe(recipeDoc);
                System.out.println(recipeDoc.getString("name"));
            }
        }
        //JSONArray recipes = meals.getJSONArray("recipes");
    }

}
