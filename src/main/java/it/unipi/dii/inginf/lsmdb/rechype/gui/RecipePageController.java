package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.recipe.Recipe;
import it.unipi.dii.inginf.lsmdb.rechype.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.RecipeService;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.RecipeServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
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
    @FXML private Button LikeButton;
    private ObservableList<PieChart.Data> pieData;
    private RecipeServiceFactory recipeServiceFactory;
    private RecipeService recipeService;
    private UserServiceFactory userServiceFactory;
    private UserService userService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        recipeServiceFactory = RecipeServiceFactory.create();
        recipeService = recipeServiceFactory.getService();
        userServiceFactory = UserServiceFactory.create();
        userService = userServiceFactory.getService();
    }

    public void setGui(){
        //retrieving the recipe from key-value
        JSONObject jsonRecipe = recipeService.getCachedRecipe(jsonParameters.getString("_id"));

        //setting text informations
        try{
            authorLabel.setText("Author: "+jsonRecipe.getString("author"));
        }catch(JSONException ex){
            authorLabel.setText("Author: unknown");
        }
        try{
            Name.setText("title: "+jsonRecipe.getString("name"));
        }catch(JSONException ex){
            Name.setText("title: unknown");
        }
        try{ //the description is in html, so we need to parse it
            Document htmlParsed=Jsoup.parse(jsonRecipe.getString("description"));
            DescriptionText.setText("description: "+htmlParsed.text());
        }catch(JSONException ex){
            DescriptionText.setText("description: unknown");
        }
        try{
            MethodText.setText("method: "+jsonRecipe.getString("method"));
        }catch(JSONException ex){
            MethodText.setText("method: unknown");
        }
        try{
            Servings.setText("Servings: "+String.valueOf(jsonRecipe.getInt("servings")));
        }catch(JSONException ex){
            Servings.setText("Servings: unknown");
        }
        try{
            WeightPerServing.setText("Weight Per Serving: "+String.valueOf(jsonRecipe.getInt("weightPerServing"))+" g");
        }catch(JSONException ex){
            WeightPerServing.setText("Weight Per Serving: undefined");
        }
        try {
            ReadyInMinutes.setText("Ready in Minutes: " + String.valueOf(jsonRecipe.getInt("readyInMinutes")));
        }catch(JSONException ex){
            ReadyInMinutes.setText("Ready in Minutes: undefined");
        }

        //setting recipe's image
        InputStream inputStream=null;
        try {
            inputStream = new URL(jsonRecipe.getString("image")).openStream();
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
        if(jsonRecipe.getBoolean("vegan")){
            inputStream=RecipePageController.class.getResourceAsStream("/images/icons/vegan_on.png");
        }else{
            inputStream=RecipePageController.class.getResourceAsStream("/images/icons/vegan.png");
        }
        VeganIcon.setImage(new Image(inputStream));
        VeganIcon.setPreserveRatio(true);
        VeganIcon.setSmooth(true);
        VeganIcon.setCache(true);

        if(jsonRecipe.getBoolean("vegetarian")){
            inputStream=RecipePageController.class.getResourceAsStream("/images/icons/vegetarian_on.png");
        }else{
            inputStream=RecipePageController.class.getResourceAsStream("/images/icons/vegetarian.png");
        }
        VegetarianIcon.setImage(new Image(inputStream));
        VegetarianIcon.setPreserveRatio(true);
        VegetarianIcon.setSmooth(true);
        VegetarianIcon.setCache(true);

        if(jsonRecipe.getBoolean("glutenFree")){
            inputStream=RecipePageController.class.getResourceAsStream("/images/icons/gluten_on.png");
        }else{
            inputStream=RecipePageController.class.getResourceAsStream("/images/icons/gluten.png");
        }
        GlutenIcon.setImage(new Image(inputStream));
        GlutenIcon.setPreserveRatio(true);
        GlutenIcon.setSmooth(true);
        GlutenIcon.setCache(true);

        if(jsonRecipe.getBoolean("dairyFree")){
            inputStream=RecipePageController.class.getResourceAsStream("/images/icons/dairy_on.png");
        }else{
            inputStream=RecipePageController.class.getResourceAsStream("/images/icons/dairy.png");
        }
        DairyIcon.setImage(new Image(inputStream));
        DairyIcon.setPreserveRatio(true);
        DairyIcon.setSmooth(true);
        DairyIcon.setCache(true);

        //price per serving
        if(jsonRecipe.get("pricePerServing")!=null){

            if(jsonRecipe.get("pricePerServing") instanceof Integer){
                PriceIcon.setText(Recipe.getPriceSymbol(Double.valueOf(jsonRecipe.getInt("pricePerServing"))));
            }
            else{
                PriceIcon.setText(Recipe.getPriceSymbol(jsonRecipe.getDouble("pricePerServing")));
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
        JSONArray nutrients=jsonRecipe.getJSONArray("nutrients");


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
        Likes.setText("Likes: "+String.valueOf(jsonRecipe.getInt("likes")));
        //Like Button event
        LikeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                recipeService.addLike(jsonRecipe.getJSONObject("_id"), userService.getLoggedUser().getUsername());
            }
        });
    }
}
