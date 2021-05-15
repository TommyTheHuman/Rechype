package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.Ingredient;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.IngredientService;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.IngredientServiceFactory;
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
import javafx.scene.text.Text;
import org.json.JSONArray;
import org.json.JSONObject;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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

    @FXML private Text textFieldsError;

    private RecipeServiceFactory recipeServiceFactory;
    private RecipeService recipeService;

    private UserServiceFactory userServiceFactory;
    private UserService userService;

    private IngredientServiceFactory ingredientServiceFactory;
    private IngredientService ingredientService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        ingredientServiceFactory = IngredientServiceFactory.create();
        ingredientService = ingredientServiceFactory.getService();

        recipeServiceFactory = RecipeServiceFactory.create();
        recipeService = recipeServiceFactory.getService();

        userServiceFactory = UserServiceFactory.create();
        userService = userServiceFactory.getService();

        User loggedUser = userService.getLoggedUser();

        ingredients.setDisable(true);
        textFieldsError.setOpacity(0);

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
                if(checkField()){
                    String fieldIngredient = ingredients.getText();
                    String[] singleIngredient = fieldIngredient.trim().split(", ");
                    List<String> ingredientName = new ArrayList<>();
                    for(String ingr: singleIngredient){
                        String[] details = ingr.trim().split(":");
                        ingredientName.add(details[0]);
                    }

                    List<Ingredient> recipeIngredient = ingredientService.getIngredientFromString(ingredientName);

                    Recipe recipe = new Recipe(title.getText(), loggedUser.getUsername(), imageUrl.getText(),
                            description.getText(), method.getText(), ingredients.getText(), vegan.isSelected(), glutenFree.isSelected(),
                            dairyFree.isSelected(), vegetarian.isSelected(), Double.parseDouble(servings.getText()),
                            Double.parseDouble(readyInMinutes.getText()), Double.parseDouble(weightPerServing.getText()),
                            Double.parseDouble(pricePerServing.getText()));

                    recipeService.addRecipe(recipe);
                    textFieldsError.setOpacity(0);

                    // cambio scena, vado sulla visualizzazione della ricetta appena creata
                }
            }
        });
    }

    private boolean checkField(){
        if(title.getText().equals("") || imageUrl.getText().equals("") || servings.getText().equals("")
            || readyInMinutes.getText().equals("") || weightPerServing.getText().equals("") || pricePerServing.getText().equals("")
             ||  description.getText().equals("") || method.getText().equals("") || ingredients.getText().equals("")){
            textFieldsError.setOpacity(100);
            return false;
        }

        return true;
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