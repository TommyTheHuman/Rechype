package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.profile.ProfileService;
import it.unipi.dii.inginf.lsmdb.rechype.profile.ProfileServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.Recipe;
import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.bson.Document;
import org.json.JSONObject;

import javax.print.Doc;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AddMealController extends JSONAdder implements Initializable {

    @FXML private ComboBox mealType;
    @FXML private VBox searchedRecipesVBox;
    @FXML private VBox selectedRecipesVBox;
    @FXML private VBox searchedDrinksVbox;
    @FXML private TextField mealTitle;
    @FXML private Button saveMealButton;
    @FXML private Text errorMsg;

    private UserServiceFactory userServiceFactory;
    private UserService userService;
    private ProfileServiceFactory profileServiceFactory;
    private ProfileService profileService;

    private GuiElementsBuilder builder;

    private List<Document> recipeDocs;
    private List<Document> drinkDocs;
    private List<Document> recipesSelected;
    private List<Document> drinksSelected;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        userServiceFactory = UserServiceFactory.create();
        userService = userServiceFactory.getService();

        profileServiceFactory = ProfileServiceFactory.create();
        profileService = profileServiceFactory.getService();

        builder = new GuiElementsBuilder();
        recipesSelected = new ArrayList<>();
        drinksSelected = new ArrayList<>();

        mealType.setItems(FXCollections.observableArrayList("Breakfast", "Brunch", "Lunch", "Appetizer", "Dinner"));
        errorMsg.setOpacity(0);

        // Display user's recipes.
        searchedRecipesVBox.getChildren().clear();
        recipeDocs = userService.getRecipes(userService.getLoggedUser().getUsername());
        for(Document doc: recipeDocs){
            Recipe recipe = new Recipe(doc);
            HBox hbox = builder.createRecipeBlock(recipe);
            hbox.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    recipesSelected.add(doc);
                    selectedRecipesVBox.getChildren().add(hbox);
                    hbox.setOnMouseClicked(null);
                }
            });
            searchedRecipesVBox.getChildren().add(hbox);
        }

        // Create meal and back to My Profile
        saveMealButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
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
                        errorMsg.setText("An error occured");
                        errorMsg.setOpacity(100);
                    }
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
        if(recipesSelected.size() < 2) {
            errorMsg.setText("Add some recipes.");
            errorMsg.setOpacity(100);
            return false;
        }
        errorMsg.setOpacity(0);
        return true;

    }
}
