package it.unipi.dii.inginf.lsmdb.rechype.ingredient;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class Ingredient {
    private String name;
    private String imageUrl;
    private List<Nutrients> nutrition;

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
            nutrition = new ArrayList<>();
            imageUrl = doc.get("image").toString();
            List<Document> nutrientsList = (List<Document>) doc.get("nutrients");

            for(int i=0; i<nutrientsList.size(); i++){
                name = nutrientsList.get(i).getString("name");

                if(nutrientsList.get(i).get("amount").getClass().getName().equals("java.lang.Integer")){
                    amount = (double) nutrientsList.get(i).getInteger("amount");
                } else {
                    amount = nutrientsList.get(i).getDouble("amount");
                }
                unit = nutrientsList.get(i).getString("unit");
                nutrition.add(new Nutrients(name, amount, unit));
            }
            this.imageUrl = imageUrl;
            this.nutrition = nutrition;
        }
    }

    public String getName(){ return name; }
    public String getImageUrl(){ return imageUrl; }
    public String getNutrients(){ return nutrition.toString(); }

//    check: COME METETRE GLI IDENTIFICATORI
    private class Nutrients {
        String name;
        Double amount;
        String unit;

        Nutrients(String name, Double amount, String unit){
            this.name = name;
            this.amount = amount;
            this.unit = unit;
        }

        @Override
        public String toString() {
            return "Nutrients{" +
                    "name='" + name + '\'' +
                    ", amount=" + amount +
                    ", unit='" + unit + '\'' +
                    '}';
        }

    }



}
