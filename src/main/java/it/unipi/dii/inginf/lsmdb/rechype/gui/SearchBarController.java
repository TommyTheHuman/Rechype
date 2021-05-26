package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.drink.Drink;
import it.unipi.dii.inginf.lsmdb.rechype.drink.DrinkService;
import it.unipi.dii.inginf.lsmdb.rechype.drink.DrinkServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.HaloDBDriver;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.Recipe;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.RecipeService;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.RecipeServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SearchBarController extends JSONAdder implements Initializable {

    @FXML private Button searchBtn;
    @FXML private TextField searchText;
    @FXML private VBox resultBox;
    @FXML private Button closeSearch;
    @FXML private CheckBox checkBoxUsers;
    @FXML private CheckBox checkBoxDrinks;
    @FXML private CheckBox checkBoxRecipes;
    @FXML private ScrollPane scrollSearch;
    @FXML private AnchorPane searchAnchor;
    @FXML private Text errorMsg;
    
    private String lastSearchedText;
    private GuiElementsBuilder builder;

    private UserServiceFactory userServiceFactory;
    private UserService userService;
    private User loggedUser;

    private RecipeServiceFactory recipeServiceFactory;
    private RecipeService recipeService;

    private DrinkServiceFactory drinkServiceFactory;
    private DrinkService drinkService;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        builder = new GuiElementsBuilder();

        userServiceFactory = UserServiceFactory.create();
        userService = userServiceFactory.getService();
        loggedUser = userService.getLoggedUser();

        recipeServiceFactory = RecipeServiceFactory.create();
        recipeService = recipeServiceFactory.getService();

        drinkServiceFactory = DrinkServiceFactory.create();
        drinkService = drinkServiceFactory.getService();

        searchAnchor.setVisible(false);
        checkBoxDrinks.selectedProperty().setValue(true);

        //clear al the checkbox at load
        checkBoxUsers.setSelected(false);
        checkBoxDrinks.setSelected(false);
        checkBoxRecipes.setSelected(false);

        searchBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(checkField()){
                    errorMsg.setOpacity(0);
                    resultBox.getChildren().clear();
                    lastSearchedText = searchText.getText();

                    if(checkBoxUsers.isSelected()) {
                        List<User> listOfUsers = userService.searchUser(lastSearchedText, 0, 10);

                        for (User user : listOfUsers) {
                            resultBox.getChildren().addAll(builder.createUserBlock(user), new Separator(Orientation.HORIZONTAL));
                        }
                    }

                    if(checkBoxRecipes.isSelected()){
                        List<Recipe> listOfRecipes = recipeService.searchRecipe(lastSearchedText, 0, 10);

                        for (Recipe recipe : listOfRecipes) {
                            resultBox.getChildren().addAll(builder.createRecipeBlock(recipe), new Separator(Orientation.HORIZONTAL));
                        }
                    }

                    if(checkBoxDrinks.isSelected()){
                        List<Drink> listOfDrinks = drinkService.searchDrink(lastSearchedText, 0, 10);

                        for (Drink drink : listOfDrinks) {
                            resultBox.getChildren().addAll(builder.createDrinkBlock(drink), new Separator(Orientation.HORIZONTAL));
                        }
                    }

                    resultBox.setStyle("-fx-background-color: white !important");
                    searchAnchor.setVisible(true);
                }else{
                    errorMsg.setText("Choose category.");
                    errorMsg.setOpacity(100);
                }


            }
        });

        closeSearch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                resultBox.getChildren().clear();
                searchAnchor.setVisible(false);
            }
        });

        checkBoxUsers.setOnAction((event) ->{
            checkBoxUsers.setSelected(true);
            checkBoxDrinks.setSelected(false);
            checkBoxRecipes.setSelected(false);
        });

        checkBoxDrinks.setOnAction((event) ->{
            checkBoxUsers.setSelected(false);
            checkBoxDrinks.setSelected(true);
            checkBoxRecipes.setSelected(false);
        });

        checkBoxRecipes.setOnAction((event) ->{
            checkBoxUsers.setSelected(false);
            checkBoxDrinks.setSelected(false);
            checkBoxRecipes.setSelected(true);
        });

        scrollSearch.vvalueProperty().addListener(new ChangeListener<Number>() {
            int offset=0;
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(scrollSearch.getVvalue()==scrollSearch.getVmax()){
                    offset=resultBox.getChildren().size()/2;
                    if(checkBoxUsers.isSelected()){
                        List<User> listOfUsers = userService.searchUser(lastSearchedText, offset, 10);
                        for (User user : listOfUsers) {
                                resultBox.getChildren().addAll(builder.createUserBlock(user), new Separator(Orientation.HORIZONTAL));
                        };
                    }
                    else if(checkBoxRecipes.isSelected()){
                        List<Recipe> listOfRecipes = recipeService.searchRecipe(lastSearchedText, offset, 10);
                        for (Recipe recipe : listOfRecipes) {
                            resultBox.getChildren().addAll(builder.createRecipeBlock(recipe), new Separator(Orientation.HORIZONTAL));
                        };
                    }
                    else if(checkBoxDrinks.isSelected()){
                        List<Drink> listOfDrinks = drinkService.searchDrink(lastSearchedText, offset, 10);
                        for(Drink drink: listOfDrinks){
                            resultBox.getChildren().addAll(builder.createDrinkBlock(drink), new Separator(Orientation.HORIZONTAL));
                        }
                    }
                }
            }
        });

    }

    private boolean checkField(){
        if(checkBoxDrinks.isSelected() || checkBoxRecipes.isSelected() || checkBoxUsers.isSelected())
            return true;
        else
            return false;
    }
}
