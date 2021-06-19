package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.util.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.Recipe;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.RecipeService;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.RecipeServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.ByteArrayInputStream;
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
    @FXML private VBox ingredientsBox;
    @FXML private PieChart NutritionsPie;
    @FXML private ImageView RecipeImage;
    @FXML private ImageView VeganIcon;
    @FXML private ImageView VegetarianIcon;
    @FXML private ImageView DairyIcon;
    @FXML private ImageView GlutenIcon;
    @FXML private Text PriceIcon;
    @FXML private ImageView LikeButton;
    @FXML private ImageView SaveButton;
    private ObservableList<PieChart.Data> pieData;
    private RecipeService recipeService;
    private UserService userService;
    private JSONObject jsonRecipe;
    private String imgLikePath;
    private String imgSavePath;
    private int likes;
    private GuiElementsBuilder builder;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        recipeService = RecipeServiceFactory.create().getService();
        userService = UserServiceFactory.create().getService();
        builder = new GuiElementsBuilder();
    }

    public void setGui(){

        jsonRecipe = new JSONObject(recipeService.searchRecipeById(jsonParameters.getString("_id")).toJson());

        //setting text informations
        try {
            authorLabel.setText("Author: " + jsonRecipe.getString("author"));
        } catch (JSONException ex) {
            authorLabel.setText("Author: unknown");
        }
        try {
            Name.setText(jsonRecipe.getString("name"));
        } catch (JSONException ex) {
            Name.setText("Title: unknown");
        }
        try { //the description is in html, so we need to parse it
            Document htmlParsed = Jsoup.parse(jsonRecipe.getString("description"));
            DescriptionText.setText(htmlParsed.text());
        } catch (JSONException ex) {
            DescriptionText.setText("unknown");
        }
        try {
            MethodText.setText(jsonRecipe.getString("method"));
        } catch (JSONException ex) {
            MethodText.setText("unknown");
        }
        try {
            Servings.setText("Servings: " + jsonRecipe.getInt("servings"));
        } catch (JSONException ex) {
            Servings.setText("Servings: unknown");
        }
        try {
            WeightPerServing.setText("Weight Per Serving: " + jsonRecipe.getInt("weightPerServing") + " g");
        } catch (JSONException ex) {
            WeightPerServing.setText("Weight Per Serving: undefined");
        }
        try {
            ReadyInMinutes.setText("Ready in Minutes: " + jsonRecipe.getInt("readyInMinutes"));
        } catch (JSONException ex) {
            ReadyInMinutes.setText("Ready in Minutes: undefined");
        }

        //setting recipe's image
        byte[] imgBytes=recipeService.getCachedImage(jsonParameters.getString("_id"));
        InputStream inputStream;
        if(imgBytes==null){
            inputStream = GuiElementsBuilder.class.getResourceAsStream("/images/icons/cloche.png");
        }else{
            inputStream = new ByteArrayInputStream(imgBytes);
        }
        RecipeImage.setImage(new Image(inputStream));

        //setting the icons
        if (jsonRecipe.getBoolean("vegan")) {
            inputStream = RecipePageController.class.getResourceAsStream("/images/icons/vegan_on.png");
        } else {
            inputStream = RecipePageController.class.getResourceAsStream("/images/icons/vegan.png");
        }
        VeganIcon.setImage(new Image(inputStream));
        VeganIcon.setPreserveRatio(true);
        VeganIcon.setSmooth(true);
        VeganIcon.setCache(true);

        if (jsonRecipe.getBoolean("vegetarian")) {
            inputStream = RecipePageController.class.getResourceAsStream("/images/icons/vegetarian_on.png");
        } else {
            inputStream = RecipePageController.class.getResourceAsStream("/images/icons/vegetarian.png");
        }
        VegetarianIcon.setImage(new Image(inputStream));
        VegetarianIcon.setPreserveRatio(true);
        VegetarianIcon.setSmooth(true);
        VegetarianIcon.setCache(true);

        if (jsonRecipe.getBoolean("glutenFree")) {
            inputStream = RecipePageController.class.getResourceAsStream("/images/icons/gluten_on.png");
        } else {
            inputStream = RecipePageController.class.getResourceAsStream("/images/icons/gluten.png");
        }
        GlutenIcon.setImage(new Image(inputStream));
        GlutenIcon.setPreserveRatio(true);
        GlutenIcon.setSmooth(true);
        GlutenIcon.setCache(true);

        if (jsonRecipe.getBoolean("dairyFree")) {
            inputStream = RecipePageController.class.getResourceAsStream("/images/icons/dairy_on.png");
        } else {
            inputStream = RecipePageController.class.getResourceAsStream("/images/icons/dairy.png");
        }
        DairyIcon.setImage(new Image(inputStream));
        DairyIcon.setPreserveRatio(true);
        DairyIcon.setSmooth(true);
        DairyIcon.setCache(true);

        //adding the list of ingredients
        JSONArray ingredients = jsonRecipe.getJSONArray("ingredients");
        for(int i=0; i<ingredients.length(); i++){
            ingredientsBox.getChildren().addAll(builder.createSimpleIngredientBlock(ingredients.getJSONObject(i)),
            new Separator(Orientation.HORIZONTAL));
        }

        //price per serving
        if (jsonRecipe.get("pricePerServing") != null) {

            if (jsonRecipe.get("pricePerServing") instanceof Integer) {
                PriceIcon.setText(Recipe.getPriceSymbol(Double.valueOf(jsonRecipe.getInt("pricePerServing"))));
            } else {
                PriceIcon.setText(Recipe.getPriceSymbol(jsonRecipe.getDouble("pricePerServing")));
            }
        }

        //defining the amount for each nutritional value and add data to the piechart
        Map<String, Integer> divi = new HashMap<>(); //divisor for each unit to transform the value in grams
        divi.put("mg", 1000);
        divi.put("cg", 100);
        divi.put("dg", 10);
        divi.put("g", 1);
        Double amount;
        String name;
        pieData = FXCollections.observableArrayList();
        JSONArray nutrients = jsonRecipe.getJSONArray("nutrients");


        for (int i = 0; i < nutrients.length(); i++) {
            try {
                name = nutrients.getJSONObject(i).getString("name");
                if (name.equals("Calories")) {
                    if (nutrients.getJSONObject(i).get("amount") instanceof Integer) {
                        int kcalAmount = nutrients.getJSONObject(i).getInt("amount");
                        Kcal.setText("Kcal: " + kcalAmount);
                    } else {
                        double kcalAmount = nutrients.getJSONObject(i).getDouble("amount");
                        Kcal.setText("Kcal: " + kcalAmount);
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
            } catch (JSONException ex) {
                LogManager.getLogger("RecipePageController.class").warn("Recipes: json retrieve failed");
            }
        }
        NutritionsPie.setData(pieData);
        NutritionsPie.setLegendVisible(false);
        NutritionsPie.setTitle("Nutritional Information");
        likes=jsonRecipe.getInt("likes");
        Likes.setText(String.valueOf(likes));
        LikeButton.setStyle("-fx-cursor: hand;");

        //check if like is already present and then set the right like/like_on image
        if (!userService.checkRecipeLike(jsonParameters.getString("_id"), "recipe")){
            imgLikePath = "/images/icons/likeIcon.png";
        } else {
            imgLikePath = "/images/icons/likeIcon_on.png";
        }
        LikeButton.setImage(new Image(imgLikePath));
        LikeButton.setPreserveRatio(true);
        LikeButton.setSmooth(true);
        LikeButton.setCache(true);

        LikeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (imgLikePath.equals("/images/icons/likeIcon.png")) {
                String res = recipeService.addLike(jsonParameters.getString("_id"), userService.getLoggedUser().getUsername());
                if (res.equals("LikeOk")) {
                    imgLikePath = "/images/icons/likeIcon_on.png";
                    likes=likes+1;
                    Likes.setText(String.valueOf(likes));
                    LikeButton.setImage(new Image(imgLikePath));
                }
            } else {
                String res = recipeService.removeLike(jsonParameters.getString("_id"), userService.getLoggedUser().getUsername());
                if (res.equals("LikeOk")) {
                    imgLikePath = "/images/icons/likeIcon.png";
                    likes=likes-1;
                    Likes.setText(String.valueOf(likes));
                    LikeButton.setImage(new Image(imgLikePath));
                }
            }
            LikeButton.setPreserveRatio(true);
            LikeButton.setSmooth(true);
            LikeButton.setCache(true);
            event.consume();
        });

        //check if the user is the author of the recipe, if it is the favourite button cannot be pressed
        //in that case no logic is needed
        if (userService.getLoggedUser().getUsername().equals(jsonRecipe.getString("author"))) {
            SaveButton.setImage(new Image("/images/icons/saveBtnIcon_on.png"));
            SaveButton.setPreserveRatio(true);
            SaveButton.setSmooth(true);
            SaveButton.setCache(true);
            SaveButton.setStyle("-fx-cursor: hand;");
        }else{
            //check if recipe is already added or not to the user favourites
            if (!userService.checkSavedRecipe(jsonParameters.getString("_id"), "recipe")) {
                imgSavePath = "/images/icons/saveBtnIcon.png";
            } else {
                imgSavePath = "/images/icons/saveBtnIcon_on.png";
            }
            SaveButton.setImage(new Image(imgSavePath));
            SaveButton.setPreserveRatio(true);
            SaveButton.setSmooth(true);
            SaveButton.setCache(true);
            SaveButton.setStyle("-fx-cursor: hand;");

            //Using the same function "addNewRecipe" for creation/add to fav operation
            //The NoParse field is to prevent the parsing from the daemon software, by the fact that there's no consistency
            //to correct
            SaveButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                org.bson.Document recipeDoc = org.bson.Document.parse(jsonRecipe.toString());
                recipeDoc.append("NoParse", true);
                if (imgSavePath.equals("/images/icons/saveBtnIcon.png")) {
                    String res = userService.addNewRecipe(recipeDoc, "recipe");
                    if (res.equals("RecipeOk")) {
                        imgSavePath = "/images/icons/saveBtnIcon_on.png";
                        SaveButton.setImage(new Image(imgSavePath));
                    }
                } else {
                    String res = userService.removeRecipe(jsonParameters.getString("_id"), "recipe");
                    if (res.equals("RecipeOk")) {
                        imgSavePath = "/images/icons/saveBtnIcon.png";
                        SaveButton.setImage(new Image(imgSavePath));
                    }
                }
                SaveButton.setPreserveRatio(true);
                SaveButton.setSmooth(true);
                SaveButton.setCache(true);
                event.consume();
            });
        }
    }
}
