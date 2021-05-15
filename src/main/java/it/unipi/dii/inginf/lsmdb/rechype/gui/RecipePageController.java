package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.recipe.Recipe;
import it.unipi.dii.inginf.lsmdb.rechype.util.JSONAdder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class RecipePageController extends JSONAdder implements Initializable {

    @FXML private Text authorLabel;
    @FXML private Text Name;
    @FXML private Text DescriptionText;
    @FXML private Text Kcal;
    @FXML private Text Servings;
    @FXML private Text WeightPerServing;
    @FXML private Text ReadyInMinutes;
    @FXML private Text Likes;
    @FXML private Text MethodText;
    @FXML private PieChart NutritionsPie;
    @FXML private ImageView RecipeImage;
    @FXML private ImageView VeganIcon;
    @FXML private ImageView VegetarianIcon;
    @FXML private ImageView DairyIcon;
    @FXML private ImageView GlutenIcon;
    @FXML private Text PriceIcon;
    private ObservableList<PieChart.Data> pieData;


    @Override
    public void initialize(URL location, ResourceBundle resources) {}

    public void setGui(){

        //setting text informations
        try{
            authorLabel.setText("Author: "+jsonParameters.getString("author"));
        }catch(JSONException ex){
            authorLabel.setText("Author: unknown");
        }
        try{
            Name.setText("title: "+jsonParameters.getString("name"));
        }catch(JSONException ex){
            Name.setText("title: unknown");
        }
        try{ //the description is in html, so we need to parse it
            Document htmlParsed=Jsoup.parse(jsonParameters.getString("description"));
            DescriptionText.setText("description: "+htmlParsed.text());
        }catch(JSONException ex){
            DescriptionText.setText("description: unknown");
        }
        try{
            MethodText.setText("method: "+jsonParameters.getString("method"));
        }catch(JSONException ex){
            MethodText.setText("method: unknown");
        }
        try{
            Servings.setText("Servings: "+String.valueOf(jsonParameters.getInt("servings")));
        }catch(JSONException ex){
            Servings.setText("Servings: unknown");
        }
        try{
            WeightPerServing.setText("Weight Per Serving: "+String.valueOf(jsonParameters.getInt("weightPerServing"))+" g");
        }catch(JSONException ex){
            WeightPerServing.setText("Weight Per Serving: undefined");
        }
        try {
            ReadyInMinutes.setText("Ready in Minutes: " + String.valueOf(jsonParameters.getInt("readyInMinutes")));
        }catch(JSONException ex){
            ReadyInMinutes.setText("Ready in Minutes: undefined");
        }

        //setting recipe's image
        InputStream inputStream=null;
        try {
            inputStream = new URL(jsonParameters.getString("image")).openStream();
            RecipeImage.setImage(new Image(inputStream));
        }catch(IOException ie){
            LogManager.getLogger("RecipePageController.class").info("Recipe's image not found");
        }

        //setting default image if no image are found
        if(inputStream==null){
            inputStream=RecipePageController.class.getResourceAsStream("/images/icons/cloche.png");
            ImageView standardIconRecipe=new ImageView(new Image(inputStream));
        }

        //setting the icons
        if(jsonParameters.getBoolean("vegan")){
            inputStream=RecipePageController.class.getResourceAsStream("/images/icons/vegan_on.png");
        }else{
            inputStream=RecipePageController.class.getResourceAsStream("/images/icons/vegan.png");
        }
        VeganIcon.setImage(new Image(inputStream));
        VeganIcon.setPreserveRatio(true);
        VeganIcon.setSmooth(true);
        VeganIcon.setCache(true);

        if(jsonParameters.getBoolean("vegetarian")){
            inputStream=RecipePageController.class.getResourceAsStream("/images/icons/vegetarian_on.png");
        }else{
            inputStream=RecipePageController.class.getResourceAsStream("/images/icons/vegetarian.png");
        }
        VegetarianIcon.setImage(new Image(inputStream));
        VegetarianIcon.setPreserveRatio(true);
        VegetarianIcon.setSmooth(true);
        VegetarianIcon.setCache(true);

        if(jsonParameters.getBoolean("glutenFree")){
            inputStream=RecipePageController.class.getResourceAsStream("/images/icons/gluten_on.png");
        }else{
            inputStream=RecipePageController.class.getResourceAsStream("/images/icons/gluten.png");
        }
        GlutenIcon.setImage(new Image(inputStream));
        GlutenIcon.setPreserveRatio(true);
        GlutenIcon.setSmooth(true);
        GlutenIcon.setCache(true);

        if(jsonParameters.getBoolean("dairyFree")){
            inputStream=RecipePageController.class.getResourceAsStream("/images/icons/dairy_on.png");
        }else{
            inputStream=RecipePageController.class.getResourceAsStream("/images/icons/dairy.png");
        }
        DairyIcon.setImage(new Image(inputStream));
        DairyIcon.setPreserveRatio(true);
        DairyIcon.setSmooth(true);
        DairyIcon.setCache(true);

        //price per serving
        if(jsonParameters.get("pricePerServing")!=null){

            if(jsonParameters.get("pricePerServing") instanceof Integer){
                PriceIcon.setText(Recipe.getPriceSymbol(Double.valueOf(jsonParameters.getInt("pricePerServing"))));
            }
            else{
                PriceIcon.setText(Recipe.getPriceSymbol(jsonParameters.getDouble("pricePerServing")));
            }
        }

        //defining the amount for each nutritional value and add data to the piechart
        Map<String, Integer> divi=new HashMap<>(); //divisor for each unit to transform the value in grams
        divi.put("mg", 1000);
        divi.put("cg", 100);
        divi.put("dg", 10);
        divi.put("g", 1);
        Double amount;
        String name;
        pieData= FXCollections.observableArrayList();
        JSONArray nutrients=jsonParameters.getJSONArray("nutrients");


        for (int i=0; i<nutrients.length(); i++){
            try {
                name = nutrients.getJSONObject(i).getString("name");
                if (name.equals("Calories")) {
                    if (nutrients.getJSONObject(i).get("amount") instanceof Integer) {
                        int kcalAmount = nutrients.getJSONObject(i).getInt("amount");
                        Kcal.setText("Kcal: " + String.valueOf(kcalAmount));
                    } else {
                        double kcalAmount = nutrients.getJSONObject(i).getDouble("amount");
                        Kcal.setText("Kcal: " + String.valueOf(kcalAmount));
                    }
                    continue;
                }
                if (nutrients.getJSONObject(i).get("amount") instanceof Integer) {
                    amount = Double.valueOf(nutrients.getJSONObject(i).getInt("amount"));
                } else {
                    amount = nutrients.getJSONObject(i).getDouble("amount");
                }
                amount = amount / divi.get(nutrients.getJSONObject(i).getString("unit"));
                pieData.add(new PieChart.Data(name, amount));
            }catch(JSONException ex){
                LogManager.getLogger("RecipePageController.class").warn("Recipes: json retrieve failed");
            }
        }
        NutritionsPie.setData(pieData);
        NutritionsPie.setLegendVisible(false);
        NutritionsPie.setTitle("Nutritional Information");
        Likes.setText("Likes: "+String.valueOf(jsonParameters.getInt("likes")));
    }

    public void setField(String fieldName, String jsonKey, String type){
        if(type.equals("string")){

        }
    }
}
