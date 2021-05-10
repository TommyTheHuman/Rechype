package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.Recipe;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.RecipeService;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.RecipeServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import org.json.JSONObject;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class RecipeAddController extends JSONAdder implements Initializable {

    @FXML private Button addIngredientButton;
    @FXML private Button addRecipeButton;

    @FXML private TextField title;
    @FXML private TextField imageUrl;
    @FXML private TextField servings;
    @FXML private TextField readyInMinutes;
    @FXML private TextField weightPerServing;
    @FXML private TextField pricePerServing;

    @FXML private TextArea description;
    @FXML private TextArea method;
    @FXML private TextArea ingredients;

    @FXML private CheckBox vegan;
    @FXML private CheckBox dairyFree;
    @FXML private CheckBox vegetarian;
    @FXML private CheckBox glutenFree;

    private RecipeServiceFactory recipeServiceFactory;
    private RecipeService recipeService;

    private UserServiceFactory userServiceFactory;
    private UserService userService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        recipeServiceFactory = RecipeServiceFactory.create();
        recipeService = recipeServiceFactory.getService();

        userServiceFactory = UserServiceFactory.create();
        userService = userServiceFactory.getService();

        User loggedUser = userService.getLoggedUser();


        addIngredientButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Main.changeScene("IngredientSearch", new JSONObject());

            }
        });

        addRecipeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                Recipe recipe = new Recipe(title.getText(), loggedUser.getUsername(), imageUrl.getText(),
                        description.getText(), method.getText(), ingredients.getText(), vegan.isSelected(), glutenFree.isSelected(),
                        dairyFree.isSelected(), vegetarian.isSelected(), Double.parseDouble(servings.getText()),
                        Double.parseDouble(readyInMinutes.getText()), Double.parseDouble(weightPerServing.getText()),
                    Double.parseDouble(pricePerServing.getText()));

                recipeService.addRecipe(recipe);

            }
        });

    }
}