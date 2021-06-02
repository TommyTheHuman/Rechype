package it.unipi.dii.inginf.lsmdb.rechype.ingredient;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Ingredient {
    private final String name;
    private String imageUrl;
    private double amount;
    private List<Nutrients> nutrition;


    public Ingredient(String name, String imageUrl, double amount){
        this.name = name;
        this.imageUrl = imageUrl;
        this.amount = amount;
    }

    public Ingredient(String name, String imageUrl, String nutrition){
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public Ingredient(Document doc){
        String name;
        Double amount;
        String unit;

        this.name = doc.get("_id").toString();
        this.imageUrl = "no.png";
        this.nutrition = null;

        if(doc.containsKey("nutrients")){
            this.nutrition = new ArrayList<>();
            this.imageUrl = doc.get("image").toString();
            List<Document> nutrientsList = (List<Document>) doc.get("nutrients");

            for(int i=0; i<nutrientsList.size(); i++){
                name = nutrientsList.get(i).getString("name");

                if(nutrientsList.get(i).get("amount").getClass().getName().equals("java.lang.Integer")){
                    amount = (double) nutrientsList.get(i).getInteger("amount");
                } else {
                    amount = nutrientsList.get(i).getDouble("amount");
                }
                unit = nutrientsList.get(i).getString("unit");
                this.nutrition.add(new Nutrients(name, amount, unit));
            }
        }
    }

    public Ingredient(JSONObject json){
        String name;
        Double amount;
        String unit;

        this.name = json.getString("_id");
        this.imageUrl = "no.png";
        this.nutrition = null;
        if(json.has("nutrients")){
            this.nutrition = new ArrayList<>();
            this.imageUrl = json.getString("image");
            JSONArray nutrientsList = json.getJSONArray("nutrients");
            JSONObject nutrientJSON;
            for(int i=0; i<nutrientsList.length(); i++){
                nutrientJSON = nutrientsList.getJSONObject(i);
                name = nutrientJSON.getString("name");
                if(nutrientJSON.get("amount").getClass().getName().equals("java.lang.Integer")){
                    amount = (double) nutrientJSON.getInt("amount");
                } else {
                    amount = nutrientJSON.getDouble("amount");
                }
                nutrientJSON.get("amount");
                unit = nutrientJSON.getString("unit");

                nutrition.add(new Nutrients(name, amount, unit));
            }
        }
    }

    public String getName(){ return name; }
    public String getImageUrl(){ return imageUrl; }
    public Double getQuantity(){ return amount;}
    public List<Nutrients> getNutrients(){ return nutrition; }

    public class Nutrients {
        String name;
        Double amount;
        String unit;

        Nutrients(String name, Double amount, String unit){
            this.name = name;
            this.amount = amount;
            this.unit = unit;
        }

        public String getNutrName() {
            return name;
        }

        public Double getNutrAmount() {
            return amount;
        }

        public String getNutrUnit() {
            return unit;
        }
    }



}
