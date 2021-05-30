package it.unipi.dii.inginf.lsmdb.rechype.drink;

import it.unipi.dii.inginf.lsmdb.rechype.ingredient.Ingredient;
import org.bson.Document;
import org.json.JSONObject;

import java.util.List;

public class Drink {
    private String _id;
    private String name;
    private String author;
    private String description;
    private String image;
    private String method;
    private List<Ingredient> ingredients;
    private String tag;
    private JSONObject jsonRepresentation;

    public Drink(Document doc){
        JSONObject jsonObject = new JSONObject(doc.toJson());
        jsonRepresentation = jsonObject;
        if(jsonObject.has("description")){
            _id=jsonObject.getJSONObject("_id").getString("$oid");
            description=jsonObject.getString("description");
            method=jsonObject.getString("method");
        }else{
            _id=jsonObject.getString("_id");
        }
        name=jsonObject.getString("name");
        author=jsonObject.getString("author");
        if(jsonObject.get("image") instanceof String)
            image=jsonObject.getString("image");
        else
            image="";
        tag=jsonObject.getString("tag");
    }

    public String getName(){
        return this.name;
    }

    public String getImage(){
        return this.image;
    }

    public String getAuthor(){
        return this.author;
    }

    public String getTag(){
        return tag;
    }

    public String getId(){
        return _id;
    }

}
