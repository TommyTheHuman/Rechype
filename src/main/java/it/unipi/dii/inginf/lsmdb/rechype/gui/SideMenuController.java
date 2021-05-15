package it.unipi.dii.inginf.lsmdb.rechype.gui;



import it.unipi.dii.inginf.lsmdb.rechype.util.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import org.json.JSONObject;

import java.net.URL;
import java.util.ResourceBundle;


/**
 * Initializable è necessario?
 */
public class SideMenuController extends JSONAdder implements Initializable {


    @FXML private Button logOut;
    @FXML private Text userName;
    @FXML private Hyperlink personalProfile;
    @FXML private Hyperlink myRecipes;
    @FXML private Hyperlink homePageBtn;
    @FXML private Hyperlink addRecipe;


    private User loggedUser;
    private UserServiceFactory userServiceFactory;
    private UserService userService;




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        userServiceFactory = UserServiceFactory.create();
        userService = userServiceFactory.getService();

        loggedUser = userService.getLoggedUser();

        userName.setText(loggedUser.getUsername());

        logOut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Main.changeScene("Landing", new JSONObject());

            }
        });

        personalProfile.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Main.changeScene("MyProfile", new JSONObject());

            }
        });

        myRecipes.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Main.changeScene("MyRecipes", new JSONObject());

            }
        });

        addRecipe.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Main.changeScene("RecipeAdd", new JSONObject());

            }
        });

        homePageBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Main.changeScene("HomePage", new JSONObject());

            }
        });
    }

}
