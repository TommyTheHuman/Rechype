package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.util.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.drink.DrinkService;
import it.unipi.dii.inginf.lsmdb.rechype.drink.DrinkServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.profile.ProfileService;
import it.unipi.dii.inginf.lsmdb.rechype.profile.ProfileServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.RecipeService;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.RecipeServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.chart.PieChart;
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

    @FXML private CheckBox checkRecipes;
    @FXML private CheckBox checkDrinks;
    @FXML private CheckBox checkRecipes1;
    @FXML private CheckBox checkDrinks1;

    @FXML private PieChart distributionPie;
    @FXML private Button goBtnDistr;
    @FXML private Button goBtnMostUsedIngr;
    @FXML private VBox vboxMostUsedIngr;
    @FXML private ComboBox comboIngr;
    @FXML private Label errLabel;
    @FXML private VBox vboxMostSavedRecipes;

    @FXML private Button goBtnMostSavedRecipes;

    @FXML private Button BanButton;
    @FXML private TextField textFieldBan;
    @FXML private Label labelBanned;

    private ObservableList<PieChart.Data> pieData;

    private UserService userService;

    private RecipeService recipeService;

    private ProfileService profileService;

    private DrinkService drinkService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        recipeService = RecipeServiceFactory.create().getService();

        userService = UserServiceFactory.create().getService();

        profileService = ProfileServiceFactory.create().getService();

        drinkService = DrinkServiceFactory.create().getService();

        logoutBtn.setOnAction(event -> Main.changeScene("Landing", new JSONObject()));


        errLabel.setVisible(false);


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
        comboIngr.setItems(observableListRecipe);

        goBtnDistr.setOnAction(event -> {
            pieData = FXCollections.observableArrayList();

            List<Document> result = recipeService.getPriceDistribution();

            List<String> price = new ArrayList<>();
            price.add(0,"$");
            price.add(1,"$$");
            price.add(2,"$$$");
            price.add(3,"$$$$");
            for (Document doc : result) {
                Integer range = doc.getInteger("_id");
                int sum = doc.getInteger("count");
                pieData.add(new PieChart.Data(price.get(range-1), sum));
            }
            distributionPie.setData(pieData);
            distributionPie.setLegendVisible(false);

        });


        goBtnMostUsedIngr.setOnAction(event -> {
            vboxMostUsedIngr.getChildren().clear();
            vboxMostUsedIngr.setSpacing(15);

            List<Document> listRecipes;

            String category;
            if(comboIngr.getSelectionModel().isEmpty()){
                errLabel.setVisible(true);
                return;
            }else{
                category = comboIngr.getValue().toString();
            }

            if(checkRecipes.isSelected()){
                listRecipes = recipeService.getMostUsedIngr(category);
            }else{
                listRecipes = drinkService.getMostUsedIngr(category);
            }
            if(listRecipes.size() == 0){
                HBox hbox = new HBox();
                Text noResult = new Text();
                noResult.setText("Not enough data.");
                hbox.getChildren().add(noResult);
                vboxMostUsedIngr.getChildren().add(hbox);
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
                name.setText(listRecipes.get(i).getString("_id"));
                like.setText("in " + listRecipes.get(i).getInteger("count").toString()+ " recipes.");
                hbox.getChildren().addAll(number, name, like);
                vboxMostUsedIngr.getChildren().addAll(hbox, new Separator(Orientation.HORIZONTAL));
            }


        });


        goBtnMostSavedRecipes.setOnAction(event -> {
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
        });




        checkRecipes.setSelected(true);
        checkRecipes1.setSelected(true);

        checkRecipes.setOnAction((event) ->{
            comboIngr.setItems(observableListRecipe);
            checkRecipes.setSelected(true);
            checkDrinks.setSelected(false);
        });

        checkDrinks.setOnAction((event) ->{
            comboIngr.setItems(observableListDrink);
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



        //defining the event for ban button
        BanButton.setOnAction(event -> {
            if(userService.banUser(textFieldBan.getText()).equals("BanOk")){
                labelBanned.setText("Successfully Banned");
                profileService.deleteProfile(textFieldBan.getText());
            }else{
                labelBanned.setText("Ban has failed");
            }
        });

    }
}
