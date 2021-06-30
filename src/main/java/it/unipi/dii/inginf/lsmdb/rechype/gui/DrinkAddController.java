package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.util.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.drink.DrinkService;
import it.unipi.dii.inginf.lsmdb.rechype.drink.DrinkServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.Ingredient;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.IngredientService;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.IngredientServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;
import org.json.JSONObject;

import java.net.URL;
import java.util.*;

public class DrinkAddController extends JSONAdder implements Initializable {
    @FXML private TextField title;
    @FXML private TextField imageUrl;
    @FXML private Button addIngredientButton;
    @FXML private Button addDrinkButton;

    @FXML private TextArea description;
    @FXML private TextArea method;
    @FXML private TextArea ingredients;
    @FXML private ComboBox ComboBoxTag;

    @FXML private Text textFieldsError;

    private DrinkService drinkService;

    private UserService userService;

    private IngredientService ingredientService;

    public void initialize(URL location, ResourceBundle resources) {
        ingredientService = IngredientServiceFactory.create().getService();

        drinkService = DrinkServiceFactory.create().getService();

        userService = UserServiceFactory.create().getService();

        User loggedUser = userService.getLoggedUser();

        ingredients.setDisable(true);
        textFieldsError.setOpacity(0);

        List<String> tagList = new ArrayList<>();
        tagList.add("beer");
        tagList.add("cocktail");
        tagList.add("other");
        ComboBoxTag.setItems(FXCollections.observableArrayList(tagList));

        addIngredientButton.setOnAction(event -> {
            JSONObject par = new JSONObject()
                    .put("Drink", "true")
                    .put("title", title.getText())
                    .put("imageUrl", imageUrl.getText())
                    .put("description", description.getText()).put("method", method.getText())
                    .put("ingredients", ingredients==null?"":ingredients.getText())
                    .put("tag", ComboBoxTag.getValue()==null? "": ComboBoxTag.getValue());
            Main.changeScene("IngredientSearch", par);
        });

        addDrinkButton.setOnAction(event -> {
            if (checkField()) {
                // Get ingredient name and amount inserted
                String fieldIngredient = ingredients.getText();
                String[] singleIngredient = fieldIngredient.trim().split(", ");
                List<String> ingredientNames = new ArrayList<>();
                Map<String, String> amount = new HashMap<>();
                for (String ingr : singleIngredient) {
                    String[] details = ingr.trim().split(":");
                    ingredientNames.add(details[0]);
                    details[1] = details[1].replaceAll("\\s+", "");
                    amount.put(details[0], details[1]);
                }

                JSONObject jsonIngredient;
                List<Document> docIngredients = new ArrayList<>();
                Ingredient auxIngr;

                List<Ingredient> ingredientsList=ingredientService.searchIngredientsList(ingredientNames);
                for(Ingredient ingr: ingredientsList){
                    docIngredients.add(new Document().append("ingredient", ingr.getName()).append("amount", amount.get(ingr.getName())));
                }

                //doc of drink to be inserted
                Document doc = new Document().append("name", title.getText()).append("author", loggedUser.getUsername())
                        .append("description", description.getText()).append("image", imageUrl.getText())
                        .append("method", method.getText()).append("ingredients", docIngredients).append("likes", 0)
                        .append("tag", ComboBoxTag.getValue());

                    // check on correct drink add
                    textFieldsError.setOpacity(0);
                    if (drinkService.addDrink(doc).equals("DrinkAdded")) {
                        userService.addNewRecipe(doc, "drink");
                        drinkService.putDrinkInCache(doc);
                        JSONObject par = new JSONObject().put("_id", doc.getString("_id"));
                        Main.changeScene("DrinkPage", par);
                    } else {
                        LogManager.getLogger("DrinkAddController.class").error("MongoDB: failed to insert drink");
                    }
            }
        });

    }
    private boolean checkField(){
        if(title.getText().equals("") || imageUrl.getText().equals("") ||
            description.getText().equals("") || method.getText().equals("") ||
            ingredients.getText().equals("") || ComboBoxTag.getSelectionModel().isEmpty()){
                textFieldsError.setOpacity(100);
                return false;
        }
        return true;
    }

    public void setGui(){
        JSONObject par = jsonParameters;
        if(par.toString().equals("{}"))
            return;
        title.setText(par.get("title").toString());
        imageUrl.setText(par.get("imageUrl").toString());
        description.setText(par.get("description").toString());
        method.setText(par.get("method").toString());
        ingredients.setText(par.get("ingredients").toString());
        ComboBoxTag.setValue(par.get("tag").toString());
    }
}
