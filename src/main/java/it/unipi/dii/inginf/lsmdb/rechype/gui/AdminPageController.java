package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.profile.ProfileService;
import it.unipi.dii.inginf.lsmdb.rechype.profile.ProfileServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.drink.DrinkService;
import it.unipi.dii.inginf.lsmdb.rechype.drink.DrinkServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.RecipeService;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.RecipeServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;

import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.bson.Document;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AdminPageController extends JSONAdder implements Initializable {

    @FXML private Button logoutBtn;
    @FXML private Button goBtnBestUserByLike;
    @FXML private Button goBtnBestUserByLikeAgeCountry;
    @FXML private ComboBox<String> comboAge;
    @FXML private ComboBox<String> comboCategory;
    @FXML private ComboBox<String> comboNation;
    @FXML private Button goBtnBestUserByHealth;
    @FXML private ComboBox<String> comboLevel;

    @FXML private ComboBox<String> comboMinutes;
    @FXML private Button goBtnPopularIngredient;
    @FXML private CheckBox checkFat;
    @FXML private CheckBox checkProtein;
    @FXML private CheckBox checkCalories;
    @FXML private CheckBox checkRecipes;
    @FXML private CheckBox checkDrinks;
    @FXML private CheckBox checkRecipes1;
    @FXML private CheckBox checkDrinks1;

    @FXML private VBox vboxPopularIngredients;
    @FXML private VBox vboxBestUserByLikeCat;
    @FXML private VBox vboxMostSavedRecipes;
    @FXML private VBox vboxBestUserByLike;
    @FXML private VBox vboxBestUserByHealth;
    @FXML private Button goBtnMostSavedRecipes;
    @FXML private Button btnClearUserByLike;
    @FXML private Button btnClearUserHealth;
    @FXML private Button BanButton;
    @FXML private TextField textFieldBan;
    @FXML private Label labelBanned;

    private UserServiceFactory userServiceFactory;
    private UserService userService;

    private RecipeServiceFactory recipeServiceFactory;
    private RecipeService recipeService;

    private ProfileService profileService;

    private DrinkServiceFactory drinkServiceFactory;
    private DrinkService drinkService;

    private GuiElementsBuilder builder;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        builder = new GuiElementsBuilder();

        recipeService = RecipeServiceFactory.create().getService();

        userService = UserServiceFactory.create().getService();

        profileService = ProfileServiceFactory.create().getService();

        drinkServiceFactory = DrinkServiceFactory.create();
        drinkService = drinkServiceFactory.getService();

        logoutBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Main.changeScene("Landing", new JSONObject());

            }
        });

        comboNation.setItems(LandingPageController.getNations());

        List<String> listTag = new ArrayList<>();
        listTag.add("cocktail");
        listTag.add("beer");
        listTag.add("other");
        ObservableList<String> observableListDrink = FXCollections.observableList(listTag);


        List<String> listCategory = new ArrayList<>();
        listCategory.add("vegan");
        listCategory.add("vegetarian");
        listCategory.add("glutenFree");
        listCategory.add("dairyFree");
        ObservableList<String> observableListRecipe = FXCollections.observableList(listCategory);
        comboCategory.setItems(observableListRecipe);

        List<String> listAge = new ArrayList<>();
        listAge.add("under-24");
        listAge.add("24-32");
        listAge.add("32-40");
        listAge.add("40-over");
        ObservableList<String> observableList1 = FXCollections.observableList(listAge);
        comboAge.setItems(observableList1);

        List<String> listLevel = new ArrayList<>();
        listLevel.add("bronze");
        listLevel.add("silver");
        listLevel.add("gold");
        ObservableList<String> observableListLvl = FXCollections.observableList(listLevel);
        comboLevel.setItems(observableListLvl);

        List<String> listMinutes = new ArrayList<>();
        listMinutes.add("15");
        listMinutes.add("30");
        listMinutes.add("45");
        ObservableList<String> observableListMin = FXCollections.observableList(listMinutes);
        comboMinutes.setItems(observableListMin);

        goBtnBestUserByLike.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                vboxBestUserByLikeCat.getChildren().clear();
                String aux = "noCategory";
                vboxBestUserByLikeCat.setSpacing(15);
                if(!comboCategory.getSelectionModel().isEmpty()){
                    aux = comboCategory.getValue().toString();
                }
                List<Document> listDoc;
                if(checkRecipes.isSelected()){
                    listDoc = recipeService.getUserByLikeAndCategory(aux);
                }else{
                    listDoc = drinkService.getUserByLikeAndCategory(aux);
                }

                if(listDoc.size() == 0){
                    HBox hbox = new HBox();
                    Text noResult = new Text();
                    noResult.setText("Not enough data.");
                    hbox.getChildren().add(noResult);
                    vboxBestUserByLikeCat.getChildren().add(hbox);
                    return;
                }

                for(Integer i = 0; i< listDoc.size(); i++){
                    HBox hbox = new HBox(15);
                    Text name = new Text();
                    Text number = new Text();
                    Text like = new Text();
                    Integer rank = i;
                    rank = rank +1;
                    number.setText(rank.toString() + ") ");
                    name.setText(listDoc.get(i).getString("author"));
                    like.setText("likes:" + listDoc.get(i).getInteger("likes").toString());
                    hbox.getChildren().addAll(number, name, like);
                    vboxBestUserByLikeCat.getChildren().addAll(hbox, new Separator(Orientation.HORIZONTAL));
                }

            }
        });


        goBtnBestUserByLikeAgeCountry.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                vboxBestUserByLike.getChildren().clear();
                int min, max;

                if(comboAge.getSelectionModel().isEmpty()){
                    min = - 1;
                    max = -1;
                }else{
                    String aux = comboAge.getValue().toString();
                    String[] tokens = aux.trim().split("-");
                    if(tokens[0].equals("under"))
                        min = 0;
                    else
                        min = Integer.parseInt(tokens[0]);

                    if(tokens[1].equals("over"))
                        max = 200;
                    else
                        max = Integer.parseInt(tokens[1]);
                }
                String nation;
                if(!comboNation.getSelectionModel().isEmpty()){
                    nation = comboNation.getValue().toString();
                }else{
                    nation = "noCountry";
                }

                vboxBestUserByLike.setSpacing(15);
                List<Document> listDoc;
                if(checkRecipes.isSelected()){
                    listDoc = recipeService.getUserByLikeNumber(min, max, nation);
                }else{
                    listDoc = drinkService.getUserByLikeAndNationAndAge(min,max, nation);
                }
                if(listDoc.size() == 0){
                    HBox hbox = new HBox();
                    Text noResult = new Text();
                    noResult.setText("Not enough data.");
                    hbox.getChildren().add(noResult);
                    vboxBestUserByLike.getChildren().add(hbox);
                    return;
                }
                for(Integer i=0; i<listDoc.size(); i++){
                    HBox hbox = new HBox(15);
                    Text name = new Text();
                    Text number = new Text();
                    Text like = new Text();
                    Integer rank = i;
                    rank = rank +1;
                    number.setText(rank.toString() + ") ");
                    name.setText(listDoc.get(i).getString("_id"));
                    like.setText("likes:" + listDoc.get(i).getInteger("likes").toString());
                    hbox.getChildren().addAll(number, name, like);
                    vboxBestUserByLike.getChildren().addAll(hbox, new Separator(Orientation.HORIZONTAL));
                }
            }
        });


        goBtnMostSavedRecipes.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                vboxMostSavedRecipes.getChildren().clear();
                vboxMostSavedRecipes.setSpacing(15);

                List<Document> listRecipes;
                if(checkRecipes1.isSelected()){
                    listRecipes = userService.getMostSavedRecipes("recipes");
                }else{
                    listRecipes = userService.getMostSavedRecipes("drinks");
                }
                if(listRecipes.size() == 0){
                    HBox hbox = new HBox();
                    Text noResult = new Text();
                    noResult.setText("Not enough data.");
                    hbox.getChildren().add(noResult);
                    vboxMostSavedRecipes.getChildren().add(hbox);
                    return;
                }
                for(Integer i=0; i<listRecipes.size(); i++){
                    HBox hbox = new HBox(15);
                    Text name = new Text();
                    Text number = new Text();
                    Text like = new Text();
                    Integer rank = i;
                    rank = rank +1;
                    number.setText(rank.toString() + ") ");
                    name.setText(listRecipes.get(i).getString("name"));
                    like.setText("Saved " + listRecipes.get(i).getInteger("count").toString() + " times.");
                    hbox.getChildren().addAll(number, name, like);
                    vboxMostSavedRecipes.getChildren().addAll(hbox, new Separator(Orientation.HORIZONTAL));
                }
            }
        });

        goBtnBestUserByHealth.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                vboxBestUserByHealth.getChildren().clear();
                String level;
                if(comboLevel.getSelectionModel().isEmpty()){
                    level = "noLevel";
                }else{
                    level = comboLevel.getValue().toString();
                }

                vboxBestUserByHealth.setSpacing(15);
                List<Document> listDoc = userService.getTophealthyUsers(level);
                if(listDoc.size() == 0){
                    HBox hbox = new HBox();
                    Text noResult = new Text();
                    noResult.setText("Not enough data.");
                    hbox.getChildren().add(noResult);
                    vboxBestUserByHealth.getChildren().add(hbox);
                    return;
                }
                for(Integer i=0; i<listDoc.size(); i++){
                    HBox hbox = new HBox(15);
                    Text name = new Text();
                    Text number = new Text();
                    Text like = new Text();
                    Integer rank = i;
                    rank = rank +1;
                    number.setText(rank.toString() + ") ");
                    name.setText(listDoc.get(i).getString("_id"));
                    like.setText("Has " + listDoc.get(i).getInteger("count").toString() + " healthy recipes.");
                    hbox.getChildren().addAll(number, name, like);
                    vboxBestUserByHealth.getChildren().addAll(hbox, new Separator(Orientation.HORIZONTAL));
                }
            }
        });


        goBtnPopularIngredient.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                vboxPopularIngredients.getChildren().clear();
                int minutes;

                if(comboMinutes.getSelectionModel().isEmpty()){
                    minutes = -1;
                }else{
                    minutes = Integer.parseInt(comboMinutes.getValue().toString());
                }

                String nutrients = "noNutrient";
                if(checkFat.isSelected()){
                    nutrients = "Fat";
                }else if(checkCalories.isSelected()){
                    nutrients = "Calories";
                }else if(checkProtein.isSelected()){
                    nutrients = "Protein";
                }
                vboxPopularIngredients.setSpacing(15);
                List<Document> listDoc = recipeService.getPopularIngredient(nutrients, minutes);
                if(listDoc.size() == 0){
                    HBox hbox = new HBox();
                    Text noResult = new Text();
                    noResult.setText("Not enough data.");
                    hbox.getChildren().add(noResult);
                    vboxPopularIngredients.getChildren().add(hbox);
                    return;
                }
                for(Integer i=0; i<listDoc.size(); i++){
                    HBox hbox = new HBox(15);
                    Text name = new Text();
                    Text number = new Text();
                    Text like = new Text();
                    Integer rank = i;
                    rank = rank +1;
                    number.setText(rank.toString() + ") ");
                    name.setText(listDoc.get(i).getString("_id"));
                    like.setText("in " + listDoc.get(i).getInteger("count").toString()+ " recipes.");
                    hbox.getChildren().addAll(number, name, like);
                    vboxPopularIngredients.getChildren().addAll(hbox, new Separator(Orientation.HORIZONTAL));
                }
            }
        });

        btnClearUserByLike.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                textFieldBan.setText("");
                comboNation.setValue("");
                comboAge.setValue("");
                comboCategory.setValue("");
            }
        });

        btnClearUserHealth.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                comboLevel.setValue("");
                comboMinutes.setValue("");
                checkFat.setSelected(false);
                checkProtein.setSelected(false);
                checkCalories.setSelected(false);
            }
        });

        checkRecipes.setSelected(true);
        checkRecipes1.setSelected(true);

        checkRecipes.setOnAction((event) ->{
            comboCategory.setItems(observableListRecipe);
            checkRecipes.setSelected(true);
            checkDrinks.setSelected(false);
        });

        checkDrinks.setOnAction((event) ->{
            comboCategory.setItems(observableListDrink);
            checkDrinks.setSelected(true);
            checkRecipes.setSelected(false);
        });

        checkRecipes1.setOnAction((event) ->{
            checkRecipes1.setSelected(true);
            checkDrinks1.setSelected(false);
        });

        checkDrinks1.setOnAction((event) ->{
            checkDrinks1.setSelected(true);
            checkRecipes1.setSelected(false);
        });

        checkFat.setOnAction((event) ->{
            checkFat.setSelected(true);
            checkProtein.setSelected(false);
            checkCalories.setSelected(false);
        });

        checkProtein.setOnAction((event) ->{

            checkFat.setSelected(false);
            checkProtein.setSelected(true);
            checkCalories.setSelected(false);
        });

        checkCalories.setOnAction((event) ->{

            checkFat.setSelected(false);
            checkProtein.setSelected(false);
            checkCalories.setSelected(true);
        });

        //defining the event for ban button
        BanButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(userService.banUser(textFieldBan.getText()).equals("BanOk")){
                    labelBanned.setText("Successfully Banned");
                    profileService.deleteProfile(textFieldBan.getText());
                }else{
                    labelBanned.setText("Ban has failed");
                }
            }
        });

    }
}
