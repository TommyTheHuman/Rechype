package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.util.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.drink.Drink;
import it.unipi.dii.inginf.lsmdb.rechype.drink.DrinkService;
import it.unipi.dii.inginf.lsmdb.rechype.drink.DrinkServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.Ingredient;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.IngredientService;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.IngredientServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.HaloDBDriver;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.Recipe;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.RecipeService;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.RecipeServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.bson.Document;
import org.json.JSONObject;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class HomePageController extends JSONAdder implements Initializable {

    UserService userService = UserServiceFactory.create().getService();
    RecipeService recipeService = RecipeServiceFactory.create().getService();
    DrinkService drinkService = DrinkServiceFactory.create().getService();
    IngredientService ingredientService = IngredientServiceFactory.create().getService();
    GuiElementsBuilder builder = new GuiElementsBuilder();
    @FXML private VBox boxSuggestedRecipes;
    @FXML private VBox boxSuggestedDrinks;
    @FXML private VBox boxSuggestedUsers;
    @FXML private VBox boxBestRecipes;
    @FXML private VBox boxBestDrinks;
    @FXML private VBox boxBestUsers;
    @FXML private VBox boxBestIngredients;
    @FXML private Button reloadButton;
    private static List<Document> recipes;
    private static List<Document> drinks;
    private static List<Document> users;
    private static List<Document> bestRecipes;
    private static List<Document> bestDrinks;
    private static List<Document> bestUsers;
    private static List<Document> bestIngredients;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    @Override
    public void setGui() {

        if(!userService.getLockSuggestions()){
            recipes=userService.getSuggestedRecipes();
            drinks=userService.getSuggestedDrinks();
            users=userService.getSuggestedUsers();
            bestRecipes=recipeService.getBestRecipes();
            bestDrinks=drinkService.getBestDrinks();
            bestUsers=userService.getBestUsers();
            bestIngredients=ingredientService.getBestIngredients();
            userService.setLockSuggestions(true);
        }

        //defining the reload button
        reloadButton.setOnAction(event -> {
            userService.setLockSuggestions(false);
            Main.changeScene("HomePage", new JSONObject());
        });

        for (Document recipe : recipes) {
            boxSuggestedRecipes.getChildren().addAll(setRecipe(new Recipe(recipe)),
                    new Separator(Orientation.HORIZONTAL));
        }
        for (Document drink : drinks) {
            boxSuggestedDrinks.getChildren().addAll(setDrink(new Drink(drink)),
                    new Separator(Orientation.HORIZONTAL));
        }
        for (Document user : users) {
            boxSuggestedUsers.getChildren().addAll(setUser(new User(user)),
                    new Separator(Orientation.HORIZONTAL));
        }
        for (Document bestRecipe : bestRecipes) {
            boxBestRecipes.getChildren().addAll(setRecipe(new Recipe(bestRecipe)),
                    new Separator(Orientation.HORIZONTAL));
        }
        for (Document bestDrink : bestDrinks) {
            boxBestDrinks.getChildren().addAll(setDrink(new Drink(bestDrink)),
                    new Separator(Orientation.HORIZONTAL));
        }
        for (Document bestUser : bestUsers) {
            boxBestUsers.getChildren().addAll(setUser(new User(bestUser)),
                    new Separator(Orientation.HORIZONTAL));
        }
        for (Document bestIngredient : bestIngredients) {
            boxBestIngredients.getChildren().addAll(setIngredient(new Ingredient(bestIngredient)),
                    new Separator(Orientation.HORIZONTAL));
        }
    }

    private HBox setRecipe(Recipe recipe){
        HBox recipeBlock=builder.createRecipeBlock(recipe);
        recipeBlock.setOnMouseClicked((MouseEvent e) ->{
            JSONObject par = new JSONObject().put("_id", recipe.getId());
            Main.changeScene("RecipePage", par);
        });
        return recipeBlock;
    }

    private HBox setDrink(Drink drink){
        HBox drinkBlock=builder.createDrinkBlock(drink);
        drinkBlock.setOnMouseClicked((MouseEvent e) ->{
            JSONObject par = new JSONObject().put("_id", drink.getId());
            Main.changeScene("DrinkPage", par);
        });
        return drinkBlock;
    }

    private HBox setUser(User user){
        HBox userBlock=builder.createUserBlock(user);
        userBlock.setOnMouseClicked((MouseEvent e) ->{
            JSONObject par = new JSONObject().put("_id", user.getUsername());
            Main.changeScene("UserProfile", par);
        });
        return userBlock;
    }

    private HBox setIngredient(Ingredient ingredient){
        HBox ingredientBlock=builder.createSimpleIngredientBlock(
        new JSONObject().put("ingredient", ingredient.getName()).put("image", ingredient.getImageUrl()));
        return ingredientBlock;
    }

}
