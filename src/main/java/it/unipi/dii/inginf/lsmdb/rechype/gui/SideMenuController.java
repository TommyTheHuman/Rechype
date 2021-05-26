package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.json.JSONObject;

import java.io.InputStream;
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
    @FXML private Hyperlink addDrink;
    @FXML private ImageView userImage;


    private User loggedUser;
    private UserServiceFactory userServiceFactory;
    private UserService userService;




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        userServiceFactory = UserServiceFactory.create();
        userService = userServiceFactory.getService();

        loggedUser = userService.getLoggedUser();
        userName.setText(loggedUser.getUsername());

        InputStream inputImage = GuiElementsBuilder.class.getResourceAsStream("/images/levels/0.png");
        if(loggedUser.getLevel()<5){
            inputImage = GuiElementsBuilder.class.getResourceAsStream("/images/levels/0.png");
        }
        else if(loggedUser.getLevel()<10){
            inputImage = GuiElementsBuilder.class.getResourceAsStream("/images/levels/1.png");
        }
        else if(loggedUser.getLevel()<15){
            inputImage = GuiElementsBuilder.class.getResourceAsStream("/images/levels/2.png");
        }

        userImage.setImage(new Image(inputImage));
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

        addDrink.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Main.changeScene("DrinkAdd", new JSONObject());

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
