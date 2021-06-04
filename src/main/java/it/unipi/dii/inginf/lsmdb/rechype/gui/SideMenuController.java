package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.util.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;


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
    private UserService userService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        userService = UserServiceFactory.create().getService();

        loggedUser = userService.getLoggedUser();
        userName.setText(loggedUser.getUsername());

        //set profile image based on level
        InputStream inputImage = GuiElementsBuilder.class.getResourceAsStream("/images/levels/0.png");
        if(loggedUser.getLevel()<=5){
            inputImage = GuiElementsBuilder.class.getResourceAsStream("/images/levels/0.png");
        }
        else if(loggedUser.getLevel()<=10 && loggedUser.getLevel()>5){
            inputImage = GuiElementsBuilder.class.getResourceAsStream("/images/levels/1.png");
        }
        else if(loggedUser.getLevel()>10){
            inputImage = GuiElementsBuilder.class.getResourceAsStream("/images/levels/2.png");
        }

        userImage.setImage(new Image(inputImage));
        logOut.setOnAction(event -> Main.changeScene("Landing", new JSONObject()));

        personalProfile.setOnAction(event -> Main.changeScene("MyProfile", new JSONObject()));

        myRecipes.setOnAction(event -> Main.changeScene("MyRecipes", new JSONObject()));

        addRecipe.setOnAction(event -> Main.changeScene("RecipeAdd", new JSONObject()));

        addDrink.setOnAction(event -> Main.changeScene("DrinkAdd", new JSONObject()));

        homePageBtn.setOnAction(event -> Main.changeScene("HomePage", new JSONObject()));
    }

}
