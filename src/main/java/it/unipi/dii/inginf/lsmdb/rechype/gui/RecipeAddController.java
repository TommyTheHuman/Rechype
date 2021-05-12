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
import org.json.JSONArray;
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

     //   ingredients.setDisable(true);

        addIngredientButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                JSONObject par = new JSONObject().put("title", title.getText()).put("imageUrl", imageUrl.getText())
                        .put("servings", servings.getText()).put("readyInMinutes", readyInMinutes.getText()).put("weightPerServing", weightPerServing.getText())
                        .put("pricePerServing", pricePerServing.getText()).put("description", description.getText())
                        .put("method", method.getText()).put("ingredients", ingredients.getText()).put("vegan", vegan.isSelected())
                        .put("dairyFree", dairyFree.isSelected()).put("vegetarian", vegetarian.isSelected())
                        .put("vegetarian", vegetarian.isSelected()).put("glutenFree", glutenFree.isSelected());
                Main.changeScene("IngredientSearch", par);
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

    @Override
    public void setGui(){
        JSONObject par = jsonParameters;
        if(par.toString().equals("{}"))
            return;
        title.setText(par.get("title").toString());
        imageUrl.setText(par.get("imageUrl").toString());
        servings.setText(par.get("servings").toString());
        readyInMinutes.setText(par.get("readyInMinutes").toString());
        weightPerServing.setText(par.get("weightPerServing").toString());
        pricePerServing.setText(par.get("pricePerServing").toString());
        description.setText(par.get("description").toString());
        method.setText(par.get("method").toString());
        vegan.setSelected(par.getBoolean("vegan"));
        dairyFree.setSelected(par.getBoolean("dairyFree"));
        vegetarian.setSelected(par.getBoolean("vegetarian"));
        glutenFree.setSelected(par.getBoolean("glutenFree"));
        ingredients.setText(par.get("ingredients").toString());

    }
}