package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.JSONAdder;
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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import org.bson.Document;
import org.decimal4j.util.DoubleRounder;
import org.json.JSONObject;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.math.RoundingMode;

import javax.print.Doc;
import java.net.URL;
import java.util.*;

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
                    Map<String, Integer> divi=new HashMap<>(); //divisor for each unit to transform the value in grams
                    divi.put("mg", 1000);
                    divi.put("cg", 100);
                    divi.put("dg", 10);
                    divi.put("g", 1);
                    Double auxAmount;

                    // Get ingredient name and amount inserted
                    String fieldIngredient = ingredients.getText();
                    String[] singleIngredient = fieldIngredient.trim().split(", ");
                    List<String> ingredientNames = new ArrayList<>();
                    List<Double> amount = new ArrayList<>();
                    for(String ingr: singleIngredient){
                        String[] details = ingr.trim().split(":");
                        ingredientNames.add(details[0]);
                        details[1] = details[1].replaceAll("\\s+","");
                        details[1] = details[1].substring(0, details[1].length()-1);
                        amount.add(Double.parseDouble(details[1]));
                    }

                    List<Ingredient> cachedIngredients = new ArrayList<>();
                    JSONObject jsonIngredient;
                    List<Document> docIngredients = new ArrayList<>();
                    List<Document> docNutrients = new ArrayList<>();
                    Ingredient auxIngr;

                    Integer counter = 0;
                    // Insert ingredients composed by (name, amount) in a Document
                    for(String ingr: ingredientNames){
                        jsonIngredient = ingredientService.getCachedIngredient(ingr);
                        auxIngr = new Ingredient(jsonIngredient);
                        cachedIngredients.add(auxIngr);
                        docIngredients.add(new Document().append("name", auxIngr.getName()).append("amount", amount.get(counter)));
                        counter++;
                    }

                    String nutrName;
                    Double nutrAmount;
                    String nutrUnit;
                    Double[] nutrientsTotAmount = {0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d};
                    counter = 0;
                    // Get the sum of nutrients of all ingredients selected
                    for(Ingredient ingr: cachedIngredients){
                        if(ingr.getNutrients() != null){
                            for(int i=0; i<ingr.getNutrients().size(); i++) {
                                nutrName = ingr.getNutrients().get(i).getNutrName();
                                nutrAmount = ingr.getNutrients().get(i).getNutrAmount();
                                nutrUnit = ingr.getNutrients().get(i).getNutrUnit();
                                if (!nutrUnit.equals("kcal")) {
                                    // Convert in grams
                                    nutrAmount = nutrAmount / divi.get(nutrUnit);
                                }
                                if (nutrName.equals("Fiber")) {
                                    // Sum nutrients considering that nutrAmount is for 100g
                                    nutrientsTotAmount[0] += nutrAmount * amount.get(counter) / 100;
                                }
                                if (nutrName.equals("Carbohydrates")) {
                                    nutrientsTotAmount[1] += nutrAmount * amount.get(counter) / 100;
                                }
                                if (nutrName.equals("Calories")) {
                                    nutrientsTotAmount[2] += nutrAmount * amount.get(counter) / 100;
                                }
                                if (nutrName.equals("Sugar")) {
                                    nutrientsTotAmount[3] += nutrAmount * amount.get(counter) / 100;
                                }
                                if (nutrName.equals("Fat")) {
                                    nutrientsTotAmount[4] += nutrAmount * amount.get(counter) / 100;
                                }
                                if (nutrName.equals("Calcium")) {
                                    nutrientsTotAmount[5] += nutrAmount * amount.get(counter) / 100;
                                }
                                if (nutrName.equals("Protein")) {
                                    nutrientsTotAmount[6] += nutrAmount * amount.get(counter) / 100;
                                }
                            }
                        }
                        counter++;
                    }

                    String[] nutrNames = {"Fiber","Carbohydrates","Calories","Sugar","Fat","Calcium","Protein"};

                    for(Integer i = 0; i<7; i++) {
                        if(nutrNames[i].equals("Calories")){
                            docNutrients.add(new Document().append("name", nutrNames[i]).append("amount", DoubleRounder.round(nutrientsTotAmount[i],2)).append("unit", "kcal"));
                        }else{
                            docNutrients.add(new Document().append("name", nutrNames[i]).append("amount", DoubleRounder.round(nutrientsTotAmount[i],2)).append("unit", "g"));
                        }
                    }

                    Document doc = new Document().append("name", title.getText()).append("author", loggedUser.getUsername())
                            .append("vegetarian", vegetarian.isSelected()).append("vegan", vegan.isSelected()).append("glutenFree", glutenFree.isSelected())
                            .append("dairyFree", dairyFree.isSelected()).append("pricePerServing", DoubleRounder.round(Double.parseDouble(pricePerServing.getText()),1))
                            .append("weightPerServing", Double.parseDouble(weightPerServing.getText()))
                            .append("servings", Double.parseDouble(servings.getText())).append("image", imageUrl.getText())
                            .append("description", description.getText()).append("readyInMinutes", Double.parseDouble(readyInMinutes.getText()))
                            .append("method", method.getText()).append("likes", 0).append("ingredients", docIngredients)
                            .append("nutrients", docNutrients);

                    textFieldsError.setOpacity(0);
                    if(recipeService.addRecipe(doc).equals("RecipeAdded")) {
                        userService.addNewRecipe(doc);
                        HaloDBDriver.getObject().flush();
                        recipeService.putRecipeInCache(doc);
                        JSONObject par = new JSONObject().put("_id", doc.getString("_id"));
                        Main.changeScene("RecipePage", par);
                    }else{
                        //error on recipeAdd
                    }
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