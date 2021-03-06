package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.util.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.drink.DrinkService;
import it.unipi.dii.inginf.lsmdb.rechype.drink.DrinkServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.ByteArrayInputStream;
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
    @FXML private VBox ingredientsBox;
    @FXML private ImageView beerIcon;
    @FXML private ImageView cocktailIcon;
    @FXML private ImageView otherIcon;
    private DrinkService drinkService;
    private UserService userService;
    private GuiElementsBuilder builder;
    private String imgLikePath;
    private String imgSavePath;
    private int likes;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        drinkService = DrinkServiceFactory.create().getService();
        userService = UserServiceFactory.create().getService();
        builder = new GuiElementsBuilder();
    }

    public void setGui() {
        JSONObject jsonDrink
        = new JSONObject(drinkService.searchDrinkById(jsonParameters.getString("_id")).toJson());

        //set all the drink fields
        try {
            authorLabel.setText("Author: " + jsonDrink.getString("author"));
        }catch(JSONException jex){
            authorLabel.setText("Author: unknown");
        }
        try{
            Name.setText(jsonDrink.getString("name"));
        }catch(JSONException jex){
            Name.setText("Title: unknown");
        }
        try {
            Document htmlParsed = Jsoup.parse(jsonDrink.getString("description"));
            DescriptionText.setText(htmlParsed.text());
        }catch(JSONException jex){
            DescriptionText.setText("unknown");
        }
        try{
            MethodText.setText(jsonDrink.getString("method"));
        }catch(JSONException jex){
            MethodText.setText("unknown");
        }

        //setting the drink's image
        InputStream inputStreamImg;
        byte[] imgBytes=drinkService.getCachedImage(jsonParameters.getString("_id"));
        if(imgBytes==null){
            inputStreamImg = GuiElementsBuilder.class.getResourceAsStream("/images/icons/cloche.png");
        }else{
            inputStreamImg = new ByteArrayInputStream(imgBytes);
        }
        DrinkImage.setImage(new Image(inputStreamImg));

        //adding the list of ingredients
        JSONArray ingredients = jsonDrink.getJSONArray("ingredients");
        for(int i=0; i<ingredients.length(); i++){
            ingredientsBox.getChildren().addAll(builder.createSimpleIngredientBlock(ingredients.getJSONObject(i)),
            new Separator(Orientation.HORIZONTAL));
        }

        //setting the icons
        switch (jsonDrink.getString("tag")) {
            case "other": {
                InputStream inputStream =
                        DrinkPageController.class.getResourceAsStream("/images/icons/milkshakeIcon_on.png");
                otherIcon.setImage(new Image(inputStream));
                inputStream =
                        DrinkPageController.class.getResourceAsStream("/images/icons/beerIcon.png");
                beerIcon.setImage(new Image(inputStream));
                inputStream =
                        DrinkPageController.class.getResourceAsStream("/images/icons/cocktailIcon.png");
                cocktailIcon.setImage(new Image(inputStream));
                break;
            }
            case "beer": {
                InputStream inputStream =
                        DrinkPageController.class.getResourceAsStream("/images/icons/milkshakeIcon.png");
                otherIcon.setImage(new Image(inputStream));
                inputStream =
                        DrinkPageController.class.getResourceAsStream("/images/icons/beerIcon_on.png");
                beerIcon.setImage(new Image(inputStream));
                inputStream =
                        DrinkPageController.class.getResourceAsStream("/images/icons/cocktailIcon.png");
                cocktailIcon.setImage(new Image(inputStream));
                break;
            }
            case "cocktail": {
                InputStream inputStream =
                        DrinkPageController.class.getResourceAsStream("/images/icons/milkshakeIcon.png");
                otherIcon.setImage(new Image(inputStream));
                inputStream =
                        DrinkPageController.class.getResourceAsStream("/images/icons/beerIcon.png");
                beerIcon.setImage(new Image(inputStream));
                inputStream =
                        DrinkPageController.class.getResourceAsStream("/images/icons/cocktailIcon_on.png");
                cocktailIcon.setImage(new Image(inputStream));
                break;
            }
        }

        //setting likes
        likes=jsonDrink.getInt("likes");
        Likes.setText(String.valueOf(likes));
        LikeButton.setStyle("-fx-cursor: hand;");

        //setting like events
        if (!userService.checkRecipeLike(jsonParameters.getString("_id"), "drink")) { //mod
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
                String res = drinkService.addLike(jsonParameters.getString("_id"), userService.getLoggedUser().getUsername());
                if (res.equals("LikeOk")) {
                    imgLikePath = "/images/icons/likeIcon_on.png";
                    likes=likes+1;
                    Likes.setText(String.valueOf(likes));
                    LikeButton.setImage(new Image(imgLikePath));
                }
            } else {
                String res = drinkService.removeLike(jsonParameters.getString("_id"), userService.getLoggedUser().getUsername());
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
        if (userService.getLoggedUser().getUsername().equals(jsonDrink.getString("author"))) {
            SaveButton.setImage(new Image("/images/icons/saveBtnIcon_on.png"));
            SaveButton.setPreserveRatio(true);
            SaveButton.setSmooth(true);
            SaveButton.setCache(true);
            SaveButton.setStyle("-fx-cursor: hand;");
        }else{
            //check if recipe is already added or not to the user favourites
            if (!userService.checkSavedRecipe(jsonParameters.getString("_id"), "drink")) { //mod
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
                org.bson.Document drinkDoc = org.bson.Document.parse(jsonDrink.toString());
                drinkDoc.append("NoParse", true);
                if (imgSavePath.equals("/images/icons/saveBtnIcon.png")) {
                    String res = userService.addNewRecipe(drinkDoc, "drink"); //mod
                    if (res.equals("RecipeOk")){
                        imgSavePath = "/images/icons/saveBtnIcon_on.png";
                        SaveButton.setImage(new Image(imgSavePath));
                    }
                } else {
                    String res = userService.removeRecipe(jsonParameters.getString("_id"), "drink"); //mod
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
