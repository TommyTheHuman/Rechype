package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.RecipeService;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.RecipeServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

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

    @FXML private AnchorPane searchAnchor;

    private GuiElementsBuilder builder;

    private UserServiceFactory userServiceFactory;
    private UserService userService;
    private User loggedUser;

    private RecipeServiceFactory recipeServiceFactory;
    private RecipeService recipeService;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        builder = new GuiElementsBuilder();

        userServiceFactory = UserServiceFactory.create();
        userService = userServiceFactory.getService();
        loggedUser = userService.getLoggedUser();

        recipeServiceFactory = RecipeServiceFactory.create();
        recipeService = recipeServiceFactory.getService();


        searchAnchor.setVisible(false);
        checkBoxDrinks.selectedProperty().setValue(true);

        searchBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String text = searchText.getText();

                if(checkBoxUsers.isSelected()) {
                    List<User> listOfUsers = userService.searchUser(text);

                    for (User user : listOfUsers) {
                        resultBox.getChildren().addAll(builder.createUserBlock(user), new Separator(Orientation.HORIZONTAL));
                    }
                }

                if(checkBoxRecipes.isSelected()){

                }

                resultBox.setStyle("-fx-background-color: white !important");
                searchAnchor.setVisible(true);
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

    }
}
