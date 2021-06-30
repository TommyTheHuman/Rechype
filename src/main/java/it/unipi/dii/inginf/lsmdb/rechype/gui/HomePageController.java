package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.util.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.drink.Drink;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.Recipe;
import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.bson.Document;
import org.json.JSONObject;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;


public class HomePageController extends JSONAdder implements Initializable {

    UserService userService = UserServiceFactory.create().getService();
    GuiElementsBuilder builder = new GuiElementsBuilder();
    @FXML private VBox boxSuggestedRecipes;
    @FXML private VBox boxSuggestedDrinks;
    @FXML private VBox boxSuggestedUsers;
    @FXML private VBox boxBestUsers;
    @FXML private Button reloadButton;
    private static List<Document> recipes;
    private static List<Document> drinks;
    private static List<Document> users;
    private static List<Document> bestUsers;
    private static ObservableList<Node> recipesNodes = FXCollections.observableArrayList();
    private static ObservableList<Node> drinksNodes = FXCollections.observableArrayList();
    private static ObservableList<Node> usersNodes = FXCollections.observableArrayList();
    private static ObservableList<Node> bestRecipesNodes = FXCollections.observableArrayList();
    private static ObservableList<Node> bestDrinksNodes = FXCollections.observableArrayList();
    private static ObservableList<Node> bestUsersNodes = FXCollections.observableArrayList();
    private static ObservableList<Node> bestIngredientsNodes = FXCollections.observableArrayList();
    private Timer timer = new Timer();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    @Override
    public void setGui() {

        //defining the reload button
        reloadButton.setOnAction(event -> {
            userService.setLockSuggestions(false);
            Main.changeScene("HomePage", new JSONObject());
        });

        //saving the suggestions in static memory if is not reloaded
        if (userService.getLockSuggestions()) {
            boxSuggestedRecipes.getChildren().addAll(recipesNodes);
            boxSuggestedDrinks.getChildren().addAll(drinksNodes);
            boxSuggestedUsers.getChildren().addAll(usersNodes);
            boxBestUsers.getChildren().addAll(bestUsersNodes);
            recipesNodes = boxSuggestedRecipes.getChildren();
            drinksNodes = boxSuggestedDrinks.getChildren();
            usersNodes = boxSuggestedUsers.getChildren();
            bestUsersNodes = boxBestUsers.getChildren();
        }
        //load for the first time the suggestion or reloading it
        else {
            recipes = userService.getSuggestedRecipes();
            drinks = userService.getSuggestedDrinks();
            users = userService.getSuggestedUsers();
            bestUsers = userService.getBestUsers();

            //calling global suggestion functions
            for (Document bestUser : bestUsers) {
                boxBestUsers.getChildren().addAll(setUser(new User(bestUser)),
                        new Separator(Orientation.HORIZONTAL));
                bestUsersNodes = boxBestUsers.getChildren();
            }

            //calling all the suggested functions
            for (Document recipe : recipes) {
                boxSuggestedRecipes.getChildren().addAll(setRecipe(new Recipe(recipe)),
                        new Separator(Orientation.HORIZONTAL));
                recipesNodes = boxSuggestedRecipes.getChildren();
            }
            for (Document drink : drinks) {
                boxSuggestedDrinks.getChildren().addAll(setDrink(new Drink(drink)),
                        new Separator(Orientation.HORIZONTAL));
                drinksNodes = boxSuggestedDrinks.getChildren();
            }
            for (Document user : users) {
                boxSuggestedUsers.getChildren().addAll(setUser(new User(user)),
                        new Separator(Orientation.HORIZONTAL));
                usersNodes=boxSuggestedUsers.getChildren();
            }
            userService.setLockSuggestions(true);
        }
    }

    //add the eventListener on the boxes
    private HBox setRecipe(Recipe recipe){
        HBox recipeBlock=builder.createSimpleRecipeBlock(recipe);

        return recipeBlock;
    }

    private HBox setDrink(Drink drink){
        HBox drinkBlock=builder.createSimpleDrinkBlock(drink);
        return drinkBlock;
    }

    private HBox setUser(User user){
        HBox userBlock=builder.createSimpleUserBlock(user);
        return userBlock;
    }

    public static void flushSuggestion(){
        recipesNodes.clear();
        drinksNodes.clear();
        usersNodes.clear();
        bestRecipesNodes.clear();
        bestDrinksNodes.clear();
        bestUsersNodes.clear();
        bestIngredientsNodes.clear();
    }

}
