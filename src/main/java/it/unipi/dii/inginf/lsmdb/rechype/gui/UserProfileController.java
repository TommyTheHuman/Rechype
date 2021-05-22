package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.HaloDBDriver;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.Recipe;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.bson.Document;
import org.json.JSONObject;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class UserProfileController extends JSONAdder implements Initializable {

    @FXML private Text userText;
    @FXML private VBox recipeVBox;

    private GuiElementsBuilder builder;

    private UserServiceFactory userServiceFactory;
    private UserService userService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userServiceFactory = UserServiceFactory.create();
        userService = userServiceFactory.getService();
    }

    @Override
    public void setGui(){
        JSONObject fields=userService.getCachedUser(jsonParameters.getString("_id"));
        userText.setText(fields.getString("_id").toString());

        builder = new GuiElementsBuilder();

        List<Document> recipeList = userService.getNestedRecipes(userText.getText());

        for(Document doc :recipeList){
            Recipe recipe = new Recipe(doc);
            HBox recipeBlock = builder.createRecipeBlock(recipe);
            recipeBlock.setOnMouseClicked((MouseEvent e) ->{
                JSONObject par = new JSONObject().put("_id", recipe.getId()).append("cached", false);
                Main.changeScene("RecipePage", par);
            });

            //recipeBlock.setMaxWidth(300.0);
            //recipeBlock.lookup("mainContainer");

            recipeVBox.getChildren().add(recipeBlock);
        }


    }
}
