package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.drink.DrinkService;
import it.unipi.dii.inginf.lsmdb.rechype.drink.DrinkServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.RecipeServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
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
import java.util.ResourceBundle;

public class DrinkPageController extends JSONAdder implements Initializable {

    @FXML private Text authorLabel;
    @FXML private Text Name;
    @FXML private Text DescriptionText;
    @FXML private Text Likes;
    @FXML private Text MethodText;
    @FXML private ImageView DrinkImage;
    @FXML private ImageView LikeButton;
    @FXML private ImageView SaveButton;
    @FXML private ScrollPane ScrollIngredients;
    @FXML private VBox ingredientsBox;
    @FXML private ImageView beerIcon;
    @FXML private ImageView cocktailIcon;
    @FXML private ImageView otherIcon;
    private DrinkServiceFactory drinkServiceFactory;
    private DrinkService drinkService;
    private UserServiceFactory userServiceFactory;
    private UserService userService;
    private GuiElementsBuilder builder;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        drinkServiceFactory = DrinkServiceFactory.create();
        drinkService = drinkServiceFactory.getService();
        userServiceFactory = UserServiceFactory.create();
        userService = userServiceFactory.getService();
        builder = new GuiElementsBuilder();
    }

    public void setGui() {
        JSONObject jsonDrink = drinkService.getCachedDrink(jsonParameters.getString("_id"));

        try {
            authorLabel.setText("Author: " + jsonDrink.getString("author"));
        }catch(JSONException jex){
            authorLabel.setText("Author: unknown");
        }
        try{
            Name.setText("Title: "+jsonDrink.getString("name"));
        }catch(JSONException jex){
            Name.setText("Title: unknown");
        }
        try {
            Document htmlParsed = Jsoup.parse(jsonDrink.getString("description"));
            DescriptionText.setText("description: " + htmlParsed.text());
        }catch(JSONException jex){
            DescriptionText.setText("description: unknown");
        }
        try{
            MethodText.setText(jsonDrink.getString("method"));
        }catch(JSONException jex){
            MethodText.setText("method: unknown");
        }

        //setting the drink's image
        InputStream inputStreamImg = null;
        try {
            inputStreamImg = new URL(jsonDrink.getString("image")).openStream();
            DrinkImage.setImage(new Image(inputStreamImg));
        } catch (IOException ie) {
            LogManager.getLogger("RecipePageController.class").info("Recipe's image not found");
        }

        //setting default image if no image are found
        if (inputStreamImg == null) {
            inputStreamImg = DrinkPageController.class.getResourceAsStream("/images/icons/cloche.png");
            DrinkImage.setImage(new Image(inputStreamImg));
        }

        //setting likes
        Likes.setText("Likes: "+String.valueOf(jsonDrink.getInt("likes")));

        //adding the list of ingredients
        JSONArray ingredients = jsonDrink.getJSONArray("ingredients");
        for(int i=0; i<ingredients.length(); i++){
            ingredientsBox.getChildren().addAll(builder.createSimpleIngredientBlock(ingredients.getJSONObject(i)),
            new Separator(Orientation.HORIZONTAL));
        }

        //setting the icons
        if(jsonDrink.getString("tag").equals("other")){
            InputStream inputStream =
            DrinkPageController.class.getResourceAsStream("/images/icons/milkshakeIcon_on.png");
            otherIcon.setImage(new Image(inputStream));
            inputStream=
            DrinkPageController.class.getResourceAsStream("/images/icons/beerIcon.png");
            beerIcon.setImage(new Image(inputStream));
            inputStream=
            DrinkPageController.class.getResourceAsStream("/images/icons/cocktailIcon.png");
            cocktailIcon.setImage(new Image(inputStream));
        }
        else if(jsonDrink.getString("tag").equals("beer")){
            InputStream inputStream =
            DrinkPageController.class.getResourceAsStream("/images/icons/milkshakeIcon.png");
            otherIcon.setImage(new Image(inputStream));
            inputStream=
            DrinkPageController.class.getResourceAsStream("/images/icons/beerIcon_on.png");
            beerIcon.setImage(new Image(inputStream));
            inputStream=
            DrinkPageController.class.getResourceAsStream("/images/icons/cocktailIcon.png");
            cocktailIcon.setImage(new Image(inputStream));
        }
        else if(jsonDrink.getString("tag").equals("cocktail")){
            InputStream inputStream =
            DrinkPageController.class.getResourceAsStream("/images/icons/milkshakeIcon.png");
            otherIcon.setImage(new Image(inputStream));
            inputStream=
            DrinkPageController.class.getResourceAsStream("/images/icons/beerIcon.png");
            beerIcon.setImage(new Image(inputStream));
            inputStream=
            DrinkPageController.class.getResourceAsStream("/images/icons/cocktailIcon_on.png");
            cocktailIcon.setImage(new Image(inputStream));
        }


    }
}
