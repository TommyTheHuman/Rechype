package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.util.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.drink.Drink;
import it.unipi.dii.inginf.lsmdb.rechype.drink.DrinkService;
import it.unipi.dii.inginf.lsmdb.rechype.drink.DrinkServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.Recipe;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.RecipeService;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.RecipeServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
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
    @FXML private Text textAge;
    @FXML private Text textUserFilter;
    @FXML private Text textDrinksFilter;
    @FXML private Text textRecipeFilter;
    @FXML private AnchorPane filterAnchor;
    @FXML private Button closeFilters;


    @FXML private CheckBox checkGluten;
    @FXML private CheckBox checkDairy;
    @FXML private CheckBox checkVegan;
    @FXML private CheckBox checkVegetarian;
    @FXML private ComboBox selectPrice;
    @FXML private CheckBox recipeLikeSort;

    @FXML private CheckBox drinkLikeSort;
    @FXML private ComboBox drinkType;

    @FXML private TextField userAgeFilter;
    @FXML private ComboBox userLevelFilter;
    
    private String lastSearchedText;
    private GuiElementsBuilder builder;

    private UserService userService;

    private RecipeService recipeService;

    private DrinkService drinkService;

    private JSONObject filters = new JSONObject();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        builder = new GuiElementsBuilder();

        userService = UserServiceFactory.create().getService();

        recipeService = RecipeServiceFactory.create().getService();

        drinkService = DrinkServiceFactory.create().getService();

        //Only numbers into the text field
        userAgeFilter.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                userAgeFilter.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        //populate recipe combo box filter
        List<String> price = new ArrayList<>();
        price.add("$");
        price.add("$$");
        price.add("$$$");
        price.add("$$$$");
        ObservableList<String> priceList = FXCollections.observableArrayList(price);
        selectPrice.setItems(priceList);

        //populate user combobox filter
        List<String> level = new ArrayList<>();
        level.add("bronze");
        level.add("silver");
        level.add("gold");
        ObservableList<String> lvlList = FXCollections.observableArrayList(level);
        userLevelFilter.setItems(lvlList);

        //populate drink combobox filter
        List<String> type = new ArrayList<>();
        type.add("beer");
        type.add("cocktail");
        type.add("other");
        ObservableList<String> typeList = FXCollections.observableArrayList(type);
        drinkType.setItems(typeList);

        searchAnchor.setVisible(false);
        checkBoxDrinks.selectedProperty().setValue(true);
        filterAnchor.setVisible(false);

        //clear al the checkbox at load
        checkBoxUsers.setSelected(false);
        checkBoxDrinks.setSelected(false);
        checkBoxRecipes.setSelected(false);

        searchBtn.setOnAction(event -> {
            if(checkField()){
                errorMsg.setOpacity(0);
                resultBox.getChildren().clear();
                lastSearchedText = searchText.getText();

                if(checkBoxUsers.isSelected()) {

                    filters = new JSONObject();
                    if(userAgeFilter.getText().length() > 0) {
                        filters.put("Age", userAgeFilter.getText());
                    }

                    if(!userLevelFilter.getSelectionModel().isEmpty()){
                        filters.put("Level", userLevelFilter.getValue().toString());
                    }

                    List<User> listOfUsers = userService.searchUser(lastSearchedText, 0, 10, filters);

                    for (User user : listOfUsers) {
                        resultBox.getChildren().addAll(builder.createUserBlock(user), new Separator(Orientation.HORIZONTAL));
                    }
                }

                if(checkBoxRecipes.isSelected()){

                    filters = new JSONObject();
                    filters.put("DairyFree", checkDairy.isSelected()).put("GlutenFree", checkGluten.isSelected()).put("Vegan", checkVegan.isSelected())
                            .put("Vegetarian", checkVegetarian.isSelected()).put("RecipeSort", recipeLikeSort.isSelected());

                    if(!selectPrice.getSelectionModel().isEmpty()){
                        filters.put("Price", selectPrice.getValue().toString());
                    }

                    List<Recipe> listOfRecipes = recipeService.searchRecipe(lastSearchedText, 0, 10, filters);

                    for (Recipe recipe : listOfRecipes) {
                        resultBox.getChildren().addAll(builder.createRecipeBlock(recipe), new Separator(Orientation.HORIZONTAL));
                    }
                }

                if(checkBoxDrinks.isSelected()){

                    filters = new JSONObject();
                    filters.put("DrinkSort", drinkLikeSort.isSelected());
                    if(!drinkType.getSelectionModel().isEmpty()){
                        filters.put("tag", drinkType.getValue().toString());
                    }

                    List<Drink> listOfDrinks = drinkService.searchDrink(lastSearchedText, 0, 10, filters);

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


        });

        closeSearch.setOnAction(event -> {
            resultBox.getChildren().clear();
            searchAnchor.setVisible(false);
        });

        checkBoxUsers.setOnAction((event) ->{
            filterAnchor.setVisible(true);

            userEnable();
            recipeDisable();
            drinkDisable();

            checkBoxUsers.setSelected(true);
            checkBoxDrinks.setSelected(false);
            checkBoxRecipes.setSelected(false);
        });

        checkBoxDrinks.setOnAction((event) ->{
            filterAnchor.setVisible(true);

            drinkEnable();
            recipeDisable();
            userDisable();

            checkBoxUsers.setSelected(false);
            checkBoxDrinks.setSelected(true);
            checkBoxRecipes.setSelected(false);
        });

        checkBoxRecipes.setOnAction((event) ->{
            filterAnchor.setVisible(true);

            recipeEnable();
            userDisable();
            drinkDisable();

            checkBoxUsers.setSelected(false);
            checkBoxDrinks.setSelected(false);
            checkBoxRecipes.setSelected(true);
        });


        closeFilters.setOnAction(event -> {
            userDisable();
            drinkDisable();
            recipeDisable();
            filterAnchor.setVisible(false);
        });


        scrollSearch.vvalueProperty().addListener(new ChangeListener<>() {
            int offset = 0;

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (scrollSearch.getVvalue() == scrollSearch.getVmax()) {
                    offset = resultBox.getChildren().size() / 2;
                    if (checkBoxUsers.isSelected()) {
                        List<User> listOfUsers = userService.searchUser(lastSearchedText, offset, 10, filters);
                        for (User user : listOfUsers) {
                            resultBox.getChildren().addAll(builder.createUserBlock(user), new Separator(Orientation.HORIZONTAL));
                        }
                    } else if (checkBoxRecipes.isSelected()) {
                        List<Recipe> listOfRecipes = recipeService.searchRecipe(lastSearchedText, offset, 10, filters);
                        for (Recipe recipe : listOfRecipes) {
                            resultBox.getChildren().addAll(builder.createRecipeBlock(recipe), new Separator(Orientation.HORIZONTAL));
                        }
                    } else if (checkBoxDrinks.isSelected()) {
                        List<Drink> listOfDrinks = drinkService.searchDrink(lastSearchedText, offset, 10, filters);
                        for (Drink drink : listOfDrinks) {
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

    private void recipeDisable(){
        checkDairy.setSelected(false);
        checkVegan.setSelected(false);
        checkGluten.setSelected(false);
        checkVegetarian.setSelected(false);
        recipeLikeSort.setSelected(false);
        selectPrice.setValue("");
        checkGluten.setDisable(true);
        checkDairy.setDisable(true);
        checkVegan.setDisable(true);
        checkVegetarian.setDisable(true);
        selectPrice.setDisable(true);
        recipeLikeSort.setDisable(true);
        textRecipeFilter.setFill(Color.rgb(171,171,171));
    }

    private void recipeEnable(){
        checkGluten.setDisable(false);
        checkDairy.setDisable(false);
        checkVegan.setDisable(false);
        checkVegetarian.setDisable(false);
        selectPrice.setDisable(false);
        recipeLikeSort.setDisable(false);
        textRecipeFilter.setFill(Color.BLACK);
    }

    private void drinkDisable(){
        drinkLikeSort.setSelected(false);
        drinkType.setValue("");

        drinkLikeSort.setDisable(true);
        drinkType.setDisable(true);
        textDrinksFilter.setFill(Color.rgb(171,171,171));
    }

    private void drinkEnable(){
        drinkLikeSort.setDisable(false);
        drinkType.setDisable(false);
        textDrinksFilter.setFill(Color.BLACK);
    }

    private void userDisable(){
        userAgeFilter.setDisable(true);
        userLevelFilter.setDisable(true);

        userAgeFilter.setText("");
        userLevelFilter.setValue("");
        textUserFilter.setFill(Color.rgb(171,171,171));
        textAge.setFill(Color.rgb(171,171,171));
    }

    private void userEnable(){
        userAgeFilter.setDisable(false);
        userLevelFilter.setDisable(false);
        textUserFilter.setFill(Color.BLACK);
        textAge.setFill(Color.BLACK);
    }
}
