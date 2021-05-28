package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.RecipeService;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.RecipeServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.bson.Document;
import org.json.JSONObject;

import javax.print.Doc;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AdminPageController extends JSONAdder implements Initializable {

    @FXML private Button logoutBtn;
    @FXML private Button goBtnBestUserByLike;
    @FXML private Button goBtnBestUserByRecipeNumber;
    @FXML private ComboBox comboAge;
    @FXML private ComboBox comboCategory;
    @FXML private ComboBox comboNation;
    @FXML private VBox vboxBestUserByLike;
    @FXML private Text errorMsg;

    private UserServiceFactory userServiceFactory;
    private UserService userService;

    private RecipeServiceFactory recipeServiceFactory;
    private RecipeService recipeService;

    private GuiElementsBuilder builder;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        builder = new GuiElementsBuilder();

        recipeServiceFactory = RecipeServiceFactory.create();
        recipeService = recipeServiceFactory.getService();

        userServiceFactory = UserServiceFactory.create();
        userService = userServiceFactory.getService();

        errorMsg.setOpacity(0);

        logoutBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Main.changeScene("Landing", new JSONObject());

            }
        });

        comboNation.setItems(LandingPageController.getNations());

        List<String> listCategory = new ArrayList<>();
        listCategory.add("vegan");
        listCategory.add("vegetarian");
        listCategory.add("glutenFree");
        listCategory.add("dairyFree");
        ObservableList<String> observableList = FXCollections.observableList(listCategory);
        comboCategory.setItems(observableList);

        List<String> listAge = new ArrayList<>();
        listAge.add("under-24");
        listAge.add("24-32");
        listAge.add("32-40");
        listAge.add("40-over");
        ObservableList<String> observableList1 = FXCollections.observableList(listAge);
        comboAge.setItems(observableList1);

        goBtnBestUserByLike.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String aux = "noCategory";
                if(!comboCategory.getSelectionModel().isEmpty()){
                    aux = comboCategory.getValue().toString();
                }
                List<Document> listRecipes = recipeService.getUserByLike(aux);
                for(Document doc: listRecipes){
                    System.out.println(doc.toJson());
                }
            }
        });

        goBtnBestUserByRecipeNumber.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int min, max;

                if(comboAge.getSelectionModel().isEmpty()){
                    min = - 1;
                    max = -1;
                }else{
                    String aux = comboAge.getValue().toString();
                    String[] tokens = aux.trim().split("-");
                    if(tokens[0].equals("less"))
                        min = 0;
                    else
                        min = Integer.parseInt(tokens[0]);

                    if(tokens[1].equals("over"))
                        max = 100;
                    else
                        max = Integer.parseInt(tokens[1]);
                }
                String nation;
                if(!comboNation.getSelectionModel().isEmpty()){
                    nation = comboNation.getValue().toString();
                }else{
                    nation = "noCountry";
                }



                List<Document> listDoc = userService.getRankingByRecipesNumber(min, max, nation);
                for (Document doc: listDoc){
                    System.out.println(doc.toJson());
                }
            }
        });




    }
}
