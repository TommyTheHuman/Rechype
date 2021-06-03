package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.util.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.Ingredient;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.IngredientService;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.IngredientServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.profile.ProfileService;
import it.unipi.dii.inginf.lsmdb.rechype.profile.ProfileServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.bson.Document;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AddIngredientFridgeController extends JSONAdder implements Initializable {

    @FXML private TextField ingredientText;
    @FXML private VBox searchedIngredientVBox;
    @FXML private VBox selectedIngredientVBox;
    @FXML private Button doneBtn;
    @FXML private ScrollPane scrollBoxIngredients;

    @FXML private Text inputQuantityError;

    private IngredientService ingredientService;

    private ProfileService profileService;

    private User loggedUser;
    private UserService userService;

    private GuiElementsBuilder builder;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        inputQuantityError.setOpacity(0);

        ingredientService = IngredientServiceFactory.create().getService();

        profileService = ProfileServiceFactory.create().getService();

        userService = UserServiceFactory.create().getService();

        builder = new GuiElementsBuilder();

        doneBtn.setOnAction(event -> {
            JSONObject par = jsonParameters;
            int counter = 0;

            List<Document> listOfIngredients = new ArrayList<>();

            for(Node node: selectedIngredientVBox.getChildren()){
                if(node instanceof VBox){
                    HBox hbox = (HBox) ((VBox)node).getChildren().get(1);
                    TextField quantityFields = (TextField) hbox.getChildren().get(1);
                    String quantityString = quantityFields.getText();
                    if(quantityString.length() == 0){
                        inputQuantityError.setOpacity(100);
                        return;
                    }

                    String imageUrl = "https://spoonacular.com/cdn/ingredients_100x100/" + builder.idSelectedIngredient.get(counter);
                    Document ingredient = new Document().append("name", builder.idSelectedIngredient.get(counter)).append("quantity", quantityString).append("image", imageUrl);
                    listOfIngredients.add(ingredient);
                    counter++;
                }
            }

            loggedUser = userService.getLoggedUser();
            inputQuantityError.setOpacity(0);
            profileService.addFridge(listOfIngredients, loggedUser.getUsername());
            par.append("changeTab", true);
            Main.changeScene("MyProfile", par);
        });

        //      When the user type a more than 3 letters create the boxes contaning the ingredient and display those in searchedIngredientVBox
        ingredientText.setOnKeyTyped(event -> {
            String text = ingredientText.getText();
            searchedIngredientVBox.getChildren().clear();
            if(text.length() > 2){
                for(Ingredient ingr: ingredientService.searchIngredients(text, 0, 10)){

                    searchedIngredientVBox.getChildren().addAll(builder.createIngredientBlock(ingr, selectedIngredientVBox), new Separator(Orientation.HORIZONTAL));
                }
            }
        });

        scrollBoxIngredients.vvalueProperty().addListener(new ChangeListener<>() {
            int offset = 0;

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (scrollBoxIngredients.getVvalue() == scrollBoxIngredients.getVmax()) {
                    offset = searchedIngredientVBox.getChildren().size() / 2;

                    List<Ingredient> listOfIngredients = ingredientService.searchIngredients(ingredientText.getText(), offset, 10);
                    for (Ingredient ingr : listOfIngredients) {
                        searchedIngredientVBox.getChildren().addAll(builder.createIngredientBlock(ingr, selectedIngredientVBox), new Separator(Orientation.HORIZONTAL));
                    }
                }
            }
        });
    }

    @Override
    public void setGui(){


    }

}
