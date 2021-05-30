package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.drink.Drink;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.HaloDBDriver;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.Recipe;
import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
    @FXML private Button followBtn;
    @FXML private Label errMsg;

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
        userText.setText(fields.getString("_id"));
        User user = userService.getLoggedUser();

        builder = new GuiElementsBuilder();
        List<Document> recipeList = userService.getRecipes(userText.getText());
        List<Document> drinkList = userService.getDrinks(userText.getText());

        if(recipeList != null){
            for(Document doc :recipeList){
                Recipe recipe = new Recipe(doc);
                HBox recipeBlock = builder.createRecipeBlock(recipe);
                recipeBlock.setOnMouseClicked((MouseEvent e) ->{
                    JSONObject par = new JSONObject().put("_id", recipe.getId());
                    Main.changeScene("RecipePage", par);
                });

                recipeVBox.getChildren().add(recipeBlock);
             }

            for(Document docDrink :drinkList){
                Drink drink = new Drink(docDrink);
                HBox drinkBlock = builder.createDrinkBlock(drink);
                drinkBlock.setOnMouseClicked((MouseEvent e) ->{
                    JSONObject par = new JSONObject().put("_id", drink.getId());
                    Main.changeScene("DrinkPage", par);
                });

                recipeVBox.getChildren().add(drinkBlock);
            }

        }

        if(!user.getUsername().equals(fields.getString("_id"))) {

            Boolean testFollow = userService.checkForFollow(user.getUsername(), userText.getText());
            if(!testFollow){
                followBtn.setText("Follow");
            }else{
                followBtn.setText("Unfollow");
            }

            followBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {

                    String result = userService.addFollow(user.getUsername(), userText.getText().toString(), followBtn.getText().toString());
                    if ((result != "followOk") && (result != "followDelOk")) {
                        errMsg.setText("Error in adding Follow");
                        errMsg.setStyle("-fx-text-fill: red;");
                    }

                    Boolean test = userService.checkForFollow(user.getUsername(), userText.getText().toString());
                    if (!test) {
                        followBtn.setText("Follow");
                    } else {
                        followBtn.setText("Unfollow");
                    }

                }
            });
        }
        else{
            followBtn.setText("Follow");
            followBtn.setDisable(true);
        }

    }


}
