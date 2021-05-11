package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.Ingredient;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.IngredientService;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.IngredientServiceFactory;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AddIngredientController extends JSONAdder implements Initializable {

    @FXML private TextField ingredientText;
    @FXML private VBox searchedIngredientVBox;
    @FXML private VBox selectedIngredientVBox;

    private IngredientServiceFactory ingredientServiceFactory;
    private IngredientService ingredientService;

    private GuiElementsBuilder builder;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ingredientServiceFactory = IngredientServiceFactory.create();
        ingredientService = ingredientServiceFactory.getService();

        builder = new GuiElementsBuilder();

        ingredientText.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                String text = ingredientText.getText();
                searchedIngredientVBox.getChildren().clear();
                if(text.length() > 2){
                    for(Ingredient ingr: ingredientService.searchIngredients(text)){
                        try {
                            searchedIngredientVBox.getChildren().addAll(builder.createIngredientBlock(ingr, selectedIngredientVBox), new Separator(Orientation.HORIZONTAL));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }
}
