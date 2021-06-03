package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.util.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.drink.Drink;
import it.unipi.dii.inginf.lsmdb.rechype.profile.ProfileService;
import it.unipi.dii.inginf.lsmdb.rechype.profile.ProfileServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.Recipe;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.bson.Document;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AddMealController extends JSONAdder implements Initializable {

    @FXML private ComboBox mealType;
    @FXML private VBox recipesBox;
    @FXML private VBox drinksBox;
    @FXML private VBox selectedRecipesVBox;
    @FXML private TextField mealTitle;
    @FXML private Button saveMealButton;
    @FXML private Text errorMsg;

    private UserService userService;
    private ProfileService profileService;

    private List<Document> recipesSelected;
    private List<Document> drinksSelected;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        userService = UserServiceFactory.create().getService();

        profileService = ProfileServiceFactory.create().getService();

        GuiElementsBuilder builder = new GuiElementsBuilder();
        recipesSelected = new ArrayList<>();
        drinksSelected = new ArrayList<>();

        mealType.setItems(FXCollections.observableArrayList("Breakfast", "Brunch", "Lunch", "Appetizer", "Dinner"));
        errorMsg.setOpacity(0);

        // Display user's recipes.
        recipesBox.getChildren().clear();
        Document docUser = userService.getRecipeAndDrinks(userService.getLoggedUser().getUsername());
        List<Document> recipeDocs = (List<Document>) docUser.get("recipes");
        for(Document doc: recipeDocs){
            Recipe recipe = new Recipe(doc);
            HBox hbox = builder.createRecipeBlock(recipe);
            hbox.setOnMouseClicked(event -> {
                recipesSelected.add(doc);
                selectedRecipesVBox.getChildren().add(hbox);
                hbox.setOnMouseClicked(null);
            });
            recipesBox.getChildren().add(hbox);
        }

        List<Document> drinkDocs = (List<Document>) docUser.get("drinks");

        for(Document doc: drinkDocs){
            Drink drink = new Drink(doc);

            HBox hbox = builder.createDrinkBlock(drink);
            hbox.setOnMouseClicked(event -> {
                drinksSelected.add(doc);
                selectedRecipesVBox.getChildren().add(hbox);
                hbox.setOnMouseClicked(null);
            });
            drinksBox.getChildren().add(hbox);

        }


        // Create meal and back to My Profile
        saveMealButton.setOnAction(event -> {
            if(checkField()){
                String mealTypeText = mealType.getValue().toString();
                String title = mealTitle.getText();
                String returnValue = profileService.addMeal(title, mealTypeText, recipesSelected, drinksSelected, userService.getLoggedUser().getUsername());
                if(returnValue.equals("DuplicateTitle")){
                    errorMsg.setText("Duplicate Meal Title.");
                    errorMsg.setOpacity(100);
                    return;
                }
                if(returnValue.equals("AddOK")) {
                    errorMsg.setOpacity(0);
                    Main.changeScene("MyProfile", null);
                }else{
                    errorMsg.setText("An error occurred");
                    errorMsg.setOpacity(100);
                }
            }
        });
    }

    private boolean checkField(){
        if(mealType.getSelectionModel().isEmpty() || mealTitle.getText().equals("")){
            errorMsg.setText("Complete all fields.");
            errorMsg.setOpacity(100);
            return false;
        }
        if((recipesSelected.size() + recipesSelected.size()) < 2) {
            errorMsg.setText("Add some recipes.");
            errorMsg.setOpacity(100);
            return false;
        }
        errorMsg.setOpacity(0);
        return true;

    }

    @Override
    public void setGui(){}
}
