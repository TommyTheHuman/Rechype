package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.drink.Drink;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.Ingredient;
import it.unipi.dii.inginf.lsmdb.rechype.profile.Profile;
import it.unipi.dii.inginf.lsmdb.rechype.profile.ProfileService;
import it.unipi.dii.inginf.lsmdb.rechype.profile.ProfileServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.Recipe;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ResourceBundle;

public class MyProfileController extends JSONAdder implements Initializable {

    @FXML private Button addIngredientBtn;
    @FXML private Button createMealBtn;
    @FXML private VBox vboxMeals;
    @FXML private VBox vboxFridge;
    @FXML private TabPane tabPane;

    private GuiElementsBuilder builder;

    private ProfileServiceFactory profileServiceFactory;
    private ProfileService profileService;

    private UserServiceFactory userServiceFactory;
    private UserService userService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        builder = new GuiElementsBuilder();
        profileServiceFactory = ProfileServiceFactory.create();
        profileService = profileServiceFactory.getService();
        userServiceFactory = UserServiceFactory.create();
        userService = userServiceFactory.getService();
        vboxMeals.setSpacing(30);
        vboxFridge.setSpacing(20);
        Profile profile = profileService.getProfile(userService.getLoggedUser().getUsername());
        for(int i = 0; i < profile.getMeals().length(); i++){
            VBox vboxMeal = new VBox(5);
            Text title = new Text(profile.getMeals().getJSONObject(i).getString("title"));
            title.setFont(Font.font ("Verdana", 20));
            vboxMeal.getChildren().add(title);
            HBox recipeBox = new HBox();
            JSONArray recipes = profile.getMeals().getJSONObject(i).getJSONArray("recipes");
            for(int j = 0; j < recipes.length(); j++){
                Document recipeDoc = Document.parse(recipes.getJSONObject(j).toString());
                Recipe recipe = new Recipe(recipeDoc);
                HBox singleRecipe = builder.createRecipeBlock(recipe);
                singleRecipe.setOnMouseClicked((MouseEvent e) ->{
                    JSONObject par = new JSONObject().put("_id", recipe.getId());
                    Main.changeScene("RecipePage", par);
                });
                recipeBox.getChildren().add(singleRecipe);
                vboxMeal.getChildren().addAll(recipeBox);
                recipeBox = new HBox();
            }

            JSONArray drinks = profile.getMeals().getJSONObject(i).getJSONArray("drinks");
            for(int j = 0; j < drinks.length(); j++){
                Document drinkDoc = Document.parse(drinks.getJSONObject(j).toString());
                Drink drink = new Drink(drinkDoc);
                HBox singleDrink = builder.createDrinkBlock(drink);
                singleDrink.setOnMouseClicked((MouseEvent e) ->{
                    JSONObject par = new JSONObject().put("_id", drink.getId());
                    Main.changeScene("DrinkPage", par);
                });
                recipeBox.getChildren().add(singleDrink);
                vboxMeal.getChildren().addAll(recipeBox);
                recipeBox = new HBox();
            }

            Button deleteBtn = new Button("Delete");

            deleteBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    profileService.deleteMeal(title.getText(), profile.getUsername());
                    vboxMeals.getChildren().remove(vboxMeal);
                }
            });
            vboxMeal.getChildren().addAll(deleteBtn, new Separator(Orientation.HORIZONTAL));
            vboxMeals.getChildren().addAll(vboxMeal);
        }

        for(int i = 0; i < profile.getFridge().length(); i++){
            JSONObject ingredients = profile.getFridge().getJSONObject(i);

            Ingredient ingr = new Ingredient(ingredients.getString("name"), ingredients.getString("image"), ingredients.getDouble("quantity"));

            HBox ingrBox = builder.createIngredientBlock(ingr, null);
            ingrBox.setOnMouseClicked(null);

            Button deleteBtn = new Button("Delete");
            VBox container = new VBox(ingrBox, deleteBtn, new Separator(Orientation.HORIZONTAL));
            deleteBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    profileService.deleteIngredient(profile.getUsername(), ingredients.getString("name"));
                    vboxFridge.getChildren().removeAll(container);
                }
            });

            vboxFridge.getChildren().addAll(container);
        }

        addIngredientBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Main.changeScene("IngredientSearchFridge", new JSONObject());
            }
        });

        createMealBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Main.changeScene("MealAdd", new JSONObject());
            }
        });
    }

    @Override
    public void setGui(){
        if(jsonParameters != null && jsonParameters.has("changeTab")){
            tabPane.getSelectionModel().select(1);
        }
    }
}
