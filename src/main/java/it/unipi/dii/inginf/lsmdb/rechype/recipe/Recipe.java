package it.unipi.dii.inginf.lsmdb.rechype.recipe;

import org.apache.logging.log4j.LogManager;
import org.bson.Document;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;

public class Recipe {

    private String name;
    private String author;
    private String image;
    private String description;
    private String method;
    private String ingredients;   // DOVRA' ESSERE UN ARRAY DI INGREDIENTS
    private Integer originalId;

    private boolean vegan;
    private boolean glutenFree;
    private boolean dairyFree;
    private boolean vegetarian;

    private double servings;
    private double readyInMinutes;
    private double weightPerServing;
    private double pricePerServing;
    private String _id;
    private JSONObject jsonRepresentation; //comment
    private int likes;

    public Recipe(){}

    public Recipe(String name, String author, String image, String description,
        String method, String ingredients, boolean vegan, boolean glutenFree, boolean dairyFree, boolean vegetarian,
        double servings, double readyInMinute, double weightPerServing, double pricePerServing){
        this.name = name;
        this.author = author;
        this.image = image;
        this.description = description;
        this.method = method;
        this.ingredients = ingredients;
        this.vegan = vegan;
        this.glutenFree = glutenFree;
        this.dairyFree = dairyFree;
        this.vegetarian = vegetarian;
        this.servings = servings;
        this.readyInMinutes = readyInMinute;
        this.weightPerServing = weightPerServing;
        this.pricePerServing = pricePerServing;
        this._id="";
        this.likes = 0;
    }

    public Recipe(Document doc){
        //we save the document to get a json representation of the class
        this.jsonRepresentation = new JSONObject(doc.toJson());

        if(doc.get("name")==null){
            this.name="";
        }
        this.name = doc.get("name")==null? "" : doc.get("name").toString();
        this.author = doc.get("author")==null? "" : doc.get("author").toString();
        this.image = doc.get("image")==null? null : doc.get("image").toString();
        this.description = doc.get("description")==null? "" : doc.get("description").toString();
        this.method = doc.get("method")==null? "" : doc.get("method").toString();
        this.ingredients = ingredients;
        this.vegan = doc.get("vegan")==null? false : doc.getBoolean("vegan");
        this.glutenFree = doc.get("glutenFree")==null? false : doc.getBoolean("glutenFree");
        this.dairyFree = doc.get("dairyFree")==null? false : doc.getBoolean("dairyFree");
        this.vegetarian = doc.get("vegetarian")==null? false : doc.getBoolean("vegetarian");
        this.readyInMinutes = doc.get("readyInMinute")==null? 0: doc.getDouble("readyInMinute");

        //weight and price may be integer
        if(doc.get("weightPerServing") == null){
            this.weightPerServing = 0;
        }
        else if(doc.get("weightPerServing") instanceof Integer){
            this.weightPerServing = Double.valueOf(doc.getInteger("weightPerServing"));
        }
        else{
            this.weightPerServing = doc.getDouble("weightPerServing");
        }
        if(doc.get("pricePerServing") == null){
            this.pricePerServing = 0;
        }
        else if(doc.get("pricePerServing") instanceof Integer){
            this.pricePerServing = Double.valueOf(doc.getInteger("pricePerServing"));
        }
        else{
            this.pricePerServing = doc.getDouble("pricePerServing");
        }

        this.likes = doc.get("likes")==null? 0 : Integer.parseInt(doc.get("likes").toString());
        JSONObject json = new JSONObject(doc.toJson());
        this._id=json.getJSONObject("_id").getString("$oid");
    }

    //if the json doesn't have some fields is a problem so we must access from t
    /*public Recipe(Document doc){
        String[] keys=doc.keySet().toArray(new String[doc.keySet().size()]);
        String name="";
        for(int i=0; i<keys.length; i++){
            try {
                name=keys[i];
                Field field=this.getClass().getDeclaredField(keys[i]);

                if(name.equals("_id")) {
                    JSONObject json = new JSONObject(doc.toJson());
                    field.set(this, json.getJSONObject("_id").getString("$oid"));
                    continue;
                }

                if(doc.get(name) instanceof Integer){
                    if(name.equals("originalId") || name.equals("likes")){
                        field.set(this, doc.getInteger(name));
                    }else {
                        field.set(this, Double.valueOf(doc.getInteger(name)));
                    }
                }
                else if(doc.get(name) instanceof Double){
                    field.set(this, doc.getDouble(name));
                }
                else if (doc.get(name) instanceof Boolean){
                    field.set(this, doc.getBoolean(name));
                }
                else {
                    field.set(this, doc.getString(name));
                }
            }catch(NoSuchFieldException | SecurityException | IllegalAccessException ex){
                LogManager.getLogger("Recipe.class").fatal("field "+name+" is missing");
                System.exit(-1);
            }
        }
    }*/

    //neo4j constructor with less fields but all the private fields must be set anyway
    public JSONObject getJSON() { return jsonRepresentation; } //da provare

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getImage() {
        return image;
    }

    public String getDescription() {
        return description;
    }

    public boolean isVegan() {
        return vegan;
    }

    public boolean isDairyFree() {
        return dairyFree;
    }

    public boolean isGlutenFree() {
        return glutenFree;
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    public double getPricePerServing() {
        return pricePerServing;
    }

    public double getReadyInMinute() {
        return readyInMinutes;
    }

    public double getServings() {
        return servings;
    }

    public double getWeightPerServing() {
        return weightPerServing;
    }

    public int getLikes() {
        return likes;
    }

    public String getMethod() {
        return method;
    }

    public String getId() {return _id; }

    public static String getPriceSymbol(double price){
        if(price>=1500){
            return "$$$$";
        }
        else if(price>=1000){
            return "$$$";
        }
        else if(price>=500){
            return"$$";
        }
        else{
            return "$";
        }
    }

}
